package com.works.patimati.notification;

import java.util.Map;

/**
 * Mobil cihazlara push bildirimi gönderir.
 *
 * <p>Emsal: {@code storage} paketi ({@code ImageStorageService} arayüz +
 * {@code S3ImageStorageService} uygulama). Arayüzün amacı taşıyıcıyı
 * (Firebase) çağıranlardan ayırmak ve <b>gönderimin gerçekten olup
 * olmadığını sınanabilir kılmaktır.</b>
 */
public interface PushNotificationService {

    /**
     * Tek bir cihaza bildirim gönderir.
     *
     * <p><b>Hiçbir zaman istisna atmaz.</b> Bildirim gönderilememesi çağıranın
     * asıl işini (ilan kaydı, analiz sonucunun yazılması) düşürmemelidir;
     * bu yüzden sonuç istisnayla değil {@link PushResult} ile bildirilir.
     *
     * @param token  alıcının FCM jetonu; {@code null} ya da boşluk olabilir
     * @param baslik bildirim başlığı
     * @param govde  bildirim gövdesi
     * @param veri   uygulamaya iletilecek ek alanlar; {@code null} olabilir
     * @return gerçekte ne olduğu — çağıran günlüğü <b>buna göre</b> yazmalıdır
     */
    PushResult send(String token, String baslik, String govde, Map<String, String> veri);
}
