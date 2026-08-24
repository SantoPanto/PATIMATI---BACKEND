package com.works.patimati.controller;

import com.works.patimati.dto.message.ChatRoomResponseDTO;
import com.works.patimati.dto.message.MessageResponse;
import com.works.patimati.dto.message.MessageSendRequest;
import com.works.patimati.service.MessageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class ChatController {

    private static final int MAX_PAGE_SIZE = 100;

    private final MessageService messageService;

    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomResponseDTO>> getUserChatRooms(Authentication authentication) {
        return ResponseEntity.ok(
                messageService.getUserChatRooms(authentication.getName())
        );
    }

    @PostMapping("/rooms/{partnerId}")
    public ResponseEntity<ChatRoomResponseDTO> createOrGetRoom(
            @PathVariable Long partnerId,
            Principal principal
    ) {
        return ResponseEntity.ok(
                messageService.createOrGetRoom(principal.getName(), partnerId)
        );
    }

    @MessageMapping("/chat")
    public void processMessage(@Payload @Valid MessageSendRequest request, Principal principal) {
        messageService.sendMessage(principal.getName(), request);
    }

    @GetMapping({"/history/{otherUserId}", "/rooms/{otherUserId}/messages"})
    public ResponseEntity<Page<MessageResponse>> getChatHistory(
            Authentication authentication,
            @PathVariable Long otherUserId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(MAX_PAGE_SIZE) int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
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