package com.works.patimati.dto.message;

import com.works.patimati.dto.ad.SharedAdDTO;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.MessageType;

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
        User.Role partnerRole,
        MessageType type,
        SharedAdDTO sharedAd
) {
    /** Geriye dönük uyum: type/sharedAd bilinmeyen çağıranlar için (düz metin varsayılır). */
    public MessageResponse(
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
            User.Role partnerRole
    ) {
        this(id, senderId, senderName, recipientId, recipientName, partnerId, partnerName, partnerAvatar, content, timestamp, isRead, partnerRole, MessageType.TEXT, null);
    }
}
