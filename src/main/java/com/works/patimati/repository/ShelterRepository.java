package com.works.patimati.repository;

import com.works.patimati.entity.Shelter;
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
public interface ShelterRepository extends JpaRepository<Shelter, Long> {

    Optional<Shelter> findByUser_Uid(Long userUid);

    Page<Shelter> findByCityIgnoreCase(String city, Pageable pageable);

    /**
     * Ana haritayı besleyen yarıçap sorgusu -- {@code PointOfInterestRepository.findNearby}
     * ile AYNI desen. {@code location IS NULL} olan barınaklar zaten
     * {@code ST_DWithin} sonucuna girmez, ekstra filtre gerekmiyor.
     */
    @Query(value = """
        SELECT *
          FROM shelters s
         WHERE ST_DWithin(
                   s.location::geography,
                   CAST(:origin AS geography),
                   :distanceInMeters
               ) = TRUE
        """, nativeQuery = true)
    List<Shelter> findNearby(
            @Param("origin") Point origin,
            @Param("distanceInMeters") double distanceInMeters
    );
}
