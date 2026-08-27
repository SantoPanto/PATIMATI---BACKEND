package com.works.patimati.repository;

import com.works.patimati.entity.VetClinic;
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
public interface VetClinicRepository extends JpaRepository<VetClinic, Long> {

    Optional<VetClinic> findByUser_Uid(Long userUid);

    Page<VetClinic> findByCityIgnoreCase(String city, Pageable pageable);

    /**
     * Ana haritayı besleyen yarıçap sorgusu -- {@code PointOfInterestRepository.findNearby}
     * ile AYNI desen. {@code location IS NULL} olan klinikler zaten
     * {@code ST_DWithin} sonucuna girmez, ekstra filtre gerekmiyor.
     */
    @Query(value = """
        SELECT *
          FROM vet_clinics v
         WHERE ST_DWithin(
                   v.location::geography,
                   CAST(:origin AS geography),
                   :distanceInMeters
               ) = TRUE
        """, nativeQuery = true)
    List<VetClinic> findNearby(
            @Param("origin") Point origin,
            @Param("distanceInMeters") double distanceInMeters
    );
}
