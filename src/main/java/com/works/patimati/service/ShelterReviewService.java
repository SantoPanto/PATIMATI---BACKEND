package com.works.patimati.service;

import com.works.patimati.dto.shelter.ShelterReviewResponse;
import com.works.patimati.dto.shelter.ShelterReviewUpsertRequest;
import com.works.patimati.entity.Shelter;
import com.works.patimati.entity.ShelterReview;
import com.works.patimati.entity.User;
import com.works.patimati.exception.BusinessException;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.ShelterRepository;
import com.works.patimati.repository.ShelterReviewRepository;
import com.works.patimati.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Barınak değerlendirme (1-5 yıldız + yorum) akışı -- {@link VetClinicReviewService}'in
 * {@code shelter} alanına göre birebir kopyası: yazar (shelter, author)
 * başına TEK satır, upsert edilir.
 */
@Service
@RequiredArgsConstructor
public class ShelterReviewService {

    private final ShelterReviewRepository shelterReviewRepository;
    private final ShelterRepository shelterRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public ShelterReviewResponse upsertMine(String authorEmail, Long shelterId, ShelterReviewUpsertRequest request) {
        User author = findUserByEmail(authorEmail);
        Shelter shelter = findShelterById(shelterId);

        if (shelter.getUser().getUid().equals(author.getUid())) {
            throw new BusinessException("Kendi barınak kartınızı değerlendiremezsiniz");
        }

        Optional<ShelterReview> existing =
                shelterReviewRepository.findByShelter_IdAndAuthor_Uid(shelterId, author.getUid());

        boolean isNew = existing.isEmpty();
        ShelterReview review = existing.orElseGet(() -> ShelterReview.builder()
                .shelter(shelter)
                .author(author)
                .build());

        review.setRating(request.rating());
        review.setComment(request.comment());
        review.setUpdatedAt(OffsetDateTime.now());

        ShelterReview saved = shelterReviewRepository.save(review);

        // Bildirim spamini önlemek için yalnızca OLUŞTURMA anında gönderilir --
        // her düzenlemede değil.
        if (isNew) {
            notificationService.createAndSend(
                    shelter.getUser(),
                    "Barınağınıza yeni bir değerlendirme geldi",
                    author.getFirstName() + " " + author.getLastName() + " barınağınıza " + request.rating()
                            + " yıldız verdi.",
                    "SHELTER_REVIEW",
                    Map.of("shelterId", String.valueOf(shelter.getId()))
            );
        }

        return toResponse(saved, author.getUid());
    }

    /** Sayfalı yorum listesi -- görüntüleyici isteğe bağlı, anonim çağrılarda {@code null}. */
    @Transactional(readOnly = true)
    public Page<ShelterReviewResponse> listForShelter(Long shelterId, Pageable pageable, Long viewerUidOrNull) {
        return shelterReviewRepository.findByShelter_IdOrderByCreatedAtDesc(shelterId, pageable)
                .map(review -> toResponse(review, viewerUidOrNull));
    }

    /** Kimliği doğrulanmış çağıranın e-postasını {@code User.uid}'e çözer -- controller'ın {@code canEdit} hesabı için. */
    @Transactional(readOnly = true)
    public Long resolveViewerUid(String authenticatedEmail) {
        return findUserByEmail(authenticatedEmail).getUid();
    }

    /** Çağıranın kendi yorumu -- yoksa 404. */
    @Transactional(readOnly = true)
    public ShelterReviewResponse getMine(String authorEmail, Long shelterId) {
        User author = findUserByEmail(authorEmail);
        ShelterReview review = shelterReviewRepository.findByShelter_IdAndAuthor_Uid(shelterId, author.getUid())
                .orElseThrow(() -> new ResourceNotFoundException("Bu barınağa yaptığınız bir değerlendirme yok"));
        return toResponse(review, author.getUid());
    }

    /** Arama (shelterId, callerUid) ile sınırlı -- başka bir kullanıcının yorumunu silme yolu hiç açılmıyor. */
    @Transactional
    public void deleteMine(String authorEmail, Long shelterId) {
        User author = findUserByEmail(authorEmail);
        ShelterReview review = shelterReviewRepository.findByShelter_IdAndAuthor_Uid(shelterId, author.getUid())
                .orElseThrow(() -> new ResourceNotFoundException("Bu barınağa yaptığınız bir değerlendirme yok"));
        shelterReviewRepository.delete(review);
    }

    private ShelterReviewResponse toResponse(ShelterReview review, Long viewerUidOrNull) {
        User author = review.getAuthor();
        boolean canEdit = viewerUidOrNull != null && viewerUidOrNull.equals(author.getUid());

        return new ShelterReviewResponse(
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

    private Shelter findShelterById(Long shelterId) {
        return shelterRepository.findById(shelterId)
                .orElseThrow(() -> new ResourceNotFoundException("Barınak bulunamadı: " + shelterId));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + email));
    }
}
