package com.works.patimati.ai;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.works.patimati.ai.dto.AiAnalysisResult;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.User;
import com.works.patimati.notification.PushNotificationService;
import com.works.patimati.notification.PushResult;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.service.AdMatchService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link AiMatchNotifier} günlüğe <b>gerçekte olanı</b> yazmalı.
 *
 * <p><b>Neden bu test var:</b> başarı satırı koşulsuzdu. Gönderim metodu
 * jeton yokken sessizce dönüyor, Firebase başlatılmamışsa istisna yutuluyordu;
 * yine de her eşleşme için "Olası eşleşme bildirimi" satırı yazılıyordu.
 * 2026-08-17'de ölçüldü: {@code users} tablosunda dolu FCM jetonu <b>0/4</b> ve
 * {@code firebase-adminsdk.json} depoda yok ⇒ o gün <b>hiçbir bildirim
 * gitmiyordu</b>, ama günlük hepsinin gittiğini söylüyordu. Tek gözlem noktası,
 * hakkında bilgi vermesi gereken şey konusunda yanıltıyordu.
 *
 * <p><b>İddia düzyazıya değil YAPIYA yaslanır:</b> Türkçe metin aranmaz;
 * sahte servise yapılan <b>çağrı sayısı</b> ile INFO/WARN <b>olay sayısı</b>
 * ölçülür. Metnin sözcükleri değişebilir, iddia değişmez.
 *
 * <p><b>Kapsam sınırı:</b> jeton denetimi ve Firebase'in kapalı olması artık
 * {@code notification} paketinde karara bağlanıyor ve burada sahtelenmiştir ⇒
 * o dallar bu testle ölçülmez, {@code PushGonderimSonucuTest} ile ölçülür.
 *
 * <p>Veritabanı ve broker gerektirmez.
 */
class BildirimGunluguDogruluguTest {

    private static final long YENI_ILAN_ID = 100L;
    private static final long ESLESEN_ILAN_ID = 200L;

    /**
     * Bu testin fikstüründe kullanıcıların {@code uid}'i yok ⇒ eşleşme satırı
     * yazılmaz ve {@link AdMatchService} hiç çağrılmaz. Bilerek öyle: buranın
     * konusu <b>günlük doğruluğu</b>, kayıt değil. Kayıt ve tekrar koruması
     * {@code EslesmeKaydiVeTekrarKorumasiTest} ile ölçülüyor.
     */
    private static final double ESIK = 0.80;

    private AdRepository adRepository;
    private SahtePushServisi push;
    private AiMatchNotifier notifier;

    private ch.qos.logback.classic.Logger logger;
    private ListAppender<ILoggingEvent> gunlukKaydedici;
    private Level eskiSeviye;

    @BeforeEach
    void hazirla() {
        adRepository = mock(AdRepository.class);
        push = new SahtePushServisi();
        notifier = new AiMatchNotifier(adRepository, push, mock(AdMatchService.class));

        LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
        logger = ctx.getLogger(AiMatchNotifier.class);
        eskiSeviye = logger.getLevel();
        // Seviye açıkça sabitleniyor: kapalı bir INFO seviyesi testi sessizce
        // "hiç satır yazılmadı" diye yeşile boyardı.
        logger.setLevel(Level.INFO);

        gunlukKaydedici = new ListAppender<>();
        gunlukKaydedici.start();
        logger.addAppender(gunlukKaydedici);
    }

    @AfterEach
    void temizle() {
        logger.detachAppender(gunlukKaydedici);
        gunlukKaydedici.stop();
        logger.setLevel(eskiSeviye);
    }

