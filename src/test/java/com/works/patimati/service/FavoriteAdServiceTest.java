package com.works.patimati.service;

import com.works.patimati.dto.ad.AdResponse;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.FavoriteAd;
import com.works.patimati.entity.User;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.FavoriteAdRepository;
import com.works.patimati.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FavoriteAdServiceTest {

    private FavoriteAdRepository favoriteAdRepository;
    private UserRepository userRepository;
    private AdRepository adRepository;
    private AdService adService;
    private FavoriteAdService favoriteAdService;

    @BeforeEach
    void setUp() {
        favoriteAdRepository = mock(FavoriteAdRepository.class);
        userRepository = mock(UserRepository.class);
        adRepository = mock(AdRepository.class);
        adService = mock(AdService.class);
        favoriteAdService = new FavoriteAdService(favoriteAdRepository, userRepository, adRepository, adService);
    }

    @Test
    @DisplayName("getMyFavorites metodu resim URL'lerini imzalamak için adService.toResponseWithTemporaryPhotoUrls kullanmalıdır")
    void getMyFavorites_presignedUrlleriDonmeli() {
        // Arrange
        String email = "test@patimati.me";
        Pageable pageable = PageRequest.of(0, 10);

        Ad ad = Ad.builder()
                .id(1L)
                .title("Kayıp Kedi")
                .photoUrls(List.of("ads/2026/08/kedi.jpg"))
                .build();

        User user = User.builder()
                .uid(100L)
                .email(email)
                .build();

        FavoriteAd favoriteAd = FavoriteAd.builder()
                .id(10L)
                .user(user)
                .ad(ad)
                .build();

        Page<FavoriteAd> favoritePage = new PageImpl<>(List.of(favoriteAd), pageable, 1);
        when(favoriteAdRepository.findByUserEmail(email, pageable)).thenReturn(favoritePage);

        AdResponse expectedResponse = new AdResponse(
                1L, "Kayıp Kedi", null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, false, null, null,
                List.of("https://media.patimati.me/patimati-medya-kutusu/ads/2026/08/kedi.jpg?X-Amz-Signature=123"),
                null, null, 100L, "Test User", true, false, null, null, null, null, null, null, null
        );

        when(adService.toResponseWithTemporaryPhotoUrls(ad)).thenReturn(expectedResponse);

        // Act
        Page<AdResponse> result = favoriteAdService.getMyFavorites(email, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        AdResponse response = result.getContent().get(0);
        assertThat(response.photoUrls())
                .containsExactly("https://media.patimati.me/patimati-medya-kutusu/ads/2026/08/kedi.jpg?X-Amz-Signature=123");

        verify(adService).toResponseWithTemporaryPhotoUrls(ad);
    }
}
