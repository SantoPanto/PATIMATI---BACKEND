package com.works.patimati.repository;

import com.works.patimati.entity.AnimalReport;
import com.works.patimati.entity.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}