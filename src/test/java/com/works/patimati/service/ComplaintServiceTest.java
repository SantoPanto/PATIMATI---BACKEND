package com.works.patimati.service;

import com.works.patimati.dto.complaint.AdComplaintRequestDTO;
import com.works.patimati.dto.complaint.ComplaintResponse;
import com.works.patimati.dto.complaint.UserComplaintRequestDTO;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.Complaint;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.ComplaintReason;
import com.works.patimati.entity.enums.ComplaintStatus;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.ComplaintRepository;
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
class ComplaintServiceTest {

    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdRepository adRepository;

    @InjectMocks
    private ComplaintService complaintService;

    private User reporter;
    private User adOwner;
    private User reportedUser;
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

        reportedUser = User.builder()
                .uid(3L)
                .email("reported@patimati.com")
                .firstName("Ayşe")
                .lastName("Kaya")
                .build();

        ad = Ad.builder()
                .id(10L)
                .title("Kayıp Kedi")
                .description("İlan detayı")
                .user(adOwner)
                .build();
    }

    @Test
    @DisplayName("createAdComplaint - Başarılı senaryoda ilan sahibinin uid'si otomatik çekilmeli ve şikayet kaydedilmeli")
    void createAdComplaint_Success() {
        AdComplaintRequestDTO request = AdComplaintRequestDTO.builder()
                .reportedAdId(10L)
                .reason(ComplaintReason.SAHTE_ILAN)
                .description("Sahte ilan şüphesi")
                .build();

        when(userRepository.findByEmail("reporter@patimati.com")).thenReturn(Optional.of(reporter));
        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(complaintRepository.existsByReporter_UidAndReportedAd_IdAndStatusIn(eq(1L), eq(10L), any())).thenReturn(false);

        Complaint savedComplaint = Complaint.builder()
                .id(100L)
                .reporter(reporter)
                .reportedAd(ad)
                .reportedUser(adOwner)
                .reason(ComplaintReason.SAHTE_ILAN)
                .description("Sahte ilan şüphesi")
                .status(ComplaintStatus.BEKLEMEDE)
                .createdAt(Instant.now())
                .build();

        when(complaintRepository.save(any(Complaint.class))).thenReturn(savedComplaint);

        ComplaintResponse response = complaintService.createAdComplaint("reporter@patimati.com", request);

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals(10L, response.reportedAdId());
        assertEquals(2L, response.reportedUserId()); // İlan sahibinin (adOwner) uid'si otomatik atandı
        verify(complaintRepository, times(1)).save(any(Complaint.class));
    }

    @Test
    @DisplayName("createAdComplaint - İlan veritabanında bulunamadığında ResourceNotFoundException (404) fırlatılmalı")
    void createAdComplaint_AdNotFound() {
        AdComplaintRequestDTO request = AdComplaintRequestDTO.builder()
                .reportedAdId(99L)
                .reason(ComplaintReason.SAHTE_ILAN)
                .description("Açıklama")
                .build();

        when(userRepository.findByEmail("reporter@patimati.com")).thenReturn(Optional.of(reporter));
        when(adRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                complaintService.createAdComplaint("reporter@patimati.com", request)
        );
    }

    @Test
    @DisplayName("createAdComplaint - Kullanıcı kendi ilanını şikayet etmeye çalıştığında IllegalArgumentException fırlatılmalı")
    void createAdComplaint_SelfReportingError() {
        ad.setUser(reporter); // İlanın sahibi şikayet eden kişinin kendisi

        AdComplaintRequestDTO request = AdComplaintRequestDTO.builder()
                .reportedAdId(10L)
                .reason(ComplaintReason.SAHTE_ILAN)
                .description("Açıklama")
                .build();

        when(userRepository.findByEmail("reporter@patimati.com")).thenReturn(Optional.of(reporter));
        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                complaintService.createAdComplaint("reporter@patimati.com", request)
        );
        assertEquals("Kullanıcı kendi ilanını şikayet edemez.", ex.getMessage());
    }

    @Test
    @DisplayName("createUserComplaint - Başarılı senaryoda reportedAdId null olarak kaydedilmeli")
    void createUserComplaint_Success() {
        UserComplaintRequestDTO request = UserComplaintRequestDTO.builder()
                .reportedUserId(3L)
                .reason(ComplaintReason.DOLANDIRICILIK)
                .description("Dolandırıcılık şüphesi")
                .build();

        when(userRepository.findByEmail("reporter@patimati.com")).thenReturn(Optional.of(reporter));
        when(userRepository.findById(3L)).thenReturn(Optional.of(reportedUser));
        when(complaintRepository.existsByReporter_UidAndReportedUser_UidAndStatusIn(eq(1L), eq(3L), any())).thenReturn(false);

        Complaint savedComplaint = Complaint.builder()
                .id(200L)
                .reporter(reporter)
                .reportedAd(null)
                .reportedUser(reportedUser)
                .reason(ComplaintReason.DOLANDIRICILIK)
                .description("Dolandırıcılık şüphesi")
                .status(ComplaintStatus.BEKLEMEDE)
                .createdAt(Instant.now())
                .build();

        when(complaintRepository.save(any(Complaint.class))).thenReturn(savedComplaint);

        ComplaintResponse response = complaintService.createUserComplaint("reporter@patimati.com", request);

        assertNotNull(response);
        assertEquals(200L, response.id());
        assertNull(response.reportedAdId());
        assertEquals(3L, response.reportedUserId());
        verify(complaintRepository, times(1)).save(any(Complaint.class));
    }

    @Test
    @DisplayName("createUserComplaint - Şikayet edilen kullanıcı bulunamadığında ResourceNotFoundException (404) fırlatılmalı")
    void createUserComplaint_UserNotFound() {
        UserComplaintRequestDTO request = UserComplaintRequestDTO.builder()
                .reportedUserId(999L)
                .reason(ComplaintReason.DOLANDIRICILIK)
                .description("Açıklama")
                .build();

        when(userRepository.findByEmail("reporter@patimati.com")).thenReturn(Optional.of(reporter));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                complaintService.createUserComplaint("reporter@patimati.com", request)
        );
    }
}
