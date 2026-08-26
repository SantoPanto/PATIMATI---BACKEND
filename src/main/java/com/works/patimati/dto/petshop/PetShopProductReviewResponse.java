package com.works.patimati.dto.petshop;

import java.time.OffsetDateTime;

/**
 * Ürün değerlendirme görünümü -- {@code VetClinicReviewResponse}'un birebir
 * kopyası. {@code GET /api/petshop-products/{id}/reviews}.
 */
public record PetShopProductReviewResponse(
        Long id,
        Long authorId,
        String authorName,
        Integer rating,
        String comment,
        /** Çağıran bu yorumun ORİJİNAL yazarı mı -- öyleyse düzenle/sil gösterilebilir. */
        boolean canEdit,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
