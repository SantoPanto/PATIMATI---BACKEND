package com.works.patimati.controller;

import com.works.patimati.dto.ad.AdCreateRequest;
import com.works.patimati.dto.ad.AdResponse;
import com.works.patimati.dto.ad.AdUpdateRequest;
import com.works.patimati.entity.Ad;
import com.works.patimati.service.AdService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/ads")
@RequiredArgsConstructor
public class AdController {

    private static final int MAX_PAGE_SIZE = 100;

    private final AdService adService;

    @PostMapping
    public ResponseEntity<AdResponse> createAd(
            @Valid @RequestBody AdCreateRequest request,
            Authentication authentication
    ) {
        AdResponse response = adService.createAd(authentication.getName(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{adId}")
    public ResponseEntity<AdResponse> getAd(
            @PathVariable @Min(1) Long adId
    ) {
        return ResponseEntity.ok(adService.getActiveAd(adId));
    }

    @GetMapping
    public ResponseEntity<Page<AdResponse>> getAds(
            @RequestParam(required = false) Ad.AdType adType,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20")
            @Min(1) @Max(MAX_PAGE_SIZE) int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return ResponseEntity.ok(
                adService.getActiveAds(adType, pageable)
        );
    }

    @GetMapping("/me")
    public ResponseEntity<Page<AdResponse>> getMyAds(
            Authentication authentication,
            @RequestParam(defaultValue = "true") boolean active,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20")
            @Min(1) @Max(MAX_PAGE_SIZE) int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return ResponseEntity.ok(
                adService.getUserAds(
                        authentication.getName(),
                        active,
                        pageable
                )
        );
    }

    @PutMapping("/{adId}")
    public ResponseEntity<AdResponse> updateAd(
            Authentication authentication,
            @PathVariable @Min(1) Long adId,
            @Valid @RequestBody AdUpdateRequest request
    ) {
        return ResponseEntity.ok(
                adService.updateAd(
                        authentication.getName(),
                        adId,
                        request
                )
        );
    }

    @DeleteMapping("/{adId}")
    public ResponseEntity<Void> deactivateAd(
            Authentication authentication,
            @PathVariable @Min(1) Long adId
    ) {
        adService.deactivateAd(authentication.getName(), adId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<AdResponse>> getNearbyAds(
            @RequestParam
            @DecimalMin("-90.0") @DecimalMax("90.0")
            double latitude,

            @RequestParam
            @DecimalMin("-180.0") @DecimalMax("180.0")
            double longitude,

            @RequestParam(defaultValue = "5000")
            @DecimalMin(value = "1.0", inclusive = true)
            @DecimalMax("100000.0")
            double radius
    ) {
        return ResponseEntity.ok(
                adService.findNearbyAds(latitude, longitude, radius)
        );
    }
}