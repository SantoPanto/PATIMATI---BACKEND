package com.works.patimati.repository;

import com.works.patimati.entity.Ad;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdRepository extends JpaRepository<Ad, Long> {

    // KISIM 3
    @Query(value = "SELECT * FROM ads a WHERE a.active = true AND ST_DWithin(a.location, :userPoint, :distanceInMeters) = true", nativeQuery = true)
    List<Ad> findNearbyAds(@Param("userPoint") Point userPoint, @Param("distanceInMeters") double distanceInMeters);
}