    // ---------------------------------------------------------------------
    // Asıl kusur
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Hiçbir bildirim gitmediyse başarı satırı YAZILMAZ")
    void gonderimYoksaBasariSatiriYok() {
        eslesenIlanlariKur();
        push.sirayaKoy(PushResult.PUSH_DISABLED, PushResult.PUSH_DISABLED);

        notifier.recordAndNotify(yeniIlan(), List.of(gucluEslesme()), ESIK);

        // Ön koşul: gönderim GERÇEKTEN denendi. Denenmediyse aşağıdaki
        // "INFO yok" iddiası doğru çıkar ama yanlış sebeple.
        assertThat(push.cagrilar)
                .withFailMessage("Ön koşul bozuk: hiç gönderim denenmemiş, iddia anlamsız.")
                .hasSize(2);

        assertThat(seviyeSayisi(Level.INFO))
                .withFailMessage("""
                        Hiçbir bildirim gitmediği hâlde %d adet INFO satırı yazıldı.

                        Kusur tam olarak buydu: başarı satırı koşulsuzdu ve
                        gönderim sessizce başarısız olsa bile "bildirim" diyordu.
                        Yazılanlar: %s""", seviyeSayisi(Level.INFO), mesajlar())
                .isZero();

        assertThat(seviyeSayisi(Level.WARN))
                .withFailMessage("""
                        Bildirim gitmedi ama günlük SUSTU. Sessiz başarısızlık,
                        yanlış başarı kadar kötüdür: kimse push'un ölü olduğunu
                        fark edemez. Yazılanlar: %s""", mesajlar())
                .isOne();
    }

    @Test
    @DisplayName("İki alıcıya da gidince tek başarı satırı yazılır, uyarı yazılmaz")
    void ikisineDeGidinceSadeceBasariSatiri() {
        eslesenIlanlariKur();
        push.sirayaKoy(PushResult.SENT, PushResult.SENT);

        notifier.recordAndNotify(yeniIlan(), List.of(gucluEslesme()), ESIK);

        assertThat(seviyeSayisi(Level.INFO))
                .withFailMessage("İki bildirim de gitti ama başarı satırı %d kez yazıldı: %s",
                        seviyeSayisi(Level.INFO), mesajlar())
                .isOne();
        assertThat(seviyeSayisi(Level.WARN))
                .withFailMessage("Her şey yolundayken uyarı yazıldı: %s", mesajlar())
                .isZero();
    }

    @Test
    @DisplayName("Biri gidip biri gitmezse İKİ satır da yazılır — kısmi teslimat gizlenmez")
    void kismiTeslimatIkiSatirYazar() {
        eslesenIlanlariKur();
        push.sirayaKoy(PushResult.SENT, PushResult.NO_TOKEN);

        notifier.recordAndNotify(yeniIlan(), List.of(gucluEslesme()), ESIK);

        assertThat(seviyeSayisi(Level.INFO))
                .withFailMessage("Kısmi teslimatta başarı satırı yazılmadı: %s", mesajlar())
                .isOne();
        assertThat(seviyeSayisi(Level.WARN))
                .withFailMessage("""
                        Bir alıcıya gidemedi ama uyarı yok — kısmi teslimat tam
                        teslimat gibi görünüyor. Yazılanlar: %s""", mesajlar())
                .isOne();
    }

    // ---------------------------------------------------------------------
    // Eşik ve alıcı kuralları
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Eşiği geçmeyen aday bildirim TETİKLEMEZ (sözleşme §7 kural 3)")
    void zayifAdayBildirimTetiklemez() {
        eslesenIlanlariKur();

        notifier.recordAndNotify(yeniIlan(), List.of(zayifEslesme()), ESIK);

        assertThat(push.cagrilar)
                .withFailMessage("""
                        match=false olan aday için bildirim denendi.
                        Sözleşme §7 kural 3: düşük skorlu adaylar listede
                        gösterilmeli ama bildirim ATMAMALIDIR — yoksa kullanıcı
                        yanlış alarma boğulur.""")
                .isEmpty();
        assertThat(gunlukKaydedici.list)
                .withFailMessage("Bildirim gitmedi ama günlüğe satır yazıldı: %s", mesajlar())
                .isEmpty();
    }

    @Test
    @DisplayName("İlanın kayıtlı sahibi yoksa push servisine hiç gidilmez")
    void sahipsizIlanIcinServiseGidilmez() {
        Ad eslesen = ilan(ESLESEN_ILAN_ID, "eşleşen ilan", null);
        when(adRepository.findById(anyLong())).thenReturn(Optional.of(eslesen));
        push.sirayaKoy(PushResult.SENT);

        notifier.recordAndNotify(yeniIlan(), List.of(gucluEslesme()), ESIK);

        assertThat(push.cagrilar)
                .withFailMessage("""
                        Eşleşen ilanın sahibi yokken push servisine %d çağrı gitti;
                        yalnız 1 (yeni ilanın sahibi) bekleniyordu.""", push.cagrilar.size())
                .hasSize(1);
    }

