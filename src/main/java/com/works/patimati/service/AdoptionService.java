package com.works.patimati.service;

import com.works.patimati.dto.ad.AdResponse;
import com.works.patimati.dto.ad.AdoptionAdCreateRequest;
import com.works.patimati.dto.ad.AdoptionAdUpdateRequest;
import com.works.patimati.dto.ad.ResolveAdoptionAdRequest;
import com.works.patimati.dto.complaint.AdComplaintRequestDTO;
import com.works.patimati.dto.complaint.ComplaintResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Sahiplendirme (Pet Adoption) İlanları İş Mantığı Arayüzü.
 */
public interface AdoptionService {

    /**
     * Sahiplendirme ilanı oluşturur (En az 1 fotoğraf zorunludur, AI analizine gönderilmez).
     */
    AdResponse createAdoptionAd(
            String ownerEmail,
            AdoptionAdCreateRequest request,
            List<MultipartFile> images
    );

    /**
     * İlan sahibinin kendi sahiplendirme ilanını güncellemesini sağlar.
     */
    AdResponse updateAdoptionAd(
            String ownerEmail,
            Long adId,
            AdoptionAdUpdateRequest request
    );

    /**
     * İlan sahibinin sahiplendirme ilanını kaldırmasını (soft delete) sağlar.
     */
    void deleteAdoptionAd(String ownerEmail, Long adId);

    /**
     * İlan sahibinin sahiplendirme ilanını sahiplendirildi olarak kapatmasını ve ödül puanı dağıtılmasını sağlar.
     */
    void resolveAdoptionAd(String ownerEmail, Long adId, ResolveAdoptionAdRequest request);

    /**
     * Herkese açık aktif sahiplendirme ilanlarını listeler.
     */
    Page<AdResponse> getPublicAdoptionAds(Pageable pageable);

    /**
     * Herkese açık aktif sahiplendirme ilanlarını TEK bir sahibe (barınak
     * kartının {@code user_id}'sine) göre filtreler -- barınağın herkese
     * açık detay sayfasında "bu barınağın güncel sahiplendirme ilanları"nı
     * göstermek için (bkz. {@code ShelterDirectoryController},
     * {@code ShelterService#resolveOwnerUid}). {@link #getPublicAdoptionAds}'in
     * birebir paraleli, yalnızca ekstra {@code ownerUid} filtresiyle.
     */
    Page<AdResponse> getPublicAdoptionAdsByOwner(Long ownerUid, Pageable pageable);

    /**
     * Sahiplendirme ilanına yapılan şikayeti kaydeder.
     */
    ComplaintResponse createAdoptionComplaint(
            String reporterEmail,
            Long adId,
            AdComplaintRequestDTO request
    );

    /**
     * Sahiplendirme ilanına yapılan şikayeti çözer (COZULDU yapar).
     */
    ComplaintResponse resolveAdoptionComplaint(Long complaintId);
}
