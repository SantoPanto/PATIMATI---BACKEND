package com.works.patimati.ai;

import com.works.patimati.ai.dto.AiAnalysisRequest;
import com.works.patimati.ai.dto.AiCandidate;
import com.works.patimati.entity.Ad;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.storage.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * İlan kaydedildikten sonra AI'ya "şunu analiz et" isteği yayınlar.
 *
 * <p>Akış asenkrondur (sözleşme §1): kullanıcı ilanını verir ve beklemez;
 * analiz 1–3 saniye sürdüğü için senkron çağrı deneyimi bozardı.
 */
@Service
@RequiredArgsConstructor
public class AiAnalysisPublisher {

    private static final Logger log = LoggerFactory.getLogger(AiAnalysisPublisher.class);

    private final RabbitTemplate aiRabbitTemplate;
    private final AdRepository adRepository;
    private final ImageStorageService imageStorageService;

    /** Aday yarıçapı. Bildirim yarıçapından (5 km) FARKLIDIR — hayvan yürür. */
    @Value("${ai.matching.radius-km:25}")
    private double radiusKm;

    /** Aday zaman penceresi; eski ilanlar gürültü yaratır. */
    @Value("${ai.matching.window-days:90}")
    private int windowDays;

    /** Mesaj boyutu sınırı. 100 aday ≈ 700 KB (aday başına ~7 KB, 768 float). */
    @Value("${ai.matching.max-candidates:100}")
    private int maxCandidates;

    /**
     * İlanı analiz kuyruğuna gönderir.
     *
     * <p><b>Hiçbir zaman istisna fırlatmaz.</b> AI bir ektir, ürünün kalbi
     * değildir: kuyruk kapalıysa ilan yine de kaydedilmiş olmalı. Sorun
     * günlüğe yazılır, ilan {@code PENDING} kalır ve sonra yeniden denenebilir.
     *
     * <p>{@code REQUIRES_NEW} ile ayrı bir okuma işlemi açılır; çağıran tarafın
     * işlemi bu yüzden uzamaz.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void publish(Ad ad) {
        try {
            if (!isEligible(ad)) {
                return;
            }

            List<String> photoUrls = toDownloadableUrls(ad.getPhotoUrls());
            if (photoUrls.isEmpty()) {
                log.warn("İlan {} için indirilebilir fotoğraf adresi üretilemedi",
                        ad.getId());
                return;
            }

            boolean matchRequired = ad.getIsMatchRequired() != null
                    ? ad.getIsMatchRequired()
                    : (ad.getAdType() != Ad.AdType.ADOPTION);

            List<AiCandidate> candidates = matchRequired ? collectCandidates(ad) : List.of();

            AiAnalysisRequest request = new AiAnalysisRequest(
                    AiRabbitConfig.SCHEMA_VERSION,
                    UUID.randomUUID().toString(),
                    ad.getId(),
                    ad.getAdType().name(),
                    declaredSpecies(ad),
                    photoUrls,
                    candidates,
                    matchRequired);

            aiRabbitTemplate.convertAndSend(
                    AiRabbitConfig.EXCHANGE,
                    AiRabbitConfig.REQUEST_ROUTING_KEY,
                    request,
                    message -> {
                        // Kalıcı mesaj: kuyruk durable olsa bile mesaj kalıcı
                        // değilse broker yeniden başlayınca istek KAYBOLUR ve
                        // ilan sonsuza kadar PENDING kalır. İkisi birlikte gerekir.
                        message.getMessageProperties().setDeliveryMode(
                                org.springframework.amqp.core.MessageDeliveryMode.PERSISTENT);
                        message.getMessageProperties().setCorrelationId(request.requestId());
                        return message;
                    });

            log.info("AI analiz isteği yayınlandı: adId={} isMatchRequired={} aday={} fotoğraf={}",
                    ad.getId(), matchRequired, request.candidates().size(), request.photoUrls().size());

        } catch (Exception e) {
            // Yutulan istisna bilinçli: ilan zaten kaydedildi, kullanıcıyı
            // ilgilendiren iş bitti. Burada patlamak ilan oluşturmayı da
            // düşürürdü — yani AI arızası ürünü durdururdu.
            log.error("AI analiz isteği yayınlanamadı (adId={}): {}",
                    ad.getId(), e.getMessage(), e);
        }
    }

    /**
     * İlan analize uygun mu?
     *
     * <p>Fotoğrafsız ilan analiz edilemez.
     */
    private boolean isEligible(Ad ad) {
        if (ad.getId() == null) {
            log.warn("Kaydedilmemiş ilan analiz kuyruğuna gönderilemez");
            return false;
        }
        if (ad.getAdType() == null) {
            return false;
        }
        if (ad.getPhotoUrls() == null || ad.getPhotoUrls().isEmpty()) {
            log.info("İlan {} fotoğrafsız, AI analizi atlandı", ad.getId());
            return false;
        }
        return true;
    }

