package com.works.patimati.service;

import com.works.patimati.dto.message.MessageSendRequest;
import com.works.patimati.dto.message.MessageResponse;
import com.works.patimati.dto.message.ChatRoomResponseDTO;
import com.works.patimati.entity.Message;
import com.works.patimati.entity.User;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.MessageRepository;
import com.works.patimati.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MessageFraudFilterService fraudFilterService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public MessageResponse sendMessage(String senderEmail, MessageSendRequest request) {
        User sender = findUserByEmail(senderEmail);
        User recipient = userRepository.findById(request.recipientId())
                .orElseThrow(() -> new ResourceNotFoundException("Alıcı kullanıcı bulunamadı: " + request.recipientId()));

        if (sender.getUid().equals(recipient.getUid())) {
            throw new IllegalArgumentException("Kullanıcı kendisine mesaj gönderemez.");
        }

        String filteredContent = fraudFilterService.filterContent(request.content());
        Message message = Message.builder()
                .sender(sender)
                .recipient(recipient)
                .content(filteredContent)
                .timestamp(Instant.now())
                .read(false)
                .build();

        Message savedMessage = messageRepository.save(message);
        MessageResponse response = mapToResponse(savedMessage, sender);

        // Anlık İletim (Broadcast) - Hem alıcının hem de gönderenin özel WebSocket kuyruğuna iletiliyor
        messagingTemplate.convertAndSendToUser(
                String.valueOf(response.recipientId()),
                "/queue/messages",
                response
        );
        messagingTemplate.convertAndSendToUser(
                String.valueOf(response.senderId()),
                "/queue/messages",
                response
        );

        return response;
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponseDTO> getUserChatRooms(String currentUserEmail) {
        User currentUser = findUserByEmail(currentUserEmail);
        List<Message> recentMessages = messageRepository.findRecentMessagesPerConversation(currentUser.getUid());

        return recentMessages.stream().map(message -> {
            boolean isSenderCurrent = currentUser.getUid().equals(message.getSender().getUid());
            User partner = isSenderCurrent ? message.getRecipient() : message.getSender();
            String partnerName = partner.getFirstName() + " " + partner.getLastName();
            long unreadCount = messageRepository.countBySender_UidAndRecipient_UidAndReadFalse(partner.getUid(), currentUser.getUid());

            return new ChatRoomResponseDTO(
                    message.getId(),
                    partner.getUid(),
                    partnerName,
                    null,
                    message.getContent(),
                    message.getTimestamp(),
                    unreadCount
            );
        }).toList();
    }

    @Transactional
    public ChatRoomResponseDTO createOrGetRoom(String currentUserEmail, Long partnerId) {
        User currentUser = findUserByEmail(currentUserEmail);
        User partner = userRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + partnerId));

        if (currentUser.getUid().equals(partner.getUid())) {
            throw new IllegalArgumentException("Kullanıcı kendisi ile sohbet odası oluşturamaz.");
        }

        String partnerName = partner.getFirstName() + " " + partner.getLastName();

        Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<Message> history = messageRepository.findChatHistory(currentUser.getUid(), partner.getUid(), pageable);

        if (history.hasContent()) {
            Message lastMessage = history.getContent().get(0);
            long unreadCount = messageRepository.countBySender_UidAndRecipient_UidAndReadFalse(partner.getUid(), currentUser.getUid());
            return new ChatRoomResponseDTO(
                    lastMessage.getId(),
                    partner.getUid(),
                    partnerName,
                    null,
                    lastMessage.getContent(),
                    lastMessage.getTimestamp(),
                    unreadCount
            );
        }

        return new ChatRoomResponseDTO(
                null,
                partner.getUid(),
                partnerName,
                null,
                null,
                null,
                0
        );
    }

    @Transactional
    public ChatRoomResponseDTO createChatRoom(String currentUserEmail, Long partnerId) {
        return createOrGetRoom(currentUserEmail, partnerId);
    }

    @Transactional(readOnly = true)
    public Page<MessageResponse> getChatHistory(String currentUserEmail, Long otherUserId, Pageable pageable) {
        User currentUser = findUserByEmail(currentUserEmail);

        if (currentUser.getUid().equals(otherUserId)) {
            throw new IllegalArgumentException("Kullanıcı kendisi ile sohbet geçmişi sorgulayamaz.");
        }

        // Son mesajları almak için DESC sorgulanır
        Pageable descPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "timestamp", "id"));
        Page<Message> messagePage = messageRepository.findChatHistory(currentUser.getUid(), otherUserId, descPageable);

        // Kronolojik sırayla (ASC - Eskiden Yeniye, yeni mesaj en altta) sunmak için liste çevrilir
        List<MessageResponse> list = new ArrayList<>(messagePage.getContent().stream()
                .map(message -> mapToResponse(message, currentUser))
                .toList());
        Collections.reverse(list);

        return new PageImpl<>(list, pageable, messagePage.getTotalElements());
    }

    @Transactional
    public void markAsRead(String currentUserEmail, Long messageId) {
        User currentUser = findUserByEmail(currentUserEmail);
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Mesaj bulunamadı: " + messageId));
        if (message.getRecipient().getUid().equals(currentUser.getUid())) {
            message.setRead(true);
            messageRepository.save(message);
        }
    }

    @Transactional(readOnly = true)
    public long getUnreadMessageCount(String currentUserEmail) {
        User currentUser = findUserByEmail(currentUserEmail);
        return messageRepository.countByRecipient_UidAndReadFalse(currentUser.getUid());
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + email));
    }

    private MessageResponse mapToResponse(Message message) {
        return mapToResponse(message, message.getSender());
    }

    private MessageResponse mapToResponse(Message message, User currentUser) {
        boolean isSenderCurrent = currentUser != null && currentUser.getUid().equals(message.getSender().getUid());
        User partner = isSenderCurrent ? message.getRecipient() : message.getSender();
        String partnerName = partner.getFirstName() + " " + partner.getLastName();

        return new MessageResponse(
                message.getId(),
                message.getSender().getUid(),
                message.getSender().getFirstName() + " " + message.getSender().getLastName(),
                message.getRecipient().getUid(),
                message.getRecipient().getFirstName() + " " + message.getRecipient().getLastName(),
                partner.getUid(),
                partnerName,
                null,
                message.getContent(),
                message.getTimestamp(),
                message.isRead()
        );
    }
}