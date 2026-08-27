package com.works.patimati.service;

import com.works.patimati.dto.complaint.MyComplaintResponse;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.AdComplaint;
import com.works.patimati.entity.AdoptionComplaint;
import com.works.patimati.entity.User;
import com.works.patimati.entity.UserComplaint;
import com.works.patimati.entity.enums.AiStatus;
import com.works.patimati.entity.enums.ComplaintReason;
import com.works.patimati.entity.enums.Species;
import com.works.patimati.repository.AdComplaintRepository;
import com.works.patimati.repository.AdoptionComplaintRepository;
import com.works.patimati.repository.UserComplaintRepository;
import com.works.patimati.repository.UserRepository;
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
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * "Şikayetlerim" birleşik listesi (S7) GERÇEK PostgreSQL'de doğru mu?
 *
 * <p>İddialar: üç tablo tek listede birleşir; YALNIZ oturum sahibinin
 * kayıtları döner (başkasının şikayeti sızmaz); {@code tur} ayrımı doğru;
 * liste en-yeni-üstte gelir.
 *
 * <p>⚠ Yerelde parola verilmezse test ATLANIR — Skipped sayısına bakmadan
 * "kapı yeşil" denmez.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=none")
@EnabledIf("veritabaniUlasilabilir")
@Import(MyComplaintsService.class)
class BenimSikayetlerimGercekDbTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MyComplaintsService service;

    @Autowired
    private UserComplaintRepository userComplaintRepository;
    @Autowired
    private AdComplaintRepository adComplaintRepository;
    @Autowired
    private AdoptionComplaintRepository adoptionComplaintRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("üç tablo birleşir, yalnız oturum sahibinin kayıtları döner, en yeni üstte")
    void ucTabloBirlesirYalnizSahibininkilerDoner() {
        User ben = entityManager.persistFlushFind(kullanici("benim"));
        User baskasi = entityManager.persistFlushFind(kullanici("baskasi"));

        // ad_complaints.ad_id ads'e FK'li — hedefler GERÇEK ilan olmalı
        // (ilk sürüm sahte id kullandı, gerçek DB FK ile reddetti).
        Ad ilan = entityManager.persistFlushFind(ilan(baskasi, Ad.AdType.LOST));
        Ad sahiplendirmeIlani = entityManager.persistFlushFind(ilan(baskasi, Ad.AdType.ADOPTION));
        Ad baskasininHedefi = entityManager.persistFlushFind(ilan(ben, Ad.AdType.FOUND));

        userComplaintRepository.save(UserComplaint.builder()
                .reporterId(ben.getUid()).reportedUserId(baskasi.getUid())
                .reason(ComplaintReason.KOTU_DIL_KULLANIMI).description("kullanıcı şikayeti").build());
        adComplaintRepository.save(AdComplaint.builder()
                .reporterId(ben.getUid()).adId(ilan.getId())
                .reason(ComplaintReason.SAHTE_ILAN).description("ilan şikayeti").build());
        adoptionComplaintRepository.save(AdoptionComplaint.builder()
                .reporterId(ben.getUid()).adId(sahiplendirmeIlani.getId())
                .reason(ComplaintReason.UYGUNSUZ_ICERIK).description("sahiplendirme şikayeti").build());
        // Sızma kontrolü: başka kullanıcının şikayeti listeye GİRMEMELİ.
        adComplaintRepository.save(AdComplaint.builder()
                .reporterId(baskasi.getUid()).adId(baskasininHedefi.getId())
                .reason(ComplaintReason.DOLANDIRICILIK).description("başkasının şikayeti").build());

        List<MyComplaintResponse> liste = service.benimSikayetlerim(ben.getEmail());

        assertThat(liste).hasSize(3);
        assertThat(liste).extracting(MyComplaintResponse::tur)
                .containsExactlyInAnyOrder("KULLANICI", "ILAN", "SAHIPLENDIRME");
        assertThat(liste).extracting(MyComplaintResponse::description)
                .doesNotContain("başkasının şikayeti");

        // En yeni üstte: ardışık her çift için createdAt azalmalı (eşitlik serbest).
        for (int i = 1; i < liste.size(); i++) {
            assertThat(liste.get(i - 1).createdAt())
                    .isAfterOrEqualTo(liste.get(i).createdAt());
        }

        // Hedefler tur'a göre doğru alandan geliyor.
        assertThat(liste).filteredOn(s -> s.tur().equals("ILAN"))
                .singleElement()
                .extracting(MyComplaintResponse::hedefId).isEqualTo(ilan.getId());
        assertThat(liste).filteredOn(s -> s.tur().equals("KULLANICI"))
                .singleElement()
                .extracting(MyComplaintResponse::hedefId).isEqualTo(baskasi.getUid());
    }

    private final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), 4326);

    private Ad ilan(User sahip, Ad.AdType tur) {
        return Ad.builder()
                .title("Şikayet ölçüm ilanı")
                .description("Şikayet ölçümü")
                .species(Species.CAT)
                .adType(tur)
                .aiStatus(AiStatus.PENDING)
                .location(geometryFactory.createPoint(new Coordinate(28.97, 40.21)))
                .city("Ölçüm")
                .district("SikayetOlcumIlcesi")
                .photoUrls(java.util.List.of("s3://patimati/test/sikayet.jpg"))
                .user(sahip)
                .active(true)
                .suspended(false)
                .build();
    }

    private User kullanici(String on) {
        return User.builder()
                .email(on + "-sikayet-olcum-" + System.nanoTime() + "@ornek.com")
                .password("x")
                .firstName("Ölçüm")
                .lastName("Kullanıcı")
                .role(User.Role.USER)
                .build();
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
