package com.works.patimati.dto.admin;

import com.works.patimati.entity.enums.ComplaintReason;
import com.works.patimati.entity.enums.ComplaintStatus;

import java.time.Instant;

/**
 * Admin paneli ilan şikayeti inceleme detay yanıt objesi.
 * İlan başlığı, ilan sahibi ve şikayet eden kullanıcı bağlam bilgilerini barındırır.
 */
public record AdComplaintAdminResponse(
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
