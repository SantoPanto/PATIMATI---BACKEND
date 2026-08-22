package com.works.patimati.ai;

import com.works.patimati.ai.dto.AiAnalysisResult;
import com.works.patimati.dto.match.AdMatchResponseDTO;
import com.works.patimati.dto.match.AdMatchSaveRequest;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.User;
import com.works.patimati.notification.PushNotificationService;
import com.works.patimati.notification.PushResult;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.service.AdMatchService;
import com.works.patimati.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AI'ın bulduğu eşleşmeleri <b>kaydeder</b> ve ilgili kullanıcıları haberdar eder.
 *
 * <p><b>Bildirim her iki ilanın sahibine de gider</b> (ekip kararı, 29.07.2026):
 * kaybettiği hayvanı arayan da, bulduğu hayvanı bildiren de haber almak ister.
 * Kayıt da öyle: her eşleşme için <b>alıcı başına bir satır</b> yazılır, çünkü
 * "bu kullanıcıya bildirim gitti mi" sorusu kullanıcıya özeldir.
 *
 * <p><b>Dil önemli:</b> "eşleşti" değil <b>"olası eşleşme"</b> diyoruz. Bu bir
 * üslup tercihi değil, ölçüm sonucu: gerçek fotoğraflarda "aynı hayvan" ve
 * "farklı hayvan" skorları çakışıyor, hiçbir eşik ikisini temiz ayırmıyor
 * (ölçüm raporu §4). {@code match = true} "kesin aynı hayvan" değil,
 * "bildirim gönderilecek kadar eminiz" demektir. Son karar kullanıcınındır.
 *
 * <p><b>Günlük teslimatı olduğu gibi yazar.</b> Daha önce başarı satırı
 * koşulsuzdu: gönderim metodu jeton yokken sessizce dönüyor, Firebase hiç
 * başlatılmamışsa istisna yutuluyor, ama günlükte yine "bildirim" yazıyordu.
 * Yani tek gözlem noktası, hakkında bilgi vermesi gereken şey konusunda
 * yanıltıyordu. Artık {@link PushNotificationService} her alıcı için
 * {@link PushResult} döndürüyor ve satırlar <b>gerçekten gönderilene</b> göre
 * yazılıyor: gönderim yoksa INFO da yok.
 *
 * <p><b>B6 — eşleşme kaydı ve bildirim tekrarı (kapandı).</b> Bu sınıf eskiden
 * hiçbir şey saklamıyordu: bildirim gidemezse bilgi tamamen kayboluyor, aynı
 * sonuç kuyruktan tekrar teslim edilirse (RabbitMQ "en az bir kez" teslim eder)
 * aynı çifte bildirim yeniden gidiyordu. Artık:
 * <ul>
 *   <li><b>Eşiği geçmeyen aday da kaydedilir</b> ({@code passedThreshold=false}),
 *       yalnız bildirim tetiklemez (sözleşme §7 kural 3). Kaydetmeseydik
 *       "neden bu ilan listede yok" sorusu cevapsız kalırdı ve kullanıcı düşük
 *       skorlu adayları hiç göremezdi.</li>
 *   <li><b>Bildirim, kaydı {@code notification_sent_at} boş olan alıcıya gider</b>
 *       (§7 kural 4). Damga <b>gönderimden sonra</b> vurulur: ters sırada,
 *       gönderim patlarsa kayıt "gönderildi" der ve bildirim bir daha hiç
 *       denenmez. Damga yalnız <b>gerçekten giden</b> için vurulur — jeton yokken
 *       damgalamak, günlükteki eski yalanın veritabanı sürümü olurdu.</li>
 * </ul>
 *
 * <p><b>Bilinen sınır:</b> tekrar koruması {@code (kullanıcı, kaynak ilan,
 * eşleşen ilan)} üçlüsüne bakar, yani <b>yöne duyarlıdır</b>. Aynı çift ters
 * yönde ikinci kez analiz edilirse (A analiz edilip B'yi bulduktan sonra B'nin
 * yeniden analiz edilmesi) ikinci bir satır açılır ve bildirim yeniden gider.
 * Bugün ulaşılabilir değil: {@code AiAnalysisPublisher.publish} yalnız
 * {@code AdService.createAd} içinde çağrılıyor, ilan güncellemesi yeniden
 * analiz tetiklemiyor (ölçüldü 18.08). Toplu yeniden analiz eklenirse bu
 * madde yeniden açılmalı.
 */
