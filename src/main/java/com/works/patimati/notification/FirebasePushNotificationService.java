package com.works.patimati.notification;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * {@link PushNotificationService}'in Firebase Cloud Messaging uygulaması.
 *
 * <p><b>Firebase yoksa uygulama yine açılır.</b> Kimlik dosyası
 * ({@code firebase-adminsdk.json}) depoda değildir ve geliştirici
 * makinelerinde çoğu zaman bulunmaz. Bu yüzden hazır olup olmadığı
 * <b>kurucuda değil, her çağrıda</b> denetlenir; bean'in oluşması hiçbir
 * koşulda açılışı düşürmez.
 *
 * <p><b>Denetim sırası bilinçlidir: önce Firebase, sonra jeton.</b>
 * Firebase başlatılmamışsa durum <i>sistem çapındadır</i> — hiçbir alıcıya
 * gönderilemez. Jeton eksikliği ise tek kullanıcıya aittir. Ters sırada
 * denetlense, "push tamamen kapalı" gerçeği kullanıcı başına yazılan
 * "jetonu yok" satırlarının altında gizlenirdi. 2026-08-17'de ölçüldü:
 * o gün <b>her</b> alıcı iki koşulu da sağlıyordu (jeton 0/4 ve Firebase
 * kapalı), yani sıranın gözlemlenebilir bir sonucu var.
 *
 * <p><b>Sınanabilirlik:</b> Firebase'e fiilen dokunan iki işlem
 * ({@link #firebaseHazirMi()} ve {@link #firebaseGonder(Message)}) ayrı ve
 * {@code protected} bırakılmıştır. Test bir alt sınıfla ikisini de ezip
 * dört sonucu da ortam durumundan bağımsız üretebilir. Sınanmayan tek şey
 * bu iki metodun <b>kendi gövdesindeki</b> tek satırdır.
 */
@Service
public class FirebasePushNotificationService implements PushNotificationService {

    private static final Logger log = LoggerFactory.getLogger(FirebasePushNotificationService.class);

    @Override
    public PushResult send(String token, String baslik, String govde, Map<String, String> veri, String collapseKey) {
        if (!firebaseHazirMi()) {
            return PushResult.PUSH_DISABLED;
        }
        // isBlank, isEmpty DEĞİL: yalnızca boşluktan oluşan bir jeton da
        // kullanılamaz ve Firebase'e gönderilmesi anlamsız bir hataya döner.
        if (token == null || token.isBlank()) {
            return PushResult.NO_TOKEN;
        }

        try {
            Message.Builder builder = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(baslik)
                            .setBody(govde)
                            .build());
            if (veri != null) {
                veri.forEach(builder::putData);
            }
            if (collapseKey != null && !collapseKey.isBlank()) {
                builder.setAndroidConfig(AndroidConfig.builder()
                        .setCollapseKey(collapseKey)
                        .build());
            }
            firebaseGonder(builder.build());
            return PushResult.SENT;
        } catch (Exception e) {
            // Teknik ayrıntı taşıma katmanında kalır; çağıran yalnızca
            // "gitmedi, sebebi Firebase hatası" bilgisini alır ve teslimat
            // hakkındaki günlüğü ona göre yazar.
            log.warn("Firebase gönderimi hata verdi: {}", e.getMessage());
            return PushResult.FAILED;
        }
    }

    /**
     * Firebase başlatılmış mı?
     *
     * <p>{@code FirebaseMessaging.getInstance()} varsayılan uygulama yokken
     * {@code IllegalStateException} atar. Bunu deneyip yakalamak yerine
     * <b>önceden</b> soruyoruz: istisnayı yakalayıp yutmak, tam da bu paketin
     * kapattığı "sessiz başarısızlık" kalıbıdır.
     */
    protected boolean firebaseHazirMi() {
        return !FirebaseApp.getApps().isEmpty();
    }

    /**
     * Mesajı Firebase'e teslim eder.
     *
     * <p>{@code throws Exception} bilinçli: Firebase'in kendi kontrollü
     * istisnası ({@code FirebaseMessagingException}) dışarıdan
     * oluşturulamadığı için test alt sınıfının başarısızlığı temsil
     * edebilmesi gerekiyor.
     */
    protected void firebaseGonder(Message message) throws Exception {
        FirebaseMessaging.getInstance().send(message);
    }
}
