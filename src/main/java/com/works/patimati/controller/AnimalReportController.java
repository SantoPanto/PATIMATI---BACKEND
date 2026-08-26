package com.works.patimati.controller;

import com.works.patimati.dto.request.AnimalReportCreateRequest;
import com.works.patimati.dto.response.AnimalReportResponse;
import com.works.patimati.entity.enums.ReportStatus;
import com.works.patimati.service.AnimalReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class AnimalReportController {

    private final AnimalReportService animalReportService;

    // C1: Halka açık ihbar oluşturma
    @PostMapping(value = "/api/public/reports", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnimalReportResponse> createPublicReport(
            @Valid @ModelAttribute AnimalReportCreateRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photo,
            @AuthenticationPrincipal UserDetails userDetails) {

        String userEmail = (userDetails != null) ? userDetails.getUsername() : null;
        AnimalReportResponse response = animalReportService.createPublicReport(request, photo, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // C2: Belediyenin kendi ilçesindeki ihbarları listelemesi
    @GetMapping("/api/municipality/reports")
    @PreAuthorize("hasAnyRole('INSTITUTION', 'ADMIN')")
    public ResponseEntity<Page<AnimalReportResponse>> getReports(
            @RequestParam(required = false) ReportStatus status,
            Pageable pageable) {

        Page<AnimalReportResponse> reports = animalReportService.getReportsForMunicipality(status, pageable);
        return ResponseEntity.ok(reports);
    }

    // C2: İhbar durumunu güncelleme (YENI -> ISLEME_ALINDI -> TAMAMLANDI)
    @PatchMapping("/api/municipality/reports/{id}/status")
    @PreAuthorize("hasAnyRole('INSTITUTION', 'ADMIN')")
    public ResponseEntity<AnimalReportResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam ReportStatus status) {

        AnimalReportResponse response = animalReportService.updateStatus(id, status);
        return ResponseEntity.ok(response);
    }
}