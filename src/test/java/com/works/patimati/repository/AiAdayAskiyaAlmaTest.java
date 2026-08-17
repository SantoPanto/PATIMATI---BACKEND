package com.works.patimati.repository;

import com.works.patimati.ai.AiCandidateRow;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.enums.AiStatus;
import com.works.patimati.entity.enums.Species;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
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

/**
 * {@code findAiCandidates} <b>askıya alınmış ilanı aday havuzuna almamalı.</b>
 *
 * <p><b>Neden bu test var:</b> sorgu {@code active}, tip, vektör, pencere ve
 * yarıçapı süzüyordu ama {@code suspended}'ı süzmüyordu. Askıya alınan ilanı
 * ürün kararına göre yalnızca sahibi ve yöneticiler görebilir; aday havuzuna
 * girerse eşleşme üzerinden başlığı, açıklaması ve sahibinin adı üçüncü bir
 * kişiye açılır.
 *
 * <p><b>Neden gerçek veritabanı:</b> iddia SQL'in davranışı hakkında. Kaynak
 * dosyada {@code "AND a.suspended = FALSE"} dizgisini aramak yalnızca dizginin
 * varlığını kanıtlar — {@code = TRUE} yazılsa ya da {@code OR} ile bağlansa da
 * geçerdi. Sorgu native olduğu için tek doğru katman PostGIS'in kendisi.
 *
 * <p><b>Fikstür kısıtı — testin kendi kör noktası:</b> iki ilan da yarıçapın
 * <b>İÇİNDE</b> olmalı. 2026-08-17'de canlı veride ölçüldü: tek askıdaki ilan
 * (id 11) sorgudan zaten düşüyordu, ama {@code suspended} yüzünden değil
 * <b>827,876 km</b> uzakta olduğu için. Yani gerçek veriyle koşan bir test
 * yanlış yeşil yanardı.
 *
 * <p>Bu kör nokta testin kendisinde de vardı ve <b>mutasyonla yakalandı:</b>
 * askıdaki ilanı yarıçap dışına taşıyan mutasyon ilk sürümden <b>geçiyordu</b>
 * — asıl iddia o durumda da doğru çıkıyor, ama yanlış sebeple. Bu yüzden test
 * artık ön koşulunu ({@link #fikstureGuven}) <b>iddia ediyor</b>: fikstürün
 * yerleşimi varsayım değil, sınanan bir şart.
 *
 * <p><b>Şema güvenliği:</b> {@code ddl-auto} bilerek {@code none}'a
 * sabitlenmiştir. {@code @DataJpaTest} {@code HibernateJpaAutoConfiguration}
 * ve {@code FlywayAutoConfiguration}'ı dahil eder; bunlar <b>bağlam
 * açılışında, test işleminin DIŞINDA</b> koşar. Varsayılan
 * {@code ddl-auto: update} ile Hibernate geliştiricinin gerçek şemasını
 * sessizce {@code ALTER} edebilirdi ve rollback bunu geri almazdı.
 * <br>{@code validate} <b>kullanılamaz:</b> {@code AdoptionComplaint}
 * entity'sinin migration'ı yok, o tablo {@code ddl-auto: update}'e dayanıyor;
 * CI'daki veritabanı boş ve yalnız Flyway ile kurulduğu için {@code validate}
 * ilgisiz bir sebeple başarısız olurdu. Flyway açık bırakılır — CI'da şemayı
 * kuran tek şey odur.
 *
 * <p><b>Veritabanı yoksa test ATLAR, hata vermez.</b> Koşul bağlam yüklenmeden
 * önce değerlendirilmek zorunda olduğu için Spring'in özellik çözümlemesi
 * kullanılamaz; ortam değişkenleri elle okunur. İki ortam iki ayrı ad ailesi
 * kullanıyor ve <b>ikisi de denenmelidir</b>, yoksa test birinde her zaman
 * atlar ve atlanan bekçi hiçbir şey korumaz:
 * <ul>
 *   <li>CI ({@code .github/workflows/ci.yml}):
 *       {@code SPRING_DATASOURCE_URL} / {@code _USERNAME} / {@code _PASSWORD}</li>
 *   <li>Yerel ({@code application.yml} yer tutucuları):
 *       {@code DB_URL} / {@code DB_USER} / {@code DB_PASSWORD}</li>
 * </ul>
 * Öncelik Spring'inkiyle aynıdır: {@code SPRING_DATASOURCE_*} önce gelir.
 * <br>⚠ Yerelde koşturmak için parola verilmelidir, ör.
 * {@code $env:DB_PASSWORD='...'; mvn test}. Verilmezse test atlar —
 * <b>"Skipped" sayısına bakmadan "kapı yeşil" denmemelidir.</b>
 *
 * <p>⚠ Yerelde Flyway <i>doğrulama</i> yapar. Şema sürüklenmesi (checksum)
 * varsa bu test atlamaz, <b>hata verir</b>; beklenen davranış budur.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=none")
@EnabledIf("veritabaniUlasilabilir")
class AiAdayAskiyaAlmaTest {

    /** Yarıçap içinde ama gerçek veriden uzak: 2026-08-17'de 25 km'sinde 0 ilan vardı. */
    private static final double TEST_BOYLAM = 35.0;
    private static final double TEST_ENLEM = 39.0;

    private static final int WGS_84_SRID = 4326;
    private static final double YARICAP_METRE = 25_000.0;
    private static final int PENCERE_GUN = 90;

    /** Fikstürden hiçbirinin kimliği olamayacak bir değer — "kendisi hariç" kuralı devre dışı. */
    private static final long BASKA_BIR_ILAN = -1L;

    private final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), WGS_84_SRID);

    @Autowired
    private AdRepository adRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Askıya alınmış ilan, yarıçap içinde olsa bile aday havuzuna girmemeli")
    void askidakiIlanAdayOlmamali() {
        Point merkez = nokta(TEST_BOYLAM, TEST_ENLEM);

        // İKİSİ DE yarıçapın içinde: tek fark suspended.
        Ad askidaki = kaydet(ilan(merkez, true));
        Ad normal = kaydet(ilan(merkez, false));
        entityManager.flush();

        fikstureGuven(askidaki, merkez);

        List<Long> adayKimlikleri = adayKimlikleri(merkez);

        assertThat(adayKimlikleri)
                .withFailMessage("""
                        Askıya alınmamış aday sorgudan DÜŞTÜ (id %s).
                        Bu, süzgecin fazla eleme yaptığı anlamına gelir — beklenen
                        yalnızca askıdakinin düşmesiydi.
                        Dönen kimlikler: %s""", normal.getId(), adayKimlikleri)
                .contains(normal.getId());

        assertThat(adayKimlikleri)
                .withFailMessage("""
                        ASKIYA ALINMIŞ ilan aday havuzuna girdi (id %s).

                        findAiCandidates sorgusunda 'AND a.suspended = FALSE'
                        yüklemi yok ya da yanlış bağlanmış.

                        Neden önemli: aday havuzuna giren ilan AI eşleştirmesinden
                        geçer; eşleşirse başlığı, açıklaması ve sahibinin adı
                        üçüncü bir kişiye açılır. Askıdaki ilanı yalnızca sahibi
                        ve yöneticiler görebilir.

                        Dönen kimlikler: %s""", askidaki.getId(), adayKimlikleri)
                .doesNotContain(askidaki.getId());
    }

    // ---------------------------------------------------------------------
    // Yardımcılar
    // ---------------------------------------------------------------------

    /**
     * <b>Ön koşulun kendisini iddia eder.</b> Asıl iddia ("askıdaki aday
     * havuzunda yok") askıdaki ilan yarıçapın <i>dışına</i> düşerse de geçer —
     * ama o zaman test {@code suspended}'ı değil <b>yarıçapı</b> ölçüyor olur ve
     * bunu hiç belli etmez.
     *
     * <p>Ölçülerek bulundu: bu kontrol yokken, askıdaki ilanı yarıçap dışına
     * taşıyan mutasyon (M4) testten <b>geçiyordu</b>. Fikstürün yerleşimi bu
     * testin taşıyıcı öğesidir; varsayım olarak bırakılmaz, sınanır.
     */
    private void fikstureGuven(Ad askidaki, Point merkez) {
        Coordinate ilanNoktasi = askidaki.getLocation().getCoordinate();
        Coordinate kaynak = merkez.getCoordinate();
        assertThat(new double[]{ilanNoktasi.x, ilanNoktasi.y})
                .withFailMessage("""
                        FİKSTÜR BOZUK: askıya alınmış ilan kaynak noktada değil.
                        kaynak = (%s, %s)   ilan = (%s, %s)

                        Bu testin asıl iddiası ancak askıdaki ilan yarıçapın
                        İÇİNDEYKEN anlamlıdır; dışarıdayken zaten dönmez ve test
                        'suspended' hakkında hiçbir şey kanıtlamaz.""",
                        kaynak.x, kaynak.y, ilanNoktasi.x, ilanNoktasi.y)
                .containsExactly(kaynak.x, kaynak.y);
    }

    private List<Long> adayKimlikleri(Point merkez) {
        return adRepository.findAiCandidates(
                        BASKA_BIR_ILAN,
                        Ad.AdType.LOST.name(),
                        merkez,
                        YARICAP_METRE,
                        Instant.now().minus(PENCERE_GUN, ChronoUnit.DAYS))
                .stream()
                .map(AiCandidateRow::getAdId)
                .toList();
    }

    private Ad kaydet(Ad ad) {
        return entityManager.persist(ad);
    }

    /**
     * Sorgunun TÜM koşullarını sağlayan aday: aktif, karşıt tip, analizi bitmiş,
     * vektörü dolu, pencere içinde (createdAt otomatik "şimdi"), konumlu.
     * Böylece testte sağlanmayan tek değişken {@code suspended} kalır.
     */
    private Ad ilan(Point konum, boolean askida) {
        return Ad.builder()
                .title(askida ? "askıdaki test ilanı" : "normal test ilanı")
                .species(Species.CAT)          // @Builder.Default'ı olmayan tek zorunlu alan
                .adType(Ad.AdType.LOST)
                .aiStatus(AiStatus.DONE)
                .aiEmbeddings(List.of(new Ad.AiPhotoVector("s3://test/1.jpg", new float[]{0.1f, 0.2f})))
                .location(konum)
                .active(true)
                .suspended(askida)
                .build();
    }

    private Point nokta(double boylam, double enlem) {
        // DİKKAT: Coordinate(boylam, enlem) — boylam ÖNCE.
        return geometryFactory.createPoint(new Coordinate(boylam, enlem));
    }

    /**
     * Bağlam yüklenmeden ÖNCE koşar (bkz. sınıf javadoc'u). Bu yüzden
     * {@code org.junit.jupiter.api.condition.EnabledIf} kullanılır;
     * Spring'in SpEL tabanlı {@code EnabledIf}'i bağlamı yüklemek zorunda
     * olduğu için burada amacı tersine çevirirdi (atlamak yerine hata).
     */
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

    /** Spring'in önceliğiyle aynı: SPRING_DATASOURCE_* önce, sonra DB_*, sonra varsayılan. */
    private static String ortam(String once, String sonra, String varsayilan) {
        String deger = System.getenv(once);
        if (deger == null || deger.isBlank()) {
            deger = System.getenv(sonra);
        }
        return (deger == null || deger.isBlank()) ? varsayilan : deger;
    }
}
