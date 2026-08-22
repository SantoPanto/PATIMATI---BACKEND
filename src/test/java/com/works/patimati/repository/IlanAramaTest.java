package com.works.patimati.repository;

import com.works.patimati.entity.Ad;
import com.works.patimati.entity.enums.Species;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * B5: public ilan araması ({@code searchPublicActiveAds}) gerçek veritabanında.
 *
 * <p><b>Neden gerçek veritabanı:</b> iddia JPQL'in davranışı hakkında —
 * LIKE'ın üç alanda (başlık, ırk, açıklama) OR ile bağlanması, LOWER
 * katlaması ve aktif/askıda süzgeçlerinin araca "VE" ile bağlı kalması.
 * Servis testindeki mock bu katmanı hiç koşturmaz; sorgu metnindeki bir
 * yazım hatası ancak burada patlar.
 *
 * <p><b>Fikstür kısıtı:</b> geliştirme veritabanında GERÇEK ilanlar var.
 * Bu yüzden aranan terimler gerçek veride geçmeyecek benzersiz damgalar
 * taşır ve iddialar hiçbir zaman "tam N sonuç" demez — yalnız fikstür
 * kimlikleri üzerinden {@code contains} / {@code doesNotContain} der.
 *
 * <p><b>Bilinen sınır:</b> LOWER katlaması veritabanının kendi yereliyle
 * çalışır; Türkçe İ/ı kenarı (ör. "izmir" ↔ "İzmir") burada bilerek
 * SINANMAZ — ASCII büyük/küçük katlaması sınanır. Sınır PR gövdesinde ve
 * repository yorumunda yazılıdır.
 *
 * <p>Şema güvenliği ve atlama koşulu {@link AiAdayAskiyaAlmaTest} ile aynı
 * gerekçelere dayanır: {@code ddl-auto=none}, veritabanı yoksa test atlar —
 * "Skipped" sayısına bakılmadan kapı yeşil sayılmaz.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=none")
@EnabledIf("veritabaniUlasilabilir")
class IlanAramaTest {

    private static final Pageable SAYFA = PageRequest.of(0, 50);

    @Autowired
    private AdRepository adRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Arama başlık, ırk ve açıklamanın ÜÇÜNDE de tutmalı; tutmayan dönmemeli")
    void baslikIrkVeAciklamadanArar() {
        // Damga gerçek veride geçmesin diye bilerek anlamsız; büyük/küçük
        // karışık yazılıp küçük harfle aranarak ASCII katlaması da sınanıyor.
        Ad baslikta = kaydet(ilan("KaYiPzZb5 mahallesinde kayboldu", "MIXED_OR_UNKNOWN", "açıklama düz"));
        Ad irkta = kaydet(ilan("alakasız başlık", "kayipzzb5-cinsi", "açıklama düz"));
        Ad aciklamada = kaydet(ilan("alakasız başlık", "MIXED_OR_UNKNOWN",
                "Son görüldüğü yer: Merkez, KAYIPZZB5, Türkiye"));
        Ad tutmayan = kaydet(ilan("bambaşka ilan", "MIXED_OR_UNKNOWN", "bambaşka açıklama"));
        entityManager.flush();

        List<Long> bulunanlar = kimlikler("kayipzzb5");

        assertThat(bulunanlar)
                .withFailMessage("""
                        Arama üç alanın birinde tutmadı.
                        beklenen: başlık=%s ırk=%s açıklama=%s hepsi listede
                        dönen: %s""",
                        baslikta.getId(), irkta.getId(), aciklamada.getId(), bulunanlar)
                .contains(baslikta.getId(), irkta.getId(), aciklamada.getId());

        assertThat(bulunanlar)
                .withFailMessage(
                        "Terimi hiçbir alanında taşımayan ilan (id %s) arama sonucuna girdi: %s",
                        tutmayan.getId(), bulunanlar)
                .doesNotContain(tutmayan.getId());
    }

    @Test
    @DisplayName("Askıdaki ve yayından kalkmış ilan, terim tutsa bile aramada dönmemeli")
    void askidakiVePasifIlanAramadaDonmez() {
        Ad gorunur = kaydet(ilan("AskiZzB5 kayıp", "MIXED_OR_UNKNOWN", null));
        Ad askida = kaydet(ilan("AskiZzB5 kayıp", "MIXED_OR_UNKNOWN", null));
        askida.setSuspended(true);
        Ad pasif = kaydet(ilan("AskiZzB5 kayıp", "MIXED_OR_UNKNOWN", null));
        pasif.setActive(false);
        entityManager.flush();

        List<Long> bulunanlar = kimlikler("askizzb5");

        // Pozitif kontrol aynı çıktıda: görünür ilan dönüyor ki "hiçbir şey
        // dönmedi" ile "doğru süzüldü" ayırt edilebilsin.
        assertThat(bulunanlar).contains(gorunur.getId());
        assertThat(bulunanlar)
                .withFailMessage("""
                        Halka kapalı ilan arama sonucuna girdi (askıda=%s, pasif=%s, dönen=%s).
                        Arama sorgusundaki active/suspended süzgeci OR'a karışmış olabilir.""",
                        askida.getId(), pasif.getId(), bulunanlar)
                .doesNotContain(askida.getId(), pasif.getId());
    }

    @Test
    @DisplayName("Tür süzgeci aramayla birlikte çalışmalı")
    void turSuzgeciAramaylaBirlikteCalisir() {
        Ad kayip = kaydet(ilan("TurZzB5 ilanı", "MIXED_OR_UNKNOWN", null));
        Ad bulundu = kaydet(ilan("TurZzB5 ilanı", "MIXED_OR_UNKNOWN", null));
        bulundu.setAdType(Ad.AdType.FOUND);
        entityManager.flush();

        List<Long> bulunanlar = adRepository
                .searchPublicActiveAdsByAdType(Ad.AdType.FOUND, "turzzb5", SAYFA)
                .map(Ad::getId)
                .toList();

        assertThat(bulunanlar).contains(bulundu.getId());
        assertThat(bulunanlar)
                .withFailMessage(
                        "LOST ilan (id %s) FOUND süzgeçli aramada döndü: %s",
                        kayip.getId(), bulunanlar)
                .doesNotContain(kayip.getId());
    }

    // ---------------------------------------------------------------------
    // Yardımcılar
    // ---------------------------------------------------------------------

    private List<Long> kimlikler(String terim) {
        return adRepository.searchPublicActiveAds(terim, SAYFA)
                .map(Ad::getId)
                .toList();
    }

    private Ad kaydet(Ad ad) {
        return entityManager.persist(ad);
    }

    /** Sorgunun süzgeçlerini sağlayan en yalın public ilan: aktif, askıda değil. */
    private Ad ilan(String baslik, String irk, String aciklama) {
        return Ad.builder()
                .title(baslik)
                .breed(irk)
                .description(aciklama)
                .species(Species.CAT)
                .adType(Ad.AdType.LOST)
                .active(true)
                .suspended(false)
                .build();
    }

    /**
     * Bağlam yüklenmeden önce koşar; gerekçe ve ortam adları
     * {@link AiAdayAskiyaAlmaTest} ile birebir aynı (her repo testi kendi
     * kopyasını taşıyor — JUnit koşul metodu test sınıfının kendisinde arar).
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
