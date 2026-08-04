package com.works.patimati.service;

import com.works.patimati.dto.complaint.AdComplaintRequestDTO;
import com.works.patimati.dto.complaint.ComplaintResponse;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.AdComplaint;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.ComplaintStatus;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.AdComplaintRepository;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * İlan Şikayetleri İş Mantığı Servisi (Single Responsibility Principle).
 * Yalnızca 'ad_complaints' tablosu ve ilan şikayeti bounded context süreçlerini yönetir.
 */
@Service
@RequiredArgsConstructor
public class AdComplaintService {

    private static final List<ComplaintStatus> ACTIVE_STATUSES = List.of(
            ComplaintStatus.BEKLEMEDE,
            ComplaintStatus.INCELEMEDE
    );

    private final AdComplaintRepository adComplaintRepository;
    private final UserRepository userRepository;
    private final AdRepository adRepository;

    /**
     * İlan Şikayeti Oluşturma İş Akışı:
     * 1. Şikayeti gönderen (reporter) kullanıcı Spring Security oturum e-postası ile bulunur.
     * 2. Şikayet edilmek istenen ilan (reportedAdId) veritabanında doğrulanır (Yoksa 404 ResourceNotFoundException).
     * 3. İlan sahibi otomatik tespit edilerek kullanıcının kendi ilanını şikayet etmesi engellenir (400 IllegalArgumentException).
     * 4. Mükerrer şikayet denetimi AdComplaintRepository.existsByReporterIdAndAdIdAndStatusIn ile yapılır (409 IllegalStateException).
     * 5. AdComplaint nesnesi 'ad_complaints' tablosuna yazılır (DİKKAT: Tabloda reported_user_id TUTULMAZ!).
     *
     * @param reporterEmail Şikayet eden oturum kullanıcısının e-postası
     * @param request       İlan şikayet DTO'su (reportedAdId, reason, description)
     * @return ComplaintResponse DTO
     */
    @Transactional
    public ComplaintResponse createAdComplaint(String reporterEmail, AdComplaintRequestDTO request) {
        // 1. Şikayet eden kullanıcı doğrulaması
        User reporter = userRepository.findByEmail(reporterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Şikayet eden kullanıcı bulunamadı: " + reporterEmail));

        // 2. İlan doğrulaması (404 Not Found)
        Ad reportedAd = adRepository.findById(request.getReportedAdId())
                .orElseThrow(() -> new ResourceNotFoundException("Şikayet edilen ilan bulunamadı ID: " + request.getReportedAdId()));

        // İlan sahibinin tespiti (İlan Bounded Context'i üzerinden)
        User adOwner = reportedAd.getUser();
        Long adOwnerUid = adOwner != null ? adOwner.getUid() : null;

        // 3. Kendi ilanını şikayet etme engeli
        if (adOwnerUid != null && adOwnerUid.equals(reporter.getUid())) {
            throw new IllegalArgumentException("Kullanıcı kendi ilanını şikayet edemez.");
        }

        // 4. Mükerrer şikayet kontrolü (existsByReporterIdAndAdIdAndStatusIn)
        boolean existingAdComplaint = adComplaintRepository.existsByReporterIdAndAdIdAndStatusIn(
                reporter.getUid(), reportedAd.getId(), ACTIVE_STATUSES
        );
        if (existingAdComplaint) {
            throw new IllegalStateException("Bu ilan için halihazırda incelenmekte olan bir şikayetiniz bulunmaktadır.");
        }

        // 5. AdComplaint kaydı ('ad_complaints' tablosunda reported_user_id tutulmaz!)
        AdComplaint adComplaint = AdComplaint.builder()
                .reporterId(reporter.getUid())
                .adId(reportedAd.getId())
                .reason(request.getReason())
                .description(request.getDescription().trim())
                .status(ComplaintStatus.BEKLEMEDE)
                .build();

        AdComplaint savedComplaint = adComplaintRepository.save(adComplaint);

        return new ComplaintResponse(
                savedComplaint.getId(),
                savedComplaint.getReporterId(),
                reporter.getEmail(),
                savedComplaint.getAdId(),
                adOwnerUid, // Yanıt DTO'sunda kolaylık sağlamak amacıyla ilan sahibinin ID'si aktarılır
                savedComplaint.getReason(),
                savedComplaint.getDescription(),
                savedComplaint.getStatus(),
                savedComplaint.getCreatedAt()
        );
    }
}
