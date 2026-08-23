package com.works.patimati.controller;

import com.works.patimati.dto.ad.AdResponse;
import com.works.patimati.dto.admin.AdComplaintAdminResponse;
import com.works.patimati.dto.admin.AdoptionComplaintAdminResponse;
import com.works.patimati.dto.admin.UserComplaintAdminResponse;
import com.works.patimati.dto.admin.UserDetailForAdminDTO;
import com.works.patimati.service.AdminService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * Sistemdeki tüm kullanıcıları detaylı ve filtreli şekilde listeler.
     */
    @GetMapping("/users")
    public ResponseEntity<Page<UserDetailForAdminDTO>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String keyword,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        String queryTerm = StringUtils.hasText(search) ? search : keyword;
        return ResponseEntity.ok(adminService.getAllUsers(queryTerm, pageable));
    }

    /**
     * Kullanıcı hesabını engeller / dondurur.
     */
    @PutMapping("/users/{userId}/ban")
    public ResponseEntity<Map<String, String>> banUser(@PathVariable @Min(1) Long userId) {
        adminService.banUser(userId);
        return ResponseEntity.ok(Map.of("message", "User with ID " + userId + " has been successfully banned."));
    }

    /**
     * Kullanıcı engelini kaldırır.
     */
    @PutMapping("/users/{userId}/unban")
    public ResponseEntity<Map<String, String>> unbanUser(@PathVariable @Min(1) Long userId) {
        adminService.unbanUser(userId);
        return ResponseEntity.ok(Map.of("message", "User with ID " + userId + " has been successfully unbanned."));
    }

    /**
     * Sistemdeki tüm ilanları sayfalı ve filtreli olarak listeler.
     */
    @GetMapping("/ads")
    public ResponseEntity<Page<AdResponse>> getAllAds(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String keyword,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        String queryTerm = StringUtils.hasText(search) ? search : keyword;
        return ResponseEntity.ok(adminService.getAllAds(queryTerm, pageable));
    }

    /**
     * Şikayet inceleme sürecinde ilanı geçici olarak gizler (askıya alır).
     */
    @PutMapping("/ads/{adId}/suspend")
    public ResponseEntity<Map<String, String>> suspendAd(@PathVariable @Min(1) Long adId) {
        adminService.suspendAd(adId);
        return ResponseEntity.ok(Map.of("message", "Ad with ID " + adId + " has been successfully suspended."));
    }

    /**
     * İlanın gizliliğini kaldırıp tekrar aktif hale getirir.
     */
    @PutMapping("/ads/{adId}/unhide")
    public ResponseEntity<Map<String, String>> unhideAd(@PathVariable @Min(1) Long adId) {
        adminService.unhideAd(adId);
        return ResponseEntity.ok(Map.of("message", "Ad with ID " + adId + " has been successfully unhidden."));
    }

    /**
     * İlanı veritabanından tamamen siler (Hard Delete).
     */
    @DeleteMapping("/ads/{adId}")
    public ResponseEntity<Map<String, String>> deleteAdAsAdmin(@PathVariable @Min(1) Long adId) {
        adminService.deleteAdAsAdmin(adId);
        return ResponseEntity.ok(Map.of("message", "Ad with ID " + adId + " has been successfully deleted."));
    }

    /**
     * FAILED durumundaki tüm aktif ilanları topluca yeniden analize gönderir.
     * AI servisi arızası düzeltildikten sonra takılı kalan ilanları kurtarmak
     * için: arıza sırasında açılan her ilan FAILED kalıyor ve kendiliğinden
     * bir daha analiz edilmiyordu.
     */
    @PostMapping("/ads/reanalyze-failed")
    public ResponseEntity<Map<String, Object>> reanalyzeFailedAds() {
        int kuyruklanan = adminService.reanalyzeFailedAds();
        return ResponseEntity.ok(Map.of(
                "queued", kuyruklanan,
                "message", kuyruklanan + " ilan yeniden analize gönderildi."));
    }

    /**
     * İlan şikayetlerini incelemek için bağlam bilgileriyle sayfalı ve filtreli listeler.
     */
    @GetMapping("/complaints/ads")
    public ResponseEntity<Page<AdComplaintAdminResponse>> getAdComplaints(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String keyword,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        String queryTerm = StringUtils.hasText(search) ? search : keyword;
        return ResponseEntity.ok(adminService.getAdComplaints(queryTerm, pageable));
    }

    /**
     * Kullanıcı profili şikayetlerini bağlam bilgileriyle sayfalı ve filtreli listeler.
     */
    @GetMapping("/complaints/users")
    public ResponseEntity<Page<UserComplaintAdminResponse>> getUserComplaints(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String keyword,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        String queryTerm = StringUtils.hasText(search) ? search : keyword;
        return ResponseEntity.ok(adminService.getUserComplaints(queryTerm, pageable));
    }

    /**
     * Sahiplendirme ilanı şikayetlerini bağlam bilgileriyle sayfalı ve filtreli listeler.
     */
    @GetMapping("/complaints/adoptions")
    public ResponseEntity<Page<AdoptionComplaintAdminResponse>> getAdoptionComplaints(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String keyword,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        String queryTerm = StringUtils.hasText(search) ? search : keyword;
        return ResponseEntity.ok(adminService.getAdoptionComplaints(queryTerm, pageable));
    }

    /**
     * V19 sonrası bir defalık: il/ilçesi boş, koordinatı dolu ilanları
     * ters geokodlamayla doldurur. İstekler arasında ~1,1 sn beklendiği
     * için ilan sayısına göre sürer (canlıdaki ~30 ilan ≈ 35 sn).
     * Yeniden çağrılması güvenlidir: yalnız hâlâ boş olanlar denenir.
     */
    @org.springframework.web.bind.annotation.PostMapping("/ads/backfill-location")
    public ResponseEntity<java.util.Map<String, Integer>> backfillAdLocations() {
        return ResponseEntity.ok(adminService.backfillAdLocations());
    }
}
