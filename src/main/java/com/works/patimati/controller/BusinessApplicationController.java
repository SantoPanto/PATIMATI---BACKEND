package com.works.patimati.controller;

import com.works.patimati.dto.request.BusinessApplicationCreateRequest;
import com.works.patimati.dto.request.BusinessApplicationRejectRequest;
import com.works.patimati.dto.response.BusinessApplicationResponse;
import com.works.patimati.entity.enums.BusinessApplicationStatus;
import com.works.patimati.entity.enums.BusinessType;
import com.works.patimati.service.BusinessApplicationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * İşletme sahibi olma başvuruları -- kullanıcı tarafı ({@code /api/business-applications})
 * herhangi bir giriş yapmış kullanıcıya açık (bkz. SecurityConfig
 * {@code anyRequest().authenticated()}), admin tarafı ({@code /api/admin/business-applications})
 * {@code /api/admin/**} altında olduğu için zaten {@code hasRole("ADMIN")}.
 */
@Validated
@RestController
@RequiredArgsConstructor
public class BusinessApplicationController {

    private final BusinessApplicationService businessApplicationService;

    /**
     * İstek {@code multipart/form-data}: {@code data} alanı başvuru
     * bilgilerini taşıyan JSON, {@code photo} alanı opsiyonel fotoğraftır.
     */
    @PostMapping(value = "/api/business-applications", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BusinessApplicationResponse> submit(
            @Valid @RequestPart("data") BusinessApplicationCreateRequest data,
            @RequestPart(value = "photo", required = false) MultipartFile photo,
            Authentication authentication
    ) {
        return ResponseEntity.ok(businessApplicationService.submit(authentication.getName(), data, photo));
    }

    /** Kullanıcının en son başvurusunu döner; hiç başvurmadıysa 404. */
    @GetMapping("/api/business-applications/mine")
    public ResponseEntity<BusinessApplicationResponse> getMine(Authentication authentication) {
        return ResponseEntity.ok(businessApplicationService.getMine(authentication.getName()));
    }

    @GetMapping("/api/admin/business-applications")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<BusinessApplicationResponse>> listForAdmin(
            @RequestParam(required = false) BusinessApplicationStatus status,
            @RequestParam(required = false) BusinessType type,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(businessApplicationService.listForAdmin(status, type, pageable));
    }

    @PatchMapping("/api/admin/business-applications/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApplicationResponse> approve(
            @PathVariable @Min(1) Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(businessApplicationService.approve(id, authentication.getName()));
    }

    @PatchMapping("/api/admin/business-applications/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessApplicationResponse> reject(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody BusinessApplicationRejectRequest body,
            Authentication authentication
    ) {
        return ResponseEntity.ok(businessApplicationService.reject(id, authentication.getName(), body.reason()));
    }

    /** Yalnızca onaylanmış (ONAYLANDI) başvurular silinebilir -- bkz. {@link BusinessApplicationService#delete}. */
    @DeleteMapping("/api/admin/business-applications/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable @Min(1) Long id) {
        businessApplicationService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Başvuru silindi."));
    }
}
