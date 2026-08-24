package com.works.patimati.ai;

import com.works.patimati.ai.dto.AiCandidate;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.external.ExternalPetRecord;
import com.works.patimati.entity.enums.ExternalCategory;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.external.ExternalPetRecordRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Native (ads) ve external (external_pet_records) aday havuzlarının
 * BULUŞTUĞU TEK YER (Faz 2 revize blueprint §31). Eşleştirici
 * ({@code matcher.py}) kaynaktan tamamen habersiz kalır — o yalnızca düz
 * embedding/etiket/tür/mesafe alır; kaynak ayrımı tamamen burada yaşar.
 *
 * <p><b>İki çağıran, iki yön:</b>
 * <ul>
 *   <li>{@link #findCandidatesForAd}: native bir ilan için hem AD hem
 *       EXTERNAL adayları toplar (Flow B — yeni ilan, geçmiş Instagram
 *       kayıtlarını da görür).</li>
 *   <li>{@link #findCandidatesForExternalRecord}: bir external kayıt için
 *       yalnızca AD adayları toplanır — iki Instagram kaydı birbirine asla
 *       eşleştirilmez, çünkü bildirilecek gerçek bir PatiMati kullanıcısı
 *       olmaz.</li>
 * </ul>
 *
 * <p><b>Konumsuz fallback:</b> özne konumsuzsa (native ilanda bu zaten var
 * olan bir hata olarak keşfedildi — bkz. commit geçmişi) PostGIS yerine
 * sınırlı, mesafesiz bir sorgu kullanılır; eksik konum gerçek bir adayı asla
 * havuzdan düşürmez.
 */
@Component
@RequiredArgsConstructor
public class MatchCandidateGatherer {

    private static final Logger log = LoggerFactory.getLogger(MatchCandidateGatherer.class);

    private final AdRepository adRepository;
    private final ExternalPetRecordRepository externalPetRecordRepository;

    @Value("${ai.matching.radius-km:25}")
    private double radiusKm;

    @Value("${ai.matching.window-days:90}")
    private int windowDays;

    @Value("${ai.matching.max-candidates:100}")
    private int maxCandidates;

    /** Konumsuz fallback'in aday sınırı — mesafeye göre sıralanamadığı için bilerek DAHA KÜÇÜK. */
    @Value("${ai.matching.no-location-candidate-limit:30}")
    private int noLocationCandidateLimit;

    public List<AiCandidate> findCandidatesForAd(Ad ad) {
        String compatibleAdType = oppositeAdType(ad.getAdType());
        if (compatibleAdType == null) {
            return List.of();
        }
        String compatibleExternalCategory = compatibleAdType; // LOST/FOUND adları örtüşüyor
        String species = declaredSpecies(ad);
        Instant since = Instant.now().minus(windowDays, ChronoUnit.DAYS);

        List<AiCandidate> adCandidates;
        List<AiCandidate> externalCandidates;

        if (ad.getLocation() != null) {
            adCandidates = fromAdRows(
                    adRepository.findAiCandidates(ad.getId(), compatibleAdType, ad.getLocation(), radiusKm * 1000.0, since));
            externalCandidates = fromExternalRows(
                    externalPetRecordRepository.findSpatialCandidates(
                            compatibleExternalCategory, ad.getLocation(), radiusKm * 1000.0, since));
        } else {
            log.info("İlan {} konumsuz — sınırlı, mesafesiz fallback kullanılıyor", ad.getId());
            adCandidates = fromAdIds(
                    adRepository.findFallbackCandidateIds(
                            ad.getId(), compatibleAdType, since, species, noLocationCandidateLimit),
                    radiusKm);
            externalCandidates = fromExternalIds(
                    externalPetRecordRepository.findFallbackCandidateIds(
                            compatibleExternalCategory, since, species, noLocationCandidateLimit),
                    radiusKm);
        }

        return merge(adCandidates, externalCandidates, ad.getLocation() != null);
    }

    public List<AiCandidate> findCandidatesForExternalRecord(ExternalPetRecord record) {
        List<String> compatibleAdTypes = compatibleAdTypesForCategory(record.getCategory());
        if (compatibleAdTypes.isEmpty()) {
            return List.of();
        }
        String species = record.getSpecies();
        Instant since = Instant.now().minus(windowDays, ChronoUnit.DAYS);
        boolean spatial = record.getLocation() != null;
        if (!spatial) {
            log.info("External kayıt {} konumsuz — sınırlı, mesafesiz fallback kullanılıyor", record.getId());
        }

        // compatibleAdTypes normalde tek elemanlı (LOST<->FOUND). UNCERTAIN
        // kategori için İKİ eleman döner (bkz. compatibleAdTypesForCategory)
        // -- bir Ad'ın adType'ı LOST YA DA FOUND'dır, asla ikisi birden
        // olamaz, o yüzden bu iki sorgunun sonuçları örtüşmez ve ayrıca
        // dedup gerekmez.
        List<AiCandidate> adCandidates = new ArrayList<>();
        for (String compatibleAdType : compatibleAdTypes) {
            if (spatial) {
                adCandidates.addAll(fromAdRows(
                        adRepository.findAiCandidatesForExternalSubject(
                                compatibleAdType, record.getLocation(), radiusKm * 1000.0, since)));
            } else {
                adCandidates.addAll(fromAdIds(
                        adRepository.findFallbackCandidateIdsForExternalSubject(
                                compatibleAdType, since, species, noLocationCandidateLimit),
                        radiusKm));
            }
        }
        if (spatial) {
            adCandidates.sort(Comparator.comparingDouble(AiCandidate::distanceKm));
        }

        return cap(adCandidates, spatial ? maxCandidates : noLocationCandidateLimit);
    }

    // ------------------------------------------------------------------
    // AD satırlarından AiCandidate üretimi (mevcut AiAnalysisPublisher'daki
    // mantığın taşınmış hâli — beyan > AI tahmini önceliği korunur).
    // ------------------------------------------------------------------

    private List<AiCandidate> fromAdRows(List<AiCandidateRow> rows) {
        if (rows.isEmpty()) {
            return List.of();
        }
        Map<Long, Double> distances = new LinkedHashMap<>();
        rows.forEach(r -> distances.put(r.getAdId(), r.getDistanceKm()));
        return buildAdCandidates(distances);
    }

    private List<AiCandidate> fromAdIds(List<Long> ids, double placeholderDistanceKm) {
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<Long, Double> distances = new LinkedHashMap<>();
        ids.forEach(id -> distances.put(id, placeholderDistanceKm));
        return buildAdCandidates(distances);
    }

    private List<AiCandidate> buildAdCandidates(Map<Long, Double> distances) {
        List<AiCandidate> candidates = new ArrayList<>(distances.size());
        for (Ad candidate : adRepository.findAllById(distances.keySet())) {
            List<float[]> vectors = extractAdVectors(candidate);
            if (vectors.isEmpty()) {
                continue;
            }
            String candidateSpecies = declaredSpecies(candidate);
            if (candidateSpecies == null) {
                candidateSpecies = candidate.getAiSpecies() == null ? "unknown" : candidate.getAiSpecies();
            }
            candidates.add(new AiCandidate(
                    candidate.getId(),
                    vectors,
                    candidate.getAiLabels() == null ? List.of() : candidate.getAiLabels(),
                    candidateSpecies,
                    distances.getOrDefault(candidate.getId(), 0.0),
                    candidate.getAiModelVersion(),
                    null));
        }
        candidates.sort(Comparator.comparingDouble(AiCandidate::distanceKm));
        return candidates;
    }

    // ------------------------------------------------------------------
    // EXTERNAL satırlarından AiCandidate üretimi.
    // ------------------------------------------------------------------

    private List<AiCandidate> fromExternalRows(List<ExternalCandidateRow> rows) {
        if (rows.isEmpty()) {
            return List.of();
        }
        Map<Long, Double> distances = new LinkedHashMap<>();
        rows.forEach(r -> distances.put(r.getId(), r.getDistanceKm()));
        return buildExternalCandidates(distances);
    }

    private List<AiCandidate> fromExternalIds(List<Long> ids, double placeholderDistanceKm) {
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<Long, Double> distances = new LinkedHashMap<>();
        ids.forEach(id -> distances.put(id, placeholderDistanceKm));
        return buildExternalCandidates(distances);
    }

    private List<AiCandidate> buildExternalCandidates(Map<Long, Double> distances) {
        List<AiCandidate> candidates = new ArrayList<>(distances.size());
        for (ExternalPetRecord candidate : externalPetRecordRepository.findAllById(distances.keySet())) {
            List<float[]> vectors = extractExternalVectors(candidate);
            if (vectors.isEmpty()) {
                continue;
            }
            String candidateSpecies = candidate.getSpecies() == null ? "unknown" : candidate.getSpecies();
            candidates.add(new AiCandidate(
                    null,
                    vectors,
                    candidate.getAiLabels() == null ? List.of() : candidate.getAiLabels(),
                    candidateSpecies,
                    distances.getOrDefault(candidate.getId(), 0.0),
                    candidate.getAiModelVersion(),
                    candidate.getId()));
        }
        candidates.sort(Comparator.comparingDouble(AiCandidate::distanceKm));
        return candidates;
    }

    private List<AiCandidate> merge(List<AiCandidate> a, List<AiCandidate> b, boolean spatial) {
        List<AiCandidate> merged = new ArrayList<>(a.size() + b.size());
        merged.addAll(a);
        merged.addAll(b);
        if (spatial) {
            merged.sort(Comparator.comparingDouble(AiCandidate::distanceKm));
        }
        return cap(merged, spatial ? maxCandidates : noLocationCandidateLimit);
    }

    private List<AiCandidate> cap(List<AiCandidate> candidates, int limit) {
        if (candidates.size() <= limit) {
            return candidates;
        }
        return candidates.subList(0, limit);
    }

    private List<float[]> extractAdVectors(Ad ad) {
        if (ad.getAiEmbeddings() == null) {
            return List.of();
        }
        return ad.getAiEmbeddings().stream().map(Ad.AiPhotoVector::embedding).filter(Objects::nonNull).toList();
    }

    private List<float[]> extractExternalVectors(ExternalPetRecord record) {
        if (record.getAiEmbeddings() == null) {
            return List.of();
        }
        return record.getAiEmbeddings().stream().map(Ad.AiPhotoVector::embedding).filter(Objects::nonNull).toList();
    }

    private String oppositeAdType(Ad.AdType type) {
        if (type == null) {
            return null;
        }
        return switch (type) {
            case LOST -> Ad.AdType.FOUND.name();
            case FOUND -> Ad.AdType.LOST.name();
            default -> null;
        };
    }

    /**
     * Bir external kaydın hangi ad_type havuzlarına karşı aranacağını
     * belirler. LOST/FOUND için tek, KARŞIT tür döner (eskisi gibi).
     * UNCERTAIN için İKİ tür BİRDEN döner (bkz. AiAnalysisListener'daki
     * "uncertainButUsable" notu) -- kategori metin/görsel analizinden
     * belirsiz çıktıysa kaydın kendisinin LOST mu FOUND mu bir ilan olduğunu
     * bilmiyoruz, dolayısıyla hangi havuzu arayacağımızı da bilemeyiz;
     * ikisini birden taramak, gerçek bir eşleşmeyi konusu belirsiz diye hiç
     * aramamaktan iyidir. ADOPTION İÇİN yalnızca LOST havuzu taranır --
     * "yuva arıyoruz" diye paylaşılan bir hayvan (bulunmuş/sahiplendirilecek)
     * başka birinin kayıp ilanındaki hayvanıyla aynı olabilir (2026-08-19
     * kullanıcı raporu, canlı bir örnekle doğrulandı: bkz. commit) -- FOUND
     * havuzu kasıtlı olarak dahil değil, "ben bunu buldum, sahibi arıyorum"
     * ile "bu hayvana yuva arıyorum" farklı niyetlerdir, ikisini eşleştirmek
     * anlamsız olurdu. IRRELEVANT ve null için boş liste (hiç arama).
     *
     * NOT: bu yalnızca EXTERNAL (Instagram) kayıtlar için geçerli --
     * findCandidatesForAd()'daki native ADOPTION ilanları hâlâ hiçbir zaman
     * eşleştirmeye girmiyor (oppositeAdType, bu metodun ayrı bir dalı),
     * kasıtlı olarak dokunulmadı.
     */
    private List<String> compatibleAdTypesForCategory(ExternalCategory category) {
        if (category == null) {
            return List.of();
        }
        return switch (category) {
            case LOST -> List.of(Ad.AdType.FOUND.name());
            case FOUND -> List.of(Ad.AdType.LOST.name());
            case UNCERTAIN -> List.of(Ad.AdType.LOST.name(), Ad.AdType.FOUND.name());
            case ADOPTION -> List.of(Ad.AdType.LOST.name());
            default -> List.of();
        };
    }

    private String declaredSpecies(Ad ad) {
        if (ad.getSpecies() == null) {
            return null;
        }
        return switch (ad.getSpecies()) {
            case CAT -> "cat";
            case DOG -> "dog";
            default -> null;
        };
    }
}
