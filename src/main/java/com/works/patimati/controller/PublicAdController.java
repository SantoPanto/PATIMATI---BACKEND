package com.works.patimati.controller;

import com.works.patimati.dto.ad.AdResponse;
import com.works.patimati.dto.ad.AdCountersResponse;
import com.works.patimati.entity.Ad;
import com.works.patimati.service.AdService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

import com.works.patimati.service.PosterService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/public/ads")
@RequiredArgsConstructor
public class PublicAdController {

    private static final int MAX_PAGE_SIZE = 100;
    private final AdService adService;
    private final PosterService posterService;

    @GetMapping("/counters")
    public ResponseEntity<AdCountersResponse> getAdCounters() {
        return ResponseEntity.ok(adService.getAdCounters());
    }

    /**
     * Herkese açık aktif ve askıda olmayan ilanları sayfalı biçimde listeler.
     *
     * <p>{@code search} verilirse başlık, ırk ve açıklamada büyük/küçük harf
     * duyarsız metin araması yapılır (İlanlar sayfasındaki arama kutusu).</p>
     */
    @GetMapping
    public ResponseEntity<Page<AdResponse>> getPublicAds(
            @RequestParam(required = false) Ad.AdType adType,
            @RequestParam(required = false) @Size(max = 100) String search,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(MAX_PAGE_SIZE) int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return ResponseEntity.ok(
                adService.getPublicActiveAds(adType, search, pageable)
        );
    }

    /**
     * Haritayı besleyen yakın ilanları kimlik doğrulaması istemeden döndürür.
     *
     * <p>Public akışta yalnızca aktif ve askıda olmayan ilanlar gösterilir.
     * Sahip ve yönetici istisnası burada uygulanmaz.</p>
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<AdResponse>> getPublicNearbyAds(
            @RequestParam
            @DecimalMin("-90.0")
            @DecimalMax("90.0") double latitude,

            @RequestParam
            @DecimalMin("-180.0")
            @DecimalMax("180.0") double longitude,

            @RequestParam(defaultValue = "5000")
            @DecimalMin(value = "1.0", inclusive = true)
            @DecimalMax("100000.0") double radius
    ) {
        return ResponseEntity.ok(
                adService.findPublicNearbyAds(latitude, longitude, radius)
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

    /**
     * Herkese açık ilan afişi indirme ucu.
     * GET /api/public/ads/{adId}/poster
     */
    @GetMapping(value = "/{adId}/poster", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getPublicAdPoster(
            @PathVariable @Min(1) Long adId,
            org.springframework.security.core.Authentication authentication
    ) {
        String requestingUserEmail = (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal()))
                ? authentication.getName()
                : null;

        byte[] pdfBytes = posterService.generateAdPosterPdf(adId, requestingUserEmail);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"poster_" + adId + ".pdf\"");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
