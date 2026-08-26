package com.works.patimati.controller;

import com.works.patimati.dto.UserStatusEvent;
import com.works.patimati.presence.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * Sohbet ekranı bir kullanıcıyla konuşmaya başlarken (WebSocket'e henüz hiç
 * bağlanmamış ya da o kullanıcı için henüz bir {@code /topic/user-status}
 * olayı almamış olabileceği için) o kullanıcının O ANKİ çevrimiçi durumunu
 * bu uçtan okur. Sonraki değişiklikler WebSocket üzerinden canlı gelir.
 * <p>
 * <b>BİLEREK var olmayan userId için 404 DÖNMÜYOR:</b> {@code MessageService}
 * (bkz. {@code createOrGetRoom} üzerindeki uzun açıklama) burada da aynı
 * "varlık kâhini" sızıntısını -- 200/404 farkının, ardışık kimlik
 * numaralarıyla toplam kullanıcı sayısını bile ele vermesini -- önlemek
 * için bilerek tek tip cevap veriyor: var olmayan bir kullanıcı da,
 * çevrimdışı olan bir kullanıcı da aynı şekilde {@code online=false} döner.
 * Kimlik doğrulaması yine de zorunludur (bkz. SecurityConfig
 * {@code anyRequest().authenticated()}).
 */
@RestController
@RequestMapping({"/api/users", "/api/v1/users"})
@RequiredArgsConstructor
public class PresenceController {

    private final PresenceService presenceService;

    @GetMapping("/{userId}/status")
    public ResponseEntity<UserStatusEvent> getUserStatus(@PathVariable Long userId) {
        boolean online = presenceService.isOnline(userId);
        Instant lastSeen = online ? null : presenceService.getLastSeen(userId);

        return ResponseEntity.ok(new UserStatusEvent(
                userId,
                online,
                online ? "ONLINE" : "OFFLINE",
                lastSeen == null ? null : lastSeen.toString()
        ));
    }
}
