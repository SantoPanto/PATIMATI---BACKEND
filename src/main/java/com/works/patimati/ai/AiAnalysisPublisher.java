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
    private final MatchCandidateGatherer candidateGatherer;

    // Yarıçap/pencere/aday-sınırı artık MatchCandidateGatherer'da yaşıyor —
    // hem native hem external aday sorguları için TEK yerde okunuyor.

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

            AiAnalysisRequest request = AiAnalysisRequest.forAd(
                    AiRabbitConfig.SCHEMA_VERSION,
                    UUID.randomUUID().toString(),
                    ad.getId(),
                    ad.getAdType().name(),
                    declaredSpecies(ad),
                    photoUrls,
                    // Faz 2 revize blueprint §1: bu tek satır hem konumsuz
                    // ilanlar için var olan boş-liste hatasını düzeltir hem de
                    // Flow B'yi (native ilan artık external Instagram
                    // adaylarını da görür) hiçbir yeni tetikleyici mekanizma
                    // eklemeden sağlar — MatchCandidateGatherer her iki
                    // tabloyu birden okur.
                    candidateGatherer.findCandidatesForAd(ad));

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

            log.info("AI analiz isteği yayınlandı: adId={} aday={} fotoğraf={}",
                    ad.getId(), request.candidates().size(), request.photoUrls().size());

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
     * <p>ADOPTION eşleştirmeye girmez (§5) ve fotoğrafsız ilan analiz edilemez —
     * ikisi de kuyruğa boşuna mesaj koymamak için burada eleniyor.
     */
    private boolean isEligible(Ad ad) {
        if (ad.getId() == null) {
            log.warn("Kaydedilmemiş ilan analiz kuyruğuna gönderilemez");
            return false;
        }
        if (ad.getAdType() == null || ad.getAdType() == Ad.AdType.ADOPTION) {
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

    // Aday toplama artık MatchCandidateGatherer'da yaşıyor (Faz 2 revize
    // blueprint §1/§31) — hem native↔native hem native↔external adayları
    // aynı yerde, aynı konumsuz-fallback kuralıyla üretmek için.
}
