package com.works.patimati.dto.admin;

import com.works.patimati.entity.Ad;
import com.works.patimati.entity.enums.InstagramPublishStatus;

import java.time.Instant;

/**
 * Admin paneli "Instagram Kuyruğu" listesi -- {@code AdComplaintAdminResponse}
 * gibi düz/denormalize: ilan bağlamı ayrı bir istek olmadan görünsün diye.
 */
public record InstagramPublishQueueAdminResponse(
        Long id,
        Long adId,
        String adTitle,
        Ad.AdType adType,
        String ownerDisplayName,
        String photoUrl,
        String suggestedCaption,
        InstagramPublishStatus status,
        String failureReason,
        Instant createdAt,
        /** Yalnızca status=PUBLISHED iken dolu -- gerçek Instagram gönderisinin bağlantısı. */
        String igPermalink
) {
}
