package com.works.patimati.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.enums.AdResolutionStatus;
import java.time.Instant;
import java.util.List;
import java.util.Collection;

@Repository
public interface MunicipalityPanelRepository extends JpaRepository<Ad, Long> {

    // :district NULL ise süzgeç yok — yalnız İLÇESİZ YÖNETİCİ bu yoldan gelir
    // (MunicipalityScopeService.tumIlceler); kurum hesabında ilçe hep dolu.
    @Query("SELECT COUNT(a) FROM Ad a WHERE (:district IS NULL OR LOWER(a.district) = LOWER(CAST(:district AS String))) AND a.adType = :adType AND a.createdAt BETWEEN :startDate AND :endDate")
    long countAdsByDistrictAndType(
        @Param("district") String district,
        @Param("adType") Ad.AdType adType,
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );

    @Query("SELECT COUNT(a) FROM Ad a WHERE (:district IS NULL OR LOWER(a.district) = LOWER(CAST(:district AS String))) AND a.resolutionStatus IN :statuses AND a.resolvedByAdId IS NOT NULL AND a.createdAt BETWEEN :startDate AND :endDate")
    long countReunionsByDistrict(
        @Param("district") String district,
        @Param("statuses") Collection<AdResolutionStatus> statuses,
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );

    /**
     * Yoğunluk haritası noktaları — üç kaynaktan (27.08 kullanıcı geri
     * bildirimi: haritada ihbarlar ve kavuşanlar görünmüyordu, plan §6 da
     * {@code ad_sightings}'i sayıyordu):
     *
     * <ul>
     *   <li><b>ads</b> — kavuşmuş ilan (kavuşma statüsü + {@code resolved_by_ad_id}
     *       dolu; {@code countReunionsByDistrict} ile aynı tanım) {@code REUNION}
     *       kategorisiyle, kalanlar kendi {@code ad_type}'ıyla;</li>
     *   <li><b>animal_reports</b> — vatandaş ihbarları kendi türüyle
     *       (YARALI/SAHIPSIZ/DIGER); ilçe kendi sütunundan;</li>
     *   <li><b>ad_sightings</b> — "gördüm" bildirimleri {@code SIGHTING}
     *       kategorisiyle; tabloda ilçe olmadığından bağlı İLANIN ilçesiyle
     *       süzülür (görülme komşu ilçeye taşabilir — bilinçli yaklaşıklık).</li>
     * </ul>
     *
     * <p>{@code ORDER BY created_at DESC} bilinçli: eski sorguda sırasız LIMIT
     * hangi satırların döneceğini belirsiz bırakıyordu; şimdi limit "en yeni N"
     * anlamına geliyor.
     */
    @Query(value = """
            SELECT t.lat, t.lng, t.kategori, t.created_at FROM (
                SELECT ST_Y(a.location) AS lat, ST_X(a.location) AS lng,
                       CASE WHEN a.resolution_status IN ('FOUND','ADOPTED')
                                 AND a.resolved_by_ad_id IS NOT NULL
                            THEN 'REUNION' ELSE a.ad_type END AS kategori,
                       a.created_at AS created_at
                FROM ads a
                WHERE (CAST(:district AS text) IS NULL OR LOWER(a.district) = LOWER(CAST(:district AS text)))
                  AND a.location IS NOT NULL
                  AND a.created_at BETWEEN :startDate AND :endDate
                UNION ALL
                SELECT ST_Y(r.location), ST_X(r.location), r.type, r.created_at
                FROM animal_reports r
                WHERE (CAST(:district AS text) IS NULL OR LOWER(r.district) = LOWER(CAST(:district AS text)))
                  AND r.created_at BETWEEN :startDate AND :endDate
                UNION ALL
                SELECT ST_Y(s.location), ST_X(s.location), 'SIGHTING', s.created_at
                FROM ad_sightings s
                JOIN ads bagli ON bagli.id = s.ad_id
                WHERE (CAST(:district AS text) IS NULL OR LOWER(bagli.district) = LOWER(CAST(:district AS text)))
                  AND s.created_at BETWEEN :startDate AND :endDate
            ) t
            ORDER BY t.created_at DESC
            LIMIT :limitCount
            """, nativeQuery = true)
    List<Object[]> getHeatmapPoints(
        @Param("district") String district,
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        @Param("limitCount") int limitCount
    );
}
