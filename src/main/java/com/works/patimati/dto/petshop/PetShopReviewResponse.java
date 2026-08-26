package com.works.patimati.dto.petshop;

import java.time.OffsetDateTime;

/** Petshop (dükkan seviyesi) değerlendirme görünümü -- {@code GET /api/petshops/{id}/reviews}. */
public record PetShopReviewResponse(
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
