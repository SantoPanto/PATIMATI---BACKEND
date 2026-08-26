package com.works.patimati.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.enums.AdResolutionStatus;
import java.time.Instant;
import java.util.List;

@Repository
public interface MunicipalityPanelRepository extends JpaRepository<Ad, Long> {

    @Query("SELECT COUNT(a) FROM Ad a WHERE a.district = :district AND a.adType = :adType AND a.createdAt BETWEEN :startDate AND :endDate")
    long countAdsByDistrictAndType(
        @Param("district") String district, 
        @Param("adType") Ad.AdType adType, 
        @Param("startDate") Instant startDate, 
        @Param("endDate") Instant endDate
    );

    @Query("SELECT COUNT(a) FROM Ad a WHERE a.district = :district AND a.resolutionStatus = :status AND a.resolvedByAdId IS NOT NULL AND a.createdAt BETWEEN :startDate AND :endDate")
    long countReunionsByDistrict(
        @Param("district") String district, 
        @Param("status") AdResolutionStatus status,
        @Param("startDate") Instant startDate, 
        @Param("endDate") Instant endDate
    );

    @Query(value = "SELECT ST_Y(a.location) as lat, ST_X(a.location) as lng, a.ad_type as type, a.created_at as created_at " +
                   "FROM ads a WHERE a.district = :district AND a.created_at BETWEEN :startDate AND :endDate " +
                   "LIMIT :limitCount", nativeQuery = true)
    List<Object[]> getHeatmapPoints(
        @Param("district") String district, 
        @Param("startDate") Instant startDate, 
        @Param("endDate") Instant endDate, 
        @Param("limitCount") int limitCount
    );
}
