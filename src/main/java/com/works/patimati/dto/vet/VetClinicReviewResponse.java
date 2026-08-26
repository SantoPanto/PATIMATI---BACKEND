package com.works.patimati.dto.vet;

import java.time.OffsetDateTime;

/** Klinik değerlendirme görünümü -- {@code GET /api/vet-clinics/{id}/reviews}. */
public record VetClinicReviewResponse(
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
