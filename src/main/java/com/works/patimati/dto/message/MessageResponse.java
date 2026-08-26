package com.works.patimati.dto.message;

import com.works.patimati.entity.User;

import java.time.Instant;

public record MessageResponse(
        Long id,
        Long senderId,
        String senderName,
        Long recipientId,
        String recipientName,
        Long partnerId,
        String partnerName,
        String partnerAvatar,
        String content,
        Instant timestamp,
        boolean isRead,
        /** Karşı tarafın rolü -- sohbette rol rozeti (RoleBadge) gösterebilmek için. */
        User.Role partnerRole
) {
}