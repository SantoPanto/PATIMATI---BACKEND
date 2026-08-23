package com.works.patimati.repository;

import com.works.patimati.entity.Ad;
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
 * {@code ads.description} GERÇEKTEN uzun metin tutabiliyor mu — şemanın
 * kendisine sorulur.
 *
 * <p><b>Ölçülen arıza (24.08.2026, canlı):</b> sütun canlıda hâlâ
 * {@code character varying(255)}. V18 tam olarak bunu {@code TEXT}'e
 * çevirmek için yazılmıştı ama canlı şemada etkisi yok. DTO ise 3000
 * karaktere izin veriyor ({@code @Size(max = 3000)}) ⇒ 255'i aşan her
 * açıklama {@code value too long for type character varying(255)} ile
 * düşüyor, kullanıcı da şunu görüyor:
 * "Gönderilen verilerden biri kaydedilemedi: bir alan izin verilen sınırı
 * aşıyor ya da beklenen biçimde değil."
 *
 * <p><b>Neden birim testi yakalayamazdı:</b> kusur kodda DEĞİL, ortamın
 * şemasında. Uygulama kodu, DTO doğrulaması ve tüm sahte-veritabanlı testler
 * yeşil yanarken ürün kırıktı. Yakalayan tek şey şemayı gerçek veritabanına
 * sormaktır.
 *
 * <p><b>İki ayrı iddia, bilerek:</b> ① sütunun TİPİ ({@code information_schema}
 * ne diyor) ② uzun bir açıklamanın gerçekten YAZILIP OKUNABİLMESİ. Tip
 * doğruysa ama araya bir CHECK/trigger girerse ikincisi yakalar; yazma
 * çalışıp tip {@code varchar(4000)} gibi bir şeye çekilirse birincisi
 * yakalar.
 *
 * <p>⚠ Yerelde parola verilmezse test ATLANIR — "Skipped" sayısına bakmadan
 * "kapı yeşil" denmez ({@code EslesmeKaydiSemaTest} ile aynı kanca).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=none")
@EnabledIf("veritabaniUlasilabilir")
class AciklamaSutunuTipiTest {

    private static final int WGS_84_SRID = 4326;

    /** Sahadaki sahiplendirme açıklaması birleştirilerek üretiliyor; 255 kolay aşılıyor. */
    private static final int UZUN_ACIKLAMA = 1200;

    private final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), WGS_84_SRID);

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("ads.description sütunu TEXT olmalı (canlıda varchar(255) kalmıştı)")
    void aciklamaSutunuTextOlmali() {
        Object tip = entityManager.getEntityManager()
                .createNativeQuery("""
                        SELECT data_type
                        FROM information_schema.columns
                        WHERE table_name = 'ads' AND column_name = 'description'
                        """)
                .getSingleResult();

        assertThat(String.valueOf(tip))
                .as("ads.description tipi — varchar(255) kalırsa uzun açıklamalı "
                        + "her ilan 'value too long' ile düşer")
                .isEqualTo("text");
    }

    @Test
    @DisplayName("255 karakterden uzun açıklama yazılıp geri okunabilmeli")
    void uzunAciklamaYazilipOkunabilmeli() {
        User sahip = entityManager.persistFlushFind(kullanici());

        String aciklama = "ö".repeat(UZUN_ACIKLAMA);
        Ad kaydedilen = entityManager.persistFlushFind(ilan(sahip, aciklama));
        entityManager.clear();

        Ad okunan = entityManager.find(Ad.class, kaydedilen.getId());

        assertThat(okunan.getDescription()).hasSize(UZUN_ACIKLAMA);
        assertThat(okunan.getDescription()).isEqualTo(aciklama);
    }

    private User kullanici() {
        return User.builder()
                .email("aciklama-olcum-" + System.nanoTime() + "@ornek.com")
                .password("x")
                .firstName("Ölçüm")
                .lastName("Kullanıcı")
                .role(User.Role.USER)
                .build();
    }

    private Ad ilan(User sahip, String aciklama) {
        return Ad.builder()
                .title("Açıklama uzunluğu ölçümü")
                .description(aciklama)
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
    // Veritabanı koşulu — EslesmeKaydiSemaTest'in ikizi (bkz. o sınıfın javadoc'u)
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