    // ---------------------------------------------------------------------
    // Bildirimin içeriği
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Bildirim karşı ilanın başlığını ve kimliğini taşır, her iki tarafa da gider")
    void bildirimIcerigiKarsiIlaniGosterir() {
        eslesenIlanlariKur();
        push.sirayaKoy(PushResult.SENT, PushResult.SENT);

        notifier.recordAndNotify(yeniIlan(), List.of(gucluEslesme()), ESIK);

        assertThat(push.cagrilar).hasSize(2);

        Cagri yeniIlaninSahibine = push.cagrilar.get(0);
        assertThat(yeniIlaninSahibine.token()).isEqualTo("jeton-yeni");
        assertThat(yeniIlaninSahibine.govde()).contains("eşleşen ilan");
        assertThat(yeniIlaninSahibine.veri())
                .containsEntry("type", "AI_MATCH")
                .containsEntry("adId", String.valueOf(ESLESEN_ILAN_ID));

        Cagri eslesenIlaninSahibine = push.cagrilar.get(1);
        assertThat(eslesenIlaninSahibine.token()).isEqualTo("jeton-eslesen");
        assertThat(eslesenIlaninSahibine.govde()).contains("yeni ilan");
        assertThat(eslesenIlaninSahibine.veri())
                .containsEntry("adId", String.valueOf(YENI_ILAN_ID));
    }

    // ---------------------------------------------------------------------
    // Fikstür ve yardımcılar
    // ---------------------------------------------------------------------

    private void eslesenIlanlariKur() {
        Ad eslesen = ilan(ESLESEN_ILAN_ID, "eşleşen ilan", kullanici("jeton-eslesen"));
        when(adRepository.findById(anyLong())).thenReturn(Optional.of(eslesen));
    }

    private Ad yeniIlan() {
        return ilan(YENI_ILAN_ID, "yeni ilan", kullanici("jeton-yeni"));
    }

    private Ad ilan(Long id, String baslik, User sahip) {
        return Ad.builder().id(id).title(baslik).user(sahip).build();
    }

    private User kullanici(String jeton) {
        return User.builder().fcmToken(jeton).build();
    }

    private AiAnalysisResult.Match gucluEslesme() {
        return new AiAnalysisResult.Match(ESLESEN_ILAN_ID, 0.91, 0.9, 0.8, 1.0, true, 0, 0);
    }

    private AiAnalysisResult.Match zayifEslesme() {
        return new AiAnalysisResult.Match(ESLESEN_ILAN_ID, 0.42, 0.4, 0.3, 1.0, false, 0, 0);
    }

    private long seviyeSayisi(Level seviye) {
        return gunlukKaydedici.list.stream().filter(e -> e.getLevel() == seviye).count();
    }

    private List<String> mesajlar() {
        return gunlukKaydedici.list.stream()
                .map(e -> e.getLevel() + " " + e.getFormattedMessage())
                .toList();
    }

    /** Çağrıldığı parametreleri saklar ve sırayla programlanmış sonucu döner. */
    private record Cagri(String token, String baslik, String govde, Map<String, String> veri) {
    }

    private static final class SahtePushServisi implements PushNotificationService {

        private final List<Cagri> cagrilar = new ArrayList<>();
        private final Deque<PushResult> sonuclar = new ArrayDeque<>();

        void sirayaKoy(PushResult... programlanan) {
            sonuclar.addAll(List.of(programlanan));
        }

        @Override
        public PushResult send(String token, String baslik, String govde, Map<String, String> veri) {
            cagrilar.add(new Cagri(token, baslik, govde, veri));
            PushResult sonuc = sonuclar.poll();
            if (sonuc == null) {
                throw new IllegalStateException(
                        "Sahte servis beklenenden fazla çağrıldı — fikstür eksik programlanmış.");
            }
            return sonuc;
        }
    }
}
