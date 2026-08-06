package com.works.patimati.dto.complaint;

import com.works.patimati.entity.enums.ComplaintReason;
import com.works.patimati.entity.enums.ComplaintStatus;

import java.time.Instant;

public record ComplaintResponse(
        Long id,
        Long reporterId,
        String reporterEmail,
        Long reportedAdId,
        Long reportedUserId,
        ComplaintReason reason,
        String description,
        ComplaintStatus status,
        Instant createdAt
) {
}
