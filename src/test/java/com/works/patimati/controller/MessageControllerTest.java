package com.works.patimati.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.works.patimati.dto.message.MessageResponse;
import com.works.patimati.dto.message.MessageSendRequest;
import com.works.patimati.exception.GlobalExceptionHandler;
import com.works.patimati.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MessageControllerTest {

    private MessageService messageService;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        messageService = mock(MessageService.class);
        objectMapper = new ObjectMapper();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new MessageController(messageService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/messages - String principal ile mesaj başarıyla gönderilmeli")
    void shouldSendMessageSuccessfullyWithAuthentication() throws Exception {
        String senderEmail = "sender@patimati.com";
        MessageSendRequest request = new MessageSendRequest(2L, "Merhaba test mesajı");
        MessageResponse mockResponse = new MessageResponse(
                1L,
                1L, "Gönderen Adı",
                2L, "Alıcı Adı",
                2L, "Alıcı Adı",
                null,
                "Merhaba test mesajı",
                Instant.now(),
                false,
                null
        );

        when(messageService.sendMessage(eq(senderEmail), any(MessageSendRequest.class)))
                .thenReturn(mockResponse);

        Authentication auth = new UsernamePasswordAuthenticationToken(senderEmail, null, List.of());

        mockMvc.perform(post("/api/v1/messages")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value("Merhaba test mesajı"));

        verify(messageService, times(1)).sendMessage(eq(senderEmail), any(MessageSendRequest.class));
    }

    @Test
    @DisplayName("GET /api/v1/messages/history/{otherUserId} - Sohbet geçmişi başarıyla döndürülmeli")
    void shouldGetChatHistorySuccessfully() throws Exception {
        String currentUserEmail = "user@patimati.com";
        Long otherUserId = 2L;

        MessageResponse mockMessage = new MessageResponse(
                1L, 1L, "User One", 2L, "User Two", 2L, "User Two",
                null, "Selam", Instant.now(), true, null
        );
        Page<MessageResponse> mockPage = new PageImpl<>(List.of(mockMessage), PageRequest.of(0, 20), 1);

        when(messageService.getChatHistory(eq(currentUserEmail), eq(otherUserId), any()))
                .thenReturn(mockPage);

        Authentication auth = new UsernamePasswordAuthenticationToken(currentUserEmail, null, List.of());

        mockMvc.perform(get("/api/v1/messages/history/{otherUserId}", otherUserId)
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L));

        verify(messageService, times(1)).getChatHistory(eq(currentUserEmail), eq(otherUserId), any());
    }

    @Test
    @DisplayName("PUT /api/v1/messages/{id}/read - Mesaj okundu olarak işaretlenmeli")
    void shouldMarkMessageAsReadSuccessfully() throws Exception {
        String currentUserEmail = "user@patimati.com";
        Long messageId = 100L;

        doNothing().when(messageService).markAsRead(currentUserEmail, messageId);

        Authentication auth = new UsernamePasswordAuthenticationToken(currentUserEmail, null, List.of());

        mockMvc.perform(put("/api/v1/messages/{id}/read", messageId)
                        .principal(auth))
                .andExpect(status().isOk());

        verify(messageService, times(1)).markAsRead(currentUserEmail, messageId);
    }

    @Test
    @DisplayName("GET /api/v1/messages/unread-count - Okunmamış mesaj sayısı başarıyla döndürülmeli")
    void shouldGetUnreadMessageCountSuccessfully() throws Exception {
        String currentUserEmail = "user@patimati.com";

        when(messageService.getUnreadMessageCount(currentUserEmail)).thenReturn(5L);

        Authentication auth = new UsernamePasswordAuthenticationToken(currentUserEmail, null, List.of());

        mockMvc.perform(get("/api/v1/messages/unread-count")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        verify(messageService, times(1)).getUnreadMessageCount(currentUserEmail);
    }
}
