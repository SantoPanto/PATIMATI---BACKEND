package com.works.patimati.controller;

import com.works.patimati.dto.ad.AdResponse;
import com.works.patimati.dto.admin.AdComplaintAdminResponse;
import com.works.patimati.dto.admin.UserComplaintAdminResponse;
import com.works.patimati.dto.admin.UserDetailForAdminDTO;
import com.works.patimati.service.AdminService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private static final int MAX_PAGE_SIZE = 100;
    private final AdminService adminService;

    /**
     * Sistemdeki tüm kullanıcıları detaylı şekilde listeler.
     */
    @GetMapping("/users")
    public ResponseEntity<Page<UserDetailForAdminDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(MAX_PAGE_SIZE) int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(adminService.getAllUsers(pageable));
    }

    /**
     * Kullanıcı hesabını engeller / dondurur.
     */
    @PutMapping("/users/{userId}/ban")
    public ResponseEntity<Void> banUser(@PathVariable @Min(1) Long userId) {
        adminService.banUser(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Kullanıcı engelini kaldırır.
     */
    @PutMapping("/users/{userId}/unban")
    public ResponseEntity<Void> unbanUser(@PathVariable @Min(1) Long userId) {
        adminService.unbanUser(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Sistemdeki tüm ilanları sayfalı olarak listeler.
     */
    @GetMapping("/ads")
    public ResponseEntity<Page<AdResponse>> getAllAds(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(MAX_PAGE_SIZE) int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(adminService.getAllAds(pageable));
    }

    /**
     * Şikayet inceleme sürecinde ilanı geçici olarak gizler (askıya alır).
     */
    @PutMapping("/ads/{adId}/suspend")
    public ResponseEntity<Void> suspendAd(@PathVariable @Min(1) Long adId) {
        adminService.suspendAd(adId);
        return ResponseEntity.noContent().build();
    }

    /**
     * İlanın gizliliğini kaldırıp tekrar aktif hale getirir.
     */
    @PutMapping("/ads/{adId}/unhide")
    public ResponseEntity<Void> unhideAd(@PathVariable @Min(1) Long adId) {
        adminService.unhideAd(adId);
        return ResponseEntity.noContent().build();
    }

    /**
     * İlanı veritabanından tamamen siler (Hard Delete).
     */
    @DeleteMapping("/ads/{adId}")
    public ResponseEntity<Void> deleteAdAsAdmin(@PathVariable @Min(1) Long adId) {
        adminService.deleteAdAsAdmin(adId);
        return ResponseEntity.noContent().build();
    }

    /**
     * İlan şikayetlerini incelemek için bağlam bilgileriyle listeler.
     */
    @GetMapping("/complaints/ads")
    public ResponseEntity<Page<AdComplaintAdminResponse>> getAdComplaints(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(MAX_PAGE_SIZE) int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(adminService.getAdComplaints(pageable));
    }

    /**
     * Kullanıcı profili şikayetlerini bağlam bilgileriyle listeler.
     */
    @GetMapping("/complaints/users")
    public ResponseEntity<Page<UserComplaintAdminResponse>> getUserComplaints(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(MAX_PAGE_SIZE) int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(adminService.getUserComplaints(pageable));
    }
}
