package com.works.patimati.service;

import com.works.patimati.entity.AnimalReport;
import com.works.patimati.entity.enums.ReportStatus;
import com.works.patimati.entity.enums.ReportType;
import com.works.patimati.repository.AnimalReportRepository;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Panel "İhbar analizi" sorguları (S6) GERÇEK PostgreSQL'de doğru mu?
 *
 * <p><b>Neden gerçek DB:</b> bu modülün 27.08 dersi — panel BE'si "testli"
 * görünüyordu ama cast hattını hiçbir test gerçek sürücü tipiyle koşmamıştı,
 * heatmap canlı tipte 500 verdi ({@code IsiHaritasiZamanTipiTest}). Günlük
 * seri de native sorgu: gün kolonunun sürücüden hangi tiple geldiği ORM/JDBC
 * sürümüne bağlı — {@code gunecevir} o yüzden tip-korumalı ve burada gerçek
 * satırla sınanıyor.
 *
 * <p>İlçe süzgeci sözleşmesi panel repo'suyla aynı: null = ilçesiz yönetici,
 * süzgeç atlanır; dolu ilçe KİLİTLİ kalır (kurum başka ilçeyi göremez).
 *
 * <p>⚠ Yerelde parola verilmezse test ATLANIR — Skipped sayısına bakmadan
 * "kapı yeşil" denmez.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=none")
@EnabledIf("veritabaniUlasilabilir")
class IhbarIstatistikleriGercekDbTest {

    private static final int WGS_84_SRID = 4326;

    /** Gerçek/demo ilçe adlarıyla çakışmasın — sorgular yalnız bu testin kayıtlarını görsün. */
    private static final String OLCUM_ILCESI = "IhbarIstatistikOlcumIlcesi";
    private static final String IKINCI_ILCE = OLCUM_ILCESI + "-Ikinci";

    private final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), WGS_84_SRID);

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AnimalReportRepository repository;

    @Test
    @DisplayName("durum ve tür kırılımı ilçeye KİLİTLİ; ikinci ilçenin kaydı sızmaz")
    void durumVeTurKirilimiIlceyeKilitli() {
        ihbar(OLCUM_ILCESI, ReportType.YARALI, ReportStatus.YENI);
        ihbar(OLCUM_ILCESI, ReportType.YARALI, ReportStatus.YENI);
        ihbar(OLCUM_ILCESI, ReportType.SAHIPSIZ, ReportStatus.ISLEME_ALINDI);
        ihbar(OLCUM_ILCESI, ReportType.DIGER, ReportStatus.TAMAMLANDI);
        ihbar(IKINCI_ILCE, ReportType.YARALI, ReportStatus.YENI);

        Map<ReportStatus, Long> durumlar = sayim(
                repository.durumaGoreSay(OLCUM_ILCESI, bas(), son()), ReportStatus.class);
        assertThat(durumlar).containsEntry(ReportStatus.YENI, 2L)
                .containsEntry(ReportStatus.ISLEME_ALINDI, 1L)
                .containsEntry(ReportStatus.TAMAMLANDI, 1L);

        Map<ReportType, Long> turler = sayim(
                repository.tureGoreSay(OLCUM_ILCESI, bas(), son()), ReportType.class);
        assertThat(turler).containsEntry(ReportType.YARALI, 2L)
                .containsEntry(ReportType.SAHIPSIZ, 1L)
                .containsEntry(ReportType.DIGER, 1L);
    }

    @Test
    @DisplayName("null ilçe (ilçesiz yönetici) süzgeçsiz tarar — iki ilçenin toplamı döner")
    void nullIlceSuzgecsizTarar() {
        ihbar(OLCUM_ILCESI, ReportType.YARALI, ReportStatus.YENI);
        ihbar(IKINCI_ILCE, ReportType.YARALI, ReportStatus.YENI);

        Map<ReportStatus, Long> durumlar = sayim(
                repository.durumaGoreSay(null, bas(), son()), ReportStatus.class);

        // Süzgeçsiz sorgu TÜM tabloyu tarar (gerçek/demo veri dahil) — iddia
        // eşitlikle değil, en az iki ölçüm kaydıyla kurulur.
        assertThat(durumlar.getOrDefault(ReportStatus.YENI, 0L)).isGreaterThanOrEqualTo(2L);
    }

    @Test
    @DisplayName("günlük seri gerçek sürücü tipinden LocalDate'e iner ve güne göre gruplar")
    void gunlukSeriGercekSurucuTipindenIner() {
        ihbar(OLCUM_ILCESI, ReportType.YARALI, ReportStatus.YENI);
        ihbar(OLCUM_ILCESI, ReportType.SAHIPSIZ, ReportStatus.YENI);

        List<Object[]> satirlar = repository.gunlukIhbarSayilari(OLCUM_ILCESI, bas(), son());

        assertThat(satirlar)
                .as("iki kayıt da bugüne yazıldı; tek gün satırı dönmeli")
                .hasSize(1);

        LocalDate gun = MunicipalityPanelService.gunecevir(satirlar.get(0)[0]);
        long adet = ((Number) satirlar.get(0)[1]).longValue();

        assertThat(gun).isEqualTo(LocalDate.now());
        assertThat(adet).isEqualTo(2L);
    }

    // ------------------------------------------------------------------

    private void ihbar(String ilce, ReportType tur, ReportStatus durum) {
        entityManager.persistFlushFind(AnimalReport.builder()
                .reporterContact("05550001122")
                .type(tur)
                .status(durum)
                .location(geometryFactory.createPoint(new Coordinate(28.972, 40.213)))
                .city("Ölçüm")
                .district(ilce)
                .build());
    }

    private static LocalDateTime bas() {
        return LocalDateTime.now().minusHours(1);
    }

    private static LocalDateTime son() {
        return LocalDateTime.now().plusHours(1);
    }

    private static <E extends Enum<E>> Map<E, Long> sayim(List<Object[]> satirlar, Class<E> tip) {
        return satirlar.stream().collect(Collectors.toMap(
                satir -> tip.cast(satir[0]),
                satir -> ((Number) satir[1]).longValue()));
    }

    // ---------------------------------------------------------------------
    // Veritabanı koşulu — IsiHaritasiZamanTipiTest'in ikizi
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
        if (deger == null || deger.isBlank()) {
            deger = System.getenv(ikinci);
        }
        return (deger == null || deger.isBlank()) ? varsayilan : deger;
    }
}
