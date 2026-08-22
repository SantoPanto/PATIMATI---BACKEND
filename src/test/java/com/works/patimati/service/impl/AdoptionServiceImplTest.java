package com.works.patimati.service.impl;

import com.works.patimati.dto.ad.AdResponse;
import com.works.patimati.dto.ad.AdoptionAdUpdateRequest;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.AgeGroup;
import com.works.patimati.entity.enums.PetColor;
import com.works.patimati.entity.enums.PetGender;
import com.works.patimati.entity.enums.Species;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.AdoptionComplaintRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.service.AdService;
import com.works.patimati.service.RewardService;
import com.works.patimati.storage.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdoptionServiceImplTest {

    private AdRepository adRepository;
    private UserRepository userRepository;
    private AdoptionComplaintRepository adoptionComplaintRepository;
    private ImageStorageService imageStorageService;
    private AdService adService;
    private RewardService rewardService;
    private AdoptionServiceImpl adoptionService;

    @BeforeEach
    void setUp() {
        adRepository = mock(AdRepository.class);
        userRepository = mock(UserRepository.class);
        adoptionComplaintRepository = mock(AdoptionComplaintRepository.class);
        imageStorageService = mock(ImageStorageService.class);
        adService = mock(AdService.class);
        rewardService = mock(RewardService.class);

        adoptionService = new AdoptionServiceImpl(
                adRepository,
                userRepository,
                adoptionComplaintRepository,
                imageStorageService,
                adService,
                rewardService
        );
    }

    @Test
    void shouldUpdateAdoptionAdSuccessfullyWhenColorsIsNull() {
        String ownerEmail = "owner@example.com";
        User owner = User.builder().uid(1L).email(ownerEmail).build();
        Ad existingAd = Ad.builder()
                .id(100L)
                .adType(Ad.AdType.ADOPTION)
                .user(owner)
                .colors(Set.of(PetColor.BLACK))
                .build();

        AdoptionAdUpdateRequest request = new AdoptionAdUpdateRequest(
                "Updated Adoption Title",
                "Description",
                Species.CAT,
                "British Shorthair",
                PetGender.FEMALE,
                AgeGroup.YOUNG,
                null, // Null colors!
                null,
                null,
                null,
                BigDecimal.valueOf(41.0),
                BigDecimal.valueOf(29.0)
        );

        when(adRepository.findByIdAndActiveTrue(100L)).thenReturn(Optional.of(existingAd));
        when(adRepository.save(any(Ad.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(adService.toResponseWithTemporaryPhotoUrls(any(Ad.class))).thenReturn(mock(AdResponse.class));

        AdResponse response = adoptionService.updateAdoptionAd(ownerEmail, 100L, request);

        assertThat(response).isNotNull();
        assertThat(existingAd.getColors()).isEmpty();
    }

    @Test
    void shouldMapDateToEntityWhenCreatingAdoptionAd() {
        String ownerEmail = "owner@example.com";
        User owner = User.builder().uid(1L).email(ownerEmail).build();
        java.time.LocalDate testDate = java.time.LocalDate.of(2026, 8, 20);

        com.works.patimati.dto.ad.AdoptionAdCreateRequest request = new com.works.patimati.dto.ad.AdoptionAdCreateRequest(
                "Sahiplendirilecek Kedi",
                "Aciklama",
                Species.CAT,
                "Tekir",
                PetGender.FEMALE,
                AgeGroup.YOUNG,
                Set.of(),
                null,
                null,
                null,
                "2026-08-20",
                BigDecimal.valueOf(41.0),
                BigDecimal.valueOf(29.0),
                null,
                null,
                null
        );

        org.springframework.mock.web.MockMultipartFile image = new org.springframework.mock.web.MockMultipartFile(
                "images",
                "cat.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3}
        );

        when(userRepository.findByEmail(ownerEmail)).thenReturn(Optional.of(owner));
        when(imageStorageService.uploadImages(any())).thenReturn(java.util.List.of("https://example.com/cat.jpg"));

        org.mockito.ArgumentCaptor<Ad> adCaptor = org.mockito.ArgumentCaptor.forClass(Ad.class);
        when(adRepository.save(adCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        when(adService.toResponseWithTemporaryPhotoUrls(any(Ad.class))).thenReturn(mock(AdResponse.class));

        adoptionService.createAdoptionAd(ownerEmail, request, java.util.List.of(image));

        Ad savedAd = adCaptor.getValue();
        assertThat(savedAd.getLostDate()).isEqualTo(testDate);
    }
}
