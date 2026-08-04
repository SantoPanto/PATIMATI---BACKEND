package com.works.patimati.service;

import com.works.patimati.dto.complaint.AdComplaintRequestDTO;
import com.works.patimati.dto.complaint.ComplaintResponse;
import com.works.patimati.dto.complaint.UserComplaintRequestDTO;
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

/**
 * Şikayet işlemleri iş mantığı servisi.
 * Kullanıcı şikayeti ve İlan şikayeti olmak üzere iki ayrı akış sunar.
 */
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

    /**
     * Kullanıcı Profil Şikayeti Oluşturma Akışı:
     * 1. Gelen reportedUserId (uid) değerinin veritabanında varlığını kontrol eder, yoksa 404 (ResourceNotFoundException) fırlatır.
     * 2. Şikayet eden kullanıcının (reporter) bilgilerini Spring Security Context'ten gelen e-posta ile doğrular.
     * 3. Kullanıcının kendi profilini şikayet etmesini engeller (400 Bad Request / IllegalArgumentException).
     * 4. Aksi durumda reportedAdId alanı null olarak veritabanına kaydedilir.
     * 5. Mükerrer şikayet kontrolü yapar (409 Conflict / IllegalStateException).
     *
     * @param reporterEmail Şikayeti oluşturan (oturum açmış) kullanıcının e-postası.
     * @param request       Kullanıcı şikayet isteği DTO'su.
     * @return ComplaintResponse
     */
    @Transactional
    public ComplaintResponse createUserComplaint(String reporterEmail, UserComplaintRequestDTO request) {
        // 1. Şikayet eden kullanıcıyı veritabanından çek
        User reporter = userRepository.findByEmail(reporterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Şikayet eden kullanıcı bulunamadı: " + reporterEmail));

        // 2. Şikayet edilen kullanıcıyı (reportedUserId) veritabanında doğrula (bulunamazsa 404)
        User reportedUser = userRepository.findById(request.getReportedUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Şikayet edilen kullanıcı bulunamadı ID: " + request.getReportedUserId()));

        // 3. Kendi profilini şikayet etme engeli
        if (reportedUser.getUid().equals(reporter.getUid())) {
            throw new IllegalArgumentException("Kullanıcı kendi profilini şikayet edemez.");
        }

        // 4. Mükerrerlik / Rate-Limiting Kontrolü (Aynı kullanıcı için aktif şikayet var mı?)
        boolean existingUserComplaint = complaintRepository.existsByReporter_UidAndReportedUser_UidAndStatusIn(
                reporter.getUid(), reportedUser.getUid(), ACTIVE_STATUSES
        );
        if (existingUserComplaint) {
            throw new IllegalStateException("Bu kullanıcı için halihazırda incelenmekte olan bir şikayetiniz bulunmaktadır.");
        }

        // 5. Entity oluşturma ve kaydetme (reportedAd alanı null olarak atanır)
        Complaint complaint = Complaint.builder()
                .reporter(reporter)
                .reportedAd(null)
                .reportedUser(reportedUser)
                .reason(request.getReason())
                .description(request.getDescription().trim())
                .status(ComplaintStatus.BEKLEMEDE)
                .build();

        Complaint savedComplaint = complaintRepository.save(complaint);

        return mapToResponse(savedComplaint);
    }

    /**
     * İlan Şikayeti Oluşturma Akışı:
     * 1. Gelen reportedAdId ile ilgili ilanı veritabanından bulur, bulamazsa 404 (ResourceNotFoundException) fırlatır.
     * 2. İlan bulunursa, ilanın sahibinin (User) uid bilgisini arka planda otomatik olarak çeker ve reportedUser alanına atar.
     * 3. Şikayet eden kullanıcının (reporter) kendi ilanını şikayet etmesini engeller.
     * 4. Mükerrer şikayet kontrolü yapar (409 Conflict / IllegalStateException).
     *
     * @param reporterEmail Şikayeti oluşturan (oturum açmış) kullanıcının e-postası.
     * @param request       İlan şikayet isteği DTO'su.
     * @return ComplaintResponse
     */
    @Transactional
    public ComplaintResponse createAdComplaint(String reporterEmail, AdComplaintRequestDTO request) {
        // 1. Şikayet eden kullanıcıyı veritabanından çek
        User reporter = userRepository.findByEmail(reporterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Şikayet eden kullanıcı bulunamadı: " + reporterEmail));

        // 2. İlanı veritabanında doğrula (bulunamazsa 404)
        Ad reportedAd = adRepository.findById(request.getReportedAdId())
                .orElseThrow(() -> new ResourceNotFoundException("Şikayet edilen ilan bulunamadı ID: " + request.getReportedAdId()));

        // Güvenlik: İlanın sahibini arka planda otomatik olarak çek (client manipülasyonunu engelle)
        User reportedUser = reportedAd.getUser();

        // 3. Kendi ilanını şikayet etme engeli
        if (reportedUser != null && reportedUser.getUid().equals(reporter.getUid())) {
            throw new IllegalArgumentException("Kullanıcı kendi ilanını şikayet edemez.");
        }

        // 4. Mükerrerlik / Rate-Limiting Kontrolü (Aynı ilan için aktif şikayet var mı?)
        boolean existingAdComplaint = complaintRepository.existsByReporter_UidAndReportedAd_IdAndStatusIn(
                reporter.getUid(), reportedAd.getId(), ACTIVE_STATUSES
        );
        if (existingAdComplaint) {
            throw new IllegalStateException("Bu ilan için halihazırda incelenmekte olan bir şikayetiniz bulunmaktadır.");
        }

        // 5. Entity oluşturma ve kaydetme
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

    /**
     * Complaint Entity nesnesini ComplaintResponse DTO'suna dönüştürür.
     */
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
