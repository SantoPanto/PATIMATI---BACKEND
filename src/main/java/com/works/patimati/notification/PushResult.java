package com.works.patimati.notification;

/**
 * Bir push bildiriminin <b>gerçekte ne olduğu</b>.
 *
 * <p><b>Neden var:</b> gönderim metodu daha önce {@code void} idi ve jeton
 * yokken sessizce dönüyordu. Çağıran ne olduğunu bilemediği için günlüğe
 * koşulsuz "bildirim gönderildi" yazıyordu — hiçbir şey gönderilmemiş olsa
 * bile. Sonucu döndürmek, o yalanı yapısal olarak imkânsız kılar.
 *
 * <p>Açıklamalar <b>sonuç odaklıdır</b>: günlüğü okuyanın öğrenmesi gereken
 * ilk şey bildirimin gitmediğidir, sebebi ikinci sıradadır.
 */
public enum PushResult {

    /** Firebase'e teslim edildi. */
    SENT("gönderildi"),

    /**
     * Firebase hiç başlatılmamış ⇒ <b>kimseye</b> gönderilemez.
     * Sistem çapında bir durumdur, bu yüzden alıcıya özel durumlardan
     * <b>önce</b> denetlenir (bkz. {@link FirebasePushNotificationService}).
     */
    PUSH_DISABLED("Firebase başlatılmadı (kimlik dosyası yok)"),

    /** İlanın kayıtlı bir sahibi yok — veri anomalisi, jeton eksikliği değil. */
    NO_RECIPIENT("ilanın kayıtlı sahibi yok"),

    /** Alıcının FCM jetonu yok ya da yalnızca boşluktan oluşuyor. */
    NO_TOKEN("kullanıcının FCM jetonu yok"),

    /** Firebase çağrısı hata verdi; teknik ayrıntı gönderen katmanda günlüklenir. */
    FAILED("Firebase hata verdi");

    private final String aciklama;

    PushResult(String aciklama) {
        this.aciklama = aciklama;
    }

    /** Günlüğe yazılacak, sonuç odaklı kısa sebep. */
    public String aciklama() {
        return aciklama;
    }

    public boolean gonderildi() {
        return this == SENT;
    }
}
