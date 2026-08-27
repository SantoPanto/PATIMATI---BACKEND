package com.works.patimati.service;

import com.works.patimati.dto.request.BusinessApplicationCreateRequest;
import com.works.patimati.dto.response.BusinessApplicationResponse;
import com.works.patimati.entity.User;
import com.works.patimati.entity.VetClinic;
import com.works.patimati.entity.enums.AnimalType;
import com.works.patimati.entity.enums.BusinessApplicationStatus;
import com.works.patimati.entity.enums.BusinessType;
import com.works.patimati.exception.BusinessException;
import com.works.patimati.repository.VetClinicRepository;
import com.works.patimati.storage.ImageStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * İşletme başvuru akışı (#175/#186, Mert) — 27.08 incelemesinin bekçileri.
 *
 * <p>Modül birleştiğinde HİÇ testi yoktu ve canlıya gidiyor. Buradaki vakalar
 * incelemede bulunan üç kusuru da kilitler:
 * <ol>
 *   <li>Kurum (belediye) hesabı başvurabiliyordu; onay rolün üstüne yazdığı
 *       için belediye yetkisi sessizce silinirdi.</li>
 *   <li>Onay VET kartına, yüklenmiş başvurunun {@code PersistentSet}'ini
 *       veriyordu — Hibernate'te "shared references to a collection" adayı;
 *       akış GERÇEK DB'de uçtan uca hiç koşmamıştı.</li>
 *   <li>Admin listesinde yalnız-tür süzgeci durumu sessizce BEKLEMEDE'ye
 *       zorluyordu.</li>
 * </ol>
 *
 * <p>⚠ Yerelde parola verilmezse test ATLANIR — Skipped sayısına bakmadan
 * "kapı yeşil" denmez.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=none")
@EnabledIf("veritabaniUlasilabilir")
@Import(BusinessApplicationService.class)
class IsletmeBasvurusuGercekDbTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BusinessApplicationService service;

    @Autowired
    private VetClinicRepository vetClinicRepository;

    /** Bildirim/FCM ve S3 bu ölçümün konusu değil — sahte. */
    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private ImageStorageService imageStorageService;

    @Test
    @DisplayName("onay: rol VET olur, kart başvuru bilgileriyle (animalTypes dahil) GERÇEK DB'de oluşur")
    void onayRoluDegistirirVeKartiOlusturur() {
        User basvuran = entityManager.persistFlushFind(kullanici("basvuran", User.Role.USER));
        User admin = entityManager.persistFlushFind(kullanici("admin", User.Role.ADMIN));

        BusinessApplicationResponse basvuru =
                service.submit(basvuran.getEmail(), istek(BusinessType.VET), null);
        entityManager.flush();
        entityManager.clear(); // onay, başvuruyu DB'den yüklesin — PersistentSet yolu gerçek olsun

        service.approve(basvuru.id(), admin.getEmail());
        entityManager.flush();

        User guncel = entityManager.find(User.class, basvuran.getUid());
        assertThat(guncel.getRole()).isEqualTo(User.Role.VET);

        VetClinic kart = vetClinicRepository.findByUser_Uid(basvuran.getUid()).orElseThrow();
        assertThat(kart.getName()).isEqualTo("Ölçüm Veteriner Kliniği");
        assertThat(kart.getCity()).isEqualTo("Bursa");
        assertThat(kart.getAnimalTypes())
                .containsExactlyInAnyOrder(AnimalType.CAT, AnimalType.DOG);
    }

    @Test
    @DisplayName("kurum (belediye) hesabı başvuramaz — rolü ONAYLA silinemez")
    void kurumHesabiBasvuramaz() {
        User kurum = entityManager.persistFlushFind(kullanici("kurum", User.Role.INSTITUTION));

        assertThatThrownBy(() -> service.submit(kurum.getEmail(), istek(BusinessType.VET), null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Kurum");
    }

    @Test
    @DisplayName("başvuru ile onay arasında rol değiştiyse onay ROLÜN ÜSTÜNE YAZMAZ, açık hata verir")
    void onayAnindaRolKaymasiYakalanir() {
        User basvuran = entityManager.persistFlushFind(kullanici("kayan", User.Role.USER));
        User admin = entityManager.persistFlushFind(kullanici("admin2", User.Role.ADMIN));

        BusinessApplicationResponse basvuru =
                service.submit(basvuran.getEmail(), istek(BusinessType.PETSHOP), null);
        entityManager.flush();

        // Arada hesap kuruma yükseltildi (gerçek senaryo: admin "Kuruma yükselt")
        basvuran.setRole(User.Role.INSTITUTION);
        entityManager.persistAndFlush(basvuran);
        entityManager.clear();

        assertThatThrownBy(() -> service.approve(basvuru.id(), admin.getEmail()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("rolü bu arada değişmiş");

        assertThat(entityManager.find(User.class, basvuran.getUid()).getRole())
                .as("rol PETSHOP'a EZİLMEMELİ")
                .isEqualTo(User.Role.INSTITUTION);
    }

    @Test
    @DisplayName("bekleyen başvuru varken ikinci başvuru reddedilir")
    void bekleyenVarkenIkinciBasvuruReddedilir() {
        User basvuran = entityManager.persistFlushFind(kullanici("cift", User.Role.USER));

        service.submit(basvuran.getEmail(), istek(BusinessType.BARINAK), null);
        entityManager.flush();

        assertThatThrownBy(() -> service.submit(basvuran.getEmail(), istek(BusinessType.VET), null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("incelenmekte");
    }

    @Test
    @DisplayName("yalnız-tür süzgeci karara bağlanmış başvuruları da getirir (BEKLEMEDE'ye zorlanmaz)")
    void yalnizTurSuzgeciKararlilariDaGetirir() {
        User basvuran = entityManager.persistFlushFind(kullanici("suzgec", User.Role.USER));
        User admin = entityManager.persistFlushFind(kullanici("admin3", User.Role.ADMIN));

        BusinessApplicationResponse basvuru =
                service.submit(basvuran.getEmail(), istek(BusinessType.VET), null);
        entityManager.flush();
        service.reject(basvuru.id(), admin.getEmail(), "Ölçüm reddi");
        entityManager.flush();

        var yalnizTur = service.listForAdmin(null, BusinessType.VET, PageRequest.of(0, 50));

        assertThat(yalnizTur.getContent())
                .as("durum verilmeden tür süzülünce REDDEDILDI kayıt da dönmeli")
                .anyMatch(b -> b.id().equals(basvuru.id())
                        && b.status() == BusinessApplicationStatus.REDDEDILDI);
    }

    // ------------------------------------------------------------------

    private BusinessApplicationCreateRequest istek(BusinessType tur) {
        Set<AnimalType> hayvanlar = new LinkedHashSet<>();
        hayvanlar.add(AnimalType.CAT);
        hayvanlar.add(AnimalType.DOG);
        return new BusinessApplicationCreateRequest(
                tur,
                "Ölçüm Veteriner Kliniği",
                "Ölçüm Mah. Deneme Sok. No:1",
                "Bursa",
                "Nilüfer",
                "0500 000 00 00",
                "09:00-18:00",
                new BigDecimal("40.21"),
                new BigDecimal("28.97"),
                hayvanlar
        );
    }

    private User kullanici(String on, User.Role rol) {
        return User.builder()
                .email(on + "-isletme-olcum-" + System.nanoTime() + "@ornek.com")
                .password("x")
                .firstName("Ölçüm")
                .lastName("Kullanıcı")
                .role(rol)
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
