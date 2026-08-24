package com.works.patimati.ai;

import com.works.patimati.ai.dto.AiAnalysisResult;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.enums.AiStatus;
import com.works.patimati.entity.enums.ExternalCategory;
import com.works.patimati.entity.enums.ExternalProcessingStatus;
import com.works.patimati.entity.external.ExternalPetRecord;
import com.works.patimati.entity.external.ExternalSourceMedia;
import com.works.patimati.entity.external.ExternalSourcePost;
import com.works.patimati.external.ExternalMatchingService;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.external.ExternalPetRecordRepository;
import com.works.patimati.repository.external.ExternalSourceMediaRepository;
import com.works.patimati.repository.external.ExternalSourcePostRepository;
import com.works.patimati.service.PotentialMatchService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * AI'dan dönen sonucu ilana YA DA external kayda yazar (entegrasyon
 * sözleşmesi §4; Faz 2 revize blueprint §1/§2).
 *
 * <p><b>Yönlendirme:</b> {@code result.externalRecordId()} doluysa Aşama 1
 * (yalnızca analiz) sonucu bir {@link ExternalPetRecord}'a yazılır ve
 * ardından kategori uyumluysa (LOST/FOUND, needs_review değilse) Aşama 2
 * ({@link ExternalMatchingService}) tetiklenir. Aksi hâlde native davranış
 * — {@link Ad}'a yazılır — AYNEN korunur, tek fark eşleşmelerin artık
 * {@link AiMatchNotifier} yerine {@link PotentialMatchService} üzerinden
 * kalıcı/dedup'lı hâle gelmesidir.
 *
 * <p><b>Tekrar teslime dayanıklıdır</b> (§8): her iki yol da idempotent-by-
 * overwrite'tır. RabbitMQ "en az bir kez" teslim eder, yani aynı mesaj iki
 * kez gelebilir; satır yazımı {@code ad_id}/{@code externalRecordId}
 * üzerinden idempotenttir. Eski yaklaşımın (bkz. {@link AiMatchNotifier})
 * açık bıraktığı "tekrar teslimde aynı çifte bildirim iki kez gider"
 * sorunu, bildirimi {@code PotentialMatchService} üzerinden DB seviyesinde
 * dedup'layarak kapatılmıştır.
 */
@Component
@RequiredArgsConstructor
public class AiAnalysisListener {

    private static final Logger log = LoggerFactory.getLogger(AiAnalysisListener.class);

    private final AdRepository adRepository;
    private final ExternalPetRecordRepository externalPetRecordRepository;
    private final ExternalSourcePostRepository externalSourcePostRepository;
    private final ExternalSourceMediaRepository externalSourceMediaRepository;
    private final PotentialMatchService potentialMatchService;
    private final ExternalMatchingService externalMatchingService;

    @RabbitListener(queues = AiRabbitConfig.RESULT_QUEUE,
                    containerFactory = "aiListenerContainerFactory")
    @Transactional
    public void onResult(AiAnalysisResult result) {
        if (result == null || (result.adId() == null && result.externalRecordId() == null)) {
            log.error("AI sonucunda ne ad_id ne external_record_id var, mesaj atlandı: {}", result);
            return;
        }

        if (result.isForExternalRecord()) {
            handleExternalResult(result);
        } else {
            handleNativeResult(result);
        }
    }

    // ------------------------------------------------------------------
    // NATIVE — davranış aynı, yalnızca eşleşme kalıcılığı değişti.
    // ------------------------------------------------------------------

