package com.works.patimati.service;

import com.works.patimati.dto.ad.AdResponse;
import com.works.patimati.dto.admin.AdComplaintAdminResponse;
import com.works.patimati.dto.admin.AdoptionComplaintAdminResponse;
import com.works.patimati.dto.admin.ExternalPostAdminResponse;
import com.works.patimati.dto.admin.UserComplaintAdminResponse;
import com.works.patimati.dto.admin.UserDetailForAdminDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Yönetici (Admin) Paneli iş mantığı arayüzü.
 */
public interface AdminService {

    /**
     * Sistemdeki tüm kullanıcıları detay DTO formatında sayfalı ve arama filtreli olarak listeler.
     */
    Page<UserDetailForAdminDTO> getAllUsers(String search, Pageable pageable);

    /**
     * Belirtilen kullanıcıyı engeller / hesabını dondurur.
     */
    void banUser(Long userId);

    /**
     * Belirtilen kullanıcının engelini kaldırır.
     */
    void unbanUser(Long userId);

    /**
     * Sistemdeki tüm ilanları sayfalı ve arama filtreli olarak listeler.
     */
    Page<AdResponse> getAllAds(String search, Pageable pageable);

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
     * FAILED durumundaki tüm aktif ilanları yeniden analize gönderir;
     * kuyruğa yazılan ilan sayısını döner.
     */
    int reanalyzeFailedAds();

    /**
     * İlan şikayetlerini bağlam bilgileriyle (ilan başlığı, sahibi, şikayet eden) sayfalı ve arama filtreli listeler.
     */
    Page<AdComplaintAdminResponse> getAdComplaints(String search, Pageable pageable);

    /**
     * Kullanıcı profili şikayetlerini bağlam bilgileriyle sayfalı ve arama filtreli listeler.
     */
    Page<UserComplaintAdminResponse> getUserComplaints(String search, Pageable pageable);

    /**
     * Sahiplendirme ilanı şikayetlerini bağlam bilgileriyle sayfalı ve arama filtreli listeler.
     */
    Page<AdoptionComplaintAdminResponse> getAdoptionComplaints(String search, Pageable pageable);

    /**
     * Collector'ın topladığı tüm Instagram gönderilerini (eşleşsin eşleşmesin)
     * en yeniden eskiye sayfalı listeler.
     */
    Page<ExternalPostAdminResponse> getExternalPosts(Pageable pageable);

    /**
     * V19 öncesi ilanların il/ilçesini koordinattan doldurur (bir defalık,
     * dağıtımdan sonra çağrılır). Dönüş: toplam / dolan / cozulemeyen.
     */
    java.util.Map<String, Integer> backfillAdLocations();
}
