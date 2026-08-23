package com.works.patimati.dto;

import java.time.OffsetDateTime;
import java.util.Map;

public record NotificationResponse(
        Long id,
        String title,
        String body,
        String type,
        String referenceId,
        Map<String, String> data,
        boolean read,
        OffsetDateTime createdAt
) {
}
