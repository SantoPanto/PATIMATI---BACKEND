package com.works.patimati.ai;

import com.works.patimati.entity.Ad;
import com.works.patimati.entity.PotentialMatch;
import com.works.patimati.entity.PotentialMatchRecipient;
import com.works.patimati.entity.User;
import com.works.patimati.notification.PushNotificationService;
import com.works.patimati.notification.PushResult;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Tek bir alıcıya tek bir FCM bildirimi gönderir.
 *
 * <p>Faz 2 revize blueprint §3/§4 ile bu sınıfın rolü değişti: artık ham AI
 * sonucunu doğrudan işlemez — {@code AiAnalysisListener}, hangi eşleşmelerin
 * gerçekten YENİ olduğuna (dedup) {@code PotentialMatchService} üzerinden
 * karar verir, bu sınıf yalnızca "şu alıcıya şu eşleşmeyi bildir" der.
 *
 * <p><b>Dil önemli:</b> "eşleşti" değil <b>"olası eşleşme"</b> diyoruz — bu
 * bir üslup tercihi değil, ölçüm sonucu: gerçek fotoğraflarda "aynı hayvan"
 * ve "farklı hayvan" skorları çakışıyor, hiçbir eşik ikisini temiz ayırmıyor
 * (ölçüm raporu §4). Son karar kullanıcınındır.
 *
 * <p><b>Tekrar teslim / yeniden analiz sonrası aynı çifte bildirim ikinci
 * kez gitmesin diye</b> (eskiden bu sınıfın açık bıraktığı bir sorundu):
 * artık {@code PotentialMatchService} DB seviyesinde dedup'lıyor ve yalnızca
 * gerçekten YENİ bir {@link PotentialMatchRecipient} satırı için
 * {@link #sendOne} çağrılıyor — bu sınıf hâlâ hiçbir şey saklamıyor, ama
 * artık saklamak zorunda da değil.
 *
 * <p><b>Gönderim {@link PushNotificationService} üzerinden yapılır,
 * Firebase'e doğrudan DOKUNULMAZ.</b> main'deki e203b28 ("B5") bu sınıfı
 * zaten bu servise taşımıştı (koşulsuz başarı logu kaldırıldı, jeton
 * denetimi {@code .isBlank()}'e düzeltildi, Firebase-kapalı/jeton-yok
 * ayrımı {@link PushResult} ile netleşti) — ama Faz 2'nin
 * {@link PotentialMatchRecipient} tabanlı yeniden yazımıyla çakışan bir
 * merge conflict'te bu geçiş kayboldu ve sınıf sessizce eski
 * doğrudan-Firebase haline geri döndü (jeton denetimi {@code .isBlank()}
 * hariç — o bir şekilde korundu). Bu, o kaybı geri kazanan düzeltmedir.
 */
@Service
@RequiredArgsConstructor
public class AiMatchNotifier {

    private static final Logger log = LoggerFactory.getLogger(AiMatchNotifier.class);

    private final PushNotificationService pushNotificationService;

    /**
     * {@link PushNotificationService#send} ile gönderir. {@code collapseKey},
     * alıcı satırının kendi kimliğine bağlanır: aynı bildirim (nadir de olsa)
     * iki kez tetiklenirse cihaz/OS seviyesinde tek bildirime düşürülür —
     * "idempotent-as-practical" teslimatın (blueprint §4/§13) mevcut FCM
     * yeteneğiyle sağlandığı yer burasıdır. Bu, {@code PotentialMatchService}'in
     * DB seviyesindeki dedup'ının YERİNE geçmez — o birincil savunma
     * (aynı satır için {@link #sendOne} zaten iki kez çağrılmaz), bu ikincil
     * (bir çağrı, yanıtı kaybolmuş bir gönderimden sonra süpürücü tarafından
     * tekrar denenirse cihazda yine de tek bildirime düşer).
     *
     * @return true ise gönderim BAŞARILI (ya da gönderilecek alıcı/token
     *         yoktu — bu durumda tekrar denemenin bir anlamı kalmaz); false
     *         ise BAŞARISIZ (Firebase kapalı ya da gönderim hata verdi),
     *         çağıran taraf {@code sendAttempts} artırıp PENDING'de bırakmalı.
     */
    public boolean sendOne(PotentialMatchRecipient recipient) {
        User user = recipient.getRecipient();
        if (user == null) {
            // Veri anomalisi (ilanın kayıtlı sahibi yok), jeton eksikliği
            // değil -- ama sonsuz yeniden denemenin bir anlamı yine kalmaz.
            log.warn("Eşleşme bildirimi atlandı (recipientId={}): ilgili kullanıcı yok", recipient.getId());
            return true;
        }

        PushResult sonuc = pushNotificationService.send(
                user.getFcmToken(),
                "Olası eşleşme bulundu",
                buildBody(recipient.getPotentialMatch(), recipient.getRole()),
                Map.of(
                        "type", "POTENTIAL_MATCH",
                        "potentialMatchId", String.valueOf(recipient.getPotentialMatch().getId()),
                        "recipientId", String.valueOf(recipient.getId())
                ),
                "potential-match-" + recipient.getId()
        );

        return switch (sonuc) {
            case SENT -> true;
            // Gönderilecek bir cihaz yok -- "başarısızlık" değil, "yapacak
            // bir şey yok" durumu; sonsuz yeniden denemeyi önlemek için
            // başarılı sayılır. Günlükte yalnızca bunun DIŞINDAKİ (gerçek
            // arıza) durumlar için satır yazılır -- gönderim yoksa INFO/WARN
            // de yok, tıpkı main'deki B5 düzeltmesinin ilkesi gibi.
            case NO_TOKEN, NO_RECIPIENT -> true;
            case PUSH_DISABLED, FAILED -> {
                log.warn("Eşleşme bildirimi gönderilemedi (recipientId={}): {}",
                        recipient.getId(), sonuc.aciklama());
                yield false;
            }
        };
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
