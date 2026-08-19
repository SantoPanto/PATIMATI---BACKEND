package com.works.patimati.repository;

import com.works.patimati.entity.Ad;
import com.works.patimati.entity.AdMatch;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.AiStatus;
import com.works.patimati.entity.enums.Species;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * {@code ad_match} satırı <b>gerçekten yazılabiliyor mu</b> — şemanın kendisine sorulur.
 *
 * <p><b>Neden gerçek veritabanı:</b> B6'nın birim testi
 * ({@code EslesmeKaydiVeTekrarKorumasiTest}) {@code AdMatchService}'i sahteliyor
 * ⇒ <b>JPA ve şema katmanını hiç görmüyor.</b> Orada "eşiği geçmeyen de
 * kaydediliyor" iddiası yalnızca <i>servise doğru çağrı yapıldığını</i>
 * kanıtlar; satırın veritabanına <b>düştüğünü</b> kanıtlamaz. Sütun adı
 * kaymışsa, NOT NULL bir alan boş bırakılıyorsa ya da benzersiz kısıt
 * beklenenden başka bir üçlüdeyse birim test <b>yeşil yanar, ürün kırıktır.</b>
 *
 * <p>Ölçülen üç şey, üçü de B6'nın taşıyıcı varsayımı:
 * <ol>
 *   <li>eşiği geçmeyen satır yazılabiliyor ({@code passed_threshold = false}),</li>
 *   <li>{@code threshold_at_time} <b>boş bırakılamıyor</b> — bu yüzden AI eşiği
 *       göndermezse {@code AiMatchNotifier} kaydı hiç denemiyor; denese
 *       dinleyicinin işlemi düşer ve AI sonucu ilana hiç yazılmaz,</li>
 *   <li>aynı {@code (kullanıcı, kaynak, eşleşen)} üçlüsü <b>ikinci kez
 *       yazılamıyor</b> — tekrar koruması okumaya değil, veritabanı kısıtına
 *       dayanıyor.</li>
 * </ol>
 *
 * <p><b>Veritabanı yoksa test ATLAR, hata vermez</b> — koşul
 * {@code AiAdayAskiyaAlmaTest} ile aynıdır ve <b>bilerek kopyalanmıştır</b>:
 * o testin {@code @EnabledIf} kancasını paylaşılan bir sınıfa taşımak, kanca
 * yanlış bağlanırsa <i>hata vermeden</i> "atlandı"ya düşerdi ve iki bekçi
 * birden sessizce ölürdü. İki ortam iki ayrı ad ailesi kullanıyor, ikisi de
 * denenir. ⚠ Yerelde parola verilmezse atlar —
 * <b>"Skipped" sayısına bakmadan "kapı yeşil" denmez.</b>
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=none")
@EnabledIf("veritabaniUlasilabilir")
class EslesmeKaydiSemaTest {

    /** AI'nın o an gönderdiği eşik; sistemde sabit olarak hiçbir yerde geçmez. */
    private static final double ESIK = 0.37;

    private static final int WGS_84_SRID = 4326;

