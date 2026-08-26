package com.works.patimati.repository;

import com.works.patimati.entity.PetShop;
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
public interface PetShopRepository extends JpaRepository<PetShop, Long> {

    Optional<PetShop> findByUser_Uid(Long userUid);

    Page<PetShop> findByCityIgnoreCase(String city, Pageable pageable);

    /**
     * Ana haritayı besleyen yarıçap sorgusu -- {@code PointOfInterestRepository.findNearby}
     * ile AYNI desen. {@code location IS NULL} olan dükkanlar zaten
     * {@code ST_DWithin} sonucuna girmez, ekstra filtre gerekmiyor.
     */
    @Query(value = """
        SELECT *
          FROM petshops p
         WHERE ST_DWithin(
                   p.location::geography,
                   CAST(:origin AS geography),
                   :distanceInMeters
               ) = TRUE
        """, nativeQuery = true)
    List<PetShop> findNearby(
            @Param("origin") Point origin,
            @Param("distanceInMeters") double distanceInMeters
    );
}
