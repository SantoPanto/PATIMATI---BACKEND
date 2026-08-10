package com.works.patimati.controller;

import com.works.patimati.dto.ad.AdResponse;
import com.works.patimati.entity.Ad;
import com.works.patimati.service.AdService;
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

@Validated
@RestController
@RequestMapping("/api/public/ads")
@RequiredArgsConstructor
public class PublicAdController {

    private static final int MAX_PAGE_SIZE = 100;
    private final AdService adService;

    /**
     * Herkese açık aktif ve askıda olmayan ilanları sayfalı biçimde listeler.
     */
    @GetMapping
    public ResponseEntity<Page<AdResponse>> getPublicAds(
            @RequestParam(required = false) Ad.AdType adType,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(MAX_PAGE_SIZE) int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return ResponseEntity.ok(
                adService.getPublicActiveAds(adType, pageable)
        );
    }

    /**
     * Herkese açık tek bir aktif ilanın detayını getirir.
     */
    @GetMapping("/{adId}")
    public ResponseEntity<AdResponse> getPublicAd(
            @PathVariable @Min(1) Long adId
    ) {
        return ResponseEntity.ok(
                adService.getPublicActiveAd(adId)
        );
    }
}
