package com.works.patimati.controller;

import com.works.patimati.dto.ad.AdResponse;
import com.works.patimati.dto.admin.AdComplaintAdminResponse;
import com.works.patimati.dto.admin.CreatePetShopAccountRequest;
import com.works.patimati.dto.admin.CreateShelterAccountRequest;
import com.works.patimati.dto.admin.CreateVetAccountRequest;
import com.works.patimati.dto.admin.ExternalPostAdminResponse;
import com.works.patimati.dto.admin.InstagramPublishQueueAdminResponse;
import com.works.patimati.dto.admin.InstagramPublishRequest;
import com.works.patimati.dto.admin.UserComplaintAdminResponse;
import com.works.patimati.dto.admin.UserDetailForAdminDTO;
import com.works.patimati.entity.enums.InstagramPublishStatus;
import com.works.patimati.service.AdminService;
import com.works.patimati.service.InstagramPublishService;
import com.works.patimati.service.PetShopService;
import com.works.patimati.service.ShelterService;
import com.works.patimati.service.VetClinicService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private static final int MAX_PAGE_SIZE = 100;
    private final AdminService adminService;
    private final InstagramPublishService instagramPublishService;
    private final VetClinicService vetClinicService;
    private final PetShopService petShopService;
    private final ShelterService shelterService;

    /**
     * Sistemdeki tüm kullanıcıları detaylı şekilde listeler.
     */
    @GetMapping("/users")
    public ResponseEntity<Page<UserDetailForAdminDTO>> getAllUsers(
            @RequestParam(required = false) String search,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(adminService.getAllUsers(search, pageable));
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
     * Sistemdeki tüm ilanları sayfalı olarak listeler.
     */
    @GetMapping("/ads")
    public ResponseEntity<Page<AdResponse>> getAllAds(
            @RequestParam(required = false) String search,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(adminService.getAllAds(search, pageable));
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
     * İlan şikayetlerini incelemek için bağlam bilgileriyle listeler.
     */
    @GetMapping("/complaints/ads")
    public ResponseEntity<Page<AdComplaintAdminResponse>> getAdComplaints(
            @RequestParam(required = false) String search,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(adminService.getAdComplaints(search, pageable));
    }

    /**
     * Kullanıcı profili şikayetlerini bağlam bilgileriyle listeler.
     */
    @GetMapping("/complaints/users")
    public ResponseEntity<Page<UserComplaintAdminResponse>> getUserComplaints(
            @RequestParam(required = false) String search,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(adminService.getUserComplaints(search, pageable));
    }

    /**
     * Sahiplendirme ilanı şikayetlerini bağlam bilgileriyle listeler.
     */
    @GetMapping("/complaints/adoptions")
    public ResponseEntity<Page<com.works.patimati.dto.admin.AdoptionComplaintAdminResponse>> getAdoptionComplaints(
            @RequestParam(required = false) String search,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(adminService.getAdoptionComplaints(search, pageable));
    }

    /**
     * Collector'ın topladığı tüm Instagram gönderilerini (eşleşsin eşleşmesin) listeler.
     */
    @GetMapping("/external-posts")
    public ResponseEntity<Page<ExternalPostAdminResponse>> getExternalPosts(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(MAX_PAGE_SIZE) int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "detectedAt"));
        return ResponseEntity.ok(adminService.getExternalPosts(pageable));
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

    /**
     * İlanların PatiMati'nin Instagram hesabında paylaşılma kuyruğu --
     * yalnızca izin verilmiş (bkz. Ad.instagramShareConsent) LOST/FOUND/
     * ADOPTION ilanları burada listelenir.
     */
    @GetMapping("/instagram-queue")
    public ResponseEntity<Page<InstagramPublishQueueAdminResponse>> getInstagramQueue(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(MAX_PAGE_SIZE) int size,
            @RequestParam(required = false) InstagramPublishStatus status
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(instagramPublishService.listQueue(pageable, status));
    }

    /**
     * Kuyruktaki bir ilanı Instagram'a yayınlar -- caption admin tarafından
     * düzenlenmiş olabilir (bkz. InstagramPublishRequest).
     */
    @PostMapping("/instagram-queue/{id}/publish")
    public ResponseEntity<Map<String, String>> publishToInstagram(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody InstagramPublishRequest body,
            Authentication authentication
    ) {
        boolean basarili = instagramPublishService.publish(id, body.caption(), authentication.getName());
        if (!basarili) {
            // Kuyruk kaydı YİNE DE FAILED olarak güncellendi (admin panelde
            // görünür, düzenlenip tekrar denenebilir) -- 502, "isteğin kendisi
            // reddedildi" değil "aşağı akış (Instagram) başarısız oldu" demek.
            return ResponseEntity.status(502)
                    .body(Map.of("message", "İlan Instagram'a gönderilemedi. Kuyrukta 'Başarısız' olarak işaretlendi, tekrar deneyebilirsiniz."));
        }
        return ResponseEntity.ok(Map.of("message", "İlan Instagram'a gönderildi."));
    }

    /**
     * Admin bu ilanı Instagram'da paylaşmamaya karar verir -- kuyruk
     * kaydını SKIPPED yapar, ilanı etkilemez.
     */
    @PostMapping("/instagram-queue/{id}/skip")
    public ResponseEntity<Map<String, String>> skipInstagramQueueItem(
            @PathVariable @Min(1) Long id,
            Authentication authentication
    ) {
        instagramPublishService.skip(id, authentication.getName());
        return ResponseEntity.ok(Map.of("message", "İlan Instagram kuyruğundan çıkarıldı."));
    }

    /**
     * Bir veteriner hesabı açar (self-servis kayıt YOK) -- vet, bu hesapla
     * giriş yapıp kendi klinik bilgi kartını {@code /vet/panel}'den oluşturur.
     */
    @PostMapping("/vet-accounts")
    public ResponseEntity<Map<String, String>> createVetAccount(@Valid @RequestBody CreateVetAccountRequest body) {
        vetClinicService.createVetAccount(body);
        return ResponseEntity.ok(Map.of("message", "Veteriner hesabı oluşturuldu."));
    }

    /**
     * Bir petshop hesabı açar (self-servis kayıt YOK) -- sahibi, bu hesapla
     * giriş yapıp kendi petshop bilgi kartını {@code /petshop/panel}'den oluşturur.
     */
    @PostMapping("/petshop-accounts")
    public ResponseEntity<Map<String, String>> createPetShopAccount(@Valid @RequestBody CreatePetShopAccountRequest body) {
        petShopService.createPetShopAccount(body);
        return ResponseEntity.ok(Map.of("message", "Petshop hesabı oluşturuldu."));
    }

    /**
     * Bir barınak hesabı açar (self-servis kayıt YOK) -- sahibi, bu hesapla
     * giriş yapıp kendi barınak bilgi kartını {@code /barinak/panel}'den oluşturur.
     */
    @PostMapping("/shelter-accounts")
    public ResponseEntity<Map<String, String>> createShelterAccount(@Valid @RequestBody CreateShelterAccountRequest body) {
        shelterService.createShelterAccount(body);
        return ResponseEntity.ok(Map.of("message", "Barınak hesabı oluşturuldu."));
    }
}
