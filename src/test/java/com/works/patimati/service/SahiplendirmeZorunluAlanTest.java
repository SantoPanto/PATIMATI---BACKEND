package com.works.patimati.service;

import com.works.patimati.ai.AiAnalysisPublisher;
import com.works.patimati.dto.ad.AdoptionAdCreateRequest;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.AgeGroup;
import com.works.patimati.entity.enums.PetGender;
import com.works.patimati.entity.enums.Species;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.AdoptionComplaintRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.service.impl.AdoptionServiceImpl;
import com.works.patimati.storage.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Sahiplendirme ilanında "Gönderilen verilerden biri kaydedilemedi: bir alan
 * izin verilen sınırı aşıyor ya da beklenen biçimde değil." (23.08 saha
 * görüntüsü 6) — bu mesaj {@code DataIntegrityViolationException}'ın
 * karşılığıdır (GlobalExceptionHandler:346), yani bir VERİTABANI KISITI
 * patlamıştır.
 *
 * <p><b>Ölçülen boşluk:</b> {@code ads.gender} ve {@code ads.age_group}
 * V3'te {@code VARCHAR(20) NOT NULL}; DTO'da ikisi de {@code @NotNull}
 * DEĞİL. Kayıp/bulundu yolu bunu {@code AdMapper}'da
 * {@code defaultValue(..., UNKNOWN)} ile koruyor, sahiplendirme yolu
 * korumuyordu — aynı sınıftaki {@code coatPattern}/{@code eyeColor} için
 * koruma daha önce eklenmiş, bu ikisi atlanmıştı.
 *
 * <p><b>Neden ön yüzde görünmüyordu:</b> {@code AdoptionCreatePage} bu
 * boşluğu bilerek geçici çözümle kapatıyor ve alanları hep {@code UNKNOWN}
 * gönderiyor (dosyadaki yorum bunu anlatıyor). Yani kural sunucuda değil
 * istemcide tutuluyordu; ikinci bir istemci (mobil uygulama, betik, eski
 * paket) aynı isteği alansız gönderdiğinde kısıt patlar.
 *
 * <p>Bu testler kuralın SUNUCUDA durduğunu kilitler.
 */
class SahiplendirmeZorunluAlanTest {

    private static final String SAHIP_EPOSTA = "sahip@ornek.com";

    private AdRepository adRepository;
    private AdoptionServiceImpl adoptionService;

    @BeforeEach
    void setUp() {
        adRepository = mock(AdRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ImageStorageService imageStorageService = mock(ImageStorageService.class);
        AdService adService = mock(AdService.class);

        adoptionService = new AdoptionServiceImpl(
                adRepository,
                userRepository,
                mock(AdoptionComplaintRepository.class),
                imageStorageService,
                adService,
                mock(RewardService.class),
                mock(AiAnalysisPublisher.class)
        );

        User sahip = User.builder().uid(7L).email(SAHIP_EPOSTA)
                .firstName("Deneme").lastName("Sahip").build();
        when(userRepository.findByEmail(SAHIP_EPOSTA)).thenReturn(Optional.of(sahip));
        when(imageStorageService.uploadImages(anyList())).thenReturn(List.of("foto/1.jpg"));
        when(adRepository.save(any(Ad.class))).thenAnswer(c -> {
            Ad kaydedilen = c.getArgument(0);
            kaydedilen.setId(500L);
            return kaydedilen;
        });
    }

    private AdoptionAdCreateRequest istek(PetGender cinsiyet, AgeGroup yasGrubu) {
        return new AdoptionAdCreateRequest(
                "Yuva arıyor",
                "açıklama",
                Species.CAT,
                "Tekir",
                cinsiyet,
                yasGrubu,
                null,          // colors
                null,          // coatPattern
                null,          // eyeColor
                null,          // microchipNumber
                "2026-08-23",
                new BigDecimal("39.58"),
                new BigDecimal("26.87"),
                null,          // city
                null,          // district
                Boolean.FALSE
        );
    }

    private List<MultipartFile> tekFoto() {
        return List.of(new MockMultipartFile(
                "images", "kedi.jpg", "image/jpeg", new byte[]{1, 2, 3}));
    }

    @Test
    @DisplayName("cinsiyet/yaş gönderilmezse NOT NULL sütunlara null YAZILMAZ")
    void alanlarBosGelirseUnknownYazilir() {
        adoptionService.createAdoptionAd(SAHIP_EPOSTA, istek(null, null), tekFoto());

        ArgumentCaptor<Ad> kaydedilen = ArgumentCaptor.forClass(Ad.class);
        verify(adRepository).save(kaydedilen.capture());

        assertThat(kaydedilen.getValue().getGender()).isEqualTo(PetGender.UNKNOWN);
        assertThat(kaydedilen.getValue().getAgeGroup()).isEqualTo(AgeGroup.UNKNOWN);
    }

    @Test
    @DisplayName("gerçek değer gönderildiğinde UNKNOWN'a EZİLMEZ")
    void gercekDegerKorunur() {
        adoptionService.createAdoptionAd(
                SAHIP_EPOSTA, istek(PetGender.FEMALE, AgeGroup.YOUNG), tekFoto());

        ArgumentCaptor<Ad> kaydedilen = ArgumentCaptor.forClass(Ad.class);
        verify(adRepository).save(kaydedilen.capture());

        // Pozitif kontrol: koruma, gelen veriyi bastırmıyor.
        assertThat(kaydedilen.getValue().getGender()).isEqualTo(PetGender.FEMALE);
        assertThat(kaydedilen.getValue().getAgeGroup()).isEqualTo(AgeGroup.YOUNG);
    }
}
