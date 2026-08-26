package com.works.patimati.presence;

import com.works.patimati.dto.UserStatusEvent;
import com.works.patimati.entity.User;
import com.works.patimati.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PresenceEventListenerTest {

    private PresenceService presenceService;
    private UserRepository userRepository;
    private SimpMessagingTemplate messagingTemplate;
    private PresenceEventListener presenceEventListener;

    @BeforeEach
    void setUp() {
        presenceService = mock(PresenceService.class);
        userRepository = mock(UserRepository.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);
        presenceEventListener = new PresenceEventListener(
                presenceService,
                userRepository,
                messagingTemplate
        );
    }

    @Test
    void broadcastsOnlineStatusWhenUserConnects() {
        String email = "test@patimati.com";
        User user = User.builder().uid(10L).email(email).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(presenceService.markConnected(10L, "session-123")).thenReturn(true);

        SessionConnectedEvent event = createConnectedEvent(email, "session-123");
        presenceEventListener.handleSessionConnected(event);

        ArgumentCaptor<UserStatusEvent> eventCaptor = ArgumentCaptor.forClass(UserStatusEvent.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/user-status"), eventCaptor.capture());

        UserStatusEvent statusEvent = eventCaptor.getValue();
        assertThat(statusEvent.userId()).isEqualTo(10L);
        assertThat(statusEvent.online()).isTrue();
        assertThat(statusEvent.status()).isEqualTo("ONLINE");
        assertThat(statusEvent.lastSeen()).isNull();
    }

    @Test
    void doesNotBroadcastWhenConnectionStateDoesNotChange() {
        String email = "test@patimati.com";
        User user = User.builder().uid(10L).email(email).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(presenceService.markConnected(10L, "session-456")).thenReturn(false);

        SessionConnectedEvent event = createConnectedEvent(email, "session-456");
        presenceEventListener.handleSessionConnected(event);

        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    void broadcastsOfflineStatusWhenLastSessionDisconnects() {
        User user = User.builder().uid(10L).email("test@patimati.com").build();
        Instant now = Instant.now();
        PresenceService.DisconnectOutcome outcome = new PresenceService.DisconnectOutcome(10L, now);

        when(presenceService.markDisconnected("session-123")).thenReturn(outcome);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        SessionDisconnectEvent event = createDisconnectEvent("session-123");
        presenceEventListener.handleSessionDisconnect(event);

        ArgumentCaptor<UserStatusEvent> eventCaptor = ArgumentCaptor.forClass(UserStatusEvent.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/user-status"), eventCaptor.capture());

        UserStatusEvent statusEvent = eventCaptor.getValue();
        assertThat(statusEvent.userId()).isEqualTo(10L);
        assertThat(statusEvent.online()).isFalse();
        assertThat(statusEvent.status()).isEqualTo("OFFLINE");
        assertThat(statusEvent.lastSeen()).isEqualTo(now.toString());
    }

    @Test
    void doesNotBroadcastDisconnectWhenOtherSessionsRemain() {
        when(presenceService.markDisconnected("session-123")).thenReturn(null);

        SessionDisconnectEvent event = createDisconnectEvent("session-123");
        presenceEventListener.handleSessionDisconnect(event);

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

    private SessionDisconnectEvent createDisconnectEvent(String sessionId) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(org.springframework.messaging.simp.stomp.StompCommand.DISCONNECT);
        accessor.setSessionId(sessionId);

        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        return new SessionDisconnectEvent(this, message, sessionId, CloseStatus.NORMAL);
    }
}
