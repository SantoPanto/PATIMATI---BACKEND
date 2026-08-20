package com.works.patimati.service;

import com.works.patimati.dto.ad.AdResponse;
import com.works.patimati.dto.ad.PosterSettingsUpdateRequest;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.User;
import com.works.patimati.mapper.AdMapper;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.storage.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PosterSettingsTest {

    @Mock
    private AdRepository adRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdMapper adMapper;

    @Mock
    private ImageStorageService imageStorageService;

    @InjectMocks
    private AdService adService;

    private User owner;
    private User otherUser;
    private Ad testAd;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .uid(1L)
                .email("owner@patimati.me")
                .build();

        otherUser = User.builder()
                .uid(2L)
                .email("other@patimati.me")
                .build();

        testAd = Ad.builder()
                .id(100L)
                .user(owner)
                .isPosterAllowed(false)
                .showEmailOnPoster(false)
                .showPhoneOnPoster(false)
                .build();
    }

    @Test
    void shouldUpdatePosterSettingsWhenCalledByOwner() {
        PosterSettingsUpdateRequest request = PosterSettingsUpdateRequest.builder()
                .isPosterAllowed(true)
                .showEmailOnPoster(true)
                .showPhoneOnPoster(true)
                .build();

        when(userRepository.findByEmail("owner@patimati.me")).thenReturn(Optional.of(owner));
        when(adRepository.findById(100L)).thenReturn(Optional.of(testAd));
        when(adRepository.saveAndFlush(any(Ad.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdResponse mockResponse = new AdResponse(
                100L, "Başlık", "Açıklama", Ad.AdType.LOST, null, null, null, null, null, null, null, null, null, null, null, null, false, null, null, java.util.List.of(), null, null, 1L, "Owner", true, false, null, null, null, null, true, true, true, com.works.patimati.entity.enums.AdResolutionStatus.NONE
        );
        when(adMapper.toResponse(any(Ad.class), anyList())).thenReturn(mockResponse);

        AdResponse result = adService.updatePosterSettings(100L, request, "owner@patimati.me");

        assertThat(result).isNotNull();
        assertThat(testAd.getIsPosterAllowed()).isTrue();
        assertThat(testAd.getShowEmailOnPoster()).isTrue();
        assertThat(testAd.getShowPhoneOnPoster()).isTrue();

        verify(adRepository).saveAndFlush(testAd);
    }

    @Test
    void shouldRejectUpdatePosterSettingsWhenCalledByNonOwner() {
        PosterSettingsUpdateRequest request = PosterSettingsUpdateRequest.builder()
                .isPosterAllowed(true)
                .build();

        when(userRepository.findByEmail("other@patimati.me")).thenReturn(Optional.of(otherUser));
        when(adRepository.findById(100L)).thenReturn(Optional.of(testAd));

        assertThatThrownBy(() -> adService.updatePosterSettings(100L, request, "other@patimati.me"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Bu ilanın afiş ayarlarını yalnızca ilan sahibi güncelleyebilir.");

        verify(adRepository, never()).saveAndFlush(any());
    }
}
