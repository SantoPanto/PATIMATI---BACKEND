package com.works.patimati.service;

import com.works.patimati.ai.AiAnalysisPublisher;
import com.works.patimati.dto.ad.AdCreateRequest;
import com.works.patimati.dto.ad.AdResponse;
import com.works.patimati.dto.ad.AdUpdateRequest;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.User;
import com.works.patimati.mapper.AdMapper;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.storage.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdServiceTest {

    private AdRepository adRepository;
    private UserRepository userRepository;
    private AdMapper adMapper;
    private ImageStorageService imageStorageService;
    private AiAnalysisPublisher aiAnalysisPublisher;
    private RewardService rewardService;
    private AdService adService;

    @BeforeEach
    void setUp() {
        adRepository = mock(AdRepository.class);
        userRepository = mock(UserRepository.class);
        adMapper = mock(AdMapper.class);
        imageStorageService = mock(ImageStorageService.class);
        // AI yayıncısı taklit ediliyor: birim testler kuyruk kurulumu
        // gerektirmesin. (Yayıncının kendisi zaten hata yutuyor — AI bir ek,
        // ürünün kalbi değil — ama mock, testi RabbitMQ'dan tümden bağımsız
        // tutuyor ve "yayınlandı mı" doğrulaması yapılabilmesini sağlıyor.)
        aiAnalysisPublisher = mock(AiAnalysisPublisher.class);
        rewardService = mock(RewardService.class);

        adService = new AdService(
                adRepository,
                userRepository,
                adMapper,
                imageStorageService,
                aiAnalysisPublisher,
                rewardService
        );
    }

    @Test
    void shouldCreateAdForAuthenticatedUserAndAttachUploadedImages() {
        User owner = User.builder()
                .uid(42L)
                .email("owner@patimati.com")
                .build();
        AdCreateRequest request = mock(AdCreateRequest.class);
        MultipartFile image = mock(MultipartFile.class);
        Ad ad = Ad.builder().build();
        AdResponse expectedResponse = mock(AdResponse.class);

        List<String> storedReferences = List.of(
                "s3://patimati-test/ads/2026/07/cat.jpg"
        );
        List<String> temporaryUrls = List.of(
                "https://example.com/cat.jpg"
        );

        when(userRepository.findByEmail(owner.getEmail()))
                .thenReturn(Optional.of(owner));
        when(imageStorageService.uploadImages(List.of(image)))
                .thenReturn(storedReferences);
        when(adMapper.toEntity(request)).thenReturn(ad);
        when(adRepository.saveAndFlush(ad)).thenReturn(ad);
        when(imageStorageService.createTemporaryReadUrl(
                storedReferences.get(0)
        )).thenReturn(temporaryUrls.get(0));
        when(adMapper.toResponse(ad, temporaryUrls))
                .thenReturn(expectedResponse);

        AdResponse response = adService.createAd(
                owner.getEmail(),
                request,
                List.of(image)
        );

        assertThat(response).isSameAs(expectedResponse);
        assertThat(ad.getUser()).isSameAs(owner);
        assertThat(ad.isActive()).isTrue();
        assertThat(ad.getPhotoUrls()).containsExactlyElementsOf(
                storedReferences
        );
    }

    @Test
    void shouldDeleteUploadedImagesWhenDatabaseSaveFails() {
        User owner = User.builder()
                .uid(42L)
                .email("owner@patimati.com")
                .build();
        AdCreateRequest request = mock(AdCreateRequest.class);
        MultipartFile image = mock(MultipartFile.class);
        Ad ad = Ad.builder().build();

        List<String> storedReferences = List.of(
                "s3://patimati-test/ads/2026/07/cat.jpg"
        );

        when(userRepository.findByEmail(owner.getEmail()))
                .thenReturn(Optional.of(owner));
        when(imageStorageService.uploadImages(List.of(image)))
                .thenReturn(storedReferences);
        when(adMapper.toEntity(request)).thenReturn(ad);
        when(adRepository.saveAndFlush(ad))
                .thenThrow(new IllegalStateException("Database error"));

        assertThatThrownBy(
                () -> adService.createAd(
                        owner.getEmail(),
                        request,
                        List.of(image)
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Database error");

        verify(imageStorageService).deleteImages(storedReferences);
    }

    @Test
    void shouldUpdateOnlyAnActiveAdOwnedByAuthenticatedUser() {
        User owner = User.builder()
                .uid(42L)
                .email("owner@patimati.com")
                .build();
        Ad ad = Ad.builder()
                .id(7L)
                .user(owner)
                .active(true)
                .build();
        AdUpdateRequest request = mock(AdUpdateRequest.class);
        AdResponse expectedResponse = mock(AdResponse.class);

        when(userRepository.findByEmail(owner.getEmail()))
                .thenReturn(Optional.of(owner));
        when(adRepository.findByIdAndUser_UidAndActiveTrue(
                ad.getId(),
                owner.getUid()
        )).thenReturn(Optional.of(ad));
        when(adRepository.saveAndFlush(ad)).thenReturn(ad);
        when(adMapper.toResponse(ad, List.of()))
                .thenReturn(expectedResponse);

        AdResponse response = adService.updateAd(
                owner.getEmail(),
                ad.getId(),
                request
        );

        verify(adMapper).updateEntity(ad, request);
        assertThat(response).isSameAs(expectedResponse);
    }

    @Test
    void shouldSoftDeleteOnlyAnActiveAdOwnedByAuthenticatedUser() {
        User owner = User.builder()
                .uid(42L)
                .email("owner@patimati.com")
                .build();
        Ad ad = Ad.builder()
                .id(7L)
                .user(owner)
                .active(true)
                .build();

        when(userRepository.findByEmail(owner.getEmail()))
                .thenReturn(Optional.of(owner));
        when(adRepository.findByIdAndUser_UidAndActiveTrue(
                ad.getId(),
                owner.getUid()
        )).thenReturn(Optional.of(ad));
        when(adRepository.saveAndFlush(ad)).thenReturn(ad);

        adService.deactivateAd(owner.getEmail(), ad.getId());

        assertThat(ad.isActive()).isFalse();
        verify(adRepository).saveAndFlush(ad);
        verify(imageStorageService, never()).deleteImages(any());
    }
}