    /**
     * Depolama referanslarını AI'ın indirebileceği HTTPS adreslerine çevirir.
     *
     * <p><b>Bu dönüşüm zorunlu.</b> Veritabanında fotoğraflar
     * {@code s3://kova/ads/2026/07/uuid.jpg} biçiminde bir <i>depolama
     * referansı</i> olarak duruyor — HTTP adresi değil. AI servisi yalnızca
     * https indirir ve {@code s3://} şemasını reddeder; olduğu gibi
     * gönderilseydi HER analiz {@code PHOTO_DOWNLOAD_FAILED} ile dönerdi.
     *
     * <p>Şu an süreli özel adres (presigned URL) üretiliyor, geçerlilik süresi
     * {@code app.storage.presignedUrlDuration} (15 dk). Kuyruk normalde
     * saniyeler içinde işlediği için bu bol bir pay; ancak AI servisi uzun süre
     * kapalı kalırsa bekleyen mesajın adresi ölür ve o ilan FAILED olur —
     * yeniden analiz gerekir.
     *
     * <p>Ekip kararına göre fotoğraflar ileride kendi alan adımızın altından
     * ({@code cdn.<alanadi>}) <b>public</b> sunulacak. O zaman burada süreli
     * adres üretmeye gerek kalmaz ve süre sorunu tümden ortadan kalkar.
     *
     * <p><b>Ya hepsi ya hiçbiri.</b> Bir adres üretilemezse kısaltılmış liste
     * gönderilmez, hiç gönderilmez. Sebebi ince: sonuçtaki vektörlerin sırası
     * gönderilen adreslerin sırasıyla aynıdır ve dinleyici bunları ilanın
     * <i>tam</i> fotoğraf listesiyle eşler. Araya bir fotoğraf atlanırsa sıra
     * kayar ve üçüncü fotoğrafın vektörü ikinciye yazılır — hata vermeden,
     * sessizce yanlış veri. Eksik analiz, yanlış analizden iyidir.
     */
    private List<String> toDownloadableUrls(List<String> storageReferences) {
        List<String> urls = new ArrayList<>(storageReferences.size());
        for (String reference : storageReferences) {
            try {
                urls.add(imageStorageService.createTemporaryReadUrl(reference));
            } catch (RuntimeException e) {
                log.warn("Fotoğraf adresi üretilemedi ({}): {} — vektör sırası"
                        + " kaymasın diye bu ilan hiç gönderilmiyor",
                        reference, e.getMessage());
                return List.of();
            }
        }
        return urls;
    }

    /**
     * Kullanıcının tür beyanı — AI'ın tahmininden ÖNCELİKLİDİR (§7 kural 2).
     *
     * <p>Sözleşme küçük harf ister ({@code "cat"}), enum ise büyük harf üretir.
     * AI tarafı büyük/küçük harfe duyarsız karşılaştırıyor ama biz yine de
     * sözleşmeye uygun gönderiyoruz: "gönderene sıkı, alana hoşgörülü".
     *
     * <p>Kedi/köpek dışındaki türlerde {@code null} döneriz — AI yalnızca bu
     * ikisini tanıyor; "bird" göndermek tür filtresini anlamsız yere kapatırdı.
     */
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

