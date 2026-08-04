package com.works.patimati.service;

import com.works.patimati.dto.complaint.AdComplaintRequestDTO;
import com.works.patimati.dto.complaint.ComplaintResponse;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.AdComplaint;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.ComplaintReason;
import com.works.patimati.entity.enums.ComplaintStatus;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.AdComplaintRepository;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdComplaintServiceTest {

    @Mock
    private AdComplaintRepository adComplaintRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdRepository adRepository;

    @InjectMocks
    private AdComplaintService adComplaintService;

    private User reporter;
    private User adOwner;
    private Ad ad;

    @BeforeEach
    void setUp() {
        reporter = User.builder()
                .uid(1L)
                .email("reporter@patimati.com")
                .firstName("Ahmet")
                .lastName("Yılmaz")
                .build();

        adOwner = User.builder()
                .uid(2L)
                .email("owner@patimati.com")
                .firstName("Mehmet")
                .lastName("Demir")
                .build();

        ad = Ad.builder()
                .id(10L)
                .title("Kayıp Kedi")
                .description("İlan detayı")
                .user(adOwner)
                .build();
    }

    @Test
    @DisplayName("createAdComplaint - Başarılı senaryoda AdComplaint entity ad_complaints tablosuna kaydedilmeli")
    void createAdComplaint_Success() {
        AdComplaintRequestDTO request = AdComplaintRequestDTO.builder()
                .reportedAdId(10L)
                .reason(ComplaintReason.SAHTE_ILAN)
                .description("Sahte ilan şüphesi")
                .build();

        when(userRepository.findByEmail("reporter@patimati.com")).thenReturn(Optional.of(reporter));
        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(adComplaintRepository.existsByReporterIdAndAdIdAndStatusIn(eq(1L), eq(10L), any())).thenReturn(false);

        AdComplaint savedComplaint = AdComplaint.builder()
                .id(100L)
                .reporterId(1L)
                .adId(10L)
                .reason(ComplaintReason.SAHTE_ILAN)
                .description("Sahte ilan şüphesi")
                .status(ComplaintStatus.BEKLEMEDE)
                .createdAt(Instant.now())
                .build();

        when(adComplaintRepository.save(any(AdComplaint.class))).thenReturn(savedComplaint);

        ComplaintResponse response = adComplaintService.createAdComplaint("reporter@patimati.com", request);

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals(10L, response.reportedAdId());
        assertEquals(2L, response.reportedUserId());
        verify(adComplaintRepository, times(1)).save(any(AdComplaint.class));
    }

    @Test
    @DisplayName("createAdComplaint - İlan bulunamadığında ResourceNotFoundException (404) fırlatılmalı")
    void createAdComplaint_AdNotFound() {
        AdComplaintRequestDTO request = AdComplaintRequestDTO.builder()
                .reportedAdId(99L)
                .reason(ComplaintReason.SAHTE_ILAN)
                .description("Açıklama")
                .build();

        when(userRepository.findByEmail("reporter@patimati.com")).thenReturn(Optional.of(reporter));
        when(adRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                adComplaintService.createAdComplaint("reporter@patimati.com", request)
        );
    }

    @Test
    @DisplayName("createAdComplaint - Kendi ilanını şikayet etmeye çalıştığında IllegalArgumentException fırlatılmalı")
    void createAdComplaint_SelfReportingError() {
        ad.setUser(reporter);

        AdComplaintRequestDTO request = AdComplaintRequestDTO.builder()
                .reportedAdId(10L)
                .reason(ComplaintReason.SAHTE_ILAN)
                .description("Açıklama")
                .build();

        when(userRepository.findByEmail("reporter@patimati.com")).thenReturn(Optional.of(reporter));
        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                adComplaintService.createAdComplaint("reporter@patimati.com", request)
        );
        assertEquals("Kullanıcı kendi ilanını şikayet edemez.", ex.getMessage());
    }
}
