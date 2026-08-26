package com.works.patimati.service;

import com.works.patimati.dto.vet.VetClinicReviewResponse;
import com.works.patimati.dto.vet.VetClinicReviewUpsertRequest;
import com.works.patimati.entity.User;
import com.works.patimati.entity.VetClinic;
import com.works.patimati.entity.VetClinicReview;
import com.works.patimati.exception.BusinessException;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.repository.VetClinicRepository;
import com.works.patimati.repository.VetClinicReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Klinik değerlendirme (1-5 yıldız + yorum) akışı -- {@link VetCustomerService}
 * ile AYNI find-or-create-then-save deseni: yazar (vetClinic, author) başına
 * TEK satır, upsert edilir.
 */
@Service
@RequiredArgsConstructor
public class VetClinicReviewService {

    private final VetClinicReviewRepository vetClinicReviewRepository;
    private final VetClinicRepository vetClinicRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public VetClinicReviewResponse upsertMine(String authorEmail, Long clinicId, VetClinicReviewUpsertRequest request) {
        User author = findUserByEmail(authorEmail);
        VetClinic clinic = findClinicById(clinicId);

        if (clinic.getUser().getUid().equals(author.getUid())) {
            throw new BusinessException("Kendi klinik kartınızı değerlendiremezsiniz");
        }

        Optional<VetClinicReview> existing =
                vetClinicReviewRepository.findByVetClinic_IdAndAuthor_Uid(clinicId, author.getUid());

        boolean isNew = existing.isEmpty();
        VetClinicReview review = existing.orElseGet(() -> VetClinicReview.builder()
                .vetClinic(clinic)
                .author(author)
                .build());

        review.setRating(request.rating());
        review.setComment(request.comment());
        review.setUpdatedAt(OffsetDateTime.now());

        VetClinicReview saved = vetClinicReviewRepository.save(review);

        // Bildirim spamini önlemek için yalnızca OLUŞTURMA anında gönderilir --
        // her düzenlemede değil.
        if (isNew) {
            notificationService.createAndSend(
                    clinic.getUser(),
                    "Kliniğinize yeni bir değerlendirme geldi",
                    author.getFirstName() + " " + author.getLastName() + " kliniğinize " + request.rating()
                            + " yıldız verdi.",
                    "VET_CLINIC_REVIEW",
                    Map.of("clinicId", String.valueOf(clinic.getId()))
            );
        }

        return toResponse(saved, author.getUid());
    }

    /** Sayfalı yorum listesi -- görüntüleyici isteğe bağlı, anonim çağrılarda {@code null}. */
    @Transactional(readOnly = true)
    public Page<VetClinicReviewResponse> listForClinic(Long clinicId, Pageable pageable, Long viewerUidOrNull) {
        return vetClinicReviewRepository.findByVetClinic_IdOrderByCreatedAtDesc(clinicId, pageable)
                .map(review -> toResponse(review, viewerUidOrNull));
    }

    /** Kimliği doğrulanmış çağıranın e-postasını {@code User.uid}'e çözer -- controller'ın {@code canEdit} hesabı için. */
    @Transactional(readOnly = true)
    public Long resolveViewerUid(String authenticatedEmail) {
        return findUserByEmail(authenticatedEmail).getUid();
    }

    /** Çağıranın kendi yorumu -- yoksa 404 ({@code getMyRequestStatus} 404-as-null sözleşmesiyle tutarlı). */
    @Transactional(readOnly = true)
    public VetClinicReviewResponse getMine(String authorEmail, Long clinicId) {
        User author = findUserByEmail(authorEmail);
        VetClinicReview review = vetClinicReviewRepository.findByVetClinic_IdAndAuthor_Uid(clinicId, author.getUid())
                .orElseThrow(() -> new ResourceNotFoundException("Bu kliniğe yaptığınız bir değerlendirme yok"));
        return toResponse(review, author.getUid());
    }

    /** Arama (clinicId, callerUid) ile sınırlı -- başka bir kullanıcının yorumunu silme yolu hiç açılmıyor. */
    @Transactional
    public void deleteMine(String authorEmail, Long clinicId) {
        User author = findUserByEmail(authorEmail);
        VetClinicReview review = vetClinicReviewRepository.findByVetClinic_IdAndAuthor_Uid(clinicId, author.getUid())
                .orElseThrow(() -> new ResourceNotFoundException("Bu kliniğe yaptığınız bir değerlendirme yok"));
        vetClinicReviewRepository.delete(review);
    }

    private VetClinicReviewResponse toResponse(VetClinicReview review, Long viewerUidOrNull) {
        User author = review.getAuthor();
        boolean canEdit = viewerUidOrNull != null && viewerUidOrNull.equals(author.getUid());

        return new VetClinicReviewResponse(
                review.getId(),
                author.getUid(),
                author.getFirstName() + " " + author.getLastName(),
                review.getRating(),
                review.getComment(),
                canEdit,
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }

    private VetClinic findClinicById(Long clinicId) {
        return vetClinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("Klinik bulunamadı: " + clinicId));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + email));
    }
}