    /**
     * Karşıt tipteki, yakındaki, analizi bitmiş ilanları toplar.
     *
     * <p>Vektörü olmayan aday sessizce atlanır: {@code ai_status = DONE} olsa
     * bile vektör alanı boşsa kıyaslanacak bir şey yoktur.
     */
    private List<AiCandidate> collectCandidates(Ad ad) {
        if (ad.getLocation() == null) {
            log.info("İlan {} konumsuz, aday süzmesi yapılamadı", ad.getId());
            return List.of();
        }

        String opposite = switch (ad.getAdType()) {
            case LOST -> Ad.AdType.FOUND.name();
            case FOUND -> Ad.AdType.LOST.name();
            default -> null;
        };
        if (opposite == null) {
            return List.of();
        }

        List<AiCandidateRow> rows = adRepository.findAiCandidates(
                ad.getId(),
                opposite,
                ad.getLocation(),
                radiusKm * 1000.0,
                Instant.now().minus(windowDays, ChronoUnit.DAYS));

        if (rows.isEmpty()) {
            return List.of();
        }
        if (rows.size() > maxCandidates) {
            log.info("İlan {} için {} aday bulundu, en yakın {} tanesi gönderiliyor",
                    ad.getId(), rows.size(), maxCandidates);
            rows = rows.subList(0, maxCandidates);
        }

        // Mesafeleri kimliğe göre saklayıp entity'leri TEK sorguda çekiyoruz.
        Map<Long, Double> distances = new LinkedHashMap<>();
        rows.forEach(r -> distances.put(r.getAdId(), r.getDistanceKm()));

        List<AiCandidate> candidates = new ArrayList<>(distances.size());
        for (Ad candidate : adRepository.findAllById(distances.keySet())) {
            List<float[]> vectors = extractVectors(candidate);
            if (vectors.isEmpty()) {
                continue;
            }
            // Adayın türü de BEYANDAN alınır, AI tahmininden değil — yeni ilanda
            // (declaredSpecies) zaten öyle yapılıyordu, aday tarafı geride kalmıştı.
            //
            // Neden önemli: AI, güveni %80'in altındaysa "unknown" der ve bu nadir
            // değil (ölçüldü: gerçek bir kedi fotoğrafı "unknown" çıktı). AI
            // tarafındaki tür engeli ise İKİ taraf da bilindiğinde çalışıyor.
            // Aday "unknown" olduğu anda engel sessizce devre dışı kalıyor ve
            // kedi ilanı köpek ilanına aday olarak gidiyordu.
            //
            // Ölçüm (2026-08-05, kuyruğa yazılan mesaj okunarak):
            //   önce : aday species='unknown' -> engel yok,  skor 0.1521
            //   sonra: aday species='cat'     -> species_mismatch, skor 0.0
            //
            // Sözleşme §7 kural 2: kullanıcı beyanı önceliklidir, AI tahmini ezmez.
            String candidateSpecies = declaredSpecies(candidate);
            if (candidateSpecies == null) {
                candidateSpecies = candidate.getAiSpecies() == null
                        ? "unknown" : candidate.getAiSpecies();
            }

            candidates.add(new AiCandidate(
                    candidate.getId(),
                    vectors,
                    candidate.getAiLabels() == null ? List.of() : candidate.getAiLabels(),
                    candidateSpecies,
                    distances.getOrDefault(candidate.getId(), 0.0),
                    candidate.getAiModelVersion()));
        }

        // findAllById sırayı korumaz; yakından uzağa sıralamayı geri kuruyoruz.
        candidates.sort(Comparator.comparingDouble(AiCandidate::distanceKm));
        return candidates;
    }

    private List<float[]> extractVectors(Ad ad) {
        if (ad.getAiEmbeddings() == null) {
            return List.of();
        }
        return ad.getAiEmbeddings().stream()
                .map(Ad.AiPhotoVector::embedding)
                .filter(Objects::nonNull)
                .toList();
    }
}