@Service
public class AiMatchNotifier {

    private static final Logger log = LoggerFactory.getLogger(AiMatchNotifier.class);

    private static final String BILDIRIM_BASLIGI = "Olası eşleşme bulundu";

    private final AdRepository adRepository;
    private final AdMatchService adMatchService;
    private final NotificationService notificationService;
    private final PushNotificationService legacyPushNotificationService;

    @Autowired
    public AiMatchNotifier(
            AdRepository adRepository,
            NotificationService notificationService,
            AdMatchService adMatchService
    ) {
        this.adRepository = adRepository;
        this.adMatchService = adMatchService;
        this.notificationService = notificationService;
        this.legacyPushNotificationService = null;
    }

    /**
     * Kept for existing push-only unit tests. Production wiring uses the
     * database-backed constructor above.
     */
    public AiMatchNotifier(
            AdRepository adRepository,
            PushNotificationService pushNotificationService,
            AdMatchService adMatchService
    ) {
        this.adRepository = adRepository;
        this.adMatchService = adMatchService;
        this.notificationService = null;
        this.legacyPushNotificationService = pushNotificationService;
    }

    /**
     * Eşleşmeleri kaydeder, eşiği geçenler için bildirim gönderir.
     *
     * <p>Kaydetme <b>tüm</b> eşleşmeler için yapılır; bildirim yalnız
     * {@code match = true} olanlar için. Daha düşük skorlu adaylar arayüzde
     * listelenmeli — ama bildirim atmamalı, yoksa kullanıcı yanlış alarma
     * boğulur (sözleşme §7 kural 3).
     *
     * @param matchThreshold sonucu üreten AI'daki o anki eşik. {@code null}
     *                       gelirse kayıt <b>yazılamaz</b>:
     *                       {@code ad_match.threshold_at_time} NOT NULL'dur ve
     *                       kaydı zorlamak dinleyicinin işlemini düşürür — o
     *                       zaman AI sonucu ilana hiç yazılmaz, ilan sonsuza
     *                       kadar PENDING kalır ve mesaj sonsuza kadar yeniden
     *                       teslim edilir. Bu yüzden kayıt atlanır, bildirim
     *                       eskisi gibi gider (yeni bir kesinti yaratılmaz),
     *                       ama durum sessiz kalmaz.
     */
    public void recordAndNotify(Ad ad, List<AiAnalysisResult.Match> matches, Double matchThreshold) {
        if (matches == null || matches.isEmpty()) {
            return;
        }

        boolean kayitYazilabilir = matchThreshold != null;
        if (!kayitYazilabilir) {
            log.warn("AI cevabında match_threshold yok (adId={}): {} eşleşme KAYDEDİLMİYOR ve "
                            + "bildirim tekrarı engellenemiyor. AI sürümü eski olabilir.",
                    ad.getId(), matches.size());
        }

        for (AiAnalysisResult.Match match : matches) {
            if (match.adId() == null) {
                continue;
            }
            Optional<Ad> other = adRepository.findById(match.adId());
            if (other.isEmpty()) {
                continue;
            }
            Ad matched = other.get();

            // Sıra korunuyor: önce yeni ilanın sahibi, sonra eşleşen ilanın sahibi.
            // Her alıcıya KARŞI ilan gösterilir; kendi ilanını haber vermenin anlamı yok.
            List<Alici> alicilar = List.of(
                    new Alici(ad.getUser(), matched),
                    new Alici(matched.getUser(), ad));

            List<Kayit> kayitlar = new ArrayList<>(alicilar.size());
            for (Alici alici : alicilar) {
                kayitlar.add(kayitYazilabilir
                        ? kaydet(alici.kullanici(), ad, matched, match, matchThreshold)
                        : Kayit.YOK);
            }

            if (!match.match()) {
                // Eşiği geçmedi: satırı yazıldı, bildirimi yok (sözleşme §7 kural 3).
                continue;
            }

            bildir(ad, matched, match, alicilar, kayitlar);
        }
    }

