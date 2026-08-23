package com.works.patimati.service;

import com.works.patimati.dto.message.MessageResponse;
import com.works.patimati.entity.Message;
import com.works.patimati.entity.User;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.MessageRepository;
import com.works.patimati.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class MessageServiceTest {

    private MessageRepository messageRepository;
    private UserRepository userRepository;
    private AdRepository adRepository;
    private MessageService messageService;

    private User currentUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        messageRepository = mock(MessageRepository.class);
        userRepository = mock(UserRepository.class);
        adRepository = mock(AdRepository.class);

        messageService = new MessageService(
                messageRepository,
                userRepository,
                adRepository,
                mock(MessageFraudFilterService.class),
                mock(SimpMessagingTemplate.class),
                mock(NotificationService.class)
        );

        currentUser = User.builder().uid(1L).email("user1@patimati.com").firstName("User").lastName("One").build();
        otherUser = User.builder().uid(2L).email("user2@patimati.com").firstName("User").lastName("Two").build();

        when(userRepository.findByEmail("user1@patimati.com")).thenReturn(Optional.of(currentUser));
        when(userRepository.findByEmail("user2@patimati.com")).thenReturn(Optional.of(otherUser));
    }

    @Test
    @DisplayName("getChatHistory - Page 0'da timestamp DESC ve id DESC sıralaması kullanılarak en yeni 50 mesaj döndürülmeli")
    void getChatHistory_ShouldQueryWithDescSortAndReturnNewestMessages() {
        Message msg1 = Message.builder().id(100L).sender(currentUser).recipient(otherUser).content("En Yeni").timestamp(Instant.now()).build();
        Message msg2 = Message.builder().id(99L).sender(otherUser).recipient(currentUser).content("Eski").timestamp(Instant.now().minusSeconds(60)).build();

        Page<Message> mockPage = new PageImpl<>(List.of(msg1, msg2), PageRequest.of(0, 50), 2);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(messageRepository.findChatHistory(eq(1L), eq(2L), pageableCaptor.capture()))
                .thenReturn(mockPage);

        Page<MessageResponse> result = messageService.getChatHistory("user1@patimati.com", 2L, PageRequest.of(0, 50));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).id()).isEqualTo(100L);
        assertThat(result.getContent().get(0).content()).isEqualTo("En Yeni");
        assertThat(result.getContent().get(1).id()).isEqualTo(99L);
        assertThat(result.getContent().get(1).content()).isEqualTo("Eski");

        Pageable passedPageable = pageableCaptor.getValue();
        assertThat(passedPageable.getPageNumber()).isEqualTo(0);
        assertThat(passedPageable.getPageSize()).isEqualTo(50);
        assertThat(passedPageable.getSort()).isEqualTo(
                Sort.by(
                        Sort.Order.desc("timestamp"),
                        Sort.Order.desc("id")
                )
        );
    }

    @Test
    @DisplayName("getChatHistory - Kullanıcı kendisi ile sohbet geçmişi sorgulayamaz")
    void getChatHistory_SelfHistory_ShouldThrowException() {
        assertThatThrownBy(() -> messageService.getChatHistory("user1@patimati.com", 1L, PageRequest.of(0, 50)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Kullanıcı kendisi ile sohbet geçmişi sorgulayamaz.");
    }
}
