package com.works.patimati.service;

import com.works.patimati.dto.admin.CreateShelterAccountRequest;
import com.works.patimati.dto.shelter.ShelterResponse;
import com.works.patimati.dto.shelter.ShelterUpsertRequest;
import com.works.patimati.entity.Shelter;
import com.works.patimati.entity.User;
import com.works.patimati.exception.BusinessException;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.ShelterRepository;
import com.works.patimati.repository.ShelterReviewRepository;
import com.works.patimati.repository.UserRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@code PetShopServiceTest} deseni (find-or-create-then-save + hesap
 * oluşturma) + {@code VetClinicService}'in puan hesaplama mantığı
 * ({@code resolveOwnerUid} ekstra -- barınağa özel, bkz. plan).
 */
class ShelterServiceTest {

    private static final String EPOSTA = "barinak@ornek.com";

    private ShelterRepository shelterRepository;
    private UserRepository userRepository;
    private ImageStorageService imageStorageService;
    private PasswordEncoder passwordEncoder;
    private ShelterReviewRepository shelterReviewRepository;
    private ShelterService service;
    private User shelterOwner;

    @BeforeEach
    void hazirla() {
        shelterRepository = mock(ShelterRepository.class);
        userRepository = mock(UserRepository.class);
        imageStorageService = mock(ImageStorageService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        shelterReviewRepository = mock(ShelterReviewRepository.class);
        service = new ShelterService(shelterRepository, userRepository, imageStorageService, passwordEncoder,
                shelterReviewRepository);

        shelterOwner = User.builder().uid(7L).email(EPOSTA).role(User.Role.BARINAK).build();
        when(userRepository.findByEmail(EPOSTA)).thenReturn(Optional.of(shelterOwner));
        when(shelterRepository.save(any(Shelter.class))).thenAnswer(cagri -> cagri.getArgument(0));
        when(shelterReviewRepository.findAverageRating(any())).thenReturn(null);
        when(shelterReviewRepository.countByShelter_Id(any())).thenReturn(0L);
    }

    @Test
    void kartYokkenGetirmeKaynakYokHatasiVerir() {
        when(shelterRepository.findByUser_Uid(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMine(EPOSTA))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void ilkKayitFotografsizOlusturulur() {
        when(shelterRepository.findByUser_Uid(7L)).thenReturn(Optional.empty());

        ShelterResponse yanit = service.upsertMine(EPOSTA,
                new ShelterUpsertRequest("Pati Barınağı", "Örnek Mah. 1. Sk. No:1", "Ankara", "Çankaya",
                        "0312 000 00 00", "09:00-18:00", null, null),
                null);

        ArgumentCaptor<Shelter> kaydedilen = ArgumentCaptor.forClass(Shelter.class);
        verify(shelterRepository).save(kaydedilen.capture());

        assertThat(kaydedilen.getValue().getUser()).isSameAs(shelterOwner);
        assertThat(kaydedilen.getValue().getName()).isEqualTo("Pati Barınağı");
        assertThat(kaydedilen.getValue().getPhotoReference()).isNull();
        assertThat(yanit.name()).isEqualTo("Pati Barınağı");
        assertThat(yanit.photoUrl()).isNull();
        assertThat(yanit.averageRating()).isNull();
        assertThat(yanit.reviewCount()).isZero();
        verify(imageStorageService, never()).uploadImages(any(), anyString());
    }

    @Test
    void fotografliGuncellemeEskiFotografiSilerYeniyiYukler() {
        Shelter mevcut = Shelter.builder()
                .id(3L).user(shelterOwner).name("Eski İsim").address("Eski Adres").city("İstanbul")
                .phone("0212 000 00 00").photoReference("shelters/eski.jpg")
                .build();
        when(shelterRepository.findByUser_Uid(7L)).thenReturn(Optional.of(mevcut));

        MockMultipartFile foto = new MockMultipartFile("photo", "yeni.jpg", "image/jpeg", new byte[]{1, 2, 3});
        when(imageStorageService.uploadImages(List.of(foto), "shelters"))
                .thenReturn(List.of("shelters/yeni.jpg"));
        when(imageStorageService.createTemporaryReadUrl("shelters/yeni.jpg"))
                .thenReturn("https://cdn.test/shelters/yeni.jpg");

        ShelterResponse yanit = service.upsertMine(EPOSTA,
                new ShelterUpsertRequest("Yeni İsim", "Yeni Adres", "İstanbul", null, "0212 111 11 11", null,
                        null, null),
                foto);

        assertThat(mevcut.getPhotoReference()).isEqualTo("shelters/yeni.jpg");
        assertThat(yanit.photoUrl()).isEqualTo("https://cdn.test/shelters/yeni.jpg");
        verify(imageStorageService).deleteImages(List.of("shelters/eski.jpg"));
    }

    @Test
    void eMailZatenKullanimdaysaHesapOlusturulmaz() {
        when(userRepository.findByEmail("dolu@ornek.com")).thenReturn(Optional.of(shelterOwner));

        assertThatThrownBy(() -> service.createShelterAccount(
                new CreateShelterAccountRequest("Ad", "Soyad", "dolu@ornek.com", "Sifre123")))
                .isInstanceOf(BusinessException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void gecerliIstekleBarinakRolundeHesapOlusturulur() {
        when(userRepository.findByEmail("yeni-barinak@ornek.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Sifre123")).thenReturn("sifrelenmis");

        service.createShelterAccount(new CreateShelterAccountRequest("Ad", "Soyad", "yeni-barinak@ornek.com", "Sifre123"));

        ArgumentCaptor<User> kaydedilen = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(kaydedilen.capture());
        assertThat(kaydedilen.getValue().getRole()).isEqualTo(User.Role.BARINAK);
        assertThat(kaydedilen.getValue().getPassword()).isEqualTo("sifrelenmis");
        assertThat(kaydedilen.getValue().isEnabled()).isTrue();
    }

    @Test
    void konumluGuncellemePointDogruYazilir() {
        when(shelterRepository.findByUser_Uid(7L)).thenReturn(Optional.empty());

        ShelterResponse yanit = service.upsertMine(EPOSTA,
                new ShelterUpsertRequest("Pati Barınağı", "Örnek Mah. 1. Sk. No:1", "Ankara", "Çankaya",
                        "0312 000 00 00", "09:00-18:00",
                        new BigDecimal("39.92"), new BigDecimal("32.85")),
                null);

        ArgumentCaptor<Shelter> kaydedilen = ArgumentCaptor.forClass(Shelter.class);
        verify(shelterRepository).save(kaydedilen.capture());

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
        Shelter mevcut = Shelter.builder()
                .id(3L).user(shelterOwner).name("Eski İsim").address("Eski Adres").city("İstanbul")
                .phone("0212 000 00 00").location(mevcutKonum)
                .build();
        when(shelterRepository.findByUser_Uid(7L)).thenReturn(Optional.of(mevcut));

        ShelterResponse yanit = service.upsertMine(EPOSTA,
                new ShelterUpsertRequest("Yeni İsim", "Yeni Adres", "İstanbul", null, "0212 111 11 11", null,
                        null, null),
                null);

        assertThat(mevcut.getLocation()).isSameAs(mevcutKonum);
        assertThat(yanit.latitude()).isEqualTo(39.92);
        assertThat(yanit.longitude()).isEqualTo(32.85);
    }

    @Test
    void resolveOwnerUidBarinakVarsaSahipUidiDoner() {
        Shelter barinak = Shelter.builder().id(5L).user(shelterOwner).name("Pati Barınağı").build();
        when(shelterRepository.findById(5L)).thenReturn(Optional.of(barinak));

        Long sahipUid = service.resolveOwnerUid(5L);

        assertThat(sahipUid).isEqualTo(7L);
    }

    @Test
    void resolveOwnerUidBarinakYoksaKaynakYokHatasiVerir() {
        when(shelterRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resolveOwnerUid(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void ortalamaPuanVeYorumSayisiYaniteYansir() {
        Shelter mevcut = Shelter.builder()
                .id(3L).user(shelterOwner).name("Eski İsim").address("Eski Adres").city("İstanbul")
                .phone("0212 000 00 00")
                .build();
        when(shelterRepository.findByUser_Uid(7L)).thenReturn(Optional.of(mevcut));
        when(shelterReviewRepository.findAverageRating(3L)).thenReturn(4.5);
        when(shelterReviewRepository.countByShelter_Id(3L)).thenReturn(2L);

        ShelterResponse yanit = service.getMine(EPOSTA);

        assertThat(yanit.averageRating()).isEqualTo(4.5);
        assertThat(yanit.reviewCount()).isEqualTo(2);
    }
}
