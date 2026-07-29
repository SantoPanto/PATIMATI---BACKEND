package com.works.patimati.controller;

import com.works.patimati.dto.AdCreateDto;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.User;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.service.AdService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/ads")
@RequiredArgsConstructor
public class AdController {
    private final AdService adService;
    private final UserRepository userRepository;

    //Yakındaki yayınlanmış ilanlar
    @GetMapping("/nearby")
    public ResponseEntity<List<Ad>> getNearbyAds(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5000") double radius) {
        List<Ad> ads = adService.findNearbyAds(latitude, longitude, radius);
        return ResponseEntity.ok(ads);
    }

    // DRAFT
    @PostMapping
    public ResponseEntity<Ad> createDraftAd(@RequestBody AdCreateDto dto, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        Ad draftAd = adService.createDraftAd(dto, currentUser);
        return ResponseEntity.ok(draftAd);
    }
    // İlan yayınlama
    @PostMapping("/{id}/publish")
    public ResponseEntity<?> publishAd(@PathVariable Long id, Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            Ad publishedAd = adService.publishAd(id, currentUser);
            return ResponseEntity.ok(publishedAd);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    // İlan düzenleme
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAd(@PathVariable Long id, @RequestBody AdCreateDto dto, Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            Ad updatedAd = adService.updateAd(id, dto, currentUser);
            return ResponseEntity.ok(updatedAd);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));
    }
}