package com.works.patimati.presence;

import com.works.patimati.dto.UserStatusEvent;
import com.works.patimati.entity.User;
import com.works.patimati.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

/**
 * WebSocket (STOMP) oturumlarının açılış/kapanışını dinleyip
 * {@code /topic/user-status} üzerinden çevrimiçi/çevrimdışı olayı yayınlar.
 * <p>
 * Bağlantı, kullanıcı siteye giriş yaptığında (herhangi bir sayfada, sadece
 * mesajlar sayfasında değil) kurulur ve tarayıcı sekmesi/uygulama
 * kapatıldığında -- çıkış yapılmasa bile -- WebSocket bağlantısı tarayıcı
 * tarafından kapatılır, bu da {@link SessionDisconnectEvent}'i tetikler.
 */
@Component
@RequiredArgsConstructor
public class PresenceEventListener {

    private static final Logger log = LoggerFactory.getLogger(PresenceEventListener.class);
    private static final String USER_STATUS_TOPIC = "/topic/user-status";

    private final PresenceService presenceService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        Principal principal = event.getUser();
        String sessionId = SimpMessageHeaderAccessor.getSessionId(event.getMessage().getHeaders());
        if (principal == null || sessionId == null) {
            return;
        }

        userRepository.findByEmail(principal.getName()).ifPresent(user -> {
            boolean becameOnline = presenceService.markConnected(user.getUid(), sessionId);
            if (becameOnline) {
                broadcast(user, true, null);
            }
        });
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        String sessionId = SimpMessageHeaderAccessor.getSessionId(event.getMessage().getHeaders());
        if (sessionId == null) {
            return;
        }

        PresenceService.DisconnectOutcome outcome = presenceService.markDisconnected(sessionId);
        if (outcome == null) {
            return;
        }

        userRepository.findById(outcome.userId()).ifPresentOrElse(
                user -> broadcast(user, false, outcome.lastSeen().toString()),
                () -> log.warn("Çevrimdışı olayı için kullanıcı bulunamadı: uid={}", outcome.userId())
        );
    }

    private void broadcast(User user, boolean online, String lastSeen) {
        messagingTemplate.convertAndSend(
                USER_STATUS_TOPIC,
                new UserStatusEvent(
                        user.getUid(),
                        online,
                        online ? "ONLINE" : "OFFLINE",
                        lastSeen
                )
        );
    }
}
