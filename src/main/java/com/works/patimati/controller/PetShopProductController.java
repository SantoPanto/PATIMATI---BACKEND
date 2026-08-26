package com.works.patimati.controller;

import com.works.patimati.dto.petshop.PetShopProductResponse;
import com.works.patimati.dto.petshop.PetShopProductUpsertRequest;
import com.works.patimati.service.PetShopProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Petshop sahibinin kendi ürün yönetim uçları -- yalnızca {@code PETSHOP}
 * rolündeki kullanıcılar (bkz. SecurityConfig: {@code /api/petshop/**} →
 * {@code hasRole("PETSHOP")}). Sahiplik path id ile DEĞİL, JWT'den gelen
 * e-posta ile belirlenir; güncelleme/silme ek olarak
 * {@code findByPetShop_IdAndId} ile IDOR'a karşı sınırlanır.
 */
@Validated
@RestController
@RequestMapping("/api/petshop/products")
@RequiredArgsConstructor
public class PetShopProductController {

    private static final int MAX_PAGE_SIZE = 100;

    private final PetShopProductService petShopProductService;

    @GetMapping
    public ResponseEntity<Page<PetShopProductResponse>> listMine(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(MAX_PAGE_SIZE) int size,
            Authentication authentication
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(petShopProductService.listMine(authentication.getName(), pageable));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PetShopProductResponse> create(
            @Valid @RequestPart("data") PetShopProductUpsertRequest data,
            @RequestPart(value = "photo", required = false) MultipartFile photo,
            Authentication authentication
    ) {
        return ResponseEntity.ok(petShopProductService.createMine(authentication.getName(), data, photo));
    }

    @PutMapping(value = "/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PetShopProductResponse> update(
            @PathVariable @Min(1) Long productId,
            @Valid @RequestPart("data") PetShopProductUpsertRequest data,
            @RequestPart(value = "photo", required = false) MultipartFile photo,
            Authentication authentication
    ) {
        return ResponseEntity.ok(petShopProductService.updateMine(authentication.getName(), productId, data, photo));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> delete(@PathVariable @Min(1) Long productId, Authentication authentication) {
        petShopProductService.deleteMine(authentication.getName(), productId);
        return ResponseEntity.noContent().build();
    }
}