    private void handleNativeResult(AiAnalysisResult result) {
        Optional<Ad> found = adRepository.findById(result.adId());
        if (found.isEmpty()) {
            log.warn("AI sonucu geldi ama ilan bulunamadı: adId={}", result.adId());
            return;
        }

        Ad ad = found.get();
        ad.setAiProcessedAt(result.processedAt() != null ? result.processedAt() : Instant.now());

        if (!result.isOk()) {
            logError("adId=" + result.adId(), result);
            ad.setAiStatus(AiStatus.FAILED);
            adRepository.save(ad);
            return;
        }

        applyAnalysis(ad, result);
        ad.setAiStatus(AiStatus.DONE);
        adRepository.save(ad);
        logSkipped(result);

        List<AiAnalysisResult.Match> matches = result.matches() == null ? List.of() : result.matches();
        for (AiAnalysisResult.Match match : matches) {
            if (!match.match()) {
                continue;
            }
            if (match.externalRecordId() != null) {
                // Flow B: native ilan artık external Instagram adaylarını da görüyor.
                potentialMatchService.recordExternalMatch(
                        ad.getId(), match.externalRecordId(),
                        (float) match.visual(), (float) match.label(), (float) match.location(),
                        (float) match.score(), ad.getAiModelVersion());
            } else if (match.adId() != null) {
                potentialMatchService.recordAdMatch(
                        ad.getId(), match.adId(),
                        (float) match.visual(), (float) match.label(), (float) match.location(),
                        (float) match.score(), ad.getAiModelVersion());
            }
        }

        log.info("AI analizi tamamlandı: adId={} tür={} cins={} eşleşme={} model={}",
                ad.getId(), ad.getAiSpecies(), ad.getAiBreed(), matches.size(), ad.getAiModelVersion());
    }

    // ------------------------------------------------------------------
    // EXTERNAL — Aşama 1 sonucu + Aşama 2 tetikleme kararı.
    // ------------------------------------------------------------------

    private void handleExternalResult(AiAnalysisResult result) {
        Optional<ExternalPetRecord> found = externalPetRecordRepository.findById(result.externalRecordId());
        if (found.isEmpty()) {
            log.warn("AI sonucu geldi ama external kayıt bulunamadı: externalRecordId={}",
                    result.externalRecordId());
            return;
        }

        ExternalPetRecord record = found.get();
        ExternalSourcePost post = record.getPost();
        record.setAiProcessedAt(result.processedAt() != null ? result.processedAt() : Instant.now());

        if (!result.isOk()) {
            logError("externalRecordId=" + result.externalRecordId(), result);
            record.setAiStatus(AiStatus.FAILED);
            externalPetRecordRepository.save(record);
            post.setProcessingStatus(ExternalProcessingStatus.FAILED);
            post.setFailureReason(result.error() != null ? result.error().message() : "AI analizi başarısız");
            externalSourcePostRepository.save(post);
            return;
        }

        applyExternalAnalysis(record, post, result);
        record.setAiStatus(AiStatus.DONE);
        externalPetRecordRepository.save(record);

        // DÜZELTME (2026-08-19, kullanıcı raporu #2): ADOPTION artık da
        // kategori-uyumlu sayılır -- yalnızca Instagram tarafında (bu
        // metodun kendisi zaten yalnızca external kayıtlar için çalışıyor).
        // Native ADOPTION ilanları bu değişiklikten ETKİLENMEZ (ayrı bir kod
        // yolu, bkz. applyAnalysis/native eşleştirme -- oraya hiç dokunulmadı).
        // Gerekçe: "yuva arıyoruz" diye paylaşılan bir hayvan (bulunmuş/
        // sahiplendirilecek), başka birinin LOST ilanındaki hayvanıyla aynı
        // olabilir -- canlı bir örnekte doğrulandı (bkz. commit). Hangi ad_type
        // havuzunun aranacağına MatchCandidateGatherer karar verir (ADOPTION
        // için yalnızca LOST havuzu, bkz. compatibleAdTypesForCategory).
        boolean categoryConfident = (record.getCategory() == ExternalCategory.LOST
                || record.getCategory() == ExternalCategory.FOUND
                || record.getCategory() == ExternalCategory.ADOPTION)
                && !record.isNeedsReview();

        // DÜZELTME (2026-08-19, kullanıcı raporu): kategori UNCERTAIN kaldığı
        // zaman (metin analizi caption/yorumdan ve -artık varsa- görselden
        // bile LOST/FOUND'a karar veremediği durumlar) eskiden Aşama 2 hiç
        // ÇAĞRILMIYORDU -- sistemde eşleşecek gerçek bir ilan dursa bile hiç
        // aranmıyordu. UNCERTAIN + gerçek bir hayvan fotoğrafı (aiIsPet=true)
        // varsa artık matching yine de denenir; MatchCandidateGatherer bu
        // durumda HANGİ havuzu (LOST mu FOUND mu) arayacağını bilmediği için
        // ikisini BİRDEN tarar (bkz. oppositeCategory). "metin LOST/FOUND dedi
        // ama görsel hayvan görmedi" düşürmesi (aşağıda, satır ~230) aiIsPet'i
        // zaten false yapıp bunu burada da elemeye devam eder -- o güvenlik
        // önlemi bozulmuyor.
        boolean uncertainButUsable = record.getCategory() == ExternalCategory.UNCERTAIN
                && Boolean.TRUE.equals(record.getAiIsPet());

        boolean compatible = categoryConfident || uncertainButUsable;

        if (compatible) {
            post.setProcessingStatus(ExternalProcessingStatus.ANALYZED);
            externalSourcePostRepository.save(post);
            // Aşama 2: candidates BURADA gönderilmez (kuyruk mesajı zaten
            // boş adayla gönderildi) — senkron ayrı bir çağrıdır.
            externalMatchingService.attemptMatching(record);
        } else {
            post.setProcessingStatus(ExternalProcessingStatus.COMPLETED);
            externalSourcePostRepository.save(post);
            log.info("External kayıt {} eşleştirmeye girmiyor: kategori={} needsReview={}",
                    record.getId(), record.getCategory(), record.isNeedsReview());
        }
    }

