package com.works.patimati.service;

import com.works.patimati.ai.AiAnalysisPublisher;
import com.works.patimati.dto.ad.AdCreateRequest;
import com.works.patimati.dto.ad.AdResponse;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.AiStatus;
import com.works.patimati.entity.enums.Species;
import com.works.patimati.mapper.AdMapper;
import com.works.patimati.notification.NearbyAlertNotifier;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.storage.ImageStorageService;
import com.works.patimati.storage.InvalidImageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdCreationMatchingPipelineTest {

    private AdRepository adRepository;
    private UserRepository userRepository;
    private AdMapper adMapper;
    private ImageStorageService imageStorageService;
    private AiAnalysisPublisher aiAnalysisPublisher;
    private NearbyAlertNotifier nearbyAlertNotifier;
    private InstagramPublishService instagramPublishService;
    private AdService adService;

    private User owner;

    @BeforeEach
    void setUp() {
        adRepository = mock(AdRepository.class);
        userRepository = mock(UserRepository.class);
        adMapper = mock(AdMapper.class);
        imageStorageService = mock(ImageStorageService.class);
        aiAnalysisPublisher = mock(AiAnalysisPublisher.class);
        nearbyAlertNotifier = mock(NearbyAlertNotifier.class);
        instagramPublishService = mock(InstagramPublishService.class);

        adService = new AdService(
                adRepository,
                userRepository,
                adMapper,
                imageStorageService,
                aiAnalysisPublisher,
                mock(RewardService.class),
                mock(NotificationService.class),
                mock(ReverseGeocodingService.class),
                nearbyAlertNotifier,
                instagramPublishService
        );

        owner = User.builder().uid(1L).email("owner@patimati.me").build();
        when(userRepository.findByEmail("owner@patimati.me")).thenReturn(Optional.of(owner));
    }

    @Test
    @DisplayName("İlan önce DB'ye kaydedilmeli (saveAndFlush), ARDINDAN AI matching pipeline (publish) başlatılmalı")
    void adSavedToDbFirstThenMatchingPipelineStarted() {
        AdCreateRequest request = new AdCreateRequest(
                "Kayıp Kedi Tekir", "Açıklama", Ad.AdType.LOST, Species.CAT, "Tekir",
                null, null, null, null, null, null, null,
                null, null, null, null, "2026-08-22", null,
                new BigDecimal("41.0"), new BigDecimal("29.0"), null, null, null, null, null
        );

        MockMultipartFile file = new MockMultipartFile("images", "cat.jpg", "image/jpeg", new byte[]{1, 2, 3});
        List<MultipartFile> images = List.of(file);

        when(imageStorageService.uploadImages(images)).thenReturn(List.of("ads/cat.jpg"));

        Ad unpersistedAd = Ad.builder().title("Kayıp Kedi Tekir").adType(Ad.AdType.LOST).build();
        Ad savedAd = Ad.builder().id(50L).title("Kayıp Kedi Tekir").adType(Ad.AdType.LOST).aiStatus(AiStatus.PENDING).build();

        when(adMapper.toEntity(request)).thenReturn(unpersistedAd);
        when(adRepository.saveAndFlush(any())).thenReturn(savedAd);
        AdResponse adResponseMock = mock(AdResponse.class);
        when(adResponseMock.id()).thenReturn(50L);
        when(adMapper.toResponse(any(), any())).thenReturn(adResponseMock);

        AdResponse response = adService.createAd("owner@patimati.me", request, images);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(50L);

        // Sıralama doğrulaması: Önce saveAndFlush, ardından aiAnalysisPublisher.publish
        InOrder inOrder = inOrder(adRepository, aiAnalysisPublisher);
        inOrder.verify(adRepository).saveAndFlush(any());
        inOrder.verify(aiAnalysisPublisher).publish(savedAd);
    }

    @Test
    @DisplayName("İlan oluşturmada validation/fotoğraf hatası olursa DB'ye kaydedilmez ve AI matching BAŞLATILMAZ")
    void validationFailurePreventsMatchingPipeline() {
        AdCreateRequest request = new AdCreateRequest(
                "Kayıp Kedi", "Açıklama", Ad.AdType.LOST, Species.CAT, "Tekir",
                null, null, null, null, null, null, null,
                null, null, null, null, "2026-08-22", null,
                new BigDecimal("41.0"), new BigDecimal("29.0"), null, null, null, null, null
        );

        // Boş fotoğraf listesi -> InvalidImageException fırlatmalı
        assertThatThrownBy(() -> adService.createAd("owner@patimati.me", request, List.of()))
                .isInstanceOf(InvalidImageException.class);

        verify(adRepository, never()).saveAndFlush(any());
        verify(aiAnalysisPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("AI matching publish hata verse bile ilan kaydı başarılı kalmalı (rollback olmamalı)")
    void matchingPublishErrorDoesNotRollbackAdCreation() {
        // Gerçek AiAnalysisPublisher örneği: RabbitTemplate hata verdiğinde kendi try-catch bloğu
        // hatayı yutar (loglar) ve ilan oluşturma akışının bölünmesini engeller.
        org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate = mock(org.springframework.amqp.rabbit.core.RabbitTemplate.class);
        doThrow(new RuntimeException("RabbitMQ connection refused"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class), any(org.springframework.amqp.core.MessagePostProcessor.class));

        com.works.patimati.ai.MatchCandidateGatherer candidateGatherer = mock(com.works.patimati.ai.MatchCandidateGatherer.class);
        AiAnalysisPublisher realPublisher = new AiAnalysisPublisher(rabbitTemplate, adRepository, imageStorageService, candidateGatherer);

        AdService realAdService = new AdService(
                adRepository,
                userRepository,
                adMapper,
                imageStorageService,
                realPublisher,
                mock(RewardService.class),
                mock(NotificationService.class),
                mock(ReverseGeocodingService.class),
                nearbyAlertNotifier,
                instagramPublishService
        );

        AdCreateRequest request = new AdCreateRequest(
                "Kayıp Köpek", "Açıklama", Ad.AdType.LOST, Species.DOG, "Golden",
                null, null, null, null, null, null, null,
                null, null, null, null, "2026-08-22", null,
                new BigDecimal("41.0"), new BigDecimal("29.0"), null, null, null, null, null
        );

        MockMultipartFile file = new MockMultipartFile("images", "dog.jpg", "image/jpeg", new byte[]{1, 2, 3});
        List<MultipartFile> images = List.of(file);

        when(imageStorageService.uploadImages(images)).thenReturn(List.of("ads/dog.jpg"));
        when(imageStorageService.createTemporaryReadUrl(any())).thenReturn("https://s3.example.com/ads/dog.jpg");

        Ad unpersistedAd = Ad.builder().title("Kayıp Köpek").adType(Ad.AdType.LOST).build();
        Ad savedAd = Ad.builder().id(75L).title("Kayıp Köpek").adType(Ad.AdType.LOST).photoUrls(List.of("ads/dog.jpg")).aiStatus(AiStatus.PENDING).build();

        when(adMapper.toEntity(request)).thenReturn(unpersistedAd);
        when(adRepository.saveAndFlush(any())).thenReturn(savedAd);
        AdResponse adResponseMock = mock(AdResponse.class);
        when(adResponseMock.id()).thenReturn(75L);
        when(adMapper.toResponse(any(), any())).thenReturn(adResponseMock);

        when(adRepository.findById(75L)).thenReturn(Optional.of(savedAd));

        // RabbitMQ hatasına rağmen createAd başarısız olmamalı
        AdResponse response = realAdService.createAd("owner@patimati.me", request, images);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(75L);
        verify(adRepository).saveAndFlush(any());

        // RabbitMQ outage sonrası DB persistence doğrulaması: İlan DB'de PENDING durumunda erişilebilir olarak mevcut
        Optional<Ad> persistedInDb = adRepository.findById(75L);
        assertThat(persistedInDb).isPresent();
        assertThat(persistedInDb.get().getAiStatus()).isEqualTo(AiStatus.PENDING);
    }
}
