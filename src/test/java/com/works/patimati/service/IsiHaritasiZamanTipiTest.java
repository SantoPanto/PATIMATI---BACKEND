package com.works.patimati.service;

import com.works.patimati.dto.HeatmapPointDto;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.AiStatus;
import com.works.patimati.entity.enums.Species;
import com.works.patimati.repository.MunicipalityPanelRepository;
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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Isı haritası satırı, GERÇEK JDBC dönüş tipiyle DTO'ya çevrilebiliyor mu?
 *
 * <p><b>Ölçülen arıza (27.08.2026, yerel uçtan uca):</b> {@code /panel/heatmap}
 * gerçek PostgreSQL'de 500 döndü. Native sorgudaki {@code created_at} kolonu
 * {@code timestamp with time zone}; Hibernate 6 bunu {@code Instant} olarak
 * veriyor, servis ise {@code (Timestamp)} cast'liyordu →
 * {@code ClassCastException}. Panel BE'sinin başka hiçbir testi bu hattı
 * GERÇEK sürücü tipiyle koşmuyordu — sahte katmanda tip ne verilirse o
 * "doğru"ydu.
 *
 * <p>Bu yüzden buradaki iddia sahte satırla değil, {@code getHeatmapPoints}'in
 * gerçek veritabanından getirdiği satırla kuruluyor: sürücünün verdiği tip
 * her neyse, dönüşüm onu istisnasız {@link HeatmapPointDto}'ya çevirmeli.
 *
 * <p>⚠ Yerelde parola verilmezse test ATLANIR — "Skipped" sayısına bakmadan
 * "kapı yeşil" denmez ({@code AciklamaSutunuTipiTest} ile aynı kanca).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=none")
@EnabledIf("veritabaniUlasilabilir")
class IsiHaritasiZamanTipiTest {

    private static final int WGS_84_SRID = 4326;

    /** Gerçek ilçe adlarıyla çakışmasın — sorgu yalnız bu testin kaydını görsün. */
    private static final String OLCUM_ILCESI = "IsiZamanTipiOlcumIlcesi";

    private static final double ENLEM = 40.213;
    private static final double BOYLAM = 28.972;

    private final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), WGS_84_SRID);

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MunicipalityPanelRepository repository;

    @Test
    @DisplayName("heatmap satırı gerçek sürücü tipinden DTO'ya çevrilmeli (canlıda Instant geliyor, Timestamp değil)")
    void heatmapSatiriGercekSurucuTipindenCevrilir() {
        User sahip = entityManager.persistFlushFind(kullanici());
        entityManager.persistFlushFind(ilan(sahip));

        List<Object[]> satirlar = repository.getHeatmapPoints(
                OLCUM_ILCESI,
                Instant.now().minus(1, ChronoUnit.HOURS),
                Instant.now().plus(1, ChronoUnit.HOURS),
                10);

        assertThat(satirlar)
                .as("ölçüm ilçesine tek ilan yazıldı; sorgu onu getirmeli")
                .hasSize(1);

        HeatmapPointDto nokta = MunicipalityPanelService.satirdanNokta(satirlar.get(0));

        assertThat(nokta.getLatitude()).isCloseTo(ENLEM, within(1e-6));
        assertThat(nokta.getLongitude()).isCloseTo(BOYLAM, within(1e-6));
        assertThat(nokta.getType()).isEqualTo("LOST");
        assertThat(nokta.getCreatedAt())
                .as("created_at sürücüden hangi tiple gelirse gelsin LocalDateTime'a inmeli")
                .isNotNull();
    }

    private User kullanici() {
        return User.builder()
                .email("isi-zaman-olcum-" + System.nanoTime() + "@ornek.com")
                .password("x")
                .firstName("Ölçüm")
                .lastName("Kullanıcı")
                .role(User.Role.USER)
                .build();
    }

    private Ad ilan(User sahip) {
        return Ad.builder()
                .title("Isı zaman tipi ölçümü")
                .description("Gerçek sürücü tipi ölçümü")
                .species(Species.CAT)
                .adType(Ad.AdType.LOST)
                .aiStatus(AiStatus.PENDING)
                .location(geometryFactory.createPoint(new Coordinate(BOYLAM, ENLEM)))
                .city("Ölçüm")
                .district(OLCUM_ILCESI)
                .photoUrls(List.of("s3://patimati/test/isi.jpg"))
                .user(sahip)
                .active(true)
                .suspended(false)
                .build();
    }

    // ---------------------------------------------------------------------
    // Veritabanı koşulu — AciklamaSutunuTipiTest'in ikizi (bkz. o sınıfın javadoc'u)
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

    private static String ortam(String birinci, String ikinci, String varsayilan) {
        String deger = System.getenv(birinci);
        if (deger != null && !deger.isBlank()) {
            return deger;
        }
        deger = System.getenv(ikinci);
        if (deger != null && !deger.isBlank()) {
            return deger;
        }
        return varsayilan;
    }
}
