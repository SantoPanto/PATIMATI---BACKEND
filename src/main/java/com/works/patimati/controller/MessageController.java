package com.works.patimati.controller;

import com.works.patimati.dto.message.MessageResponse;
import com.works.patimati.dto.message.MessageSendRequest;
import com.works.patimati.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Message Management", description = "Mesajlaşma, sohbet geçmişi ve okundu bilgisi işlemleri")
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @Operation(
            summary = "Yeni mesaj gönder",
            description = "Belirtilen alıcıya içerik filtresinden geçmiş mesaj gönderir."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Mesaj başarıyla iletildi"),
            @ApiResponse(responseCode = "400", description = "Geçersiz istek gövdesi veya filtreye takılan içerik"),
            @ApiResponse(responseCode = "404", description = "Alıcı kullanıcı bulunamadı")
    })
    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(
            Authentication authentication,
            @Valid @RequestBody MessageSendRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(messageService.sendMessage(authentication.getName(), request));
    }

    @Operation(
            summary = "Sohbet geçmişini getir",
            description = "Belirtilen kullanıcı ile olan mesajlaşma geçmişini sayfalanmış olarak döner."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sohbet geçmişi başarıyla getirildi"),
            @ApiResponse(responseCode = "404", description = "Kullanıcı bulunamadı")
    })
    @GetMapping("/history/{otherUserId}")
    public ResponseEntity<Page<MessageResponse>> getChatHistory(
            Authentication authentication,
            @PathVariable Long otherUserId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(messageService.getChatHistory(authentication.getName(), otherUserId, pageable));
    }

    @Operation(
            summary = "Mesajı okundu olarak işaretle",
            description = "Belirtilen ID'ye sahip mesajı mevcut alıcı kullanıcı için okundu durumuna getirir."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mesaj başarıyla okundu olarak işaretlendi"),
            @ApiResponse(responseCode = "404", description = "Mesaj bulunamadı"),
            @ApiResponse(responseCode = "403", description = "Bu mesajı okundu işaretleme yetkiniz yok")
    })
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            Authentication authentication
    ) {
        messageService.markAsRead(authentication.getName(), id);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Okunmamış mesaj sayısını getir",
            description = "Giriş yapan kullanıcının gelen kutusundaki okunmamış toplam mesaj sayısını döner."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Okunmamış mesaj sayısı başarıyla getirildi")
    })
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadMessageCount(Authentication authentication) {
        return ResponseEntity.ok(messageService.getUnreadMessageCount(authentication.getName()));
    }
}