    /**
     * Bir alıcı için eşleşme satırını yazar ya da günceller.
     *
     * <p>Satır her zaman <b>analiz edilen ilan → aday</b> yönüyle yazılır; alıcı
     * değişse de {@code sourceAd}/{@code matchedAd} değişmez. Böylece
     * {@code matched_photo_pair} içindeki indekslerin hangi ilana ait olduğu
     * tek anlamlı kalır.
     *
     * <p>{@code blockReason} <b>bilerek boş bırakılıyor.</b> Arayüz onu
     * kullanıcıya kırmızı uyarı kutusunda <i>ham metin olarak</i> gösteriyor
     * ({@code MatchCard.tsx:170}); "skor eşiğin altında kaldı" bir engel değil,
     * zaten {@code passedThreshold=false} + {@code totalScore} +
     * {@code thresholdAtTime} üçlüsüyle anlatılıyor. Oraya makine kodu yazmak
     * her düşük skorlu kartın üstüne gereksiz bir alarm koyardı.
     */
    private Kayit kaydet(User kullanici, Ad ad, Ad matched,
                         AiAnalysisResult.Match match, Double matchThreshold) {
        if (kullanici == null || kullanici.getUid() == null) {
            // ad_match.user_id NOT NULL — sahibi olmayan taraf için satır açılamaz.
            return Kayit.YOK;
        }

        AdMatchSaveRequest istek = AdMatchSaveRequest.builder()
                .userId(kullanici.getUid())
                .sourceAdId(ad.getId())
                .matchedAdId(matched.getId())
                .totalScore(match.score())
                .visualScore(match.visual())
                .tagScore(match.label())
                .locationScore(match.location())
                .thresholdAtTime(matchThreshold)
                .matchedPhotoPair(fotografCifti(match))
                .passedThreshold(match.match())
                .build();

        AdMatchResponseDTO kayit = adMatchService.saveOrUpdateMatch(istek);
        return new Kayit(kayit.getId(), kayit.getNotificationSentAt() != null);
    }

    /**
     * Eşiği geçen bir eşleşme için, <b>daha önce bildirim gitmemiş</b> alıcılara
     * gönderir ve gidenleri damgalar.
     */
    private void bildir(Ad ad, Ad matched, AiAnalysisResult.Match match,
                        List<Alici> alicilar, List<Kayit> kayitlar) {
        List<PushResult> sonuclar = new ArrayList<>(alicilar.size());
        int susturulan = 0;

        for (int i = 0; i < alicilar.size(); i++) {
            Alici alici = alicilar.get(i);
            Kayit kayit = kayitlar.get(i);

            if (kayit.bildirimGonderilmis()) {
                susturulan++;
                continue;
            }

            PushResult sonuc = gonder(
                    alici.kullanici(),
                    alici.karsiIlan(),
                    match.score(),
                    kayit.yazildi() ? kayit.id() : null
            );
            sonuclar.add(sonuc);

            // ÖNCE gönder, SONRA damgala — ve yalnız gerçekten gideni damgala.
            if (sonuc.gonderildi() && kayit.yazildi()) {
                adMatchService.markNotificationAsSent(kayit.id());
            }
        }

        if (!sonuclar.isEmpty()) {
            gunlugeYaz(ad, matched, match.score(), sonuclar);
        }
        if (susturulan > 0) {
            // Tekrar teslimin sessizce hiçbir şey yapmaması ile hiç çalışmaması
            // dışarıdan aynı görünür; ayırt edilebilsin diye yazılıyor.
            log.info("Bildirim daha önce gönderilmişti, tekrarlanmadı ({} alıcı): "
                            + "adId={} <-> adId={}",
                    susturulan, ad.getId(), matched.getId());
        }
    }

