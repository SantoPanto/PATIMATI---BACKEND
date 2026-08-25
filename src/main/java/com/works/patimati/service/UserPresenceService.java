package com.works.patimati.service;

import com.works.patimati.dto.UserStatusResponse;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Kullanıcıların WebSocket (STOMP) üzerindeki anlık çevrim içi / çevrim dışı (Presence)
 * durumlarını eşzamanlılıktan güvenli (thread-safe) biçimde takip eder.
 *
 * Bir kullanıcının birden fazla sekme/cihazdan açık bağlantısı olabileceğinden
 * oturum (sessionId) kümesi tutulur. Böylece sekme geçişlerinde gereksiz
 * çevrim dışı görünmeler (flickering) önlenir.
 */
@Service
public class UserPresenceService {

    private final ConcurrentMap<Long, Set<String>> activeUserSessions = new ConcurrentHashMap<>();

    /**
     * Kullanıcının yeni bir WebSocket oturumunu bağlar.
     * @return Kullanıcının durumu 'offline' -> 'online' olarak değiştiyse true döner.
     */
    public boolean markConnected(Long userId, String sessionId) {
        if (userId == null || sessionId == null) {
            return false;
        }

        final boolean[] stateChanged = new boolean[]{false};
        activeUserSessions.compute(userId, (id, sessions) -> {
            if (sessions == null || sessions.isEmpty()) {
                sessions = Collections.newSetFromMap(new ConcurrentHashMap<>());
                stateChanged[0] = true;
            }
            sessions.add(sessionId);
            return sessions;
        });

        return stateChanged[0];
    }

    /**
     * Kullanıcının var olan bir WebSocket oturumunun kapandığını işler.
     * @return Kullanıcının son oturumu da kapandıysa ve durumu 'online' -> 'offline' olarak değiştiyse true döner.
     */
    public boolean markDisconnected(Long userId, String sessionId) {
        if (userId == null || sessionId == null) {
            return false;
        }

        final boolean[] stateChanged = new boolean[]{false};
        activeUserSessions.computeIfPresent(userId, (id, sessions) -> {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                stateChanged[0] = true;
                return null;
            }
            return sessions;
        });

        return stateChanged[0];
    }

    public boolean isUserOnline(Long userId) {
        if (userId == null) {
            return false;
        }
        Set<String> sessions = activeUserSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    public UserStatusResponse getUserStatus(Long userId) {
        return new UserStatusResponse(userId, isUserOnline(userId));
    }
}
