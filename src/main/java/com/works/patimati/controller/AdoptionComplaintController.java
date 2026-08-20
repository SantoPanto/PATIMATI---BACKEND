package com.works.patimati.controller;

import com.works.patimati.dto.complaint.AdComplaintRequestDTO;
import com.works.patimati.dto.complaint.ComplaintResponse;
import com.works.patimati.service.AdoptionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * Sahiplendirme İlanı Şikayet Uç Noktası.
 */
@Validated
@RestController
@RequestMapping({"/api/adoptions", "/api/v1/adoption-complaints", "/api/adoptions/complaints"})
@RequiredArgsConstructor
public class AdoptionComplaintController {

    private final AdoptionService adoptionService;

    /**
     * Sahiplendirme ilanını şikayet etme uç noktası.
     * POST /api/adoptions/{adId}/complaints
     */
    @PostMapping("/{adId}/complaints")
    public ResponseEntity<ComplaintResponse> createAdoptionComplaint(
            Authentication authentication,
            @PathVariable @Min(1) Long adId,
            @Valid @RequestBody AdComplaintRequestDTO request
    ) {
        ComplaintResponse response = adoptionService.createAdoptionComplaint(
                authentication.getName(),
                adId,
                request
        );

        return ResponseEntity
                .created(URI.create("/api/adoptions/complaints/" + response.id()))
                .body(response);
    }

    /**
     * Sahiplendirme ilanı şikayetini çözüldü olarak işaretleme uç noktası (Sadece Admin).
     * PATCH /api/v1/adoption-complaints/{id}/resolve
     * PATCH /api/adoptions/complaints/{id}/resolve
     */
    @PatchMapping({"/complaints/{id}/resolve", "/{id}/resolve"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComplaintResponse> resolveAdoptionComplaint(
            @PathVariable @Min(1) Long id
    ) {
        ComplaintResponse response = adoptionService.resolveAdoptionComplaint(id);
        return ResponseEntity.ok(response);
    }
}
