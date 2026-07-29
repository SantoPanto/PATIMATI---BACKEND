package com.works.patimati.service;

import com.works.patimati.dto.complaint.ComplaintRequest;
import com.works.patimati.dto.complaint.ComplaintResponse;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.Complaint;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.ComplaintStatus;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.ComplaintRepository;
import com.works.patimati.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ComplaintService {

    private static final List<ComplaintStatus> ACTIVE_STATUSES = List.of(
            ComplaintStatus.BEKLEMEDE,
            ComplaintStatus.INCELEMEDE
    );

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final AdRepository adRepository;

    @Transactional
    public ComplaintResponse createComplaint(String reporterEmail, ComplaintRequest request) {
        // 1. Hedef Kontrolü: En az bir hedef (ilan veya kullanıcı) belirtilmiş olmalıdır.
        if (request.getReportedAdId() == null && request.getReportedUserId() == null) {
            throw new IllegalArgumentException("Şikayet etmek için bir ilan veya bir kullanıcı belirtilmelidir.");
        }

        // 2. Şikayet eden kullanıcıyı veritabanından çek
        User reporter = userRepository.findByEmail(reporterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Şikayet eden kullanıcı bulunamadı: " + reporterEmail));

        Ad reportedAd = null;
        User reportedUser = null;

        // 3. İlan üzerinden şikayet (Otomatik İlan Sahibi Ataması)
        if (request.getReportedAdId() != null) {
            reportedAd = adRepository.findById(request.getReportedAdId())
                    .orElseThrow(() -> new ResourceNotFoundException("Şikayet edilen ilan bulunamadı ID: " + request.getReportedAdId()));
            
            // SENIOR REVIZYON: İlan şikayet edildiğinde ilanın sahibi otomatik olarak reportedUser alanına set edilir.
            reportedUser = reportedAd.getUser();
        } else if (request.getReportedUserId() != null) {
            // Yalnızca kullanıcı profili şikayet edildiğinde
            reportedUser = userRepository.findById(request.getReportedUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Şikayet edilen kullanıcı bulunamadı ID: " + request.getReportedUserId()));
        }

        // 4. Kendi ilanını veya kendi profilini şikayet etme engeli
        if (reportedUser != null && reportedUser.getUid().equals(reporter.getUid())) {
            throw new IllegalArgumentException("Kullanıcı kendi profilini veya kendi ilanını şikayet edemez.");
        }

        // 5. Mükerrerlik / Rate-Limiting Kontrolü (Composite Index Kullanımlı)
        if (reportedAd != null) {
            boolean existingAdComplaint = complaintRepository.existsByReporter_UidAndReportedAd_IdAndStatusIn(
                    reporter.getUid(), reportedAd.getId(), ACTIVE_STATUSES
            );
            if (existingAdComplaint) {
                throw new IllegalStateException("Bu ilan için halihazırda incelenmekte olan bir şikayetiniz bulunmaktadır.");
            }
        } else if (reportedUser != null) {
            boolean existingUserComplaint = complaintRepository.existsByReporter_UidAndReportedUser_UidAndStatusIn(
                    reporter.getUid(), reportedUser.getUid(), ACTIVE_STATUSES
            );
            if (existingUserComplaint) {
                throw new IllegalStateException("Bu kullanıcı için halihazırda incelenmekte olan bir şikayetiniz bulunmaktadır.");
            }
        }

        // 6. Entity oluşturma ve kaydetme
        Complaint complaint = Complaint.builder()
                .reporter(reporter)
                .reportedAd(reportedAd)
                .reportedUser(reportedUser)
                .reason(request.getReason())
                .description(request.getDescription().trim())
                .status(ComplaintStatus.BEKLEMEDE)
                .build();

        Complaint savedComplaint = complaintRepository.save(complaint);

        return mapToResponse(savedComplaint);
    }

    private ComplaintResponse mapToResponse(Complaint complaint) {
        return new ComplaintResponse(
                complaint.getId(),
                complaint.getReporter().getUid(),
                complaint.getReporter().getEmail(),
                complaint.getReportedAd() != null ? complaint.getReportedAd().getId() : null,
                complaint.getReportedUser() != null ? complaint.getReportedUser().getUid() : null,
                complaint.getReason(),
                complaint.getDescription(),
                complaint.getStatus(),
                complaint.getCreatedAt()
        );
    }
}
