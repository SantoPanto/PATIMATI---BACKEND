package com.works.patimati.repository;

import com.works.patimati.entity.AdComplaint;
import com.works.patimati.entity.enums.ComplaintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * İlan Şikayetleri Spring Data JPA Repository arayüzü.
 */
@Repository
public interface AdComplaintRepository extends JpaRepository<AdComplaint, Long>, JpaSpecificationExecutor<AdComplaint> {

    /**
     * Bir kullanıcının aynı ilan için belirtilen aktif statülerde şikayet kaydının olup olmadığını kontrol eder.
     * (Veritabanındaki composite index: idx_ad_complaint_reporter_ad_status tarafından optimize edilir).
     *
     * @param reporterId Şikayet eden kullanıcının ID'si
     * @param adId       Şikayet edilen ilanın ID'si
     * @param statuses   Aktif kabul edilen şikayet statüleri (ör. BEKLEMEDE, INCELEMEDE)
     * @return Aktif şikayet var ise true, yok ise false
     */
    boolean existsByReporterIdAndAdIdAndStatusIn(
            Long reporterId,
            Long adId,
            Collection<ComplaintStatus> statuses
    );

    /**
     * Belirli bir ilana yapılmış ve belirli bir statüdeki şikayetleri listeler.
     */
    List<AdComplaint> findByAdIdAndStatus(Long adId, ComplaintStatus status);

    /**
     * Belirli bir statüdeki tüm ilan şikayetlerini listeler.
     */
    List<AdComplaint> findByStatus(ComplaintStatus status);

    /** "Şikayetlerim" (S7): kullanıcının kendi açtıkları, en yeni üstte. */
    List<AdComplaint> findByReporterIdOrderByCreatedAtDesc(Long reporterId);
}
