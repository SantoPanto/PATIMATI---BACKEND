package com.works.patimati.repository;

import com.works.patimati.entity.Ad;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdRepository extends JpaRepository<Ad, Long> {

    // Yalnızca aktif bir ilanı getirir.
    Optional<Ad> findByIdAndActiveTrue(Long adId);

    // İlanın hem aktif olduğunu hem de belirtilen kullanıcıya ait olduğunu kontrol eder.
    Optional<Ad> findByIdAndUser_UidAndActiveTrue(Long adId, Long userId);

    // İlanları aktiflik durumuna göre sayfalı biçimde listeler.
    Page<Ad> findAllByActive(
            boolean active,
            Pageable pageable
    );

    // İlanları türüne ve aktiflik durumuna göre filtreler.
    Page<Ad> findAllByAdTypeAndActive(
            Ad.AdType adType,
            boolean active,
            Pageable pageable
    );

    // Bir kullanıcıya ait ilanları aktiflik durumuna göre listeler.
    Page<Ad> findAllByUser_UidAndActive(
            Long userId,
            boolean active,
            Pageable pageable
    );

    // KISIM 3
    @Query(
            value = "SELECT * FROM ads a WHERE a.active = true AND ST_DWithin(a.location::geography, :userPoint::geography, :distanceInMeters) = true", nativeQuery = true)
    List<Ad> findNearbyAds(@Param("userPoint") Point userPoint, @Param("distanceInMeters") double distanceInMeters);
}