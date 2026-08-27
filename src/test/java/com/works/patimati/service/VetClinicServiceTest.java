package com.works.patimati.service;

import com.works.patimati.dto.admin.CreateVetAccountRequest;
import com.works.patimati.dto.vet.VetClinicResponse;
import com.works.patimati.dto.vet.VetClinicUpsertRequest;
import com.works.patimati.entity.User;
import com.works.patimati.entity.VetClinic;
import com.works.patimati.entity.enums.AnimalType;
import com.works.patimati.exception.BusinessException;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.repository.VetClinicRepository;
import com.works.patimati.repository.VetClinicReviewRepository;
import com.works.patimati.storage.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    private VetClinicReviewRepository vetClinicReviewRepository;
    private VetClinicService service;
    private User vet;

    @BeforeEach
    void hazirla() {
        vetClinicRepository = mock(VetClinicRepository.class);
        userRepository = mock(UserRepository.class);
        imageStorageService = mock(ImageStorageService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        vetClinicReviewRepository = mock(VetClinicReviewRepository.class);
        service = new VetClinicService(vetClinicRepository, userRepository, imageStorageService, passwordEncoder,
                vetClinicReviewRepository);

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
                        "0312 000 00 00", "09:00-18:00", null, null, null),
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
                new VetClinicUpsertRequest("Yeni İsim", "Yeni Adres", "İstanbul", null, "0212 111 11 11", null,
                        null, null, null),
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

    @Test
    void konumluGuncellemePointDogruYazilir() {
        when(vetClinicRepository.findByUser_Uid(7L)).thenReturn(Optional.empty());

        VetClinicResponse yanit = service.upsertMine(EPOSTA,
                new VetClinicUpsertRequest("Pati Veteriner", "Örnek Mah. 1. Sk. No:1", "Ankara", "Çankaya",
                        "0312 000 00 00", "09:00-18:00",
                        new BigDecimal("39.92"), new BigDecimal("32.85"), null),
                null);

        ArgumentCaptor<VetClinic> kaydedilen = ArgumentCaptor.forClass(VetClinic.class);
        verify(vetClinicRepository).save(kaydedilen.capture());

        Point konum = kaydedilen.getValue().getLocation();
        assertThat(konum).isNotNull();
        assertThat(konum.getY()).isEqualTo(39.92);
        assertThat(konum.getX()).isEqualTo(32.85);
        assertThat(yanit.latitude()).isEqualTo(39.92);
        assertThat(yanit.longitude()).isEqualTo(32.85);
    }

    @Test
    void latLngGonderilmedenGuncellemeKonumKorunur() {
        Point mevcutKonum = new GeometryFactory(new PrecisionModel(), 4326)
                .createPoint(new Coordinate(32.85, 39.92));
        VetClinic mevcut = VetClinic.builder()
                .id(3L).user(vet).name("Eski İsim").address("Eski Adres").city("İstanbul")
                .phone("0212 000 00 00").location(mevcutKonum)
                .build();
        when(vetClinicRepository.findByUser_Uid(7L)).thenReturn(Optional.of(mevcut));

        VetClinicResponse yanit = service.upsertMine(EPOSTA,
                new VetClinicUpsertRequest("Yeni İsim", "Yeni Adres", "İstanbul", null, "0212 111 11 11", null,
                        null, null, null),
                null);

        assertThat(mevcut.getLocation()).isSameAs(mevcutKonum);
        assertThat(yanit.latitude()).isEqualTo(39.92);
        assertThat(yanit.longitude()).isEqualTo(32.85);
    }

    @Test
    void hayvanTurleriRoundTrip() {
        when(vetClinicRepository.findByUser_Uid(7L)).thenReturn(Optional.empty());

        VetClinicResponse yanit = service.upsertMine(EPOSTA,
                new VetClinicUpsertRequest("Pati Veteriner", "Örnek Mah. 1. Sk. No:1", "Ankara", "Çankaya",
                        "0312 000 00 00", "09:00-18:00",
                        null, null, Set.of(AnimalType.DOG, AnimalType.CAT)),
                null);

        assertThat(yanit.animalTypes()).containsExactlyInAnyOrder(AnimalType.DOG, AnimalType.CAT);
    }

    @Test
    void toResponseOrtalamaPuanVeSayiyiIcerir() {
        VetClinic mevcut = VetClinic.builder()
                .id(3L).user(vet).name("İsim").address("Adres").city("Ankara").phone("0312 000 00 00")
                .build();
        when(vetClinicRepository.findByUser_Uid(7L)).thenReturn(Optional.of(mevcut));
        when(vetClinicReviewRepository.findAverageRating(3L)).thenReturn(4.5);
        when(vetClinicReviewRepository.countByVetClinic_Id(3L)).thenReturn(2L);

        VetClinicResponse yanit = service.getMine(EPOSTA);

        assertThat(yanit.averageRating()).isEqualTo(4.5);
        assertThat(yanit.reviewCount()).isEqualTo(2);
    }

    @Test
    void hicYorumYokkenOrtalamaPuanNullDoner() {
        VetClinic mevcut = VetClinic.builder()
                .id(3L).user(vet).name("İsim").address("Adres").city("Ankara").phone("0312 000 00 00")
                .build();
        when(vetClinicRepository.findByUser_Uid(7L)).thenReturn(Optional.of(mevcut));
        when(vetClinicReviewRepository.findAverageRating(3L)).thenReturn(null);
        when(vetClinicReviewRepository.countByVetClinic_Id(3L)).thenReturn(0L);

        VetClinicResponse yanit = service.getMine(EPOSTA);

        assertThat(yanit.averageRating()).isNull();
        assertThat(yanit.reviewCount()).isZero();
    }
}
