package com.works.patimati.ai;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.works.patimati.ai.dto.AiAnalysisResult;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.User;
import com.works.patimati.repository.AdRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
 */
@Service
@RequiredArgsConstructor
public class AiMatchNotifier {

    private static final Logger log = LoggerFactory.getLogger(AiMatchNotifier.class);

    private final AdRepository adRepository;

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
            send(ad.getUser(), matched, match.score());
            send(matched.getUser(), ad, match.score());

            log.info("Olası eşleşme bildirimi: adId={} <-> adId={} skor={}",
                    ad.getId(), matched.getId(), match.score());
        }
    }

    private void send(User recipient, Ad otherAd, double score) {
        if (recipient == null
                || recipient.getFcmToken() == null
                || recipient.getFcmToken().isEmpty()) {
            return;
        }
        try {
            Message message = Message.builder()
                    .setToken(recipient.getFcmToken())
                    .setNotification(Notification.builder()
                            .setTitle("Olası eşleşme bulundu")
                            .setBody("İlanınıza benzeyen bir ilan var: "
                                    + otherAd.getTitle() + ". Siz de bakar mısınız?")
                            .build())
                    .putData("type", "AI_MATCH")
                    .putData("adId", String.valueOf(otherAd.getId()))
                    .putData("score", String.valueOf(score))
                    .build();
            FirebaseMessaging.getInstance().send(message);
        } catch (Exception e) {
            // Bildirim gönderilememesi analizi geçersiz kılmaz: eşleşme
            // veritabanına zaten yazıldı, kullanıcı uygulamadan görebilir.
            log.warn("Eşleşme bildirimi gönderilemedi: {}", e.getMessage());
        }
    }
}
