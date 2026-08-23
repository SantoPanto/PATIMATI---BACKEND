package com.works.patimati.controller;

import com.works.patimati.dto.UserStatusResponse;
import com.works.patimati.service.UserPresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Kullanıcıların anlık çevrim içi / çevrim dışı (Presence) durumlarını sorgulamak
 * için REST uç noktası sağlar.
 */
@RestController
@RequestMapping({"/api/users", "/api/v1/users"})
@RequiredArgsConstructor
public class UserPresenceController {

    private final UserPresenceService userPresenceService;

    @GetMapping("/{userId:[0-9]+}/status")
    public ResponseEntity<UserStatusResponse> getUserStatus(@PathVariable Long userId) {
        return ResponseEntity.ok(userPresenceService.getUserStatus(userId));
    }
}