    /**
     * Sonuçtaki analiz bloğunu external kayda yazar — {@link #applyAnalysis}
     * ile aynı vektör/etiket mantığı, farkı NLP çıktısının (kategori vb.)
     * de burada işlenmesi.
     */
    private void applyExternalAnalysis(ExternalPetRecord record, ExternalSourcePost post, AiAnalysisResult result) {
        AiAnalysisResult.Analysis analysis = result.analysis();
        if (analysis == null) {
            log.warn("Sonuç 'ok' ama analiz bloğu boş: externalRecordId={}", result.externalRecordId());
            return;
        }

        List<String> mediaUrls = externalSourceMediaRepository.findByPostOrderByOrdinalAsc(post).stream()
                .map(ExternalSourceMedia::getStorageKey)
                .toList();
        record.setAiEmbeddings(pairVectorsWithUrls(mediaUrls, analysis.embeddings()));
        record.setAiLabels(analysis.labels());
        record.setAiIsPet(analysis.isPet());
        record.setSpecies(analysis.species());
        record.setBreed(analysis.breed());
        record.setBreedConfidence((float) analysis.breedConfidence());
        record.setAiModelVersion(result.modelVersion());
        if (analysis.colors() != null) {
            record.setColors(analysis.colors().stream().map(AiAnalysisResult.Color::name).toList());
        }

        AiAnalysisResult.NlpAttributes nlp = result.nlpAttributes();
        if (nlp != null && nlp.category() != null) {
            record.setCategory(parseCategory(nlp.category()));
            record.setCategoryConfidence(nlp.categoryConfidence() == null ? null : nlp.categoryConfidence().floatValue());
            record.setGender(nlp.gender());
            record.setAgeText(nlp.ageText());
            record.setPetName(nlp.petName());
            record.setLocationText(nlp.locationText() != null ? nlp.locationText() : post.getLocationText());
            record.setLocationConfidence(nlp.locationConfidence() == null ? null : nlp.locationConfidence().floatValue());
            record.setDistinguishingFeatures(nlp.distinguishingFeatures());
            record.setNeedsReview(Boolean.TRUE.equals(nlp.needsReview()));
        } else {
            // Metin sinyali yok (caption VE triggering_comment ikisi de boştu,
            // ya da AI sağlayıcısı hiç yanıt veremedi) — asla zorla bir karar
            // üretilmez, UNCERTAIN varsayılanında kalır.
            record.setCategory(ExternalCategory.UNCERTAIN);
            record.setLocationText(post.getLocationText());
        }

        // Görsel+metin füzyonu (blueprint §29): metin LOST/FOUND dese bile
        // görsel fotoğrafta hayvan bile görmüyorsa zorla üretilmez.
        if (Boolean.FALSE.equals(analysis.isPet())
                && (record.getCategory() == ExternalCategory.LOST || record.getCategory() == ExternalCategory.FOUND)) {
            log.info("External kayıt {}: metin {} dedi ama görsel hayvan görmedi — UNCERTAIN'a düşürülüyor",
                    record.getId(), record.getCategory());
            record.setCategory(ExternalCategory.UNCERTAIN);
            record.setNeedsReview(true);
        }
    }

