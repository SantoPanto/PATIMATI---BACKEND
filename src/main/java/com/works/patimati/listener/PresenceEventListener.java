package com.works.patimati.listener;

import com.works.patimati.dto.UserStatusResponse;
import com.works.patimati.entity.User;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.service.UserPresenceService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Optional;

/**
 * Spring WebSocket (STOMP) oturum olaylarını dinleyerek kullanıcıların
 * çevrim içi / çevrim dışı (Presence) durumlarını günceller ve değişiklikleri
 * /topic/user-status kanalına yayınlar (Broadcast).
 */
@Component
@RequiredArgsConstructor
public class PresenceEventListener {

    private static final Logger log = LoggerFactory.getLogger(PresenceEventListener.class);
    public static final String USER_STATUS_TOPIC = "/topic/user-status";

    private final UserPresenceService userPresenceService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();
        if (principal == null || principal.getName() == null) {
            return;
        }

        String email = principal.getName();
        String sessionId = accessor.getSessionId();

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return;
        }

        Long userId = userOptional.get().getUid();
        boolean stateChanged = userPresenceService.markConnected(userId, sessionId);

        if (stateChanged) {
            UserStatusResponse statusResponse = new UserStatusResponse(userId, true);
            log.info("Kullanıcı çevrim içi oldu: userId={} sessionId={}", userId, sessionId);
            messagingTemplate.convertAndSend(USER_STATUS_TOPIC, statusResponse);
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();
        if (principal == null || principal.getName() == null) {
            return;
        }

        String email = principal.getName();
        String sessionId = accessor.getSessionId();

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return;
        }

        Long userId = userOptional.get().getUid();
        boolean stateChanged = userPresenceService.markDisconnected(userId, sessionId);

        if (stateChanged) {
            UserStatusResponse statusResponse = new UserStatusResponse(userId, false);
            log.info("Kullanıcı çevrim dışı oldu: userId={} sessionId={}", userId, sessionId);
            messagingTemplate.convertAndSend(USER_STATUS_TOPIC, statusResponse);
        }
    }
}
