package com.works.patimati.ai;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.works.patimati.ai.dto.AiAnalysisResult;
import com.works.patimati.dto.match.AdMatchResponseDTO;
import com.works.patimati.dto.match.AdMatchSaveRequest;
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
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConverter;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * B6 — <b>eşleşme kaydediliyor</b> ve <b>bildirim tekrarlanmıyor</b>.
 *
 * <p><b>Neden bu test var:</b> "Eşleşmelerim" sayfası 18.08'de canlıya çıktı ama
 * <b>boş</b> dönüyordu — AI eşleşmeleri buluyor, bildirim gidiyor, fakat hiçbir
 * yere yazılmıyordu. Bildirimin kendisi de kayıtsızdı: RabbitMQ "en az bir kez"
 * teslim ettiği için aynı mesajın ikinci tesliminde aynı çifte bildirim yeniden
 * gidiyordu (sözleşme §7 kural 4 bunu yasaklıyor).
 *
 * <p><b>Fikstür gerçek dönüştürücüden geçer.</b> Alan adları telde snake_case
 * ({@code match_threshold}) ve eşleme {@code AiRabbitConfig}'teki mapper'da
 * kuruluyor. Nesneyi elle kurmak bu katmanı atlar ve "alan telde taşınıyor"
 * iddiasını <b>ölçmeden</b> yeşile boyardı.
 *
 * <p><b>Eşik fikstürde bilerek {@code 0.37}:</b> sistemde hiçbir yerde
 * kullanılmayan bir sayı. Gerçek eşik (bugün 0.80) yazılsaydı, birinin değeri
 * backend'e sabit yazması testi <b>yine geçirirdi</b> — sayı zaten tutuyor
 * olurdu. Tuhaf sayı, "değer AI'nın cevabından geliyor" iddiasını sabit yazmadan
 * ayırt edilebilir kılıyor.
 *
 * <p>Veritabanı ve broker gerektirmez.
 */
class EslesmeKaydiVeTekrarKorumasiTest {

    private static final long KAYNAK_ILAN = 42L;
    private static final long GUCLU_ESLESEN_ILAN = 200L;
    private static final long ZAYIF_ESLESEN_ILAN = 201L;

    private static final long KAYNAK_SAHIBI = 7L;
    private static final long GUCLU_ESLESEN_SAHIBI = 8L;
    private static final long ZAYIF_ESLESEN_SAHIBI = 9L;

    /** Sistemde başka hiçbir yerde geçmeyen eşik — sabit yazmayı ayırt eder. */
    private static final double ESIK = 0.37;

    /** Biri eşiği geçiyor, biri geçmiyor: §7 kural 3 ile kural 7 aynı mesajda. */
    private static final String CEVAP = """
            {
              "schema_version": 1,
              "request_id": "b7c1f3a0-0000-4000-8000-000000000010",
              "ad_id": 42,
              "status": "ok",
              "model_version": "siglip2-animal/v2",
              "processed_at": "2026-08-18T09:00:00Z",
              "analysis": {
                "species": "cat",
                "species_confidence": 0.97,
                "breed": null,
                "breed_confidence": 0.0,
                "labels": ["cat"],
                "is_pet": true,
                "embeddings": [[0.1, 0.2, 0.3]]
              },
              "matches": [
                {"ad_id": 200, "score": 0.91, "visual": 0.93, "label": 0.85,
                 "location": 1.0, "match": true, "photo_a": 0, "photo_b": 2},
                {"ad_id": 201, "score": 0.42, "visual": 0.40, "label": 0.30,
                 "location": 0.5, "match": false, "photo_a": 1, "photo_b": 0}
              ],
              "match_threshold": 0.37,
              "skipped_candidates": {
                "toplam": 1,
                "kendisi": 1,
                "tekrar_eden": 0,
                "model_surumu_uyusmuyor": 0,
                "gecersiz_embedding": 0,
                "aday_siniri_asildi": 0
              }
            }
            """;

    /** Aynı cevap, ama {@code match_threshold} alanı HİÇ yok (eski AI sürümü). */
    private static final String CEVAP_ESIKSIZ = CEVAP.replace(
            "\"match_threshold\": 0.37,", "");

