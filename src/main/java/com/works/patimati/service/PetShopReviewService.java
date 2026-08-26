package com.works.patimati.service;

import com.works.patimati.dto.petshop.PetShopReviewResponse;
import com.works.patimati.dto.petshop.PetShopReviewUpsertRequest;
import com.works.patimati.entity.PetShop;
import com.works.patimati.entity.PetShopReview;
import com.works.patimati.entity.User;
import com.works.patimati.exception.BusinessException;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.PetShopRepository;
import com.works.patimati.repository.PetShopReviewRepository;
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
 * Petshop (dükkan seviyesi) değerlendirme (1-5 yıldız + yorum) akışı --
 * {@link ShelterReviewService}'in {@code petShop} alanına göre birebir
 * kopyası: yazar (petShop, author) başına TEK satır, upsert edilir. Ürün
 * bazlı {@link PetShopProductReviewService} ile KARIŞTIRILMAMALI.
 */
@Service
@RequiredArgsConstructor
public class PetShopReviewService {

    private final PetShopReviewRepository petShopReviewRepository;
    private final PetShopRepository petShopRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public PetShopReviewResponse upsertMine(String authorEmail, Long petShopId, PetShopReviewUpsertRequest request) {
        User author = findUserByEmail(authorEmail);
        PetShop petShop = findPetShopById(petShopId);

        if (petShop.getUser().getUid().equals(author.getUid())) {
            throw new BusinessException("Kendi petshop kartınızı değerlendiremezsiniz");
        }

        Optional<PetShopReview> existing =
                petShopReviewRepository.findByPetShop_IdAndAuthor_Uid(petShopId, author.getUid());

        boolean isNew = existing.isEmpty();
        PetShopReview review = existing.orElseGet(() -> PetShopReview.builder()
                .petShop(petShop)
                .author(author)
                .build());

        review.setRating(request.rating());
        review.setComment(request.comment());
        review.setUpdatedAt(OffsetDateTime.now());

        PetShopReview saved = petShopReviewRepository.save(review);

        // Bildirim spamini önlemek için yalnızca OLUŞTURMA anında gönderilir --
        // her düzenlemede değil.
        if (isNew) {
            notificationService.createAndSend(
                    petShop.getUser(),
                    "Petshop'unuza yeni bir değerlendirme geldi",
                    author.getFirstName() + " " + author.getLastName() + " petshop'unuza " + request.rating()
                            + " yıldız verdi.",
                    "PETSHOP_REVIEW",
                    Map.of("petShopId", String.valueOf(petShop.getId()))
            );
        }

        return toResponse(saved, author.getUid());
    }

    /** Sayfalı yorum listesi -- görüntüleyici isteğe bağlı, anonim çağrılarda {@code null}. */
    @Transactional(readOnly = true)
    public Page<PetShopReviewResponse> listForPetShop(Long petShopId, Pageable pageable, Long viewerUidOrNull) {
        return petShopReviewRepository.findByPetShop_IdOrderByCreatedAtDesc(petShopId, pageable)
                .map(review -> toResponse(review, viewerUidOrNull));
    }

    /** Kimliği doğrulanmış çağıranın e-postasını {@code User.uid}'e çözer -- controller'ın {@code canEdit} hesabı için. */
    @Transactional(readOnly = true)
    public Long resolveViewerUid(String authenticatedEmail) {
        return findUserByEmail(authenticatedEmail).getUid();
    }

    /** Çağıranın kendi yorumu -- yoksa 404. */
    @Transactional(readOnly = true)
    public PetShopReviewResponse getMine(String authorEmail, Long petShopId) {
        User author = findUserByEmail(authorEmail);
        PetShopReview review = petShopReviewRepository.findByPetShop_IdAndAuthor_Uid(petShopId, author.getUid())
                .orElseThrow(() -> new ResourceNotFoundException("Bu petshop'a yaptığınız bir değerlendirme yok"));
        return toResponse(review, author.getUid());
    }

    /** Arama (petShopId, callerUid) ile sınırlı -- başka bir kullanıcının yorumunu silme yolu hiç açılmıyor. */
    @Transactional
    public void deleteMine(String authorEmail, Long petShopId) {
        User author = findUserByEmail(authorEmail);
        PetShopReview review = petShopReviewRepository.findByPetShop_IdAndAuthor_Uid(petShopId, author.getUid())
                .orElseThrow(() -> new ResourceNotFoundException("Bu petshop'a yaptığınız bir değerlendirme yok"));
        petShopReviewRepository.delete(review);
    }

    private PetShopReviewResponse toResponse(PetShopReview review, Long viewerUidOrNull) {
        User author = review.getAuthor();
        boolean canEdit = viewerUidOrNull != null && viewerUidOrNull.equals(author.getUid());

        return new PetShopReviewResponse(
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

    private PetShop findPetShopById(Long petShopId) {
        return petShopRepository.findById(petShopId)
                .orElseThrow(() -> new ResourceNotFoundException("Petshop bulunamadı: " + petShopId));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + email));
    }
}