    private final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), WGS_84_SRID);

    @Autowired
    private AdMatchRepository adMatchRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Eşiği GEÇMEYEN eşleşme satırı veritabanına yazılabilmeli")
    void esigiGecmeyenSatirYazilabilmeli() {
        Fikstur f = fikstur("zayif");

        AdMatch yazilan = adMatchRepository.saveAndFlush(
                kayit(f, ESIK, false));

        // Bellekteki nesneyi değil, VERİTABANINDAKİ satırı okuyalım.
        entityManager.clear();

        AdMatch okunan = adMatchRepository
                .findByUser_UidAndSourceAd_IdAndMatchedAd_Id(
                        f.sahip().getUid(), f.kaynak().getId(), f.eslesen().getId())
                .orElseThrow(() -> new AssertionError("""
                        Eşiği geçmeyen eşleşme satırı yazıldı ama üçlü anahtarla
                        BULUNAMADI (id %s). Sözleşme §7: düşük skorlu aday da
                        saklanır; bulunamıyorsa "Eşleşmelerim" sayfası yine boş
                        döner.""".formatted(yazilan.getId())));

        assertThat(okunan.isPassedThreshold()).isFalse();
        assertThat(okunan.getThresholdAtTime()).isEqualTo(ESIK);
        assertThat(okunan.getTotalScore()).isEqualTo(0.42);
        assertThat(okunan.getMatchedPhotoPair()).isEqualTo("{\"source\":1,\"matched\":0}");
        assertThat(okunan.getNotificationSentAt())
                .withFailMessage("Yeni satır 'bildirim gitti' damgasıyla doğdu — "
                        + "tekrar koruması daha ilk anda yanlış cevap verir.")
                .isNull();
    }

    @Test
    @DisplayName("Bildirim damgası veritabanında yaşamalı — tekrar koruması buna dayanıyor")
    void bildirimDamgasiVeritabanindaYasamali() {
        Fikstur f = fikstur("damga");
        AdMatch kayit = adMatchRepository.saveAndFlush(kayit(f, ESIK, true));

        kayit.markNotificationSent();
        adMatchRepository.saveAndFlush(kayit);
        entityManager.clear();

        assertThat(adMatchRepository.findById(kayit.getId()).orElseThrow().getNotificationSentAt())
                .withFailMessage("""
                        markNotificationSent() çağrıldı ama notification_sent_at
                        veritabanında BOŞ. Damga kalıcı değilse tekrar teslimde
                        aynı bildirim yeniden gider.""")
                .isNotNull();
    }

    @Test
    @DisplayName("threshold_at_time BOŞ bırakılamaz — eşik gelmeyince kayıt denenmemeli")
    void esiksizSatirReddedilmeli() {
        Fikstur f = fikstur("esiksiz");

        // catchThrowable: hiç istisna atılmazsa değer null olur ve AŞAĞIDAKİ
        // mesaj görünür. assertThatThrownBy kullanılsaydı "istisna beklendi"
        // diye kendi mesajıyla düşer, sebebi anlatan metin hiç okunmazdı.
        Throwable hata = catchThrowable(
                () -> adMatchRepository.saveAndFlush(kayit(f, null, true)));

        assertThat(hata)
                .withFailMessage("""
                        threshold_at_time boşken satır KABUL EDİLDİ. O zaman
                        AiMatchNotifier'daki "eşik yoksa kaydı hiç deneme" kuralı
                        gereksiz görünür ve biri onu kaldırır; oysa kısıt gerçekse
                        kaydı denemek dinleyicinin işlemini düşürür ve AI sonucu
                        ilana HİÇ yazılmaz.""")
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Aynı (kullanıcı, kaynak, eşleşen) üçlüsü ikinci kez yazılamamalı")
    void ayniUcluIkiKezYazilamamali() {
        Fikstur f = fikstur("ikiz");
        adMatchRepository.saveAndFlush(kayit(f, ESIK, true));

        Throwable hata = catchThrowable(
                () -> adMatchRepository.saveAndFlush(kayit(f, ESIK, true)));

        assertThat(hata)
                .withFailMessage("""
                        Aynı üçlü için İKİNCİ satır yazıldı. Benzersiz kısıt
                        yoksa her tekrar teslim yeni bir satır açar,
                        notification_sent_at hep boş gelir ve bildirim
                        sonsuza kadar tekrarlanır.""")
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // ---------------------------------------------------------------------
    // Fikstür
    // ---------------------------------------------------------------------

    private record Fikstur(User sahip, Ad kaynak, Ad eslesen) {
    }

    private Fikstur fikstur(String ek) {
        User sahip = entityManager.persist(kullanici(ek));
        Ad kaynak = entityManager.persist(ilan("kaynak " + ek, sahip));
        Ad eslesen = entityManager.persist(ilan("eşleşen " + ek, sahip));
        entityManager.flush();
        return new Fikstur(sahip, kaynak, eslesen);
    }

    private AdMatch kayit(Fikstur f, Double esik, boolean esigiGecti) {
        return AdMatch.builder()
                .user(f.sahip())
                .sourceAd(f.kaynak())
                .matchedAd(f.eslesen())
                .totalScore(0.42)
                .visualScore(0.40)
                .tagScore(0.30)
                .locationScore(0.5)
                .thresholdAtTime(esik)
                .passedThreshold(esigiGecti)
                .matchedPhotoPair("{\"source\":1,\"matched\":0}")
                .build();
    }

    private User kullanici(String ek) {
        return User.builder()
                .email("eslesme-" + ek + "@test.local")
                .firstName("Test")
                .lastName("Kullanıcı")
                .role(User.Role.USER)
                .build();
    }

    /** {@code AiAdayAskiyaAlmaTest} ile aynı asgari geçerli ilan. */
    private Ad ilan(String baslik, User sahip) {
        return Ad.builder()
                .title(baslik)
                .species(Species.CAT)
                .adType(Ad.AdType.LOST)
                .aiStatus(AiStatus.PENDING)
                .location(geometryFactory.createPoint(new Coordinate(35.0, 39.0)))
                .photoUrls(List.of("s3://patimati/test/a.jpg"))
                .user(sahip)
                .active(true)
                .suspended(false)
                .build();
    }

    // ---------------------------------------------------------------------
    // Veritabanı koşulu — AiAdayAskiyaAlmaTest'in ikizi (bkz. sınıf javadoc'u)
    // ---------------------------------------------------------------------

    @SuppressWarnings("unused") // @EnabledIf ile adıyla çağrılıyor
    static boolean veritabaniUlasilabilir() {
        String url = ortam("SPRING_DATASOURCE_URL", "DB_URL",
                "jdbc:postgresql://localhost:5432/patimati_db");
        String kullanici = ortam("SPRING_DATASOURCE_USERNAME", "DB_USER", "postgres");
        String parola = ortam("SPRING_DATASOURCE_PASSWORD", "DB_PASSWORD", null);
        if (parola == null) {
            return false;
        }

        int eskiZamanAsimi = DriverManager.getLoginTimeout();
        try {
            DriverManager.setLoginTimeout(3);
            try (Connection baglanti = DriverManager.getConnection(url, kullanici, parola)) {
                return baglanti.isValid(3);
            }
        } catch (SQLException e) {
            return false;
        } finally {
            DriverManager.setLoginTimeout(eskiZamanAsimi);
        }
    }

    private static String ortam(String once, String sonra, String varsayilan) {
        String deger = System.getenv(once);
        if (deger == null || deger.isBlank()) {
            deger = System.getenv(sonra);
        }
        return (deger == null || deger.isBlank()) ? varsayilan : deger;
    }
}
