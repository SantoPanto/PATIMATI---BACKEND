package com.works.patimati.ai;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.PotentialMatch;
import com.works.patimati.entity.PotentialMatchRecipient;
import com.works.patimati.entity.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Tek bir alıcıya tek bir FCM bildirimi gönderir.
 *
 * <p>Faz 2 revize blueprint §3/§4 ile bu sınıfın rolü değişti: artık ham AI
 * sonucunu doğrudan işlemez — {@code AiAnalysisListener}, hangi eşleşmelerin
 * gerçekten YENİ olduğuna (dedup) {@code PotentialMatchService} üzerinden
 * karar verir, bu sınıf yalnızca "şu alıcıya şu eşleşmeyi bildir" der.
 *
 * <p><b>Dil önemli:</b> "eşleşti" değil <b>"olası eşleşme"</b> diyoruz — bu
 * bir üslup tercihi değil, ölçüm sonucu (AI deposu, ölçüm raporu §4). Son
 * karar kullanıcınındır.
 */
@Service
@RequiredArgsConstructor
public class AiMatchNotifier {

    private static final Logger log = LoggerFactory.getLogger(AiMatchNotifier.class);

    /**
     * FCM'ye gönderir. {@code collapseKey}, alıcı satırının kendi kimliğine
     * bağlanır: aynı bildirim (nadir de olsa) iki kez tetiklenirse cihaz/OS
     * seviyesinde tek bildirime düşürülür — "idempotent-as-practical"
     * teslimatın (blueprint §4/§13) mevcut FCM yeteneğiyle sağlandığı yer
     * burasıdır, yeni bir altyapı eklenmeden.
     *
     * @return true ise gönderim BAŞARILI (ya da gönderilecek token yoktu —
     *         bu durumda tekrar denemenin bir anlamı kalmaz); false ise
     *         BAŞARISIZ, çağıran taraf {@code sendAttempts} artırıp
     *         PENDING'de bırakmalı.
     */
    public boolean sendOne(PotentialMatchRecipient recipient) {
        User user = recipient.getRecipient();
        if (user == null || user.getFcmToken() == null || user.getFcmToken().isBlank()) {
            // Gönderilecek bir cihaz yok — bu bir "başarısızlık" değil,
            // "yapacak bir şey yok" durumudur; sonsuz yeniden denemeyi
            // önlemek için başarılı sayıyoruz.
            return true;
        }

        try {
            Message message = Message.builder()
                    .setToken(user.getFcmToken())
                    .setNotification(Notification.builder()
                            .setTitle("Olası eşleşme bulundu")
                            .setBody(buildBody(recipient.getPotentialMatch(), recipient.getRole()))
                            .build())
                    .putData("type", "POTENTIAL_MATCH")
                    .putData("potentialMatchId", String.valueOf(recipient.getPotentialMatch().getId()))
                    .putData("recipientId", String.valueOf(recipient.getId()))
                    .setAndroidConfig(AndroidConfig.builder()
                            .setCollapseKey("potential-match-" + recipient.getId())
                            .build())
                    .build();
            FirebaseMessaging.getInstance().send(message);
            return true;
        } catch (Exception e) {
            log.warn("Eşleşme bildirimi gönderilemedi (recipientId={}): {}", recipient.getId(), e.getMessage());
            return false;
        }
    }

    /**
     * Hiçbir zaman kesinlik iddia etmez (sözleşme §7 kural 3): "bulundu"
     * değil "benzeyen bir kayıt tespit edildi" dili kullanılır.
     */
    private String buildBody(PotentialMatch match, PotentialMatchRecipient.Role role) {
        if (match.getCandidateKind() == PotentialMatch.CandidateKind.EXTERNAL) {
            return "Evcil hayvanınıza benzeyen bir hayvan tespit ettik. "
                    + "Kaydı inceleyerek aynı hayvan olup olmadığını kontrol edebilirsiniz.";
        }
        Ad other = role == PotentialMatchRecipient.Role.OWNER_B ? match.getAdA() : match.getAdB();
        String title = other != null && other.getTitle() != null ? other.getTitle() : "bir ilan";
        return "İlanınıza benzeyen bir ilan var: " + title + ". Siz de bakar mısınız?";
    }
}
