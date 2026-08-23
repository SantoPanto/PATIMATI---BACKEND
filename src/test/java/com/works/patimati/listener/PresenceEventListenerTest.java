package com.works.patimati.listener;

import com.works.patimati.dto.UserStatusResponse;
import com.works.patimati.entity.User;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.service.UserPresenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PresenceEventListenerTest {

    private UserPresenceService userPresenceService;
    private UserRepository userRepository;
    private SimpMessagingTemplate messagingTemplate;
    private PresenceEventListener presenceEventListener;

    @BeforeEach
    void setUp() {
        userPresenceService = mock(UserPresenceService.class);
        userRepository = mock(UserRepository.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);
        presenceEventListener = new PresenceEventListener(
                userPresenceService,
                userRepository,
                messagingTemplate
        );
    }

    @Test
    void broadcastsOnlineStatusWhenUserConnects() {
        String email = "test@patimati.com";
        User user = User.builder().uid(10L).email(email).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userPresenceService.markConnected(10L, "session-123")).thenReturn(true);

        SessionConnectedEvent event = createConnectedEvent(email, "session-123");
        presenceEventListener.handleSessionConnected(event);

        ArgumentCaptor<UserStatusResponse> responseCaptor = ArgumentCaptor.forClass(UserStatusResponse.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/user-status"), responseCaptor.capture());

        UserStatusResponse response = responseCaptor.getValue();
        assertThat(response.userId()).isEqualTo(10L);
        assertThat(response.isOnline()).isTrue();
    }

    @Test
    void broadcastsOfflineStatusWhenUserDisconnects() {
        String email = "test@patimati.com";
        User user = User.builder().uid(10L).email(email).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userPresenceService.markDisconnected(10L, "session-123")).thenReturn(true);

        SessionDisconnectEvent event = createDisconnectEvent(email, "session-123");
        presenceEventListener.handleSessionDisconnect(event);

        ArgumentCaptor<UserStatusResponse> responseCaptor = ArgumentCaptor.forClass(UserStatusResponse.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/user-status"), responseCaptor.capture());

        UserStatusResponse response = responseCaptor.getValue();
        assertThat(response.userId()).isEqualTo(10L);
        assertThat(response.isOnline()).isFalse();
    }

    @Test
    void doesNotBroadcastStatusIfPresenceStateUnchanged() {
        String email = "test@patimati.com";
        User user = User.builder().uid(10L).email(email).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userPresenceService.markConnected(10L, "session-456")).thenReturn(false);

        SessionConnectedEvent event = createConnectedEvent(email, "session-456");
        presenceEventListener.handleSessionConnected(event);

        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    private SessionConnectedEvent createConnectedEvent(String email, String sessionId) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(org.springframework.messaging.simp.stomp.StompCommand.CONNECTED);
        accessor.setSessionId(sessionId);
        Principal principal = new UsernamePasswordAuthenticationToken(email, null);
        accessor.setUser(principal);

        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        return new SessionConnectedEvent(this, message, principal);
    }

    private SessionDisconnectEvent createDisconnectEvent(String email, String sessionId) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(org.springframework.messaging.simp.stomp.StompCommand.DISCONNECT);
        accessor.setSessionId(sessionId);
        Principal principal = new UsernamePasswordAuthenticationToken(email, null);
        accessor.setUser(principal);

        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        return new SessionDisconnectEvent(this, message, sessionId, CloseStatus.NORMAL, principal);
    }
}
