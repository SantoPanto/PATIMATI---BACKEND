package com.works.patimati.repository;

import com.works.patimati.entity.AdoptionComplaint;
import com.works.patimati.entity.enums.ComplaintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * Sahiplendirme İlanı Şikayetleri Spring Data JPA Repository arayüzü.
 */
@Repository
public interface AdoptionComplaintRepository extends JpaRepository<AdoptionComplaint, Long>, JpaSpecificationExecutor<AdoptionComplaint> {

    /**
     * Bir kullanıcının aynı sahiplendirme ilanı için aktif şikayet kaydının olup olmadığını kontrol eder.
     */
    boolean existsByReporterIdAndAdIdAndStatusIn(
            Long reporterId,
            Long adId,
            Collection<ComplaintStatus> statuses
    );

    List<AdoptionComplaint> findByAdIdAndStatus(Long adId, ComplaintStatus status);

    List<AdoptionComplaint> findByStatus(ComplaintStatus status);

    /** "Şikayetlerim" (S7): kullanıcının kendi açtıkları, en yeni üstte. */
    List<AdoptionComplaint> findByReporterIdOrderByCreatedAtDesc(Long reporterId);
}
