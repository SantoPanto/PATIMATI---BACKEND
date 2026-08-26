package com.works.patimati.repository;

import com.works.patimati.entity.AnimalReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnimalReportRepository extends JpaRepository<AnimalReport, Long> {

    // Belediyenin kendi ilçesindeki ihbarları sayfalı getiren sorgu
    Page<AnimalReport> findAllByDistrictIgnoreCase(String district, Pageable pageable);

    // Duruma göre filtrelenmiş sayfalı sorgu
    Page<AnimalReport> findAllByDistrictIgnoreCaseAndStatus(String district, AnimalReport.ReportStatus status, Pageable pageable);
}