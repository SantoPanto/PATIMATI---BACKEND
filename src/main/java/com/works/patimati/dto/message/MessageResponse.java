package com.works.patimati.dto.message;

import java.time.LocalDateTime;

public record MessageResponse(
        Long id,
        Long senderId,
        String senderName,
        Long recipientId,
        String recipientName,
        String content,
        LocalDateTime timestamp,
        boolean isRead
) {
}