package com.works.patimati.controller;

import com.works.patimati.dto.shelter.ShelterResponse;
import com.works.patimati.dto.shelter.ShelterUpsertRequest;
import com.works.patimati.service.ShelterService;
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
 * Barınak bilgi kartı uçları -- yalnızca {@code BARINAK} rolündeki
 * kullanıcılar (bkz. SecurityConfig: {@code /api/shelter/**} → {@code hasRole("BARINAK")}).
 * Sahiplik path id ile DEĞİL, JWT'den gelen e-posta ile belirlenir --
 * {@code PetShopController}/{@code VetClinicController} ile AYNI desen.
 */
@Validated
@RestController
@RequestMapping("/api/shelter")
@RequiredArgsConstructor
public class ShelterController {

    private final ShelterService shelterService;

    /** Kart hiç oluşturulmamışsa 404 döner; ön yüz bunu "henüz yok" sayar. */
    @GetMapping("/card")
    public ResponseEntity<ShelterResponse> getMine(Authentication authentication) {
        return ResponseEntity.ok(shelterService.getMine(authentication.getName()));
    }

    /**
     * İstek {@code multipart/form-data}: {@code data} alanı barınak
     * bilgilerini taşıyan JSON, {@code photo} alanı opsiyonel fotoğraftır
     * (verilmezse mevcut fotoğraf korunur).
     */
    @PutMapping(value = "/card", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ShelterResponse> upsertMine(
            @Valid @RequestPart("data") ShelterUpsertRequest data,
            @RequestPart(value = "photo", required = false) MultipartFile photo,
            Authentication authentication
    ) {
        return ResponseEntity.ok(shelterService.upsertMine(authentication.getName(), data, photo));
    }
}
