package com.works.patimati.controller;

import com.works.patimati.dto.petshop.PetShopProductResponse;
import com.works.patimati.dto.petshop.PetShopPublicResponse;
import com.works.patimati.service.PetShopProductService;
import com.works.patimati.service.PetShopService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * "Hizmetler &gt; Petshop" herkese açık dizini -- SecurityConfig'de
 * GET-only permitAll (bkz. {@code /api/petshops}). Sahip/kullanıcı bilgisi
 * dönmez, yalnızca {@link PetShopPublicResponse}. {@code VetDirectoryController}
 * ile AYNI desen, ek olarak bir dükkanın herkese açık ürün listesi.
 */
@Validated
@RestController
@RequestMapping("/api/petshops")
@RequiredArgsConstructor
public class PetShopDirectoryController {

    private static final int MAX_PAGE_SIZE = 100;

    private final PetShopService petShopService;
    private final PetShopProductService petShopProductService;

    @GetMapping("/{id}")
    public ResponseEntity<PetShopPublicResponse> getPetShop(@PathVariable @Min(1) Long id) {
        return ResponseEntity.ok(petShopService.getPublicById(id));
    }

    @GetMapping
    public ResponseEntity<Page<PetShopPublicResponse>> listPetShops(
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(MAX_PAGE_SIZE) int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return ResponseEntity.ok(petShopService.listPublic(pageable, city));
    }

    /** Bir dükkanın herkese açık ürün listesi. */
    @GetMapping("/{id}/products")
    public ResponseEntity<Page<PetShopProductResponse>> listProducts(
            @PathVariable @Min(1) Long id,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(MAX_PAGE_SIZE) int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(petShopProductService.listPublicForShop(id, pageable));
    }
}
