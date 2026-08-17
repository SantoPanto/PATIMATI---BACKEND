package com.works.patimati.ai;

import com.works.patimati.ai.dto.AiAnalysisResult;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.User;
import com.works.patimati.notification.PushNotificationService;
import com.works.patimati.notification.PushResult;
import com.works.patimati.repository.AdRepository;
import lombok.RequiredArgsConstructor;
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
 * Olası eşleşme bulunduğunda ilgili kullanıcıları haberdar eder.
 *
 * <p><b>Bildirim her iki ilanın sahibine de gider</b> (ekip kararı, 29.07.2026):
 * kaybettiği hayvanı arayan da, bulduğu hayvanı bildiren de haber almak ister.
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
 * <p><b>Açık madde (B6):</b> bu sınıf <b>hiçbir şey saklamıyor.</b> Eşleşme
 * sonucu veritabanına yazılmıyor ve "bu ilan çiftine bildirim gitti" kaydı
 * yok. İki sonucu var: bildirim gidemezse bilgi tamamen kayboluyor, ve aynı
 * sonuç kuyruktan tekrar teslim edilirse (RabbitMQ "en az bir kez" teslim
 * eder, ayrıca model yükseltmesi sonrası toplu yeniden analiz) aynı çifte
 * bildirim <b>yeniden</b> gider. Sözleşme §7 kural 4 bunu yasaklıyor;
 * karşılığı Java tarafında bir bildirim kaydıdır (§11 notu).
 */
@Service
@RequiredArgsConstructor
public class AiMatchNotifier {

    private static final Logger log = LoggerFactory.getLogger(AiMatchNotifier.class);

    private static final String BILDIRIM_BASLIGI = "Olası eşleşme bulundu";

    private final AdRepository adRepository;
    private final PushNotificationService pushNotificationService;

    /**
     * Eşiği geçen eşleşmeler için bildirim gönderir.
     *
     * <p>Yalnızca {@code match = true} olanlar bildirim tetikler. Daha düşük
     * skorlu adaylar da arayüzde listelenmeli — ama bildirim atmamalı, yoksa
     * kullanıcı yanlış alarma boğulur (sözleşme §7 kural 3).
     */
    public void notifyMatches(Ad ad, List<AiAnalysisResult.Match> matches) {
        List<AiAnalysisResult.Match> strong = matches.stream()
                .filter(AiAnalysisResult.Match::match)
                .toList();

        if (strong.isEmpty()) {
            return;
        }

        for (AiAnalysisResult.Match match : strong) {
            Optional<Ad> other = adRepository.findById(match.adId());
            if (other.isEmpty()) {
                continue;
            }
            Ad matched = other.get();

            // Her iki tarafa da: yeni ilanın sahibi ve eşleşen ilanın sahibi.
            List<PushResult> sonuclar = new ArrayList<>(2);
            sonuclar.add(gonder(ad.getUser(), matched, match.score()));
            sonuclar.add(gonder(matched.getUser(), ad, match.score()));

            gunlugeYaz(ad, matched, match.score(), sonuclar);
        }
    }

    private PushResult gonder(User recipient, Ad otherAd, double score) {
        if (recipient == null) {
            return PushResult.NO_RECIPIENT;
        }
        return pushNotificationService.send(
                recipient.getFcmToken(),
                BILDIRIM_BASLIGI,
                "İlanınıza benzeyen bir ilan var: " + otherAd.getTitle()
                        + ". Siz de bakar mısınız?",
                Map.of("type", "AI_MATCH",
                        "adId", String.valueOf(otherAd.getId()),
                        "score", String.valueOf(score)));
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
}
