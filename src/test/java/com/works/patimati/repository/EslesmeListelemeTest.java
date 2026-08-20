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
import org.springframework.test.context.TestPropertySource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * "Eşleşmelerim" listesi <b>aynı çifti iki kez göstermemeli</b> — sorguya sorulur.
 *
 * <p><b>Neden ayrı bir sınıf:</b> {@code EslesmeKaydiSemaTest} şemanın satırı
 * kabul edip etmediğini ölçer; buradaki kusur şemada <b>değil</b>, listeleme
 * sorgusundadır. Ölçülen kusur canlı veride şuydu: {@code GET
 * /api/matches/my-matches} bir kullanıcıya <b>34 kayıt / 27 benzersiz çift</b>
 * döndürüyordu; başka bir kullanıcıda 43 çiftin <b>43'ü birden</b> ikizliydi.
 *
 * <p><b>Kusurun mekanizması:</b> "Çözüm B" mimarisi bir çift için <b>alıcı
 * başına ayrı satır</b> yazar (aynı {@code source}/{@code matched}, farklı
 * {@code user_id}); listeleme sorgusu ise satır sahipliğine <b>ek olarak</b>
 * {@code sourceAd.user} ve {@code matchedAd.user} koşullarını da taşıyordu.
 * Sonuç: kullanıcı kendi satırını <i>ve</i> karşı tarafın satırını görüyordu.
 * Kısıtın "yön duyarlı" olması <b>sebep değildi</b> — ham veride ters yönde
 * yazılmış tek bir çift bile yok (ölçüldü: 0).
 *
 * <p><b>Veritabanı yoksa test ATLAR</b> — koşul {@code EslesmeKaydiSemaTest}
 * ile aynıdır ve o sınıfın javadoc'unda anlatılan sebeple <b>bilerek
 * kopyalanmıştır</b>. ⚠ "Skipped" sayısına bakmadan "kapı yeşil" denmez.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=none")
@EnabledIf("veritabaniUlasilabilir")
class EslesmeListelemeTest {

    private static final double ESIK = 0.37;

    private static final int WGS_84_SRID = 4326;

