package com.works.patimati.presence;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kullanıcıların "çevrimiçi" olma durumunu, açık STOMP oturumlarını
 * (WebSocket sekmelerini) sayarak bellek içinde tutar.
 * <p>
 * Aynı kullanıcı birden fazla sekme/cihazdan bağlanabildiği için tek bir
 * bağlantı değil, oturum SAYISI izlenir: kullanıcı yalnızca SON oturumu da
 * kapandığında ("sitede hiç sekmesi kalmadığında") çevrimdışı sayılır. Bu,
 * kullanıcı sekmeyi kapatmadan (çıkış yapmadan) tarayıcıyı kapattığında da
 * geçerlidir -- WebSocket bağlantısı tarayıcı tarafından kapatılır ve
 * {@code SessionDisconnectEvent} tetiklenir.
 * <p>
 * Tek örnekli (single-instance) yerel/mevcut dağıtıma göre tasarlandı --
 * birden fazla backend örneği (yatay ölçekleme) varsa bu durumun paylaşılan
 * bir depoya (ör. Redis) taşınması gerekir.
 */
@Service
public class PresenceService {

    private final Map<Long, Set<String>> sessionsByUser = new ConcurrentHashMap<>();
    private final Map<String, Long> userBySession = new ConcurrentHashMap<>();
    private final Map<Long, Instant> lastSeenByUser = new ConcurrentHashMap<>();

    /**
     * @return kullanıcının bu bağlantıyla İLK KEZ çevrimiçi olduğu (önceden
     * açık başka oturumu olmadığı) doğru döner -- yalnızca bu durumda
     * {@code /topic/user-status} yayını yapılmalıdır.
     */
    public boolean markConnected(Long userId, String sessionId) {
        userBySession.put(sessionId, userId);
        Set<String> sessions = sessionsByUser.computeIfAbsent(
                userId, key -> ConcurrentHashMap.newKeySet());
        boolean becameOnline = sessions.isEmpty();
        sessions.add(sessionId);
        return becameOnline;
    }

    /**
     * @return kullanıcının SON oturumu da kapandıysa (artık hiç sekmesi
     * yoksa) ilgili bilgiyi döner; kullanıcının başka açık oturumu varsa ya
     * da oturum bu servise hiç kaydedilmemişse {@code null} döner.
     */
    public DisconnectOutcome markDisconnected(String sessionId) {
        Long userId = userBySession.remove(sessionId);
        if (userId == null) {
            return null;
        }

        Set<String> sessions = sessionsByUser.get(userId);
        if (sessions == null) {
            return null;
        }

        sessions.remove(sessionId);
        if (!sessions.isEmpty()) {
            return null;
        }

        // computeIfAbsent'in eş zamanlı oluşturduğu YENİ bir kümeyi yanlışlıkla
        // silmemek için referans eşliğiyle (remove(key, value)) kaldırılır.
        sessionsByUser.remove(userId, sessions);
        Instant now = Instant.now();
        lastSeenByUser.put(userId, now);
        return new DisconnectOutcome(userId, now);
    }

    public record DisconnectOutcome(Long userId, Instant lastSeen) {
    }

    public boolean isOnline(Long userId) {
        Set<String> sessions = sessionsByUser.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    public Instant getLastSeen(Long userId) {
        return lastSeenByUser.get(userId);
    }
}
