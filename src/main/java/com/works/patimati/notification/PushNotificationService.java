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
     * Tek bir cihaza bildirim gönderir. {@link #send(String, String, String, Map, String)}'e
     * {@code collapseKey=null} ile yetkilendirir -- çakışma anahtarına ihtiyaç
     * duymayan çağıranlar için kısayol.
     *
     * @param token  alıcının FCM jetonu; {@code null} ya da boşluk olabilir
     * @param baslik bildirim başlığı
     * @param govde  bildirim gövdesi
     * @param veri   uygulamaya iletilecek ek alanlar; {@code null} olabilir
     * @return gerçekte ne olduğu — çağıran günlüğü <b>buna göre</b> yazmalıdır
     */
    default PushResult send(String token, String baslik, String govde, Map<String, String> veri) {
        return send(token, baslik, govde, veri, null);
    }

    /**
     * Tek bir cihaza bildirim gönderir.
     *
     * <p><b>Hiçbir zaman istisna atmaz.</b> Bildirim gönderilememesi çağıranın
     * asıl işini (ilan kaydı, analiz sonucunun yazılması) düşürmemelidir;
     * bu yüzden sonuç istisnayla değil {@link PushResult} ile bildirilir.
     *
     * @param token      alıcının FCM jetonu; {@code null} ya da boşluk olabilir
     * @param baslik     bildirim başlığı
     * @param govde      bildirim gövdesi
     * @param veri       uygulamaya iletilecek ek alanlar; {@code null} olabilir
     * @param collapseKey Android taraflı çakışma anahtarı (ör. "potential-match-42");
     *                    {@code null} ya da boşluksa uygulanmaz. Aynı bildirimin
     *                    (nadir de olsa) iki kez tetiklenmesi durumunda cihaz/OS
     *                    seviyesinde tek bildirime düşürülmesini sağlar -- çağıran
     *                    tarafın DB seviyesindeki dedup'ının (varsa) ek bir savunma
     *                    katmanıdır, onun yerine geçmez.
     * @return gerçekte ne olduğu — çağıran günlüğü <b>buna göre</b> yazmalıdır
     */
    PushResult send(String token, String baslik, String govde, Map<String, String> veri, String collapseKey);
}
