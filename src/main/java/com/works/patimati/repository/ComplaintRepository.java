package com.works.patimati.repository;

import com.works.patimati.entity.Complaint;
import com.works.patimati.entity.enums.ComplaintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    // Belirli bir ilana yönelik belirtilen statüdeki şikayetleri listeler
    List<Complaint> findByReportedAd_IdAndStatus(Long adId, ComplaintStatus status);

    // Belirli bir statüdeki (ör. BEKLEMEDE) şikayetleri listeler
    List<Complaint> findByStatus(ComplaintStatus status);

    // Belirli bir kullanıcıya yönelik şikayetleri listeler
    List<Complaint> findByReportedUser_Uid(Long userId);

    // Bir kullanıcının aynı ilan için aktif şikayeti var mı kontrol eder (Composite Index destekli)
    boolean existsByReporter_UidAndReportedAd_IdAndStatusIn(
            Long reporterId,
            Long adId,
            Collection<ComplaintStatus> statuses
    );

    // Bir kullanıcının aynı kullanıcı için aktif şikayeti var mı kontrol eder (Composite Index destekli)
    boolean existsByReporter_UidAndReportedUser_UidAndStatusIn(
            Long reporterId,
            Long reportedUserId,
            Collection<ComplaintStatus> statuses
    );
}
