package com.works.patimati.controller;

import com.works.patimati.dto.vet.VetClinicResponse;
import com.works.patimati.dto.vet.VetClinicUpsertRequest;
import com.works.patimati.service.VetClinicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Veteriner klinik bilgi kartı uçları -- yalnızca {@code VET} rolündeki
 * kullanıcılar (bkz. SecurityConfig: {@code /api/vet/**} → {@code hasRole("VET")}).
 * Sahiplik path id ile DEĞİL, JWT'den gelen e-posta ile belirlenir -- IDOR
 * riski yok, herkes yalnızca KENDİ kartını görebilir/düzenleyebilir.
 */
@Validated
@RestController
@RequestMapping("/api/vet/clinic")
@RequiredArgsConstructor
public class VetClinicController {

    private final VetClinicService vetClinicService;

    /** Kart hiç oluşturulmamışsa 404 döner; ön yüz bunu "henüz yok" sayar. */
    @GetMapping
    public ResponseEntity<VetClinicResponse> getMine(Authentication authentication) {
        return ResponseEntity.ok(vetClinicService.getMine(authentication.getName()));
    }

    /**
     * İstek {@code multipart/form-data}: {@code data} alanı klinik
     * bilgilerini taşıyan JSON, {@code photo} alanı opsiyonel fotoğraftır
     * (verilmezse mevcut fotoğraf korunur).
     */
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VetClinicResponse> upsertMine(
            @Valid @RequestPart("data") VetClinicUpsertRequest data,
            @RequestPart(value = "photo", required = false) MultipartFile photo,
            Authentication authentication
    ) {
        return ResponseEntity.ok(vetClinicService.upsertMine(authentication.getName(), data, photo));
    }
}
