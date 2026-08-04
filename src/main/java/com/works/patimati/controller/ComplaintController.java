package com.works.patimati.controller;

import com.works.patimati.dto.complaint.AdComplaintRequestDTO;
import com.works.patimati.dto.complaint.ComplaintResponse;
import com.works.patimati.dto.complaint.UserComplaintRequestDTO;
import com.works.patimati.service.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Şikayet işlemleri için REST API Controller.
 * Kullanıcı şikayeti ve İlan şikayeti için iki ayrı POST uç noktası içerir.
 */
@Validated
@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    /**
     * Kullanıcı Profil Şikayeti Uç Noktası.
     * POST /api/complaints/user
     *
     * @param authentication Oturum açmış kullanıcının Spring Security Context kimlik bilgileri.
     * @param request        Kullanıcı şikayet DTO'su.
     * @return 201 Created ve oluşturulan şikayet detayları.
     */
    @PostMapping("/user")
    public ResponseEntity<ComplaintResponse> createUserComplaint(
            Authentication authentication,
            @Valid @RequestBody UserComplaintRequestDTO request
    ) {
        ComplaintResponse response = complaintService.createUserComplaint(
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
     *
     * @param authentication Oturum açmış kullanıcının Spring Security Context kimlik bilgileri.
     * @param request        İlan şikayet DTO'su.
     * @return 201 Created ve oluşturulan şikayet detayları.
     */
    @PostMapping("/ad")
    public ResponseEntity<ComplaintResponse> createAdComplaint(
            Authentication authentication,
            @Valid @RequestBody AdComplaintRequestDTO request
    ) {
        ComplaintResponse response = complaintService.createAdComplaint(
                authentication.getName(),
                request
        );

        return ResponseEntity
                .created(URI.create("/api/complaints/" + response.id()))
                .body(response);
    }
}
