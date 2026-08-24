package com.works.patimati.dto.admin;

import java.time.Instant;

/**
 * Admin paneli Instagram gönderisi listeleme yanıtı — collector'ın topladığı
 * ham external_source_posts + external_pet_records satırlarının, uygulama
 * içinde eşleşme oluşturup oluşturmadığından BAĞIMSIZ, tam görünümü
 * (Faz 2 revize blueprint §5/§10'un frontend'de hiç sunulmayan tarafı).
 */
public record ExternalPostAdminResponse(
        Long id,
        String source,
        String sourcePostId,
        String canonicalUrl,
        String authorUsername,
        String caption,
        Instant detectedAt,
        String processingStatus,
        String failureReason,
        String photoUrl,
        String category,
        Float categoryConfidence,
        String species,
        String breed,
        Boolean needsReview,
        boolean hasMatch,
        Long matchedAdId
) {
}
