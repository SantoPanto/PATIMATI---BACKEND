package com.works.patimati.notification;

import com.google.firebase.messaging.Message;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link FirebasePushNotificationService} <b>ne olduğunu doğru söylemeli.</b>
 *
 * <p><b>Neden bu test var:</b> gönderim daha önce {@code void} idi ve iki
 * ayrı yoldan sessizce başarısız oluyordu — jeton yoksa hiç denemeden dönüyor,
 * Firebase başlatılmamışsa atılan istisna yutuluyordu. Çağıran ikisini de
 * göremediği için günlüğe koşulsuz "bildirim gönderildi" yazıyordu.
 *
 * <p><b>Neden AYRI bir test:</b> jeton denetimi bu paketin içine taşındı.
 * {@code AiMatchNotifier} testi bu servisi <b>sahtesiyle</b> değiştirdiği için
 * buradaki dalların hiçbirini çalıştırmaz; o testle ölçülmeye kalkılsaydı
 * jeton denetimini bozan bir değişiklik <b>sessizce geçerdi</b>. (Aynı sınıf
 * hata 2026-08-17'de {@code AiAdayAskiyaAlmaTest}'in M4'ünde ölçülerek
 * yakalanmıştı: iddia doğru çıkıyordu ama yanlış sebeple.)
 *
 * <p><b>Firebase gerekmez.</b> Firebase'e fiilen dokunan iki metot
 * {@code protected} olduğu için alt sınıfla eziliyor; böylece dört sonucun
 * dördü de ortam durumundan bağımsız üretilebiliyor.
 */
class PushGonderimSonucuTest {

    private static final String GECERLI_JETON = "cihaz-jetonu-123";
    private static final Map<String, String> VERI = Map.of("type", "AI_MATCH");

    // ---------------------------------------------------------------------
    // Firebase yokluğu — SİSTEM ÇAPINDA durum
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Firebase başlatılmamışsa PUSH_DISABLED döner ve Firebase'e HİÇ gidilmez")
    void firebaseYoksaPushDisabled() {
        DenetlenebilirServis servis = DenetlenebilirServis.firebaseKapali();

        PushResult sonuc = servis.send(GECERLI_JETON, "başlık", "gövde", VERI);

        assertThat(sonuc)
                .withFailMessage("""
                        Firebase başlatılmamışken sonuç %s çıktı.
                        Beklenen PUSH_DISABLED — aksi hâlde çağıran hiç gitmemiş
                        bir bildirimi "gönderildi" diye günlüğe yazar.""", sonuc)
                .isEqualTo(PushResult.PUSH_DISABLED);

        assertThat(servis.gonderilenMesajlar)
                .withFailMessage("Firebase hazır değilken gönderim denendi — istisna yutma kalıbı geri gelmiş.")
                .isEmpty();
    }

    @Test
    @DisplayName("ÖNCELİK: jeton da yok Firebase de yoksa, sonuç Firebase'i söylemeli")
    void firebaseOnceliklidir() {
        DenetlenebilirServis servis = DenetlenebilirServis.firebaseKapali();

        PushResult sonuc = servis.send(null, "başlık", "gövde", VERI);

        assertThat(sonuc)
                .withFailMessage("""
                        Jeton yok VE Firebase kapalıyken sonuç %s çıktı.

                        Denetim sırası bilinçlidir: Firebase'in kapalı olması
                        SİSTEM ÇAPINDA bir durumdur (kimseye gidemez), jeton
                        eksikliği ise tek kullanıcıya aittir. Jetona önce
                        bakılırsa "push tamamen kapalı" gerçeği, kullanıcı
                        başına yazılan "jetonu yok" satırlarının altında gizlenir.

                        2026-08-17'de ölçüldü: o gün HER alıcı iki koşulu da
                        sağlıyordu (jeton 0/4, Firebase kapalı).""", sonuc)
                .isEqualTo(PushResult.PUSH_DISABLED);
    }

    // ---------------------------------------------------------------------
    // Jeton denetimi — ALICIYA ÖZEL durum
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Jeton null ise NO_TOKEN döner ve Firebase'e gidilmez")
    void jetonNullIseNoToken() {
        jetonKullanilamaz(null, "null");
    }

    @Test
    @DisplayName("Jeton boş dize ise NO_TOKEN döner")
    void jetonBosIseNoToken() {
        jetonKullanilamaz("", "boş dize");
    }

