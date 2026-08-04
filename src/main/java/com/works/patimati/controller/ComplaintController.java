package com.works.patimati.controller;

import com.works.patimati.dto.complaint.AdComplaintRequestDTO;
import com.works.patimati.dto.complaint.ComplaintResponse;
import com.works.patimati.dto.complaint.UserComplaintRequestDTO;
import com.works.patimati.service.AdComplaintService;
import com.works.patimati.service.UserComplaintService;
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
}