    private ExternalCategory parseCategory(String raw) {
        try {
            return ExternalCategory.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException e) {
            return ExternalCategory.UNCERTAIN;
        }
    }

    // ------------------------------------------------------------------
    // Paylaşılan yardımcılar (native AiAnalysisPublisher/Listener ile aynı mantık).
    // ------------------------------------------------------------------

    private void applyAnalysis(Ad ad, AiAnalysisResult result) {
        AiAnalysisResult.Analysis analysis = result.analysis();
        if (analysis == null) {
            log.warn("Sonuç 'ok' ama analiz bloğu boş: adId={}", result.adId());
            return;
        }

        ad.setAiEmbeddings(pairVectorsWithUrls(ad.getPhotoUrls(), analysis.embeddings()));
        ad.setAiLabels(analysis.labels());
        ad.setAiSpecies(analysis.species());
        ad.setAiBreed(analysis.breed());
        ad.setAiBreedConfidence((float) analysis.breedConfidence());
        ad.setAiIsPet(analysis.isPet());
        if (Boolean.FALSE.equals(analysis.isPet())) {
            log.info("İlan {} fotoğraflarında kedi/köpek görülmedi (is_pet=false)", ad.getId());
        }
        ad.setAiModelVersion(result.modelVersion());

        if (analysis.embeddings() != null && analysis.embeddings().size() < ad.getPhotoUrls().size()) {
            log.info("İlan {} için {} fotoğraftan {} tanesi analiz edildi",
                    ad.getId(), ad.getPhotoUrls().size(), analysis.embeddings().size());
        }
        if (result.failedPhotos() != null && !result.failedPhotos().isEmpty()) {
            result.failedPhotos().forEach(f -> log.info("  indirilemedi: {} — {}", f.url(), f.error()));
        }
    }

    private List<Ad.AiPhotoVector> pairVectorsWithUrls(List<String> urls, List<float[]> vectors) {
        if (vectors == null || vectors.isEmpty()) {
            return List.of();
        }
        List<String> safeUrls = urls == null ? List.of() : urls;
        int count = Math.min(safeUrls.size(), vectors.size());

        List<Ad.AiPhotoVector> paired = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            paired.add(new Ad.AiPhotoVector(safeUrls.get(i), vectors.get(i)));
        }
        for (int i = count; i < vectors.size(); i++) {
            paired.add(new Ad.AiPhotoVector(null, vectors.get(i)));
        }
        return paired;
    }

    private void logError(String context, AiAnalysisResult result) {
        String code = result.error() != null ? result.error().code() : "UNKNOWN";
        String message = result.error() != null ? result.error().message() : "";
        log.warn("AI analizi başarısız: {} kod={} mesaj={}", context, code, message);
    }

    private void logSkipped(AiAnalysisResult result) {
        AiAnalysisResult.SkippedCandidates skipped = result.skippedCandidates();
        if (skipped == null || skipped.toplam() == 0) {
            return;
        }
        if (skipped.modelSurumuUyusmuyor() > 0) {
            log.warn("adId={}: {} aday model sürümü tutmadığı için atlandı — "
                            + "o ilanların yeniden analiz edilmesi gerekiyor",
                    result.adId(), skipped.modelSurumuUyusmuyor());
        }
        log.info("adId={} elenen adaylar: toplam={} kendisi={} tekrar={} sürüm={} "
                        + "bozukVektör={} sınırAşımı={}",
                result.adId(), skipped.toplam(), skipped.kendisi(), skipped.tekrarEden(),
                skipped.modelSurumuUyusmuyor(), skipped.gecersizEmbedding(),
                skipped.adaySiniriAsildi());
    }
}
