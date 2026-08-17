package com.works.patimati.repository.external;

import com.works.patimati.ai.ExternalCandidateRow;
import com.works.patimati.entity.external.ExternalPetRecord;
import com.works.patimati.entity.external.ExternalSourcePost;
import org.locationtech.jts.geom.Point;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ExternalPetRecordRepository extends JpaRepository<ExternalPetRecord, Long> {

    /**
     * Konumu bilinen, kategori-uyumlu, analizi bitmiş external kayıtları
     * PostGIS ile süzer. {@code AdRepository.findAiCandidates} ile aynı
     * {@code ::geography} deseni — sebebi de aynı: geometry üzerinde
     * ST_DWithin mesafeyi derece cinsinden ölçer, geography'e çevirmeden
     * 25000 yazmak tüm dünyayı kapsardı.
     *
     * <p>needs_review=TRUE olan kayıtlar hiçbir zaman aday olmaz (Faz 2
     * revize blueprint §0/§23) — çoklu hayvan belirsizliği ya da düşük
     * güven, otomatik eşleştirmeye girmemeli.
     */
    @Query(value = """
            SELECT r.id AS id,
                   ST_Distance(r.location::geography, CAST(:origin AS geography)) / 1000.0 AS distanceKm
              FROM external_pet_records r
              JOIN external_source_posts p ON p.id = r.external_source_post_id
             WHERE r.category = :compatibleCategory
               AND r.needs_review = FALSE
               AND r.ai_status = 'DONE'
               AND r.ai_embeddings IS NOT NULL
               AND r.location IS NOT NULL
               AND p.detected_at >= :since
               AND ST_DWithin(r.location::geography, CAST(:origin AS geography), :radiusMeters)
             ORDER BY r.location <-> :origin
            """, nativeQuery = true)
    List<ExternalCandidateRow> findSpatialCandidates(
            @Param("compatibleCategory") String compatibleCategory,
            @Param("origin") Point origin,
            @Param("radiusMeters") double radiusMeters,
            @Param("since") Instant since);

    /**
     * Konumu OLMAYAN aday havuzu için sınırlı, mesafesiz fallback (blueprint
     * §21/§42). Eksik konum gerçek adayı elemez — bunun yerine kategori +
     * (bilinen ise) tür + zaman penceresiyle sınırlı, en yeniden eskiye
     * sıralı bir liste döner. Species parametresi null ise tür hiç
     * filtrelenmez — bilinmeyen tür yanlış elemeye yol açmasın diye.
     */
    @Query(value = """
            SELECT r.id
              FROM external_pet_records r
              JOIN external_source_posts p ON p.id = r.external_source_post_id
             WHERE r.category = :compatibleCategory
               AND r.needs_review = FALSE
               AND r.ai_status = 'DONE'
               AND r.ai_embeddings IS NOT NULL
               AND p.detected_at >= :since
               AND (:species IS NULL OR LOWER(r.species) = LOWER(:species))
             ORDER BY p.detected_at DESC
             LIMIT :maxResults
            """, nativeQuery = true)
    List<Long> findFallbackCandidateIds(
            @Param("compatibleCategory") String compatibleCategory,
            @Param("since") Instant since,
            @Param("species") String species,
            @Param("maxResults") int maxResults);

    Optional<ExternalPetRecord> findByPostAndPetIndex(ExternalSourcePost post, short petIndex);
}
