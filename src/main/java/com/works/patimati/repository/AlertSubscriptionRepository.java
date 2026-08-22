package com.works.patimati.repository;

import com.works.patimati.entity.AlertSubscription;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertSubscriptionRepository extends JpaRepository<AlertSubscription, Long> {

    Optional<AlertSubscription> findByUser_Uid(Long userUid);

    /**
     * Verilen noktayı kendi yarıçapının içinde gören AÇIK abonelikler.
     *
     * <p>Yarıçap sabit bir parametre değil, abonelik satırının kendi
     * {@code radius_meters} değeridir — herkes kendi seçtiği mesafeye göre
     * süzülür.
     *
     * <p>İki bilinen PostGIS tuzağına dikkat (ikisi de bu depoda yaşandı,
     * bkz. AdRepository.findAiCandidates üstündeki notlar):
     * <ul>
     *   <li>{@code ::geography} cast'i ŞART — geometry üstünde ST_DWithin
     *       birimi derece sayar ve metre sanılan değer dünyayı kapsar.</li>
     *   <li>Parametreye {@code :adPoint::geography} YAZILAMAZ — Hibernate
     *       parametre adını {@code adPoint::geography} diye okur; sütuna
     *       yazmak serbest, parametre için {@code CAST(... AS geography)}
     *       kullanılır.</li>
     * </ul>
     */
    @Query(value = """
            SELECT s.*
            FROM alert_subscriptions s
            WHERE s.enabled = TRUE
              AND s.user_id <> :ownerUid
              AND ST_DWithin(
                      s.location::geography,
                      CAST(:adPoint AS geography),
                      s.radius_meters
                  )
            """, nativeQuery = true)
    List<AlertSubscription> findEnabledWithinOwnRadius(
            @Param("adPoint") Point adPoint,
            @Param("ownerUid") Long ownerUid
    );
}
