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

    @GetMapping("/nearby")
    public ResponseEntity<List<Ad>> getNearbyAds(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5000") double radius) {

        List<Ad> ads = adService.findNearbyAds(latitude, longitude, radius);
        return ResponseEntity.ok(ads);
    }

    @PostMapping
    public ResponseEntity<Ad> createAd(@RequestBody AdCreateDto dto, Authentication authentication) {
        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Ad createdAd = adService.createAdAndNotifyNearbyUsers(dto, currentUser);
        return ResponseEntity.ok(createdAd);
    }
}