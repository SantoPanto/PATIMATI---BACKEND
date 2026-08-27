package com.works.patimati.controller;

import com.works.patimati.dto.complaint.AdComplaintRequestDTO;
import com.works.patimati.dto.complaint.ComplaintResponse;
import com.works.patimati.dto.complaint.MyComplaintResponse;
import com.works.patimati.dto.complaint.UserComplaintRequestDTO;
import com.works.patimati.service.AdComplaintService;
import com.works.patimati.service.MyComplaintsService;
import com.works.patimati.service.UserComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * Şikayet İşlemleri REST Controller.
 * Modüler mimariye uygun olarak AdComplaintService ve UserComplaintService servislerine istekleri yönlendirir.
 */
@Validated
@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final UserComplaintService userComplaintService;
    private final AdComplaintService adComplaintService;
    private final MyComplaintsService myComplaintsService;

    /**
     * "Şikayetlerim" (S7): oturum sahibinin üç tablodan birleşik şikayet listesi.
     * GET /api/complaints/mine — en yeni üstte.
     */
    @GetMapping("/mine")
    public ResponseEntity<java.util.List<MyComplaintResponse>> myComplaints(Authentication authentication) {
        return ResponseEntity.ok(myComplaintsService.benimSikayetlerim(authentication.getName()));
    }

    /**
     * Kullanıcı Profil Şikayeti Uç Noktası.
     * POST /api/complaints/user
     *
     * @param authentication Oturum açmış kullanıcının Spring Security Context bilgisi.
     * @param request        Kullanıcı şikayet DTO'su (reportedUserId, reason, description).
     * @return 201 Created ve oluşturulan şikayet yanıtı.
     */
    @PostMapping("/user")
    public ResponseEntity<ComplaintResponse> createUserComplaint(
            Authentication authentication,
            @Valid @RequestBody UserComplaintRequestDTO request
    ) {
        ComplaintResponse response = userComplaintService.createUserComplaint(
                authentication.getName(),
                request
        );

        return ResponseEntity
                .created(URI.create("/api/complaints/" + response.id()))
                .body(response);
    }

    /**
     * İlan Şikayeti Uç Noktası.
     * POST /api/complaints/ad
     * İstemciden KESİNLİKLE reportedUserId beklenmez! Yalnızca reportedAdId, reason ve description alınır.
     *
     * @param authentication Oturum açmış kullanıcının Spring Security Context bilgisi.
     * @param request        İlan şikayet DTO'su (reportedAdId, reason, description).
     * @return 201 Created ve oluşturulan şikayet yanıtı.
     */
    @PostMapping("/ad")
    public ResponseEntity<ComplaintResponse> createAdComplaint(
            Authentication authentication,
            @Valid @RequestBody AdComplaintRequestDTO request
    ) {
        ComplaintResponse response = adComplaintService.createAdComplaint(
                authentication.getName(),
                request
        );

        return ResponseEntity
                .created(URI.create("/api/complaints/" + response.id()))
                .body(response);
    }

    /**
     * İlan Şikayeti Çözme Uç Noktası (Sadece Admin).
     * PATCH /api/complaints/ad/{id}/resolve
     */
    @PatchMapping("/ad/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComplaintResponse> resolveAdComplaint(@PathVariable @Min(1) Long id) {
        return ResponseEntity.ok(adComplaintService.resolveAdComplaint(id));
    }

    /**
     * Kullanıcı Profil Şikayeti Çözme Uç Noktası (Sadece Admin).
     * PATCH /api/complaints/user/{id}/resolve
     */
    @PatchMapping("/user/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComplaintResponse> resolveUserComplaint(@PathVariable @Min(1) Long id) {
        return ResponseEntity.ok(userComplaintService.resolveUserComplaint(id));
    }
}
