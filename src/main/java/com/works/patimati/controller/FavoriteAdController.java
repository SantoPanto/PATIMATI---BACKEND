package com.works.patimati.controller;

import com.works.patimati.dto.ad.AdResponse;
import com.works.patimati.service.FavoriteAdService;
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
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteAdController {

    private static final int MAX_PAGE_SIZE = 100;

    private final FavoriteAdService favoriteAdService;

    @PostMapping("/{adId}")
    public ResponseEntity<Void> addFavorite(Authentication authentication, @PathVariable Long adId) {
        favoriteAdService.addFavorite(authentication.getName(), adId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{adId}")
    public ResponseEntity<Void> removeFavorite(Authentication authentication, @PathVariable Long adId) {
        favoriteAdService.removeFavorite(authentication.getName(), adId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<Page<AdResponse>> getMyFavorites(
            Authentication authentication,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(MAX_PAGE_SIZE) int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(favoriteAdService.getMyFavorites(authentication.getName(), pageable));
    }
}
