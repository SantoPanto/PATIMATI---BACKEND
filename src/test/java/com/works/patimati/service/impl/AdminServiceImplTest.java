package com.works.patimati.service.impl;

import com.works.patimati.entity.Ad;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.AdComplaintRepository;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.AdoptionComplaintRepository;
import com.works.patimati.repository.PotentialMatchRepository;
import com.works.patimati.repository.UserComplaintRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.repository.external.ExternalPetRecordRepository;
import com.works.patimati.repository.external.ExternalSourceMediaRepository;
import com.works.patimati.repository.external.ExternalSourcePostRepository;
import com.works.patimati.service.AdService;
import com.works.patimati.storage.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdminServiceImplTest {

    private UserRepository userRepository;
    private AdRepository adRepository;
    private AdComplaintRepository adComplaintRepository;
    private UserComplaintRepository userComplaintRepository;
    private AdoptionComplaintRepository adoptionComplaintRepository;
    private AdService adService;
    private ExternalSourcePostRepository externalSourcePostRepository;
    private ExternalPetRecordRepository externalPetRecordRepository;
    private ExternalSourceMediaRepository externalSourceMediaRepository;
    private PotentialMatchRepository potentialMatchRepository;
    private ImageStorageService imageStorageService;
    private AdminServiceImpl adminService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        adRepository = mock(AdRepository.class);
        adComplaintRepository = mock(AdComplaintRepository.class);
        userComplaintRepository = mock(UserComplaintRepository.class);
        adoptionComplaintRepository = mock(AdoptionComplaintRepository.class);
        adService = mock(AdService.class);
        externalSourcePostRepository = mock(ExternalSourcePostRepository.class);
        externalPetRecordRepository = mock(ExternalPetRecordRepository.class);
        externalSourceMediaRepository = mock(ExternalSourceMediaRepository.class);
        potentialMatchRepository = mock(PotentialMatchRepository.class);
        imageStorageService = mock(ImageStorageService.class);

        adminService = new AdminServiceImpl(
                userRepository,
                adRepository,
                adComplaintRepository,
                userComplaintRepository,
                adoptionComplaintRepository,
                adService,
                externalSourcePostRepository,
                externalPetRecordRepository,
                externalSourceMediaRepository,
                potentialMatchRepository,
                imageStorageService
        );
    }

    @Test
    void suspendAd_ShouldSetSuspendedTrueAndActiveFalse() {
        Ad ad = Ad.builder()
                .id(1L)
                .active(true)
                .suspended(false)
                .build();

        when(adRepository.findById(1L)).thenReturn(Optional.of(ad));
        when(adRepository.save(any(Ad.class))).thenAnswer(invocation -> invocation.getArgument(0));

        adminService.suspendAd(1L);

        assertThat(ad.isSuspended()).isTrue();
        assertThat(ad.isActive()).isFalse();
        verify(adRepository).save(ad);
    }

    @Test
    void unhideAd_ShouldSetSuspendedFalseAndActiveTrue() {
        Ad ad = Ad.builder()
                .id(1L)
                .active(false)
                .suspended(true)
                .build();

        when(adRepository.findById(1L)).thenReturn(Optional.of(ad));
        when(adRepository.save(any(Ad.class))).thenAnswer(invocation -> invocation.getArgument(0));

        adminService.unhideAd(1L);

        assertThat(ad.isSuspended()).isFalse();
        assertThat(ad.isActive()).isTrue();
        verify(adRepository).save(ad);
    }

    @Test
    void suspendAd_ShouldThrowResourceNotFoundException_WhenAdNotFound() {
        when(adRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.suspendAd(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
