package com.works.patimati.service;

import com.works.patimati.dto.ad.AdResponse;
import com.works.patimati.dto.ad.AdoptionAdCreateRequest;
import com.works.patimati.dto.ad.AdoptionAdUpdateRequest;
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
     * Herkese açık aktif sahiplendirme ilanlarını listeler.
     */
    Page<AdResponse> getPublicAdoptionAds(Pageable pageable);

    /**
     * Sahiplendirme ilanına yapılan şikayeti kaydeder.
     */
    ComplaintResponse createAdoptionComplaint(
            String reporterEmail,
            Long adId,
            AdComplaintRequestDTO request
    );
}
