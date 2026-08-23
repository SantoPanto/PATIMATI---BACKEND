package com.works.patimati.controller;

import com.works.patimati.dto.alert.AlertSubscriptionResponse;
import com.works.patimati.dto.alert.AlertSubscriptionUpsertRequest;
import com.works.patimati.service.AlertSubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Konum tabanlı uyarı aboneliği uçları. SecurityConfig'e kayıt GEREKMEZ:
 * permitAll listesinde olmayan her uç varsayılan olarak kimlik ister
 * (anyRequest().authenticated()); abonelik yalnız sahibinin e-postası
 * üzerinden okunur/yazılır.
 */
@Validated
@RestController
@RequestMapping("/api/alert-subscriptions")
@RequiredArgsConstructor
public class AlertSubscriptionController {

    private final AlertSubscriptionService alertSubscriptionService;

    /** Abonelik hiç kurulmamışsa 404 döner; ön yüz bunu "henüz yok" sayar. */
    @GetMapping("/me")
    public ResponseEntity<AlertSubscriptionResponse> getMine(Authentication authentication) {
        return ResponseEntity.ok(
                alertSubscriptionService.getMine(authentication.getName())
        );
    }

    @PutMapping("/me")
    public ResponseEntity<AlertSubscriptionResponse> upsertMine(
            @Valid @RequestBody AlertSubscriptionUpsertRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                alertSubscriptionService.upsertMine(authentication.getName(), request)
        );
    }
}