    private final MessageConverter converter = new AiRabbitConfig().aiJsonMessageConverter();

    private AdRepository adRepository;
    private SahtePushServisi push;
    private SahteKayitServisi kayitServisi;
    private AiAnalysisListener listener;

    private ch.qos.logback.classic.Logger logger;
    private ListAppender<ILoggingEvent> gunlukKaydedici;
    private Level eskiSeviye;

    @BeforeEach
    void hazirla() {
        adRepository = mock(AdRepository.class);
        ilanlariKur();

        push = new SahtePushServisi();
        kayitServisi = new SahteKayitServisi();

        listener = new AiAnalysisListener(adRepository,
                new AiMatchNotifier(adRepository, push, kayitServisi.servis()));

        LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
        logger = ctx.getLogger(AiMatchNotifier.class);
        eskiSeviye = logger.getLevel();
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
    // Kayıt
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Eşiği GEÇMEYEN aday da kaydedilir, yalnız bildirimi gitmez (§7 kural 3+7)")
    void esigiGecmeyenDeKaydedilir() {
        push.sirayaKoy(PushResult.SENT, PushResult.SENT);

        listener.onResult(coz(CEVAP));

        List<AdMatchSaveRequest> zayif = kayitServisi.istekler(ZAYIF_ESLESEN_ILAN);
        assertThat(zayif)
                .withFailMessage("""
                        Eşiği geçmeyen aday HİÇ kaydedilmedi (%d satır).
                        Sözleşme §7: düşük skorlu aday da saklanır, yalnız bildirim
                        tetiklemez. Kaydedilmezse kullanıcı "neden bu ilan listede
                        yok" sorusunun cevabını hiçbir yerde bulamaz.""", zayif.size())
                .hasSize(2);
        assertThat(zayif).allSatisfy(istek -> assertThat(istek.isPassedThreshold())
                .withFailMessage("Eşiği geçmeyen kayıt passedThreshold=true yazıldı.")
                .isFalse());

        assertThat(push.cagrilar)
                .withFailMessage("""
                        Bildirim sayısı %d; yalnız eşiği GEÇEN eşleşme için 2 (iki
                        taraf) bekleniyordu. Eşiği geçmeyen aday bildirim tetiklediyse
                        kullanıcı yanlış alarma boğulur.""", push.cagrilar.size())
                .hasSize(2);

        // Her alıcı KARŞI ilanı görür (7 → 200, 8 → 42); ortak kural, ikisinde de
        // zayıf adayın geçmemesi.
        assertThat(push.cagrilar)
                .withFailMessage("Eşiği geçmeyen aday bildirimde göründü: %s", push.cagrilar)
                .noneSatisfy(cagri -> assertThat(cagri.veri())
                        .containsEntry("adId", String.valueOf(ZAYIF_ESLESEN_ILAN)));
    }

    @Test
    @DisplayName("Her eşleşme için İKİ kullanıcı satırı yazılır — bildirim iki tarafa gidiyor")
    void herEslesmeIkiKullaniciSatiriYazar() {
        push.sirayaKoy(PushResult.SENT, PushResult.SENT);

        listener.onResult(coz(CEVAP));

        List<AdMatchSaveRequest> guclu = kayitServisi.istekler(GUCLU_ESLESEN_ILAN);
        assertThat(guclu)
                .withFailMessage("""
                        Eşleşme başına %d satır yazıldı, 2 bekleniyordu. Bildirim
                        HER İKİ ilanın sahibine gidiyor; "bu kullanıcıya gitti mi"
                        sorusu kullanıcıya özel olduğu için satır da kullanıcıya
                        özel olmalı.""", guclu.size())
                .hasSize(2);

        assertThat(guclu).extracting(AdMatchSaveRequest::getUserId)
                .containsExactlyInAnyOrder(KAYNAK_SAHIBI, GUCLU_ESLESEN_SAHIBI);

        // Yön her iki satırda da AYNI: analiz edilen ilan kaynak, aday eşleşen.
        assertThat(guclu).allSatisfy(istek -> {
            assertThat(istek.getSourceAdId()).isEqualTo(KAYNAK_ILAN);
            assertThat(istek.getMatchedAdId()).isEqualTo(GUCLU_ESLESEN_ILAN);
        });
    }

    @Test
    @DisplayName("Skor bileşenleri ve eşik, AI cevabından OLDUĞU GİBİ kaydedilir")
    void skorBilesenleriKayda_AiCevabindanGecer() {
        push.sirayaKoy(PushResult.SENT, PushResult.SENT);

        listener.onResult(coz(CEVAP));

        AdMatchSaveRequest istek = kayitServisi.istekler(GUCLU_ESLESEN_ILAN).get(0);

        assertThat(istek.getThresholdAtTime())
                .withFailMessage("""
                        threshold_at_time %s yazıldı, AI'nın gönderdiği %s değil.
                        Değerin TEK kaynağı AI'dır: backend yapılandırmasından
                        okunursa iki kaynak sessizce kayar ve kayıt geçmişe dair
                        yanlış konuşur.""", istek.getThresholdAtTime(), ESIK)
                .isEqualTo(ESIK);

        assertThat(istek.getTotalScore()).isEqualTo(0.91);
        assertThat(istek.getVisualScore()).isEqualTo(0.93);
        assertThat(istek.getTagScore())
                .withFailMessage("AI'nın 'label' skoru tag_score'a bağlanmamış: %s",
                        istek.getTagScore())
                .isEqualTo(0.85);
        assertThat(istek.getLocationScore()).isEqualTo(1.0);

        assertThat(istek.getMatchedPhotoPair())
                .withFailMessage("""
                        Eşleşen fotoğraf çifti kayboldu (%s). AI hangi iki karenin
                        tuttuğunu söylüyor; yazılmazsa bilgi bir daha üretilemez.""",
                        istek.getMatchedPhotoPair())
                .isEqualTo("{\"source\":0,\"matched\":2}");

        assertThat(istek.getBlockReason())
                .withFailMessage("""
                        blockReason dolduruldu: %s. Arayüz bu alanı kullanıcıya
                        KIRMIZI UYARI olarak ham gösteriyor (MatchCard.tsx:170);
                        "skor eşiğin altında" bir engel değil, zaten
                        passedThreshold ile anlatılıyor.""", istek.getBlockReason())
                .isNull();
    }

    // ---------------------------------------------------------------------
    // Tekrar koruması
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Aynı mesaj İKİNCİ kez teslim edilirse bildirim TEKRARLANMAZ (§7 kural 4)")
    void tekrarTeslimdeIkinciBildirimGitmez() {
        push.sirayaKoy(PushResult.SENT, PushResult.SENT);

        listener.onResult(coz(CEVAP));

        // Ön koşul: ilk teslim gerçekten gönderdi ve damgaladı. Göndermemişse
        // "ikinci teslimde gitmedi" iddiası doğru çıkar ama yanlış sebeple.
        assertThat(push.cagrilar)
                .withFailMessage("Ön koşul bozuk: ilk teslimde bildirim gitmedi.")
                .hasSize(2);
        assertThat(kayitServisi.damgalananlar)
                .withFailMessage("""
                        Bildirim gitti ama notification_sent_at damgalanmadı (%d).
                        Damga vurulmazsa tekrar koruması hiç çalışmaz.""",
                        kayitServisi.damgalananlar.size())
                .hasSize(2);

        long oncekiInfo = seviyeSayisi(Level.INFO);

        listener.onResult(coz(CEVAP));

        assertThat(push.cagrilar)
                .withFailMessage("""
                        İkinci teslimde toplam çağrı %d oldu; 2'de kalmalıydı.
                        RabbitMQ "en az bir kez" teslim eder: aynı sonuç ikinci kez
                        gelince kullanıcıya aynı bildirim yeniden gidiyor.""",
                        push.cagrilar.size())
                .hasSize(2);

        // İddia düzyazıya değil SAYIYA yaslanıyor: ikinci teslim tam bir satır
        // eklemeli. Dışarıdan bakınca "engellendi" ile "hiç çalışmadı" aynı
        // görünür; ayırt edilebilmesi gerekiyor.
        assertThat(seviyeSayisi(Level.INFO))
                .withFailMessage("""
                        Tekrar teslim günlüğe iz bırakmadı (INFO %d → %d).
                        Yazılanlar: %s""", oncekiInfo, seviyeSayisi(Level.INFO), mesajlar())
                .isEqualTo(oncekiInfo + 1);
    }

    @Test
    @DisplayName("Bildirim GİTMEDİYSE damga vurulmaz — sonraki teslimde yeniden denenir")
    void gonderilemeyenDamgalanmaz() {
        push.sirayaKoy(PushResult.NO_TOKEN, PushResult.NO_TOKEN);

        listener.onResult(coz(CEVAP));

        assertThat(push.cagrilar).hasSize(2);
        assertThat(kayitServisi.damgalananlar)
                .withFailMessage("""
                        Hiçbir bildirim gitmediği hâlde %d kayıt "gönderildi" diye
                        damgalandı. Bu, günlükteki eski yalanın veritabanı sürümü
                        olurdu: kullanıcı bildirimi hiç almaz ve bir daha da
                        denenmez.""", kayitServisi.damgalananlar.size())
                .isEmpty();

        // İkinci teslimde YENİDEN denenmeli: gerçekten gitmemişti.
        push.sirayaKoy(PushResult.SENT, PushResult.SENT);
        listener.onResult(coz(CEVAP));

        assertThat(push.cagrilar)
                .withFailMessage("Gönderilemeyen bildirim ikinci teslimde yeniden denenmedi.")
                .hasSize(4);
        assertThat(kayitServisi.damgalananlar).hasSize(2);
    }

    // ---------------------------------------------------------------------
    // Eşik telde gelmezse (eski AI sürümü)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("match_threshold gelmezse kayıt yazılmaz ama bildirim kesilmez ve günlük söyler")
    void esikGelmezseKayitYokBildirimVar() {
        push.sirayaKoy(PushResult.SENT, PushResult.SENT);

        AiAnalysisResult sonuc = coz(CEVAP_ESIKSIZ);

        // Ön koşul: fikstür gerçekten eşiksiz. Alan hâlâ doluysa test bir şey ölçmez.
        assertThat(sonuc.matchThreshold())
                .withFailMessage("Ön koşul bozuk: fikstürde match_threshold hâlâ var.")
                .isNull();

        listener.onResult(sonuc);

        assertThat(kayitServisi.tumIstekler)
                .withFailMessage("""
                        Eşik yokken kayıt yazılmaya çalışıldı (%d istek).
                        ad_match.threshold_at_time NOT NULL: kayıt dinleyicinin
                        işlemini düşürür, AI sonucu ilana HİÇ yazılmaz ve mesaj
                        sonsuza kadar yeniden teslim edilir.""",
                        kayitServisi.tumIstekler.size())
                .isEmpty();

        assertThat(push.cagrilar)
                .withFailMessage("""
                        Eşik gelmedi diye bildirim de kesildi. Eksik alan yüzünden
                        çalışan bir özelliği kapatmak, kullanıcıya sessiz bir kesinti
                        olarak yansır.""")
                .hasSize(2);

        assertThat(seviyeSayisi(Level.WARN))
                .withFailMessage("""
                        Kayıt yazılamadı ama günlük SUSTU. Sessizce eksik çalışan
                        bir koruma, hiç olmayan korumadan daha tehlikelidir.
                        Yazılanlar: %s""", mesajlar())
                .isOne();
    }

    // ---------------------------------------------------------------------
    // Fikstür ve yardımcılar
    // ---------------------------------------------------------------------

    private AiAnalysisResult coz(String json) {
        MessageProperties props = new MessageProperties();
        props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        props.setInferredArgumentType(AiAnalysisResult.class);
        return (AiAnalysisResult) converter.fromMessage(
                new Message(json.getBytes(StandardCharsets.UTF_8), props));
    }

    private void ilanlariKur() {
        Ad kaynak = ilan(KAYNAK_ILAN, "kaynak ilan", KAYNAK_SAHIBI);
        when(adRepository.findById(KAYNAK_ILAN)).thenReturn(Optional.of(kaynak));
        when(adRepository.findById(GUCLU_ESLESEN_ILAN))
                .thenReturn(Optional.of(ilan(GUCLU_ESLESEN_ILAN, "güçlü aday", GUCLU_ESLESEN_SAHIBI)));
        when(adRepository.findById(ZAYIF_ESLESEN_ILAN))
                .thenReturn(Optional.of(ilan(ZAYIF_ESLESEN_ILAN, "zayıf aday", ZAYIF_ESLESEN_SAHIBI)));
        when(adRepository.save(any(Ad.class))).thenAnswer(cagri -> cagri.getArgument(0));
    }

    private Ad ilan(long id, String baslik, long sahipId) {
        return Ad.builder()
                .id(id)
                .title(baslik)
                .photoUrls(List.of("s3://patimati/" + id + "/a.jpg"))
                .user(User.builder().uid(sahipId).fcmToken("jeton-" + sahipId).build())
                .build();
    }

    private long seviyeSayisi(Level seviye) {
        return gunlukKaydedici.list.stream().filter(e -> e.getLevel() == seviye).count();
    }

    private List<String> mesajlar() {
        return gunlukKaydedici.list.stream()
                .map(e -> e.getLevel() + " " + e.getFormattedMessage())
                .toList();
    }

    /**
     * {@link AdMatchService}'in bellekte yaşayan karşılığı.
     *
     * <p>Sahte olmasının bedeli var: gerçek servisin benzersiz kısıtı ve
     * {@code @Transactional} davranışı burada yok. Ama ölçülen şey <b>çağıranın
     * kararları</b> — hangi satır yazılıyor, damga ne zaman vuruluyor — ve o
     * kararların ikinci teslimde değişmesi için damganın <b>okunabilir</b>
     * olması yetiyor. Basit bir {@code mock}, ikinci teslimde de {@code null}
     * damga döndürüp tekrar korumasını ölçülemez hâle getirirdi.
     */
    private static final class SahteKayitServisi {

        private final List<AdMatchSaveRequest> tumIstekler = new ArrayList<>();
        private final List<Long> damgalananlar = new ArrayList<>();
        private final Map<Long, Instant> damgalar = new HashMap<>();

        AdMatchService servis() {
            AdMatchService servis = mock(AdMatchService.class);
            when(servis.saveOrUpdateMatch(any())).thenAnswer(cagri -> {
                AdMatchSaveRequest istek = cagri.getArgument(0);
                tumIstekler.add(istek);
                long id = kimlik(istek);
                return AdMatchResponseDTO.builder()
                        .id(id)
                        .passedThreshold(istek.isPassedThreshold())
                        .notificationSentAt(damgalar.get(id))
                        .build();
            });
            when(servis.markNotificationAsSent(anyLong())).thenAnswer(cagri -> {
                long id = cagri.getArgument(0);
                damgalar.put(id, Instant.parse("2026-08-18T10:00:00Z"));
                damgalananlar.add(id);
                return AdMatchResponseDTO.builder().id(id).build();
            });
            return servis;
        }

        /** Benzersiz kısıtın bellekteki karşılığı: (kullanıcı, kaynak, eşleşen). */
        private static long kimlik(AdMatchSaveRequest istek) {
            return istek.getUserId() * 1_000_000L
                    + istek.getSourceAdId() * 1_000L
                    + istek.getMatchedAdId();
        }

        List<AdMatchSaveRequest> istekler(long eslesenIlanId) {
            return tumIstekler.stream()
                    .filter(istek -> istek.getMatchedAdId() == eslesenIlanId)
                    .toList();
        }
    }

    private record Cagri(String token, String baslik, String govde, Map<String, String> veri) {
    }

    private static final class SahtePushServisi implements PushNotificationService {

        private final List<Cagri> cagrilar = new ArrayList<>();
        private final List<PushResult> sonuclar = new ArrayList<>();

        void sirayaKoy(PushResult... programlanan) {
            sonuclar.addAll(List.of(programlanan));
        }

        @Override
        public PushResult send(String token, String baslik, String govde, Map<String, String> veri) {
            cagrilar.add(new Cagri(token, baslik, govde, veri));
            if (sonuclar.isEmpty()) {
                throw new IllegalStateException(
                        "Sahte servis beklenenden fazla çağrıldı — fikstür eksik programlanmış.");
            }
            return sonuclar.remove(0);
        }
    }
}
