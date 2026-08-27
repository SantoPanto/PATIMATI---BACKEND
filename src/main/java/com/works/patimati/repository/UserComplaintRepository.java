package com.works.patimati.repository;

import com.works.patimati.entity.UserComplaint;
import com.works.patimati.entity.enums.ComplaintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * Kullanıcı Profil Şikayetleri Spring Data JPA Repository arayüzü.
 */
@Repository
public interface UserComplaintRepository extends JpaRepository<UserComplaint, Long>, JpaSpecificationExecutor<UserComplaint> {

    /**
     * Bir kullanıcının başka bir kullanıcı için belirtilen aktif statülerde şikayet kaydının olup olmadığını kontrol eder.
     * (Veritabanındaki composite index: idx_user_complaint_reporter_user_status tarafından optimize edilir).
     *
     * @param reporterId     Şikayet eden kullanıcının ID'si
     * @param reportedUserId Şikayet edilen kullanıcının ID'si
     * @param statuses       Aktif kabul edilen şikayet statüleri (ör. BEKLEMEDE, INCELEMEDE)
     * @return Aktif şikayet var ise true, yok ise false
     */
    boolean existsByReporterIdAndReportedUserIdAndStatusIn(
            Long reporterId,
            Long reportedUserId,
            Collection<ComplaintStatus> statuses
    );

    /**
     * Belirli bir kullanıcıya yapılmış ve belirli bir statüdeki şikayetleri listeler.
     */
    List<UserComplaint> findByReportedUserIdAndStatus(Long reportedUserId, ComplaintStatus status);

    /**
     * Belirli bir statüdeki tüm kullanıcı şikayetlerini listeler.
     */
    List<UserComplaint> findByStatus(ComplaintStatus status);

    /** "Şikayetlerim" (S7): kullanıcının kendi açtıkları, en yeni üstte. */
    List<UserComplaint> findByReporterIdOrderByCreatedAtDesc(Long reporterId);
}
