package com.works.patimati.controller;

import com.works.patimati.dto.message.MessageResponse;
import com.works.patimati.dto.message.MessageSendRequest;
import com.works.patimati.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class ChatController {
    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    @MessageMapping("/chat")
    public void processMessage(@Payload @Valid MessageSendRequest request, Principal principal) {
        MessageResponse response = messageService.sendMessage(principal.getName(), request);
        messagingTemplate.convertAndSendToUser(
                String.valueOf(response.recipientId()),
                "/queue/messages",
                response
        );
    }
    @GetMapping("/history/{otherUserId}")
    public ResponseEntity<Page<MessageResponse>> getChatHistory(
            Authentication authentication,
            @PathVariable Long otherUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        return ResponseEntity.ok(
                messageService.getChatHistory(authentication.getName(), otherUserId, pageable)
        );
    }
    @PutMapping("/{messageId}/read")
    public ResponseEntity<Void> markAsRead(
            Authentication authentication,
            @PathVariable Long messageId
    ) {
        messageService.markAsRead(authentication.getName(), messageId);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        return ResponseEntity.ok(
                messageService.getUnreadMessageCount(authentication.getName())
        );
    }
}