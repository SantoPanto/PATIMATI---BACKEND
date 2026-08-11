package com.works.patimati.dto.message;

import java.time.Instant;

public record MessageResponse(
        Long id,
        Long senderId,
        String senderName,
        Long recipientId,
        String recipientName,
        String content,
        Instant timestamp,
        boolean isRead
) {
}