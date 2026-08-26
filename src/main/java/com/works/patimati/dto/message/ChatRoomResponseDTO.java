package com.works.patimati.dto.message;

import com.works.patimati.entity.User;

import java.time.Instant;

public record ChatRoomResponseDTO(
        Long roomId,
        Long partnerId,
        String partnerName,
        String partnerAvatar,
        String lastMessage,
        Instant lastMessageTimestamp,
        long unreadCount,
        /** Karşı tarafın rolü -- sohbette rol rozeti (RoleBadge) gösterebilmek için. */
        User.Role partnerRole
) {
}
