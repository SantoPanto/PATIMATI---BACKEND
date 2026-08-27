package com.works.patimati.controller;

import com.works.patimati.dto.vet.VetClinicReviewResponse;
import com.works.patimati.dto.vet.VetClinicReviewUpsertRequest;
import com.works.patimati.service.VetClinicReviewService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Klinik değerlendirme (1-5 yıldız + yorum) uçları -- {@code VetDirectoryController}'a
 * DAĞITILMIYOR, sahiplik net kalsın diye ayrı controller.
 *
 * <p>GET herkese açık (SecurityConfig §0 düzeltmesi sonrası: yalnızca GET
 * permitAll). Yazma uçları (PUT/DELETE) için SecurityConfig'e YENİ bir satır
 * gerekmiyor -- düzeltme sonrası zaten {@code anyRequest().authenticated()}
 * kuralına düşüyorlar (VET rolü şartı YOK, herhangi bir giriş yapmış
 * kullanıcı puan verebilir).
 */
@Validated
@RestController
@RequestMapping("/api/vet-clinics/{id}/reviews")
@RequiredArgsConstructor
public class VetClinicReviewController {

    private static final int MAX_PAGE_SIZE = 100;

    private final VetClinicReviewService vetClinicReviewService;

    @GetMapping
    public ResponseEntity<Page<VetClinicReviewResponse>> list(
            @PathVariable @Min(1) Long id,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(MAX_PAGE_SIZE) int size,
            Authentication authentication
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(vetClinicReviewService.listForClinic(id, pageable, resolveViewerUid(authentication)));
    }

    /** Çağıranın kendi yorumu -- yoksa 404; ön yüz bunu "henüz yok" sayar. */
    @GetMapping("/me")
    public ResponseEntity<VetClinicReviewResponse> getMine(@PathVariable @Min(1) Long id, Authentication authentication) {
        return ResponseEntity.ok(vetClinicReviewService.getMine(authentication.getName(), id));
    }

    @PutMapping("/me")
    public ResponseEntity<VetClinicReviewResponse> upsertMine(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody VetClinicReviewUpsertRequest body,
            Authentication authentication
    ) {
        return ResponseEntity.ok(vetClinicReviewService.upsertMine(authentication.getName(), id, body));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMine(@PathVariable @Min(1) Long id, Authentication authentication) {
        vetClinicReviewService.deleteMine(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Anonim çağrılarda Spring Security'nin {@code Authentication}'ı
     * {@code "anonymousUser"} adıyla dolu gelir -- bunu gerçek bir e-posta
     * gibi çözmeye çalışmıyoruz, "görüntüleyen yok" sayıyoruz.
     */
    private Long resolveViewerUid(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        return vetClinicReviewService.resolveViewerUid(authentication.getName());
    }
}
