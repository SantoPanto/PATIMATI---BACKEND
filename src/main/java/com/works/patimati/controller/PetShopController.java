package com.works.patimati.controller;

import com.works.patimati.dto.petshop.PetShopResponse;
import com.works.patimati.dto.petshop.PetShopUpsertRequest;
import com.works.patimati.service.PetShopService;
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
 * Petshop bilgi kartı uçları -- yalnızca {@code PETSHOP} rolündeki
 * kullanıcılar (bkz. SecurityConfig: {@code /api/petshop/**} → {@code hasRole("PETSHOP")}).
 * Sahiplik path id ile DEĞİL, JWT'den gelen e-posta ile belirlenir --
 * {@code VetClinicController} ile AYNI desen.
 */
@Validated
@RestController
@RequestMapping("/api/petshop")
@RequiredArgsConstructor
public class PetShopController {

    private final PetShopService petShopService;

    /** Kart hiç oluşturulmamışsa 404 döner; ön yüz bunu "henüz yok" sayar. */
    @GetMapping("/card")
    public ResponseEntity<PetShopResponse> getMine(Authentication authentication) {
        return ResponseEntity.ok(petShopService.getMine(authentication.getName()));
    }

    /**
     * İstek {@code multipart/form-data}: {@code data} alanı petshop
     * bilgilerini taşıyan JSON, {@code photo} alanı opsiyonel fotoğraftır
     * (verilmezse mevcut fotoğraf korunur).
     */
    @PutMapping(value = "/card", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PetShopResponse> upsertMine(
            @Valid @RequestPart("data") PetShopUpsertRequest data,
            @RequestPart(value = "photo", required = false) MultipartFile photo,
            Authentication authentication
    ) {
        return ResponseEntity.ok(petShopService.upsertMine(authentication.getName(), data, photo));
    }
}
