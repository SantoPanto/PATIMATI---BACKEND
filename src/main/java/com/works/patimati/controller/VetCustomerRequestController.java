package com.works.patimati.controller;

import com.works.patimati.dto.vetcustomer.SendVetCustomerRequestRequest;
import com.works.patimati.dto.vetcustomer.VetCustomerRequestResponse;
import com.works.patimati.service.VetCustomerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Bir kullanıcının bir veterinere "müşteri isteği" göndermesi -- herhangi
 * bir kimlikli kullanıcı (rol şartı yok), vet'e özel uçlar
 * {@link VetCustomerController}'da (`/api/vet/**`, {@code hasRole("VET")}).
 */
@Validated
@RestController
@RequestMapping("/api/vet-customer-requests")
@RequiredArgsConstructor
public class VetCustomerRequestController {

    private final VetCustomerService vetCustomerService;

    @PostMapping
    public ResponseEntity<VetCustomerRequestResponse> sendRequest(
            @Valid @RequestBody SendVetCustomerRequestRequest body,
            Authentication authentication
    ) {
        return ResponseEntity.ok(vetCustomerService.sendRequest(authentication.getName(), body.vetId()));
    }

    /** İstek hiç gönderilmemişse 404 döner; ön yüz bunu "istek yok" sayar. */
    @GetMapping("/me")
    public ResponseEntity<VetCustomerRequestResponse> getMyRequestStatus(
            @RequestParam @Min(1) Long vetId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(vetCustomerService.getMyRequestStatus(authentication.getName(), vetId));
    }
}
