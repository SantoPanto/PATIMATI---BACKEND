package com.works.patimati.controller;

import com.works.patimati.dto.ad.AdResponse;
import com.works.patimati.service.AdoptionService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Herkese açık (Public) Sahiplendirme İlanları Controller.
 */
@Validated
@RestController
@RequestMapping("/api/public/adoptions")
@RequiredArgsConstructor
public class PublicAdoptionController {

    private static final int MAX_PAGE_SIZE = 100;
    private final AdoptionService adoptionService;

    /**
     * Herkese açık aktif sahiplendirme ilanlarını sayfalı olarak getirir.
     */
    @GetMapping
    public ResponseEntity<Page<AdResponse>> getPublicAdoptionAds(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(MAX_PAGE_SIZE) int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(adoptionService.getPublicAdoptionAds(pageable));
    }
}
