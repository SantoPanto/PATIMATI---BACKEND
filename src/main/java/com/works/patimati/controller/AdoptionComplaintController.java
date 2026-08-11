package com.works.patimati.controller;

import com.works.patimati.dto.complaint.AdComplaintRequestDTO;
import com.works.patimati.dto.complaint.ComplaintResponse;
import com.works.patimati.service.AdoptionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * Sahiplendirme İlanı Şikayet Uç Noktası.
 */
@Validated
@RestController
@RequestMapping("/api/adoptions")
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
}