    /** {@code photo_a} analiz edilen ilanın, {@code photo_b} adayın fotoğraf indeksi. */
    private String fotografCifti(AiAnalysisResult.Match match) {
        if (match.photoA() == null || match.photoB() == null) {
            return null;
        }
        return "{\"source\":" + match.photoA() + ",\"matched\":" + match.photoB() + "}";
    }

    private PushResult gonder(User recipient, Ad otherAd, double score, Long matchId) {
        if (recipient == null) {
            return PushResult.NO_RECIPIENT;
        }
        Map<String, String> data = Map.of(
                "type", "AI_MATCH",
                "adId", String.valueOf(otherAd.getId()),
                "score", String.valueOf(score)
        );
        String body = "İlanınıza benzeyen bir ilan var: " + otherAd.getTitle()
                + ". Siz de bakar mısınız?";
        String dedupeKey = matchId == null ? null : "AI_MATCH:" + matchId;

        if (notificationService != null) {
            return notificationService.createAndSend(
                    recipient,
                    BILDIRIM_BASLIGI,
                    body,
                    "AI_MATCH",
                    data,
                    dedupeKey
            );
        }

        return legacyPushNotificationService.send(
                recipient.getFcmToken(),
                BILDIRIM_BASLIGI,
                body,
                data
        );
    }

    /**
     * Çift başına <b>en fazla iki</b> satır yazar: gerçekten giden varsa INFO,
     * eksik kalan varsa sebepleriyle WARN.
     *
     * <p>Hiçbir şey gitmediyse INFO <b>hiç yazılmaz</b>; kusurun kendisi buydu.
     */
    private void gunlugeYaz(Ad ad, Ad matched, double score, List<PushResult> sonuclar) {
        long gonderilen = sonuclar.stream().filter(PushResult::gonderildi).count();

        if (gonderilen > 0) {
            log.info("Olası eşleşme bildirimi gönderildi ({}/{} alıcı): adId={} <-> adId={} skor={}",
                    gonderilen, sonuclar.size(), ad.getId(), matched.getId(), score);
        }
        if (gonderilen < sonuclar.size()) {
            log.warn("Bildirim GÖNDERİLMEDİ ({} alıcı): adId={} <-> adId={} — {}",
                    sonuclar.size() - gonderilen, ad.getId(), matched.getId(),
                    sebepler(sonuclar));
        }
    }

    /** Gönderilemeyenlerin sebepleri, tekrarsız ve ilk görülme sırasında. */
    private String sebepler(List<PushResult> sonuclar) {
        Set<String> benzersiz = sonuclar.stream()
                .filter(sonuc -> !sonuc.gonderildi())
                .map(PushResult::aciklama)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return String.join("; ", benzersiz);
    }

    /** Bir bildirim alıcısı ve ona gösterilecek karşı ilan. */
    private record Alici(User kullanici, Ad karsiIlan) {
    }

    /**
     * Yazılan satırın bildirim kararı için gereken iki bilgisi.
     *
     * <p>{@link #YOK}, satırın <b>hiç yazılamadığı</b> durumu taşır (eşik telde
     * gelmedi ya da alıcının kaydı yok). O durumda tekrar koruması işlemez;
     * bildirim eski davranışla gider.
     */
    private record Kayit(Long id, boolean bildirimGonderilmis) {

        static final Kayit YOK = new Kayit(null, false);

        boolean yazildi() {
            return id != null;
        }
    }
}
