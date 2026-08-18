package com.works.patimati.controller;

import com.works.patimati.dto.ad.AdResponse;
import com.works.patimati.dto.ad.AdoptionAdCreateRequest;
import com.works.patimati.dto.ad.AdoptionAdUpdateRequest;
import com.works.patimati.dto.ad.ResolveAdoptionAdRequest;
import com.works.patimati.service.AdoptionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Oturum açmış kullanıcılar için Sahiplendirme İlan Yönetimi Controller.
 */
@Validated
@RestController
@RequestMapping("/api/adoptions")
@RequiredArgsConstructor
public class AdoptionController {

    private final AdoptionService adoptionService;

    /**
     * Sahiplendirme ilanı oluşturur (En az 1 foto zorunludur).
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AdResponse> createAdoptionAd(
            @Valid @RequestPart("ad") AdoptionAdCreateRequest request,
            @RequestPart(value = "images", required = true) List<MultipartFile> images,
            Authentication authentication
    ) {
        AdResponse response = adoptionService.createAdoptionAd(authentication.getName(), request, images);
        return ResponseEntity
                .created(URI.create("/api/public/adoptions/" + response.id()))
                .body(response);
    }

    /**
     * İlan sahibinin kendi sahiplendirme ilanını güncellemesini sağlar.
     */
    @PutMapping("/{adId}")
    public ResponseEntity<AdResponse> updateAdoptionAd(
            Authentication authentication,
            @PathVariable @Min(1) Long adId,
            @Valid @RequestBody AdoptionAdUpdateRequest request
    ) {
        return ResponseEntity.ok(
                adoptionService.updateAdoptionAd(authentication.getName(), adId, request)
        );
    }

    /**
     * İlan sahibinin sahiplendirme ilanını sistemden kaldırmasını (soft delete) sağlar.
     */
    @DeleteMapping("/{adId}")
    public ResponseEntity<Map<String, String>> deleteAdoptionAd(
            Authentication authentication,
            @PathVariable @Min(1) Long adId
    ) {
        adoptionService.deleteAdoptionAd(authentication.getName(), adId);
        return ResponseEntity.ok(
                Map.of("message", "Adoption ad with ID " + adId + " has been successfully deleted.")
        );
    }

    /**
     * İlan sahibinin sahiplendirme ilanını sahiplendirildi olarak işaretleyip kapatmasını sağlar.
     */
    @PutMapping("/{adId}/resolve-adopted")
    public ResponseEntity<Map<String, String>> resolveAdoptionAd(
            Authentication authentication,
            @PathVariable @Min(1) Long adId,
            @RequestBody(required = false) ResolveAdoptionAdRequest request
    ) {
        adoptionService.resolveAdoptionAd(authentication.getName(), adId, request);
        return ResponseEntity.ok(
                Map.of("message", "Sahiplendirme ilanı başarıyla sahiplendirildi olarak işaretlendi ve ödül puanı tanımlandı.")
        );
    }
}
