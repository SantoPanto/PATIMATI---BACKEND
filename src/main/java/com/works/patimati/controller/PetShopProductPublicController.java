package com.works.patimati.controller;

import com.works.patimati.dto.petshop.PetShopProductReviewResponse;
import com.works.patimati.dto.petshop.PetShopProductReviewUpsertRequest;
import com.works.patimati.dto.petshop.PetShopProductResponse;
import com.works.patimati.service.PetShopProductReviewService;
import com.works.patimati.service.PetShopProductService;
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
 * Herkese açık ürün detayı + ürün değerlendirme (1-5 yıldız + yorum) uçları
 * -- tek dosyada, ürün id'leri global benzersiz olduğu için ayrı bir
 * dükkan-scope'una gerek yok. GET herkese açık (SecurityConfig: yalnızca GET
 * permitAll). Yazma uçları (PUT/DELETE) için SecurityConfig'e YENİ bir satır
 * gerekmiyor -- zaten {@code anyRequest().authenticated()} kuralına
 * düşüyorlar (PETSHOP rolü şartı YOK, herhangi bir giriş yapmış kullanıcı
 * puan verebilir). {@code VetClinicReviewController} ile AYNI desen.
 */
@Validated
@RestController
@RequestMapping("/api/petshop-products")
@RequiredArgsConstructor
public class PetShopProductPublicController {

    private static final int MAX_PAGE_SIZE = 100;

    private final PetShopProductService petShopProductService;
    private final PetShopProductReviewService petShopProductReviewService;

    /** Herkese açık ürün detayı -- bulunamazsa 404. */
    @GetMapping("/{id}")
    public ResponseEntity<PetShopProductResponse> getProduct(@PathVariable @Min(1) Long id) {
        return ResponseEntity.ok(petShopProductService.getPublicById(id));
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<Page<PetShopProductReviewResponse>> list(
            @PathVariable @Min(1) Long id,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(MAX_PAGE_SIZE) int size,
            Authentication authentication
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(petShopProductReviewService.listForProduct(id, pageable, resolveViewerUid(authentication)));
    }

    /** Çağıranın kendi yorumu -- yoksa 404; ön yüz bunu "henüz yok" sayar. */
    @GetMapping("/{id}/reviews/me")
    public ResponseEntity<PetShopProductReviewResponse> getMine(@PathVariable @Min(1) Long id, Authentication authentication) {
        return ResponseEntity.ok(petShopProductReviewService.getMine(authentication.getName(), id));
    }

    @PutMapping("/{id}/reviews/me")
    public ResponseEntity<PetShopProductReviewResponse> upsertMine(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody PetShopProductReviewUpsertRequest body,
            Authentication authentication
    ) {
        return ResponseEntity.ok(petShopProductReviewService.upsertMine(authentication.getName(), id, body));
    }

    @DeleteMapping("/{id}/reviews/me")
    public ResponseEntity<Void> deleteMine(@PathVariable @Min(1) Long id, Authentication authentication) {
        petShopProductReviewService.deleteMine(authentication.getName(), id);
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
        return petShopProductReviewService.resolveViewerUid(authentication.getName());
    }
}
