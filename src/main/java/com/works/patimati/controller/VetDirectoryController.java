package com.works.patimati.controller;

import com.works.patimati.dto.vet.VetClinicPublicResponse;
import com.works.patimati.service.VetClinicService;
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
 * "Hizmetler &gt; Veteriner" herkese açık dizini -- SecurityConfig'de
 * permitAll (bkz. {@code /api/vet-clinics}). Sahip/kullanıcı bilgisi
 * dönmez, yalnızca {@link VetClinicPublicResponse}.
 */
@Validated
@RestController
@RequestMapping("/api/vet-clinics")
@RequiredArgsConstructor
public class VetDirectoryController {

    private static final int MAX_PAGE_SIZE = 100;

    private final VetClinicService vetClinicService;

    @GetMapping("/{id}")
    public ResponseEntity<VetClinicPublicResponse> getVetClinic(@PathVariable @Min(1) Long id) {
        return ResponseEntity.ok(vetClinicService.getPublicById(id));
    }

    @GetMapping
    public ResponseEntity<Page<VetClinicPublicResponse>> listVetClinics(
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(MAX_PAGE_SIZE) int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return ResponseEntity.ok(vetClinicService.listPublic(pageable, city));
    }
}
