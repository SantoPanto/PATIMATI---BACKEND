package com.works.patimati.service.impl;

import com.works.patimati.dto.admin.InstitutionAssignmentRequest;
import com.works.patimati.entity.User;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.AdComplaintRepository;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.AdoptionComplaintRepository;
import com.works.patimati.repository.PotentialMatchRepository;
import com.works.patimati.repository.UserComplaintRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.repository.external.ExternalPetRecordRepository;
import com.works.patimati.repository.external.ExternalSourceMediaRepository;
import com.works.patimati.repository.external.ExternalSourcePostRepository;
import com.works.patimati.service.AdService;
import com.works.patimati.service.ReverseGeocodingService;
import com.works.patimati.storage.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Kullanıcıyı kurum (belediye) hesabına yükseltme —
 * {@code PUT /api/admin/users/{id}/institution} arkasındaki iş.
 *
 * <p>NEDEN VAR: bu, belediye modülünün TEK giriş kapısı. Kurum hesabı serbest
 * kayıtla açılmıyor (plan §8), çünkü kendini belediye ilan edebilen bir hesap
 * o ilçenin bütün ihbar ve ilan verisini okurdu. Dolayısıyla burada yapılan
 * hata, doğrudan yetki hatasıdır.
 *
 * <p>Ölçülen üç davranış: rol gerçekten yükseliyor mu · ilçe kırpılarak
 * saklanıyor mu (panel sorgusu {@code ads.district} ile kıyaslayacak) · ve
 * <b>yönetici rolü korunuyor mu</b> — bir ADMIN'i INSTITUTION'a çevirmek onu
 * kendi yönetici panelinden atardı.
 */
class KurumaYukseltmeTest {

    private UserRepository userRepository;
    private AdminServiceImpl adminService;

    @BeforeEach
    void kur() {
        userRepository = mock(UserRepository.class);

        adminService = new AdminServiceImpl(
                userRepository,
                mock(AdRepository.class),
                mock(AdComplaintRepository.class),
                mock(UserComplaintRepository.class),
                mock(AdoptionComplaintRepository.class),
                mock(AdService.class),
                mock(ExternalSourcePostRepository.class),
                mock(ExternalPetRecordRepository.class),
                mock(ExternalSourceMediaRepository.class),
                mock(PotentialMatchRepository.class),
                mock(ImageStorageService.class),
                mock(ReverseGeocodingService.class)
        );
    }

    private User kullanici(User.Role rol) {
        User kullanici = new User();
        kullanici.setUid(12L);
        kullanici.setEmail("hesap@patimati.local");
        kullanici.setEnabled(true);
        kullanici.setRole(rol);
        when(userRepository.findById(12L)).thenReturn(Optional.of(kullanici));
        return kullanici;
    }

    private User kaydedilen() {
        ArgumentCaptor<User> yakalayici = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(yakalayici.capture());
        return yakalayici.getValue();
    }

    @Test
    @DisplayName("Sıradan kullanıcı INSTITUTION rolüne yükseliyor ve alanları doluyor")
    void kullanici_kuruma_yukseliyor() {
        kullanici(User.Role.USER);

        adminService.assignInstitution(12L, new InstitutionAssignmentRequest(
                "Nilüfer Belediyesi", "Bursa", "Nilüfer"));

        User kaydedilen = kaydedilen();
        assertThat(kaydedilen.getRole()).isEqualTo(User.Role.INSTITUTION);
        assertThat(kaydedilen.getInstitutionName()).isEqualTo("Nilüfer Belediyesi");
        assertThat(kaydedilen.getInstitutionCity()).isEqualTo("Bursa");
        assertThat(kaydedilen.getInstitutionDistrict()).isEqualTo("Nilüfer");
    }

    @Test
    @DisplayName("İlçe kırpılarak saklanıyor — baştaki/sondaki boşluk kıyası bozardı")
    void ilce_kirpiliyor() {
        kullanici(User.Role.USER);

        adminService.assignInstitution(12L, new InstitutionAssignmentRequest(
                "  Nilüfer Belediyesi  ", "  Bursa ", "  Nilüfer  "));

        User kaydedilen = kaydedilen();
        assertThat(kaydedilen.getInstitutionDistrict())
                .withFailMessage("""
                        İlçe boşluklarıyla saklandı. Panel sorgusu bunu
                        ads.district ile kıyaslayacak; " Nilüfer " hiçbir ilanla
                        eşleşmez ve panel sessizce boş görünür.""")
                .isEqualTo("Nilüfer");
        assertThat(kaydedilen.getInstitutionCity()).isEqualTo("Bursa");
        assertThat(kaydedilen.getInstitutionName()).isEqualTo("Nilüfer Belediyesi");
    }

    @Test
    @DisplayName("ADMIN rolü KORUNUYOR — yönetici kendi panelinden atılmıyor")
    void yonetici_rolu_korunuyor() {
        kullanici(User.Role.ADMIN);

        adminService.assignInstitution(12L, new InstitutionAssignmentRequest(
                "Nilüfer Belediyesi", "Bursa", "Nilüfer"));

        User kaydedilen = kaydedilen();
        assertThat(kaydedilen.getRole())
                .withFailMessage("""
                        Yöneticinin rolü INSTITUTION'a çevrildi. O hesap artık
                        /api/admin/** uçlarına giremez: paneli denemek isteyen
                        yönetici, yönetici panelini kaybeder. ADMIN'de yalnız
                        kurum alanları dolmalı.""")
                .isEqualTo(User.Role.ADMIN);
        assertThat(kaydedilen.getInstitutionDistrict()).isEqualTo("Nilüfer");
    }

    @Test
    @DisplayName("Olmayan kullanıcı için 404 (ResourceNotFound) ve kayıt yapılmıyor")
    void olmayan_kullanici() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.assignInstitution(999L,
                new InstitutionAssignmentRequest("X Belediyesi", "Bursa", "Nilüfer")))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
