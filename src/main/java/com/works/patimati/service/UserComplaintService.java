package com.works.patimati.service;

import com.works.patimati.dto.complaint.ComplaintResponse;
import com.works.patimati.dto.complaint.UserComplaintRequestDTO;
import com.works.patimati.entity.User;
import com.works.patimati.entity.UserComplaint;
import com.works.patimati.entity.enums.ComplaintStatus;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.UserComplaintRepository;
import com.works.patimati.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Kullanıcı Profil Şikayetleri İş Mantığı Servisi (Single Responsibility Principle).
 * Yalnızca 'user_complaints' tablosu ve kullanıcı şikayeti bounded context süreçlerini yönetir.
 */
@Service
@RequiredArgsConstructor
public class UserComplaintService {

    private static final List<ComplaintStatus> ACTIVE_STATUSES = List.of(
            ComplaintStatus.BEKLEMEDE,
            ComplaintStatus.INCELEMEDE
    );

    private final UserComplaintRepository userComplaintRepository;
    private final UserRepository userRepository;

    /**
     * Kullanıcı Profil Şikayeti Oluşturma İş Akışı:
     * 1. Şikayeti oluşturan (reporter) kullanıcı Spring Security oturum e-postası ile bulunur.
     * 2. Şikayet edilen kullanıcı (reportedUserId) veritabanında doğrulanır (Yoksa 404 ResourceNotFoundException).
     * 3. Kullanıcının kendi profilini şikayet etmesi engellenir (400 IllegalArgumentException).
     * 4. Mükerrer şikayet denetimi UserComplaintRepository.existsByReporterIdAndReportedUserIdAndStatusIn ile yapılır (409 IllegalStateException).
     * 5. UserComplaint nesnesi 'user_complaints' tablosuna yazılır.
     *
     * @param reporterEmail Şikayet eden oturum kullanıcısının e-postası
     * @param request       Kullanıcı şikayet DTO'su (reportedUserId, reason, description)
     * @return ComplaintResponse DTO
     */
    @Transactional
    public ComplaintResponse createUserComplaint(String reporterEmail, UserComplaintRequestDTO request) {
        // 1. Şikayet eden kullanıcı doğrulaması
        User reporter = userRepository.findByEmail(reporterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Şikayet eden kullanıcı bulunamadı: " + reporterEmail));

        // 2. Şikayet edilen kullanıcı doğrulaması (404 Not Found)
        User reportedUser = userRepository.findById(request.getReportedUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Şikayet edilen kullanıcı bulunamadı ID: " + request.getReportedUserId()));

        // 3. Kendi profilini şikayet etme engeli
        if (reportedUser.getUid().equals(reporter.getUid())) {
            throw new IllegalArgumentException("Kullanıcı kendi profilini şikayet edemez.");
        }

        // 4. Mükerrer şikayet kontrolü (existsByReporterIdAndReportedUserIdAndStatusIn)
        boolean existingUserComplaint = userComplaintRepository.existsByReporterIdAndReportedUserIdAndStatusIn(
                reporter.getUid(), reportedUser.getUid(), ACTIVE_STATUSES
        );
        if (existingUserComplaint) {
            throw new IllegalStateException("Bu kullanıcı için halihazırda incelenmekte olan bir şikayetiniz bulunmaktadır.");
        }

        // 5. UserComplaint kaydı ('user_complaints' tablosu)
        UserComplaint userComplaint = UserComplaint.builder()
                .reporterId(reporter.getUid())
                .reportedUserId(reportedUser.getUid())
                .reason(request.getReason())
                .description(request.getDescription().trim())
                .status(ComplaintStatus.BEKLEMEDE)
                .build();

        UserComplaint savedComplaint = userComplaintRepository.save(userComplaint);

        return new ComplaintResponse(
                savedComplaint.getId(),
                savedComplaint.getReporterId(),
                reporter.getEmail(),
                null, // İlan ID'si null
                savedComplaint.getReportedUserId(),
                savedComplaint.getReason(),
                savedComplaint.getDescription(),
                savedComplaint.getStatus(),
                savedComplaint.getCreatedAt()
        );
    }
}
