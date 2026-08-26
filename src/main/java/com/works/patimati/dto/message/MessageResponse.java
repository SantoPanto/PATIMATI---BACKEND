package com.works.patimati.dto.message;

import com.works.patimati.dto.ad.SharedAdDTO;
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
        MessageType type,
        SharedAdDTO sharedAd
) {
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
            boolean isRead
    ) {
        this(id, senderId, senderName, recipientId, recipientName, partnerId, partnerName, partnerAvatar, content, timestamp, isRead, MessageType.TEXT, null);
    }
}