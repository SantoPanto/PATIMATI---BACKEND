package com.works.patimati.dto.admin;

import com.works.patimati.entity.enums.ComplaintReason;
import com.works.patimati.entity.enums.ComplaintStatus;

import java.time.Instant;

/**
 * Admin paneli kullanıcı profili şikayeti inceleme detay yanıt objesi.
 * Şikayet edilen kullanıcının ad/soyad/email ve şikayet edenin bilgilerini barındırır.
 */
public record UserComplaintAdminResponse(
        Long id,
        Long reporterId,
        String reporterFullName,
        String reporterEmail,
        Long reportedUserId,
        String reportedUserFullName,
        String reportedUserEmail,
        ComplaintReason reason,
        String description,
        ComplaintStatus status,
        Instant createdAt
) {
}