    @Test
    @DisplayName("Jeton yalnızca boşluksa NO_TOKEN döner — isEmpty yetmez, isBlank gerekir")
    void jetonSadeceBosluksaNoToken() {
        jetonKullanilamaz("   ", "yalnızca boşluk");
    }

    private void jetonKullanilamaz(String jeton, String nasil) {
        DenetlenebilirServis servis = DenetlenebilirServis.firebaseAcik();

        PushResult sonuc = servis.send(jeton, "başlık", "gövde", VERI);

        assertThat(sonuc)
                .withFailMessage("""
                        Jeton %s olmasına rağmen sonuç %s çıktı; NO_TOKEN bekleniyordu.

                        "   " gibi bir jeton .isEmpty() denetiminden GEÇER —
                        bu yüzden denetim .isBlank() olmak zorunda. Aksi hâlde
                        kullanılamaz jeton Firebase'e gider ve hata olarak döner.""",
                        nasil, sonuc)
                .isEqualTo(PushResult.NO_TOKEN);

        assertThat(servis.gonderilenMesajlar)
                .withFailMessage("Kullanılamaz jetonla (%s) Firebase'e gönderim denendi.", nasil)
                .isEmpty();
    }

    // ---------------------------------------------------------------------
    // Gerçek gönderim ve hata
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Firebase hazır ve jeton geçerliyse SENT döner ve tam bir mesaj gönderilir")
    void basariliGonderimSentDoner() {
        DenetlenebilirServis servis = DenetlenebilirServis.firebaseAcik();

        PushResult sonuc = servis.send(GECERLI_JETON, "başlık", "gövde", VERI);

        assertThat(sonuc).isEqualTo(PushResult.SENT);
        assertThat(servis.gonderilenMesajlar)
                .withFailMessage("SENT dönüldü ama Firebase'e hiçbir mesaj gitmedi.")
                .hasSize(1);
    }

    @Test
    @DisplayName("Gönderim hata verirse FAILED döner — istisna yutulup SENT denmez")
    void gonderimHatasiFailedDoner() {
        DenetlenebilirServis servis = DenetlenebilirServis.firebaseHataVeriyor();

        PushResult sonuc = servis.send(GECERLI_JETON, "başlık", "gövde", VERI);

        assertThat(sonuc)
                .withFailMessage("""
                        Firebase gönderimi istisna attı ama sonuç %s çıktı.
                        FAILED bekleniyordu: istisnayı yakalayıp "gönderildi"
                        demek, bu paketin kapattığı yalanın ta kendisidir.""", sonuc)
                .isEqualTo(PushResult.FAILED);
    }

    @Test
    @DisplayName("veri null olsa da gönderim yapılır — çağıran ek alan vermek zorunda değil")
    void veriNullOlabilir() {
        DenetlenebilirServis servis = DenetlenebilirServis.firebaseAcik();

        assertThat(servis.send(GECERLI_JETON, "başlık", "gövde", null))
                .isEqualTo(PushResult.SENT);
    }

    // ---------------------------------------------------------------------
    // Düzenek
    // ---------------------------------------------------------------------

    /**
     * Firebase'e dokunan iki metodu ezer.
     *
     * <p>Sınanmayan tek şey, üretimdeki bu iki metodun <b>kendi gövdesindeki</b>
     * tek satır kalır ({@code FirebaseApp.getApps()} ve
     * {@code FirebaseMessaging.getInstance().send()}). Diğer bütün dallar
     * buradan geçiyor.
     */
    private static final class DenetlenebilirServis extends FirebasePushNotificationService {

        private final boolean hazir;
        private final RuntimeException firlatilacak;
        private final List<Message> gonderilenMesajlar = new ArrayList<>();

        private DenetlenebilirServis(boolean hazir, RuntimeException firlatilacak) {
            this.hazir = hazir;
            this.firlatilacak = firlatilacak;
        }

        static DenetlenebilirServis firebaseAcik() {
            return new DenetlenebilirServis(true, null);
        }

        static DenetlenebilirServis firebaseKapali() {
            return new DenetlenebilirServis(false, null);
        }

        static DenetlenebilirServis firebaseHataVeriyor() {
            return new DenetlenebilirServis(true, new IllegalStateException("gönderim başarısız"));
        }

        @Override
        protected boolean firebaseHazirMi() {
            return hazir;
        }

        @Override
        protected void firebaseGonder(Message message) {
            if (firlatilacak != null) {
                throw firlatilacak;
            }
            gonderilenMesajlar.add(message);
        }
    }
}
