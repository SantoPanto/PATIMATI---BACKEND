package com.works.patimati.repository;

import com.works.patimati.entity.PointOfInterest;
import com.works.patimati.entity.enums.PoiSource;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PointOfInterestRepository extends JpaRepository<PointOfInterest, Long> {

    /** Senkronizasyonda upsert anahtarı: aynı OSM node/way tekrar geldiğinde günceller, çoğaltmaz. */
    Optional<PointOfInterest> findBySourceAndOsmId(PoiSource source, Long osmId);

    /**
     * Herkese açık haritayı besleyen yarıçap sorgusu (ads'teki
     * {@code findPublicNearbyAds} ile aynı ST_DWithin deseni).
     * Tür süzgeci burada değil, servis katmanında uygulanır — POI sayısı
     * bir yarıçap içinde küçük kalır, native sorguda opsiyonel liste
     * parametresi (null/IN) karmaşasından kaçınılır.
     */
    @Query(value = """
        SELECT *
          FROM points_of_interest p
         WHERE ST_DWithin(
                   p.location::geography,
                   CAST(:origin AS geography),
                   :distanceInMeters
               ) = TRUE
        """, nativeQuery = true)
    List<PointOfInterest> findNearby(
            @Param("origin") Point origin,
            @Param("distanceInMeters") double distanceInMeters
    );
}
