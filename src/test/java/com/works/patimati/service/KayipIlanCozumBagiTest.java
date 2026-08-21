package com.works.patimati.service;

import com.works.patimati.ai.AiAnalysisPublisher;
import com.works.patimati.dto.ad.ResolveLostAdRequest;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.AdResolutionStatus;
import com.works.patimati.mapper.AdMapper;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.storage.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Kayıp ilan çözüm bağı işlemlerinin (resolveLostAd) birim testleri.
 */
@ExtendWith(MockitoExtension.class)
class KayipIlanCozumBagiTest {

    private static final String OWNER_EMAIL = "owner@patimati.me";
    private static final Long OWNER_UID = 10L;
    private static final Long AD_ID = 100L;
    private static final Long FINDER_ID = 20L;

    @Mock
    private AdRepository adRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AdMapper adMapper;
    @Mock
    private ImageStorageService imageStorageService;
    @Mock
    private AiAnalysisPublisher aiAnalysisPublisher;
    @Mock
    private RewardService rewardService;
    @Mock
    private NotificationService notificationService;

    private AdService adService;
    private User owner;

    @BeforeEach
    void setUp() {
        adService = new AdService(
                adRepository,
                userRepository,
                adMapper,
                imageStorageService,
                aiAnalysisPublisher,
                rewardService,
                notificationService
        );

        owner = User.builder().uid(OWNER_UID).email(OWNER_EMAIL).build();
    }

    @Test
    void kayipIlanCozuldugundePasifeAlinirVePuanVerilir() {
        Ad ad = Ad.builder()
                .id(AD_ID)
                .user(owner)
                .adType(Ad.AdType.LOST)
                .active(true)
                .build();

        when(userRepository.findByEmail(OWNER_EMAIL)).thenReturn(Optional.of(owner));
        when(adRepository.findByIdAndUser_UidAndActiveTrue(AD_ID, OWNER_UID)).thenReturn(Optional.of(ad));

        ResolveLostAdRequest request = new ResolveLostAdRequest(FINDER_ID, null);
        adService.resolveLostAd(OWNER_EMAIL, AD_ID, request);

        assertThat(ad.isActive()).isFalse();
        assertThat(ad.getResolutionStatus()).isEqualTo(AdResolutionStatus.FOUND);
        verify(adRepository).saveAndFlush(ad);
        verify(rewardService).awardLostPoint(FINDER_ID);
    }

    @Test
    void kayipOlmayanIlanCozulemez() {
        Ad ad = Ad.builder()
                .id(AD_ID)
                .user(owner)
                .adType(Ad.AdType.FOUND)
                .active(true)
                .build();

        when(userRepository.findByEmail(OWNER_EMAIL)).thenReturn(Optional.of(owner));
        when(adRepository.findByIdAndUser_UidAndActiveTrue(AD_ID, OWNER_UID)).thenReturn(Optional.of(ad));

        ResolveLostAdRequest request = new ResolveLostAdRequest(FINDER_ID, null);
        assertThatThrownBy(() -> adService.resolveLostAd(OWNER_EMAIL, AD_ID, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("İlan bir kayıp ilanı değildir.");
    }
}
