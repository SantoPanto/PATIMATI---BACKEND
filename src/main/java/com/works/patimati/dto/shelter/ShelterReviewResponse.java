package com.works.patimati.dto.shelter;

import java.time.OffsetDateTime;

/** Barınak değerlendirme görünümü -- {@code GET /api/shelters/{id}/reviews}. */
public record ShelterReviewResponse(
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
