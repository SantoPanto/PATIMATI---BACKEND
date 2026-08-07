package com.works.patimati.service;

import com.works.patimati.dto.ad.AdResponse;
import com.works.patimati.dto.admin.AdComplaintAdminResponse;
import com.works.patimati.dto.admin.UserComplaintAdminResponse;
import com.works.patimati.dto.admin.UserDetailForAdminDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Yönetici (Admin) Paneli iş mantığı arayüzü.
 */
public interface AdminService {

    /**
     * Sistemdeki tüm kullanıcıları detay DTO formatında sayfalı olarak listeler.
     */
    Page<UserDetailForAdminDTO> getAllUsers(Pageable pageable);

    /**
     * Belirtilen kullanıcıyı engeller / hesabını dondurur.
     */
    void banUser(Long userId);

    /**
     * Belirtilen kullanıcının engelini kaldırır.
     */
    void unbanUser(Long userId);

    /**
     * Sistemdeki tüm ilanları sayfalı olarak listeler.
     */
    Page<AdResponse> getAllAds(Pageable pageable);

    /**
     * Şikayet inceleme sürecinde ilanı geçici olarak gizler (askıya alır).
     */
    void suspendAd(Long adId);

    /**
     * İlanın görünürlüğünü tekrar aktif hale getirir (gizlemeyi kaldırır).
     */
    void unhideAd(Long adId);

    /**
     * İlanı veritabanından ve ilişkilerinden tamamen siler (Hard Delete).
     */
    void deleteAdAsAdmin(Long adId);

    /**
     * İlan şikayetlerini bağlam bilgileriyle (ilan başlığı, sahibi, şikayet eden) sayfalı listeler.
     */
    Page<AdComplaintAdminResponse> getAdComplaints(Pageable pageable);

    /**
     * Kullanıcı profili şikayetlerini bağlam bilgileriyle sayfalı listeler.
     */
    Page<UserComplaintAdminResponse> getUserComplaints(Pageable pageable);
}
