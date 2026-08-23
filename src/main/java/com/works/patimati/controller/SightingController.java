package com.works.patimati.controller;

import com.works.patimati.dto.sighting.SightingCreateRequest;
import com.works.patimati.dto.sighting.SightingResponse;
import com.works.patimati.service.SightingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * "Gördüm" bildirimi uçları.
 *
 * <p>POST {@code /api/public/**} altında: SecurityConfig'in permitAll satırı
 * kapsıyor, girişsiz ziyaretçi de bırakabilir (ürün kararı — afiş QR'ı
 * senaryosu). Bu, deponun public altındaki İLK yazma ucu; koruma üçlüsü:
 * iletişim alanı zorunlu (DTO) + IP hız sınırı (SightingRateLimiter) +
 * foto doğrulaması (ImageStorageService: yalnız JPEG, 5MB, imza denetimi).
 *
 * <p>GET ise varsayılan gibi kimlikli ({@code anyRequest().authenticated()});
 * sahiplik denetimi serviste.
 */
@Validated
@RestController
@RequiredArgsConstructor
public class SightingController {

    private final SightingService sightingService;

    @PostMapping(
            value = "/api/public/ads/{adId}/sightings",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<SightingResponse> createSighting(
            @PathVariable Long adId,
            @Valid @RequestPart("sighting") SightingCreateRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photo,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        // Girişsiz istekte principal "anonymousUser" olur (PublicAdController
        // ile aynı ayıklama); girişli bırakan görülmeye bağlanır.
        String reporterEmail = (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal()))
                ? authentication.getName()
                : null;

        SightingResponse response = sightingService.createSighting(
                adId, request, photo, reporterEmail, clientIp(httpRequest));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/ads/{adId}/sightings")
    public ResponseEntity<List<SightingResponse>> listSightings(
            @PathVariable Long adId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                sightingService.listSightings(adId, authentication.getName())
        );
    }

    /**
     * Vekil arkasında gerçek istemci IP'si X-Forwarded-For'un İLK parçasıdır;
     * başlık yoksa doğrudan bağlantı adresi. Hız sınırı anahtarı olarak
     * yeterli — kimlik kanıtı değil.
     */
    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
