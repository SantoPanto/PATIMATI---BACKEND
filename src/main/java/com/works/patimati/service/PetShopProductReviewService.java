package com.works.patimati.service;

import com.works.patimati.dto.petshop.PetShopProductReviewResponse;
import com.works.patimati.dto.petshop.PetShopProductReviewUpsertRequest;
import com.works.patimati.entity.PetShopProduct;
import com.works.patimati.entity.PetShopProductReview;
import com.works.patimati.entity.User;
import com.works.patimati.exception.BusinessException;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.PetShopProductRepository;
import com.works.patimati.repository.PetShopProductReviewRepository;
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
 * Ürün değerlendirme (1-5 yıldız + yorum) akışı -- {@link VetClinicReviewService}'in
 * (product, author) çiftine göre birebir kopyası: find-or-create-then-save
 * deseni, yazar (product, author) başına TEK satır, upsert edilir.
 */
@Service
@RequiredArgsConstructor
public class PetShopProductReviewService {

    private final PetShopProductReviewRepository petShopProductReviewRepository;
    private final PetShopProductRepository petShopProductRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public PetShopProductReviewResponse upsertMine(String authorEmail, Long productId, PetShopProductReviewUpsertRequest request) {
        User author = findUserByEmail(authorEmail);
        PetShopProduct product = findProductById(productId);

        if (product.getPetShop().getUser().getUid().equals(author.getUid())) {
            throw new BusinessException("Kendi ürününüzü değerlendiremezsiniz");
        }

        Optional<PetShopProductReview> existing =
                petShopProductReviewRepository.findByProduct_IdAndAuthor_Uid(productId, author.getUid());

        boolean isNew = existing.isEmpty();
        PetShopProductReview review = existing.orElseGet(() -> PetShopProductReview.builder()
                .product(product)
                .author(author)
                .build());

        review.setRating(request.rating());
        review.setComment(request.comment());
        review.setUpdatedAt(OffsetDateTime.now());

        PetShopProductReview saved = petShopProductReviewRepository.save(review);

        // Bildirim spamini önlemek için yalnızca OLUŞTURMA anında gönderilir --
        // her düzenlemede değil.
        if (isNew) {
            notificationService.createAndSend(
                    product.getPetShop().getUser(),
                    "Ürününüze yeni bir değerlendirme geldi",
                    author.getFirstName() + " " + author.getLastName() + " \"" + product.getName() + "\" ürününe "
                            + request.rating() + " yıldız verdi.",
                    "PETSHOP_PRODUCT_REVIEW",
                    Map.of("productId", String.valueOf(product.getId()))
            );
        }

        return toResponse(saved, author.getUid());
    }

    /** Sayfalı yorum listesi -- görüntüleyici isteğe bağlı, anonim çağrılarda {@code null}. */
    @Transactional(readOnly = true)
    public Page<PetShopProductReviewResponse> listForProduct(Long productId, Pageable pageable, Long viewerUidOrNull) {
        return petShopProductReviewRepository.findByProduct_IdOrderByCreatedAtDesc(productId, pageable)
                .map(review -> toResponse(review, viewerUidOrNull));
    }

    /** Kimliği doğrulanmış çağıranın e-postasını {@code User.uid}'e çözer -- controller'ın {@code canEdit} hesabı için. */
    @Transactional(readOnly = true)
    public Long resolveViewerUid(String authenticatedEmail) {
        return findUserByEmail(authenticatedEmail).getUid();
    }

    /** Çağıranın kendi yorumu -- yoksa 404 ({@code getMyRequestStatus} 404-as-null sözleşmesiyle tutarlı). */
    @Transactional(readOnly = true)
    public PetShopProductReviewResponse getMine(String authorEmail, Long productId) {
        User author = findUserByEmail(authorEmail);
        PetShopProductReview review = petShopProductReviewRepository.findByProduct_IdAndAuthor_Uid(productId, author.getUid())
                .orElseThrow(() -> new ResourceNotFoundException("Bu ürüne yaptığınız bir değerlendirme yok"));
        return toResponse(review, author.getUid());
    }

    /** Arama (productId, callerUid) ile sınırlı -- başka bir kullanıcının yorumunu silme yolu hiç açılmıyor. */
    @Transactional
    public void deleteMine(String authorEmail, Long productId) {
        User author = findUserByEmail(authorEmail);
        PetShopProductReview review = petShopProductReviewRepository.findByProduct_IdAndAuthor_Uid(productId, author.getUid())
                .orElseThrow(() -> new ResourceNotFoundException("Bu ürüne yaptığınız bir değerlendirme yok"));
        petShopProductReviewRepository.delete(review);
    }

    private PetShopProductReviewResponse toResponse(PetShopProductReview review, Long viewerUidOrNull) {
        User author = review.getAuthor();
        boolean canEdit = viewerUidOrNull != null && viewerUidOrNull.equals(author.getUid());

        return new PetShopProductReviewResponse(
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

    private PetShopProduct findProductById(Long productId) {
        return petShopProductRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Ürün bulunamadı: " + productId));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + email));
    }
}
