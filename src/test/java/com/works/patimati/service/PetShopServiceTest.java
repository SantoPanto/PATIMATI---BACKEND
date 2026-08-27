package com.works.patimati.service;

import com.works.patimati.dto.admin.CreatePetShopAccountRequest;
import com.works.patimati.dto.petshop.PetShopResponse;
import com.works.patimati.dto.petshop.PetShopUpsertRequest;
import com.works.patimati.entity.PetShop;
import com.works.patimati.entity.User;
import com.works.patimati.exception.BusinessException;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.PetShopProductRepository;
import com.works.patimati.repository.PetShopRepository;
import com.works.patimati.repository.PetShopReviewRepository;
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

/** {@code VetClinicServiceTest}'in hayvan türü hariç aynısı. */
class PetShopServiceTest {

    private static final String EPOSTA = "petshop@ornek.com";

    private PetShopRepository petShopRepository;
    private UserRepository userRepository;
    private ImageStorageService imageStorageService;
    private PasswordEncoder passwordEncoder;
    private PetShopReviewRepository petShopReviewRepository;
    private PetShopService service;
    private User petshopOwner;

    @BeforeEach
    void hazirla() {
        petShopRepository = mock(PetShopRepository.class);
        userRepository = mock(UserRepository.class);
        imageStorageService = mock(ImageStorageService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        petShopReviewRepository = mock(PetShopReviewRepository.class);
        service = new PetShopService(petShopRepository, userRepository, imageStorageService, passwordEncoder,
                petShopReviewRepository);

        petshopOwner = User.builder().uid(7L).email(EPOSTA).role(User.Role.PETSHOP).build();
        when(userRepository.findByEmail(EPOSTA)).thenReturn(Optional.of(petshopOwner));
        when(petShopRepository.save(any(PetShop.class))).thenAnswer(cagri -> cagri.getArgument(0));
        when(petShopReviewRepository.findAverageRating(any())).thenReturn(null);
        when(petShopReviewRepository.countByPetShop_Id(any())).thenReturn(0L);
    }

    @Test
    void kartYokkenGetirmeKaynakYokHatasiVerir() {
        when(petShopRepository.findByUser_Uid(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMine(EPOSTA))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void ilkKayitFotografsizOlusturulur() {
        when(petShopRepository.findByUser_Uid(7L)).thenReturn(Optional.empty());

        PetShopResponse yanit = service.upsertMine(EPOSTA,
                new PetShopUpsertRequest("Pati Petshop", "Örnek Mah. 1. Sk. No:1", "Ankara", "Çankaya",
                        "0312 000 00 00", "09:00-18:00", null, null),
                null);

        ArgumentCaptor<PetShop> kaydedilen = ArgumentCaptor.forClass(PetShop.class);
        verify(petShopRepository).save(kaydedilen.capture());

        assertThat(kaydedilen.getValue().getUser()).isSameAs(petshopOwner);
        assertThat(kaydedilen.getValue().getName()).isEqualTo("Pati Petshop");
        assertThat(kaydedilen.getValue().getPhotoReference()).isNull();
        assertThat(yanit.name()).isEqualTo("Pati Petshop");
        assertThat(yanit.photoUrl()).isNull();
        verify(imageStorageService, never()).uploadImages(any(), anyString());
    }

    @Test
    void fotografliGuncellemeEskiFotografiSilerYeniyiYukler() {
        PetShop mevcut = PetShop.builder()
                .id(3L).user(petshopOwner).name("Eski İsim").address("Eski Adres").city("İstanbul")
                .phone("0212 000 00 00").photoReference("petshops/eski.jpg")
                .build();
        when(petShopRepository.findByUser_Uid(7L)).thenReturn(Optional.of(mevcut));

        MockMultipartFile foto = new MockMultipartFile("photo", "yeni.jpg", "image/jpeg", new byte[]{1, 2, 3});
        when(imageStorageService.uploadImages(List.of(foto), "petshops"))
                .thenReturn(List.of("petshops/yeni.jpg"));
        when(imageStorageService.createTemporaryReadUrl("petshops/yeni.jpg"))
                .thenReturn("https://cdn.test/petshops/yeni.jpg");

        PetShopResponse yanit = service.upsertMine(EPOSTA,
                new PetShopUpsertRequest("Yeni İsim", "Yeni Adres", "İstanbul", null, "0212 111 11 11", null,
                        null, null),
                foto);

        assertThat(mevcut.getPhotoReference()).isEqualTo("petshops/yeni.jpg");
        assertThat(yanit.photoUrl()).isEqualTo("https://cdn.test/petshops/yeni.jpg");
        verify(imageStorageService).deleteImages(List.of("petshops/eski.jpg"));
    }

    @Test
    void eMailZatenKullanimdaysaHesapOlusturulmaz() {
        when(userRepository.findByEmail("dolu@ornek.com")).thenReturn(Optional.of(petshopOwner));

        assertThatThrownBy(() -> service.createPetShopAccount(
                new CreatePetShopAccountRequest("Ad", "Soyad", "dolu@ornek.com", "Sifre123")))
                .isInstanceOf(BusinessException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void gecerliIstekleyPetshopRolundeHesapOlusturulur() {
        when(userRepository.findByEmail("yeni-petshop@ornek.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Sifre123")).thenReturn("sifrelenmis");

        service.createPetShopAccount(new CreatePetShopAccountRequest("Ad", "Soyad", "yeni-petshop@ornek.com", "Sifre123"));

        ArgumentCaptor<User> kaydedilen = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(kaydedilen.capture());
        assertThat(kaydedilen.getValue().getRole()).isEqualTo(User.Role.PETSHOP);
        assertThat(kaydedilen.getValue().getPassword()).isEqualTo("sifrelenmis");
        assertThat(kaydedilen.getValue().isEnabled()).isTrue();
    }

    @Test
    void konumluGuncellemePointDogruYazilir() {
        when(petShopRepository.findByUser_Uid(7L)).thenReturn(Optional.empty());

        PetShopResponse yanit = service.upsertMine(EPOSTA,
                new PetShopUpsertRequest("Pati Petshop", "Örnek Mah. 1. Sk. No:1", "Ankara", "Çankaya",
                        "0312 000 00 00", "09:00-18:00",
                        new BigDecimal("39.92"), new BigDecimal("32.85")),
                null);

        ArgumentCaptor<PetShop> kaydedilen = ArgumentCaptor.forClass(PetShop.class);
        verify(petShopRepository).save(kaydedilen.capture());

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
        PetShop mevcut = PetShop.builder()
                .id(3L).user(petshopOwner).name("Eski İsim").address("Eski Adres").city("İstanbul")
                .phone("0212 000 00 00").location(mevcutKonum)
                .build();
        when(petShopRepository.findByUser_Uid(7L)).thenReturn(Optional.of(mevcut));

        PetShopResponse yanit = service.upsertMine(EPOSTA,
                new PetShopUpsertRequest("Yeni İsim", "Yeni Adres", "İstanbul", null, "0212 111 11 11", null,
                        null, null),
                null);

        assertThat(mevcut.getLocation()).isSameAs(mevcutKonum);
        assertThat(yanit.latitude()).isEqualTo(39.92);
        assertThat(yanit.longitude()).isEqualTo(32.85);
    }
}
