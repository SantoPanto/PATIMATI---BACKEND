package com.works.patimati.ai;

import com.works.patimati.ai.dto.AiAnalysisResult;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.enums.AiStatus;
import com.works.patimati.repository.AdRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * AI'dan dönen sonucu ilana yazar (entegrasyon sözleşmesi §4).
 *
 * <p><b>Satır yazımı tekrar teslime dayanıklıdır.</b> RabbitMQ "en az bir kez"
 * teslim eder, yani aynı mesaj iki kez gelebilir. İlana yazma işi
 * {@code ad_id} üzerinden idempotenttir: aynı sonucu iki kez yazmak aynı
 * satırı aynı değerlerle günceller (§8).
 *
 * <p>⚠ <b>Bildirim ayrı bir olaydır</b> ve §8'in "ek bir tekrar-koruması
 * gerekmez" cümlesi onun için geçerli değildir: tekrar teslimde yeniden
 * gönderilirdi. Koruması artık {@link AiMatchNotifier} içinde, {@code ad_match}
 * satırının {@code notification_sent_at} damgasıyla kuruluyor (B6).
 */
@Component
@RequiredArgsConstructor
public class AiAnalysisListener {

    private static final Logger log = LoggerFactory.getLogger(AiAnalysisListener.class);

    private final AdRepository adRepository;
    private final AiMatchNotifier matchNotifier;

    @RabbitListener(queues = AiRabbitConfig.RESULT_QUEUE,
                    containerFactory = "aiListenerContainerFactory")
    @Transactional
    public void onResult(AiAnalysisResult result) {
        if (result == null || result.adId() == null) {
            // adId olmadan yazacak satırı bulamayız. Mesajı reddedip yeniden
            // kuyruğa koymak sonsuz döngü olurdu; günlüğe yazıp bırakıyoruz.
            log.error("AI sonucunda ad_id yok, mesaj atlandı: {}", result);
            return;
        }

        Optional<Ad> found = adRepository.findById(result.adId());
        if (found.isEmpty()) {
            // İlan analiz sürerken silinmiş olabilir — normal bir durum.
            log.warn("AI sonucu geldi ama ilan bulunamadı: adId={}", result.adId());
            return;
        }

        Ad ad = found.get();
        ad.setAiProcessedAt(result.processedAt() != null ? result.processedAt() : Instant.now());

        if (!result.isOk()) {
            String code = result.error() != null ? result.error().code() : "UNKNOWN";
            String message = result.error() != null ? result.error().message() : "";
            log.warn("AI analizi başarısız: adId={} kod={} mesaj={}", result.adId(), code, message);

            // İlan YAYINDA KALIR (§4). AI bir ektir; analiz edilememesi ilanı
            // görünmez yapmamalı. Yalnızca eşleştirmeye aday olarak girmez.
            ad.setAiStatus(AiStatus.FAILED);
            adRepository.save(ad);
            return;
        }

        applyAnalysis(ad, result);
        ad.setAiStatus(AiStatus.DONE);
        adRepository.save(ad);

        logSkipped(result);

        // Eşleşmelerin KAYDI ve eşiği geçenler için bildirim.
        //
        // ⚠ Burada eskiden "hangi çifte daha önce bildirim gitti bilgisini
        // notifier tutuyor" yazıyordu. YANLIŞTI: notifier de hiçbir şey
        // tutmuyordu (ölçüldü, 17.08). İki bileşen de ötekinin yaptığını
        // sanıyordu. Artık kayıt gerçekten yazılıyor ve damga oradan okunuyor.
        //
        // Eşik AI'nın cevabından geçiyor: kaydın "o an eşik neydi" sorusuna
        // doğru cevap verebilmesi için değerin KAYNAĞINDAN gelmesi gerekiyor.
        // Backend yapılandırmasından okunsaydı iki kaynak sessizce kayardı.
        List<AiAnalysisResult.Match> matches = result.matches() == null
                ? List.of() : result.matches();
        matchNotifier.recordAndNotify(ad, matches, result.matchThreshold());

        log.info("AI analizi tamamlandı: adId={} tür={} cins={} eşleşme={} model={}",
                ad.getId(), ad.getAiSpecies(), ad.getAiBreed(), matches.size(),
                ad.getAiModelVersion());
    }

    /** Sonuçtaki analiz bloğunu ilana yazar. */
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

        // "Fotoğrafta gerçekten hayvan var mı" cevabı. AI bunu baştan beri
        // gönderiyordu ama hiçbir yere yazılmıyordu: ekran görüntüsüyle açılan
        // ilan normal ilan gibi DONE olup aday havuzuna giriyordu.
        // null gelirse null yazılır — "AI söylemedi" ile "hayvan yok" farklıdır.
        ad.setAiIsPet(analysis.isPet());
        if (Boolean.FALSE.equals(analysis.isPet())) {
            // Sessiz kalmasın: ilan yayında kalıyor ama fotoğrafı işe yaramıyor.
            log.info("İlan {} fotoğraflarında kedi/köpek görülmedi (is_pet=false)",
                    ad.getId());
        }

        // model_version KRİTİK: hangi modelle üretildiğini söyler. Bu alan
        // olmadan hangi vektörlerin bayat olduğu anlaşılamaz ve model
        // değiştiğinde sessizce yanlış benzerlik hesaplanır.
        ad.setAiModelVersion(result.modelVersion());

        if (analysis.embeddings() != null
                && analysis.embeddings().size() < ad.getPhotoUrls().size()) {
            // Bazı fotoğraflar indirilemedi ya da bozuktu; analiz kalanlarla
            // yapıldı. Kayıp sessiz kalmasın.
            log.info("İlan {} için {} fotoğraftan {} tanesi analiz edildi",
                    ad.getId(), ad.getPhotoUrls().size(), analysis.embeddings().size());
        }
        if (result.failedPhotos() != null && !result.failedPhotos().isEmpty()) {
            result.failedPhotos().forEach(f ->
                    log.info("  indirilemedi: {} — {}", f.url(), f.error()));
        }
    }

    /**
     * Vektörleri fotoğraf adresleriyle eşler.
     *
     * <p><b>Burada KALICI depolama referansı saklanır</b> ({@code s3://...}),
     * AI'ya gönderilen süreli adres değil. Süreli adres 15 dakikada ölür;
     * veritabanına yazmanın anlamı olmaz.
     *
     * <p>Sözleşme, {@code analysis.embeddings} sırasının {@code photo_urls} ile
     * aynı olduğunu söyler. Yine de sayılar tutmayabilir: indirilemeyen
     * fotoğraflar atlanır. O yüzden kısa olan listeye göre eşliyoruz —
     * yanlış adresi yanlış vektöre bağlamaktansa eksik bağlamak yeğdir.
     * (Yayıncı taraf da bu yüzden kısmi liste göndermiyor; bkz.
     * {@code AiAnalysisPublisher.toDownloadableUrls}.)
     */
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
        // Adres sayısı vektörden azsa (beklenmez) kalan vektörler adressiz eklenir:
        // vektörü atmak, eşleştirme yeteneğini kaybetmek demek olurdu.
        for (int i = count; i < vectors.size(); i++) {
            paired.add(new Ad.AiPhotoVector(null, vectors.get(i)));
        }
        return paired;
    }

    /**
     * Elenen adayları raporlar.
     *
     * <p>{@code modelSurumuUyusmuyor > 0} ise ilgili ilanların vektörleri
     * bayatlamıştır ve yeniden analiz edilmeleri gerekir — bu yüzden uyarı
     * seviyesinde yazılıyor, gözden kaçmasın.
     */
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
