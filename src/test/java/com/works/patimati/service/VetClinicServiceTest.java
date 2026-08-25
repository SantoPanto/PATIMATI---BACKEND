package com.works.patimati.service;

import com.works.patimati.dto.admin.CreateVetAccountRequest;
import com.works.patimati.dto.vet.VetClinicResponse;
import com.works.patimati.dto.vet.VetClinicUpsertRequest;
import com.works.patimati.entity.User;
import com.works.patimati.entity.VetClinic;
import com.works.patimati.exception.BusinessException;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.repository.VetClinicRepository;
import com.works.patimati.storage.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VetClinicServiceTest {

    private static final String EPOSTA = "vet@ornek.com";

    private VetClinicRepository vetClinicRepository;
    private UserRepository userRepository;
    private ImageStorageService imageStorageService;
    private PasswordEncoder passwordEncoder;
    private VetClinicService service;
    private User vet;

    @BeforeEach
    void hazirla() {
        vetClinicRepository = mock(VetClinicRepository.class);
        userRepository = mock(UserRepository.class);
        imageStorageService = mock(ImageStorageService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        service = new VetClinicService(vetClinicRepository, userRepository, imageStorageService, passwordEncoder);

        vet = User.builder().uid(7L).email(EPOSTA).role(User.Role.VET).build();
        when(userRepository.findByEmail(EPOSTA)).thenReturn(Optional.of(vet));
        when(vetClinicRepository.save(any(VetClinic.class))).thenAnswer(cagri -> cagri.getArgument(0));
    }

    @Test
    void kartYokkenGetirmeKaynakYokHatasiVerir() {
        when(vetClinicRepository.findByUser_Uid(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMine(EPOSTA))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void ilkKayitFotografsizOlusturulur() {
        when(vetClinicRepository.findByUser_Uid(7L)).thenReturn(Optional.empty());

        VetClinicResponse yanit = service.upsertMine(EPOSTA,
                new VetClinicUpsertRequest("Pati Veteriner", "Örnek Mah. 1. Sk. No:1", "Ankara", "Çankaya",
                        "0312 000 00 00", "09:00-18:00"),
                null);

        ArgumentCaptor<VetClinic> kaydedilen = ArgumentCaptor.forClass(VetClinic.class);
        verify(vetClinicRepository).save(kaydedilen.capture());

        assertThat(kaydedilen.getValue().getUser()).isSameAs(vet);
        assertThat(kaydedilen.getValue().getName()).isEqualTo("Pati Veteriner");
        assertThat(kaydedilen.getValue().getPhotoReference()).isNull();
        assertThat(yanit.name()).isEqualTo("Pati Veteriner");
        assertThat(yanit.photoUrl()).isNull();
        verify(imageStorageService, never()).uploadImages(any(), anyString());
    }

    @Test
    void fotografliGuncellemeEskiFotografiSilerYeniyiYukler() {
        VetClinic mevcut = VetClinic.builder()
                .id(3L).user(vet).name("Eski İsim").address("Eski Adres").city("İstanbul")
                .phone("0212 000 00 00").photoReference("vet-clinics/eski.jpg")
                .build();
        when(vetClinicRepository.findByUser_Uid(7L)).thenReturn(Optional.of(mevcut));

        MockMultipartFile foto = new MockMultipartFile("photo", "yeni.jpg", "image/jpeg", new byte[]{1, 2, 3});
        when(imageStorageService.uploadImages(List.of(foto), "vet-clinics"))
                .thenReturn(List.of("vet-clinics/yeni.jpg"));
        when(imageStorageService.createTemporaryReadUrl("vet-clinics/yeni.jpg"))
                .thenReturn("https://cdn.test/vet-clinics/yeni.jpg");

        VetClinicResponse yanit = service.upsertMine(EPOSTA,
                new VetClinicUpsertRequest("Yeni İsim", "Yeni Adres", "İstanbul", null, "0212 111 11 11", null),
                foto);

        assertThat(mevcut.getPhotoReference()).isEqualTo("vet-clinics/yeni.jpg");
        assertThat(yanit.photoUrl()).isEqualTo("https://cdn.test/vet-clinics/yeni.jpg");
        verify(imageStorageService).deleteImages(List.of("vet-clinics/eski.jpg"));
    }

    @Test
    void eMailZatenKullanimdaysaHesapOlusturulmaz() {
        when(userRepository.findByEmail("dolu@ornek.com")).thenReturn(Optional.of(vet));

        assertThatThrownBy(() -> service.createVetAccount(
                new CreateVetAccountRequest("Ad", "Soyad", "dolu@ornek.com", "Sifre123")))
                .isInstanceOf(BusinessException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void gecerliIstekleVetRolundeHesapOlusturulur() {
        when(userRepository.findByEmail("yeni-vet@ornek.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Sifre123")).thenReturn("sifrelenmis");

        service.createVetAccount(new CreateVetAccountRequest("Ad", "Soyad", "yeni-vet@ornek.com", "Sifre123"));

        ArgumentCaptor<User> kaydedilen = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(kaydedilen.capture());
        assertThat(kaydedilen.getValue().getRole()).isEqualTo(User.Role.VET);
        assertThat(kaydedilen.getValue().getPassword()).isEqualTo("sifrelenmis");
        assertThat(kaydedilen.getValue().isEnabled()).isTrue();
    }
}
