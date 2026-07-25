package com.works.patimati.controller;

import com.works.patimati.entity.Ad;
import com.works.patimati.service.AdService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ads")
@RequiredArgsConstructor
public class AdController {

    private final AdService adService;

    // KISIM 3
    @GetMapping("/nearby")
    public ResponseEntity<List<Ad>> getNearbyAds(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5000") double radius) {

        List<Ad> ads = adService.findNearbyAds(latitude, longitude, radius);
        return ResponseEntity.ok(ads);
    }
    @PostMapping
    public ResponseEntity<Ad> createAd(
            @RequestBody Ad ad,
            @RequestParam double latitude,
            @RequestParam double longitude) {

        Ad createdAd = adService.createAdAndNotifyNearbyUsers(ad, latitude, longitude);
        return ResponseEntity.ok(createdAd);
    }
}