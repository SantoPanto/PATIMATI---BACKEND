package com.works.patimati.repository;

import com.works.patimati.entity.PotentialMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * {@link PotentialMatch} kimlik satırının INSERT-veya-yoksay erişimi.
 *
 * <p><b>Neden {@code ON CONFLICT DO NOTHING}, JPA {@code save()} değil:</b>
 * aynı çiftin yeniden puanlanması (model sürümü yükseltmesi, tekrar teslim
 * edilen kuyruk mesajı) güvenli bir hiçbir-şey-yapma olmalı — {@code save()}
 * önce {@code exists} kontrolü gerektirir ve bu iki adım arasında bir yarış
 * durumu (race condition) doğurur. Tek atomik ifade bunu ortadan kaldırır.
 *
 * <p>{@code ON CONFLICT} hedefi, ilgili migration'daki KISMİ benzersiz
 * indekslerle birebir aynı ifadeyi kullanmak ZORUNDADIR — aksi hâlde
 * PostgreSQL hangi indeksi kastettiğimizi anlayamaz.
 */
@Repository
public interface PotentialMatchRepository extends JpaRepository<PotentialMatch, Long> {

    @Query(value = """
            INSERT INTO potential_matches
                (candidate_kind, ad_a_id, ad_b_id, external_record_id,
                 visual_score, label_score, location_score, final_score, matching_version)
            VALUES ('AD', :adAId, :adBId, NULL, :visualScore, :labelScore, :locationScore, :finalScore, :matchingVersion)
            ON CONFLICT (LEAST(ad_a_id, ad_b_id), GREATEST(ad_a_id, ad_b_id)) WHERE candidate_kind = 'AD'
            DO NOTHING
            RETURNING id
            """, nativeQuery = true)
    Optional<Long> insertAdMatchIfAbsent(
            @Param("adAId") Long adAId,
            @Param("adBId") Long adBId,
            @Param("visualScore") float visualScore,
            @Param("labelScore") float labelScore,
            @Param("locationScore") float locationScore,
            @Param("finalScore") float finalScore,
            @Param("matchingVersion") String matchingVersion);

    @Query(value = """
            INSERT INTO potential_matches
                (candidate_kind, ad_a_id, ad_b_id, external_record_id,
                 visual_score, label_score, location_score, final_score, matching_version)
            VALUES ('EXTERNAL', :adAId, NULL, :externalRecordId, :visualScore, :labelScore, :locationScore, :finalScore, :matchingVersion)
            ON CONFLICT (ad_a_id, external_record_id) WHERE candidate_kind = 'EXTERNAL'
            DO NOTHING
            RETURNING id
            """, nativeQuery = true)
    Optional<Long> insertExternalMatchIfAbsent(
            @Param("adAId") Long adAId,
            @Param("externalRecordId") Long externalRecordId,
            @Param("visualScore") float visualScore,
            @Param("labelScore") float labelScore,
            @Param("locationScore") float locationScore,
            @Param("finalScore") float finalScore,
            @Param("matchingVersion") String matchingVersion);

    // Insert bir çakışma yüzünden hiçbir şey döndürmediğinde, alıcı satırları
    // yaratabilmek için mevcut satırı bulmak üzere kullanılır.
    Optional<PotentialMatch> findByCandidateKindAndAdAAndAdB(
            PotentialMatch.CandidateKind kind, com.works.patimati.entity.Ad adA, com.works.patimati.entity.Ad adB);

    Optional<PotentialMatch> findByCandidateKindAndAdAAndExternalRecord(
            PotentialMatch.CandidateKind kind, com.works.patimati.entity.Ad adA,
            com.works.patimati.entity.external.ExternalPetRecord externalRecord);

    // Admin paneli: bir external kaydın herhangi bir native ilanla eşleşip eşleşmediğini gösterir.
    boolean existsByCandidateKindAndExternalRecord(
            PotentialMatch.CandidateKind kind, com.works.patimati.entity.external.ExternalPetRecord externalRecord);
}
