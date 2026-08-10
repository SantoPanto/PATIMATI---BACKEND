package com.works.patimati.dto.admin;

import com.works.patimati.entity.enums.ComplaintReason;
import com.works.patimati.entity.enums.ComplaintStatus;

import java.time.Instant;

/**
 * Admin paneli sahiplendirme ilanı şikayeti inceleme detay yanıt objesi.
 */
public record AdoptionComplaintAdminResponse(
        Long id,
        Long reporterId,
        String reporterFullName,
        String reporterEmail,
        Long adId,
        String adTitle,
        Long adOwnerId,
        String adOwnerFullName,
        ComplaintReason reason,
        String description,
        ComplaintStatus status,
        Instant createdAt
) {
}