    private final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), WGS_84_SRID);

    @Autowired
    private AdMatchRepository adMatchRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Alıcı başına satır yazılan çift, HER İKİ kullanıcıya da BİR kez görünmeli")
    void ayniCiftIkiKezGorunmemeli() {
        Cift c = cift("ikiz");

        List<AdMatch> kaybedenin = adMatchRepository
                .findByUserIdOrderByTotalScoreDesc(c.kaybeden().getUid());
        List<AdMatch> bulanin = adMatchRepository
                .findByUserIdOrderByTotalScoreDesc(c.bulan().getUid());

        assertThat(kaybedenin)
                .withFailMessage("""
                        Kaybeden kullanıcı TEK çift için %d satır gördü. Listeleme
                        sorgusuna ilan sahipliği (sourceAd.user / matchedAd.user)
                        koşulu geri gelmiş olmalı: alıcı başına ayrı satır yazıldığı
                        için kullanıcı kendi satırını VE karşı tarafın satırını birden
                        görür, "Eşleşmelerim" aynı ilanı iki kez listeler.""",
                        kaybedenin.size())
                .hasSize(1);

        assertThat(bulanin)
                .withFailMessage("""
                        Bulan kullanıcı TEK çift için %d satır gördü — aynı kusur,
                        karşı taraftan bakılmış hâli.""", bulanin.size())
                .hasSize(1);

        // Herkes KENDİ satırını görmeli: bildirim damgası satır başına tutuluyor,
        // karşı tarafın satırı okunursa "bildirim gitti mi" sorusu yanlış cevaplanır.
        assertThat(kaybedenin.get(0).getUser().getUid()).isEqualTo(c.kaybeden().getUid());
        assertThat(bulanin.get(0).getUser().getUid()).isEqualTo(c.bulan().getUid());
    }

    @Test
    @DisplayName("Yalnız EŞLEŞEN ilanın sahibi olan kullanıcı da eşleşmesini görebilmeli")
    void karsiTarafEslesmesiniGorebilmeli() {
        Cift c = cift("kayip-yok");

        List<AdMatch> bulanin = adMatchRepository
                .findByUserIdOrderByTotalScoreDesc(c.bulan().getUid());

        // Bulan kullanıcının ilanı çiftin "matched" tarafında; satır sahipliğine
        // geçerken bu tarafın kaybolmadığını kanıtlar (ham veride de kayıp 0'dı).
        assertThat(bulanin)
                .withFailMessage("""
                        Eşleşen ilanın sahibi kendi eşleşmesini HİÇ göremedi.
                        Satır sahipliği ölçütü, alıcı başına satır yazılmadığı bir
                        durumda tarafı gizler; o zaman ölçüt yeniden düşünülmeli.""")
                .hasSize(1);
        assertThat(bulanin.get(0).getMatchedAd().getId()).isEqualTo(c.bulunan().getId());
    }

    @Test
    @DisplayName("Askıya alınmış ilanın eşleşmesi listede görünmemeli")
    void askidakiIlanListelenmemeli() {
        Cift c = cift("aski");

        // cift() sonunda clear() çağrıldığı için c.bulunan() ARTIK ayrık (detached);
        // persist onu reddeder. Yeniden bulup güncellenir, kirli-kontrol yazar.
        Ad bulunan = entityManager.find(Ad.class, c.bulunan().getId());
        bulunan.setSuspended(true);
        entityManager.flush();
        entityManager.clear();

        assertThat(adMatchRepository.findByUserIdOrderByTotalScoreDesc(c.kaybeden().getUid()))
                .withFailMessage("""
                        Askıya alınmış ilanın eşleşmesi hâlâ listeleniyor. Mükerrer
                        listeleme düzeltilirken askı süzgeci düşmüş olmalı.""")
                .isEmpty();
    }

    // ---------------------------------------------------------------------
    // Fikstür — AiMatchNotifier'ın gerçekten yazdığı biçim
    // ---------------------------------------------------------------------

    private record Cift(User kaybeden, User bulan, Ad kayip, Ad bulunan) {
    }

    /**
     * İki ayrı kullanıcı, iki ilan ve <b>alıcı başına bir</b> eşleşme satırı.
     * İki satırın {@code sourceAd}/{@code matchedAd} değerleri bilerek AYNI —
     * {@code AiMatchNotifier.kaydet} satırı her zaman "analiz edilen ilan → aday"
     * yönüyle yazar, alıcı değişse de yön değişmez.
     */
    private Cift cift(String ek) {
        User kaybeden = entityManager.persist(kullanici("kaybeden-" + ek));
        User bulan = entityManager.persist(kullanici("bulan-" + ek));
        Ad kayip = entityManager.persist(ilan("kayıp " + ek, kaybeden));
        Ad bulunan = entityManager.persist(ilan("bulunan " + ek, bulan));
        entityManager.flush();

        entityManager.persist(kayit(kaybeden, kayip, bulunan));
        entityManager.persist(kayit(bulan, kayip, bulunan));
        entityManager.flush();
        entityManager.clear();

        return new Cift(kaybeden, bulan, kayip, bulunan);
    }

    private AdMatch kayit(User alici, Ad kaynak, Ad eslesen) {
        return AdMatch.builder()
                .user(alici)
                .sourceAd(kaynak)
                .matchedAd(eslesen)
                .totalScore(0.42)
                .visualScore(0.40)
                .tagScore(0.30)
                .locationScore(0.5)
                .thresholdAtTime(ESIK)
                .passedThreshold(true)
                .matchedPhotoPair("{\"source\":1,\"matched\":0}")
                .build();
    }

    private User kullanici(String ek) {
        return User.builder()
                .email("listeleme-" + ek + "@test.local")
                .firstName("Test")
                .lastName("Kullanıcı")
                .role(User.Role.USER)
                .build();
    }

    /** {@code EslesmeKaydiSemaTest} ile aynı asgari geçerli ilan. */
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
    // Veritabanı koşulu — EslesmeKaydiSemaTest'in ikizi (bkz. sınıf javadoc'u)
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
