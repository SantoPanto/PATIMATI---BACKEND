package com.works.patimati.repository;

import com.works.patimati.entity.AnimalReport;
import com.works.patimati.entity.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AnimalReportRepository extends JpaRepository<AnimalReport, Long> {

    @Query("SELECT r FROM AnimalReport r WHERE LOWER(r.district) = LOWER(:ilce)")
    Page<AnimalReport> findAllByDistrictIgnoreCase(@Param("ilce") String ilce, Pageable pageable);

    @Query("SELECT r FROM AnimalReport r WHERE LOWER(r.district) = LOWER(:ilce) AND r.status = :status")
    Page<AnimalReport> findAllByDistrictIgnoreCaseAndStatus(@Param("ilce") String ilce,
                                                            @Param("status") ReportStatus status,
                                                            Pageable pageable);

    /** İlçesiz yönetici görünümü — süzgeçsiz; kurum hesabı bu yola girmez. */
    Page<AnimalReport> findAllByStatus(ReportStatus status, Pageable pageable);

    // ------------------------------------------------------------------
    // Panel "İhbar analizi" sorguları (S6). İlçe süzgeci panel repo'suyla
    // aynı sözleşme: null ilçe = ilçesiz YÖNETİCİ, süzgeç atlanır; CAST,
    // PostgreSQL'in null parametrede lower(bytea) hatasına karşı (bkz.
    // MunicipalityPanelRepository ve 27.08 EK-3 dersi).
    // ------------------------------------------------------------------

    @Query("SELECT r.status, COUNT(r) FROM AnimalReport r WHERE (:ilce IS NULL OR LOWER(r.district) = LOWER(CAST(:ilce AS String))) AND r.createdAt BETWEEN :start AND :end GROUP BY r.status")
    List<Object[]> durumaGoreSay(@Param("ilce") String ilce,
                                 @Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end);

    @Query("SELECT r.type, COUNT(r) FROM AnimalReport r WHERE (:ilce IS NULL OR LOWER(r.district) = LOWER(CAST(:ilce AS String))) AND r.createdAt BETWEEN :start AND :end GROUP BY r.type")
    List<Object[]> tureGoreSay(@Param("ilce") String ilce,
                               @Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end);

    /**
     * Günlük ihbar sayıları. Native: gün kesmesi tek motorda (PostgreSQL)
     * yapılır; sürücünün gün kolonu için verdiği tip garanti olmadığından
     * dönüşüm servis tarafında tip-korumalı (bkz. MunicipalityPanelService
     * zamanaCevir dersi -- aynı tuzağın tarih hâli).
     */
    @Query(value = "SELECT CAST(created_at AS date) AS gun, COUNT(*) FROM animal_reports WHERE (:ilce IS NULL OR LOWER(district) = LOWER(CAST(:ilce AS text))) AND created_at BETWEEN :start AND :end GROUP BY gun ORDER BY gun", nativeQuery = true)
    List<Object[]> gunlukIhbarSayilari(@Param("ilce") String ilce,
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);
}