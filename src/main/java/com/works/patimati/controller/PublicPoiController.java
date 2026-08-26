package com.works.patimati.controller;

import com.works.patimati.dto.poi.PoiResponse;
import com.works.patimati.entity.enums.PoiType;
import com.works.patimati.service.PoiService;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@Validated
@RestController
@RequestMapping("/api/public/pois")
@RequiredArgsConstructor
public class PublicPoiController {

    private final PoiService poiService;

    /**
     * Haritayı besleyen yakın veteriner/petshop/barınakları kimlik
     * doğrulaması istemeden döndürür (ads/nearby ile aynı desen).
     */
    @GetMapping
    public ResponseEntity<List<PoiResponse>> getNearbyPois(
            @RequestParam
            @DecimalMin("-90.0")
            @DecimalMax("90.0") double latitude,

            @RequestParam
            @DecimalMin("-180.0")
            @DecimalMax("180.0") double longitude,

            @RequestParam(defaultValue = "5000")
            @DecimalMin(value = "1.0", inclusive = true)
            @DecimalMax("100000.0") double radius,

            @RequestParam(required = false) Set<PoiType> types
    ) {
        return ResponseEntity.ok(
                poiService.findNearby(latitude, longitude, radius, types)
        );
    }
}
