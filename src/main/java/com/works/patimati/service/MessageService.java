package com.works.patimati.service;

import com.works.patimati.dto.message.MessageSendRequest;
import com.works.patimati.dto.message.MessageResponse;
import com.works.patimati.entity.Message;
import com.works.patimati.entity.User;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.MessageRepository;
import com.works.patimati.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository; // görev 1 alan kişi message entity yazdığında dikkat etsin bu kısımların adlandırılmasında
    private final UserRepository userRepository;
    private final MessageFraudFilterService fraudFilterService;
    @Transactional
    public MessageResponse sendMessage(String senderEmail, MessageSendRequest request) {
        User sender = findUserByEmail(senderEmail);
        User recipient = userRepository.findById(request.recipientId())
                .orElseThrow(() -> new ResourceNotFoundException("Alıcı kullanıcı bulunamadı: " + request.recipientId()));
        String filteredContent = fraudFilterService.filterContent(request.content());
        Message message = Message.builder()
                .sender(sender)
                .recipient(recipient)
                .content(filteredContent)
                .timestamp(Instant.now())
                .read(false)
                .build();

        Message savedMessage = messageRepository.save(message);

        return mapToResponse(savedMessage);
    }
    @Transactional(readOnly = true)
    public Page<MessageResponse> getChatHistory(String currentUserEmail, Long otherUserId, Pageable pageable) {
        User currentUser = findUserByEmail(currentUserEmail);

        return messageRepository.findChatHistory(currentUser.getUid(), otherUserId, pageable)
                .map(this::mapToResponse);
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
        return new MessageResponse(
                message.getId(),
                message.getSender().getUid(),
                message.getSender().getFirstName() + " " + message.getSender().getLastName(),
                message.getRecipient().getUid(),
                message.getRecipient().getFirstName() + " " + message.getRecipient().getLastName(),
                message.getContent(),
                message.getTimestamp(),
                message.isRead()
        );
    }
}