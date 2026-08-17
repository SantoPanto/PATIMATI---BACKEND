package com.works.patimati.dto.message;

import java.time.Instant;

public record ChatRoomResponseDTO(
        Long roomId,
        Long partnerId,
        String partnerName,
        String partnerAvatar,
        String lastMessage,
        Instant lastMessageTimestamp,
        long unreadCount
) {
}
