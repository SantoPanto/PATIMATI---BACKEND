package com.works.patimati.service;

import com.works.patimati.dto.ad.AdResponse;
import com.works.patimati.dto.admin.AdComplaintAdminResponse;
import com.works.patimati.dto.admin.AdoptionComplaintAdminResponse;
import com.works.patimati.dto.admin.UserComplaintAdminResponse;
import com.works.patimati.dto.admin.UserDetailForAdminDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Yönetici (Admin) Paneli iş mantığı arayüzü.
 */
public interface AdminService {

    /**
     * Sistemdeki tüm kullanıcıları detay DTO formatında sayfalı ve filtreli olarak listeler.
     */
    Page<UserDetailForAdminDTO> getAllUsers(String search, Pageable pageable);

    default Page<UserDetailForAdminDTO> getAllUsers(Pageable pageable) {
        return getAllUsers(null, pageable);
    }

    /**
     * Belirtilen kullanıcıyı engeller / hesabını dondurur.
     */
    void banUser(Long userId);

    /**
     * Belirtilen kullanıcının engelini kaldırır.
     */
    void unbanUser(Long userId);

    /**
     * Sistemdeki tüm ilanları sayfalı ve filtreli olarak listeler.
     */
    Page<AdResponse> getAllAds(String search, Pageable pageable);

    default Page<AdResponse> getAllAds(Pageable pageable) {
        return getAllAds(null, pageable);
    }

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
     * İlan şikayetlerini bağlam bilgileriyle (ilan başlığı, sahibi, şikayet eden) sayfalı ve filtreli listeler.
     */
    Page<AdComplaintAdminResponse> getAdComplaints(String search, Pageable pageable);

    default Page<AdComplaintAdminResponse> getAdComplaints(Pageable pageable) {
        return getAdComplaints(null, pageable);
    }

    /**
     * Kullanıcı profili şikayetlerini bağlam bilgileriyle sayfalı ve filtreli listeler.
     */
    Page<UserComplaintAdminResponse> getUserComplaints(String search, Pageable pageable);

    default Page<UserComplaintAdminResponse> getUserComplaints(Pageable pageable) {
        return getUserComplaints(null, pageable);
    }

    /**
     * Sahiplendirme ilanı şikayetlerini bağlam bilgileriyle sayfalı ve filtreli listeler.
     */
    Page<AdoptionComplaintAdminResponse> getAdoptionComplaints(String search, Pageable pageable);

    default Page<AdoptionComplaintAdminResponse> getAdoptionComplaints(Pageable pageable) {
        return getAdoptionComplaints(null, pageable);
    }

    /**
     * V19 öncesi ilanların il/ilçesini koordinattan doldurur (bir defalık,
     * dağıtımdan sonra çağrılır). Dönüş: toplam / dolan / cozulemeyen.
     */
    java.util.Map<String, Integer> backfillAdLocations();
}
