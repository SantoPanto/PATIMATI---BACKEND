package com.works.patimati.service;

import com.works.patimati.ai.AiAnalysisPublisher;
import com.works.patimati.dto.ad.AdCreateRequest;
import com.works.patimati.dto.ad.AdCountersResponse;
import com.works.patimati.dto.ad.AdResponse;
import com.works.patimati.dto.ad.AdUpdateRequest;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.AdResolutionStatus;
import com.works.patimati.mapper.AdMapper;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.storage.ImageStorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import com.works.patimati.exception.BusinessException;
import com.works.patimati.storage.InvalidImageException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.locationtech.jts.geom.Point;
import static org.mockito.ArgumentMatchers.eq;


class AdServiceTest {

    private AdRepository adRepository;
    private UserRepository userRepository;
    private AdMapper adMapper;
    private ImageStorageService imageStorageService;
    private AiAnalysisPublisher aiAnalysisPublisher;
    private RewardService rewardService;
    private NotificationService notificationService;
    private ReverseGeocodingService reverseGeocodingService;
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
        notificationService = mock(NotificationService.class);

        reverseGeocodingService = mock(ReverseGeocodingService.class);
        when(reverseGeocodingService.cozumle(any(), any()))
                .thenReturn(java.util.Optional.empty());

        adService = new AdService(
                adRepository,
                userRepository,
                adMapper,
                imageStorageService,
                aiAnalysisPublisher,
                rewardService,
                mock(NotificationService.class),
                reverseGeocodingService
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
    void shouldRejectAdCreationWhenImagesAreMissing() {
        AdCreateRequest request = mock(AdCreateRequest.class);

        /*
         * Service doğrudan çağrıldığında fotoğraf listesinin null olması
         * da iş kuralını aşmamalıdır.
         */
        assertThatThrownBy(
                () -> adService.createAd(
                        "owner@patimati.com",
                        request,
                        null
                )
        )
                .isInstanceOf(InvalidImageException.class)
                .hasMessageContaining("en az bir fotoğraf");

        /*
         * Boş bir fotoğraf listesi gönderildiğinde de aynı iş kuralının
         * uygulanması gerekir.
         */
        assertThatThrownBy(
                () -> adService.createAd(
                        "owner@patimati.com",
                        request,
                        List.of()
                )
        )
                .isInstanceOf(InvalidImageException.class)
                .hasMessageContaining("en az bir fotoğraf");

        /*
         * Fotoğraf kontrolü metodun en başında yapıldığı için kullanıcı
         * sorgusu, dosya yükleme, veritabanı kaydı ve AI kuyruğu gibi
         * hiçbir yan etki oluşmamalıdır.
         */
        verifyNoInteractions(
                userRepository,
                imageStorageService,
                adMapper,
                adRepository,
                aiAnalysisPublisher,
                rewardService
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
    void shouldUpdateOnlyAnActiveAdoptionAdOwnedByAuthenticatedUser() {
        User owner = User.builder()
                .uid(42L)
                .email("owner@patimati.com")
                .build();
        Ad ad = Ad.builder()
                .id(7L)
                .adType(Ad.AdType.ADOPTION)
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
    void shouldThrowBusinessExceptionWhenUpdatingLostAd() {
        User owner = User.builder()
                .uid(42L)
                .email("owner@patimati.com")
                .build();
        Ad lostAd = Ad.builder()
                .id(7L)
                .adType(Ad.AdType.LOST)
                .user(owner)
                .active(true)
                .build();
        AdUpdateRequest request = mock(AdUpdateRequest.class);

        when(userRepository.findByEmail(owner.getEmail()))
                .thenReturn(Optional.of(owner));
        when(adRepository.findByIdAndUser_UidAndActiveTrue(
                lostAd.getId(),
                owner.getUid()
        )).thenReturn(Optional.of(lostAd));

        assertThatThrownBy(() -> adService.updateAd(owner.getEmail(), lostAd.getId(), request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Kayıp ve Bulundu ilanlarında bilgi bütünlüğünü korumak amacıyla temel bilgilerin güncellenmesine izin verilmemektedir.");
    }

    @Test
    void shouldThrowBusinessExceptionWhenUpdatingFoundAd() {
        User owner = User.builder()
                .uid(42L)
                .email("owner@patimati.com")
                .build();
        Ad foundAd = Ad.builder()
                .id(8L)
                .adType(Ad.AdType.FOUND)
                .user(owner)
                .active(true)
                .build();
        AdUpdateRequest request = mock(AdUpdateRequest.class);

        when(userRepository.findByEmail(owner.getEmail()))
                .thenReturn(Optional.of(owner));
        when(adRepository.findByIdAndUser_UidAndActiveTrue(
                foundAd.getId(),
                owner.getUid()
        )).thenReturn(Optional.of(foundAd));

        assertThatThrownBy(() -> adService.updateAd(owner.getEmail(), foundAd.getId(), request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Kayıp ve Bulundu ilanlarında bilgi bütünlüğünü korumak amacıyla temel bilgilerin güncellenmesine izin verilmemektedir.");
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

    @Test
    void shouldReturnPublicActiveAndHappyEndingCounters() {
        when(adRepository.countByActiveTrueAndSuspendedFalse()).thenReturn(12L);
        when(adRepository.countByResolutionStatusIn(List.of(
                AdResolutionStatus.FOUND,
                AdResolutionStatus.ADOPTED
        ))).thenReturn(7L);

        AdCountersResponse result = adService.getAdCounters();

        assertThat(result.activeAds()).isEqualTo(12L);
        assertThat(result.happyEndings()).isEqualTo(7L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAllowOwnerToGetSuspendedActiveAd() {
        User owner = User.builder().uid(42L).email("owner@patimati.com").role(User.Role.USER).build();
        Ad suspendedAd = Ad.builder().id(10L).user(owner).active(true).suspended(true).build();
        AdResponse expectedResponse = mock(AdResponse.class);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(owner.getEmail(), null, List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );

        when(userRepository.findByEmail(owner.getEmail())).thenReturn(Optional.of(owner));
        when(adRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(suspendedAd));
        when(adMapper.toResponse(suspendedAd, List.of())).thenReturn(expectedResponse);

        AdResponse response = adService.getActiveAd(10L);

        assertThat(response).isSameAs(expectedResponse);
    }

    @Test
    void shouldAllowAdminToGetSuspendedActiveAd() {
        User owner = User.builder().uid(42L).email("owner@patimati.com").role(User.Role.USER).build();
        User admin = User.builder().uid(99L).email("admin@patimati.com").role(User.Role.ADMIN).build();
        Ad suspendedAd = Ad.builder().id(10L).user(owner).active(true).suspended(true).build();
        AdResponse expectedResponse = mock(AdResponse.class);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(admin.getEmail(), null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );

        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(adRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(suspendedAd));
        when(adMapper.toResponse(suspendedAd, List.of())).thenReturn(expectedResponse);

        AdResponse response = adService.getActiveAd(10L);

        assertThat(response).isSameAs(expectedResponse);
    }

    @Test
    void shouldDenyOtherUserFromGettingSuspendedActiveAd() {
        User owner = User.builder().uid(42L).email("owner@patimati.com").role(User.Role.USER).build();
        User otherUser = User.builder().uid(50L).email("other@patimati.com").role(User.Role.USER).build();
        Ad suspendedAd = Ad.builder().id(10L).user(owner).active(true).suspended(true).build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(otherUser.getEmail(), null, List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );

        when(userRepository.findByEmail(otherUser.getEmail())).thenReturn(Optional.of(otherUser));
        when(adRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(suspendedAd));

        assertThatThrownBy(() -> adService.getActiveAd(10L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void shouldFilterSuspendedAdsInGetActiveAdsForStandardUser() {
        User standardUser = User.builder().uid(42L).email("user@patimati.com").role(User.Role.USER).build();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Ad> expectedPage = new PageImpl<>(List.of());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(standardUser.getEmail(), null, List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );

        when(userRepository.findByEmail(standardUser.getEmail())).thenReturn(Optional.of(standardUser));
        when(adRepository.findAllActiveForUser(standardUser.getUid(), pageable)).thenReturn(expectedPage);

        Page<AdResponse> result = adService.getActiveAds(null, pageable);

        assertThat(result).isNotNull();
        verify(adRepository).findAllActiveForUser(standardUser.getUid(), pageable);
        verify(adRepository, never()).findAllByActive(true, pageable);
    }

    @Test
    void shouldIncludeAllSuspendedAdsInGetActiveAdsForAdmin() {
        User admin = User.builder().uid(99L).email("admin@patimati.com").role(User.Role.ADMIN).build();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Ad> expectedPage = new PageImpl<>(List.of());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(admin.getEmail(), null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );

        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(adRepository.findAllByActive(true, pageable)).thenReturn(expectedPage);

        Page<AdResponse> result = adService.getActiveAds(null, pageable);

        assertThat(result).isNotNull();
        verify(adRepository).findAllByActive(true, pageable);
    }

    @Test
    void shouldExcludeAllSuspendedAdsInGetActiveAdsForAnonymous() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Ad> expectedPage = new PageImpl<>(List.of());

        SecurityContextHolder.clearContext();

        when(adRepository.findAllByActiveTrueAndSuspendedFalse(pageable)).thenReturn(expectedPage);

        Page<AdResponse> result = adService.getActiveAds(null, pageable);

        assertThat(result).isNotNull();
        verify(adRepository).findAllByActiveTrueAndSuspendedFalse(pageable);
    }

    @Test
    void shouldUsePublicNearbyQueryAndMapCoordinatesCorrectly() {
        /*
         * Public haritada gösterilebilecek örnek bir ilan hazırlanır.
         */
        Ad ad = Ad.builder()
                .id(7L)
                .active(true)
                .suspended(false)
                .build();

        AdResponse expectedResponse = mock(AdResponse.class);

        /*
         * Repository'nin public harita sorgusu bir ilan döndürecek
         * şekilde taklit edilir.
         */
        when(adRepository.findPublicNearbyAds(
                any(Point.class),
                eq(5000.0)
        )).thenReturn(List.of(ad));

        /*
         * Entity'nin güvenli response DTO'suna dönüştürülmesi taklit edilir.
         */
        when(adMapper.toResponse(ad, List.of()))
                .thenReturn(expectedResponse);

        List<AdResponse> result = adService.findPublicNearbyAds(
                40.195,
                29.060,
                5000.0
        );

        /*
         * Repository'ye gönderilen PostGIS koordinatını yakalıyoruz.
         * Böylece enlem ve boylamın yanlış sırada kullanılmadığını doğruluyoruz.
         */
        ArgumentCaptor<Point> pointCaptor =
                ArgumentCaptor.forClass(Point.class);

        verify(adRepository).findPublicNearbyAds(
                pointCaptor.capture(),
                eq(5000.0)
        );

        // X değeri boylam olmalıdır.
        assertThat(pointCaptor.getValue().getX())
                .isEqualTo(29.060);

        // Y değeri enlem olmalıdır.
        assertThat(pointCaptor.getValue().getY())
                .isEqualTo(40.195);

        assertThat(result).containsExactly(expectedResponse);
    }

    @Test
    void shouldAlwaysGenerateSignedUrlForEveryPhotoWithoutBypass() {
        String testReference = "s3://patimati-bucket/ads/test-dummyimage.com/dog.jpg";
        String mockSignedUrl = "mock_signed_url";
        Ad ad = Ad.builder()
                .id(101L)
                .photoUrls(List.of(testReference))
                .build();

        AdResponse expectedResponse = mock(AdResponse.class);

        when(imageStorageService.createTemporaryReadUrl(testReference))
                .thenReturn(mockSignedUrl);
        when(adMapper.toResponse(ad, List.of(mockSignedUrl)))
                .thenReturn(expectedResponse);

        AdResponse response = adService.toResponseWithTemporaryPhotoUrls(ad);

        assertThat(response).isSameAs(expectedResponse);
        verify(imageStorageService).createTemporaryReadUrl(testReference);
    }
}
