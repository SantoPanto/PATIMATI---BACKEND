package com.works.patimati.service;

import com.works.patimati.dto.complaint.ComplaintResponse;
import com.works.patimati.dto.complaint.UserComplaintRequestDTO;
import com.works.patimati.entity.User;
import com.works.patimati.entity.UserComplaint;
import com.works.patimati.entity.enums.ComplaintReason;
import com.works.patimati.entity.enums.ComplaintStatus;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.UserComplaintRepository;
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
class UserComplaintServiceTest {

    @Mock
    private UserComplaintRepository userComplaintRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserComplaintService userComplaintService;

    private User reporter;
    private User reportedUser;

    @BeforeEach
    void setUp() {
        reporter = User.builder()
                .uid(1L)
                .email("reporter@patimati.com")
                .firstName("Ahmet")
                .lastName("Yılmaz")
                .build();

        reportedUser = User.builder()
                .uid(3L)
                .email("reported@patimati.com")
                .firstName("Ayşe")
                .lastName("Kaya")
                .build();
    }

    @Test
    @DisplayName("createUserComplaint - Başarılı senaryoda UserComplaint entity user_complaints tablosuna kaydedilmeli")
    void createUserComplaint_Success() {
        UserComplaintRequestDTO request = UserComplaintRequestDTO.builder()
                .reportedUserId(3L)
                .reason(ComplaintReason.DOLANDIRICILIK)
                .description("Dolandırıcılık şüphesi")
                .build();

        when(userRepository.findByEmail("reporter@patimati.com")).thenReturn(Optional.of(reporter));
        when(userRepository.findById(3L)).thenReturn(Optional.of(reportedUser));
        when(userComplaintRepository.existsByReporterIdAndReportedUserIdAndStatusIn(eq(1L), eq(3L), any())).thenReturn(false);

        UserComplaint savedComplaint = UserComplaint.builder()
                .id(200L)
                .reporterId(1L)
                .reportedUserId(3L)
                .reason(ComplaintReason.DOLANDIRICILIK)
                .description("Dolandırıcılık şüphesi")
                .status(ComplaintStatus.BEKLEMEDE)
                .createdAt(Instant.now())
                .build();

        when(userComplaintRepository.save(any(UserComplaint.class))).thenReturn(savedComplaint);

        ComplaintResponse response = userComplaintService.createUserComplaint("reporter@patimati.com", request);

        assertNotNull(response);
        assertEquals(200L, response.id());
        assertNull(response.reportedAdId());
        assertEquals(3L, response.reportedUserId());
        verify(userComplaintRepository, times(1)).save(any(UserComplaint.class));
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
                userComplaintService.createUserComplaint("reporter@patimati.com", request)
        );
    }
}
