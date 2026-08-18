package com.works.patimati.repository;

import com.works.patimati.entity.AdMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * {@link AdMatch} varlığı için veritabanı erişim katmanı (Spring Data JPA Repository).
 */
@Repository
public interface AdMatchRepository extends JpaRepository<AdMatch, Long> {

    /**
     * Kullanıcının taraf olduğu ve eşleşen ilanların askıya alınmamış (suspended = false)
     * olduğu eşleşmeleri toplam skora göre azalan sırada getirir.
     *
     * @param userId Kullanıcı ID (User.uid)
     * @return Kullanıcının aktif eşleşmeleri
     */
    @Query("SELECT m FROM AdMatch m WHERE ((m.user IS NOT NULL AND m.user.uid = :userId) " +
           "OR (m.sourceAd.user IS NOT NULL AND m.sourceAd.user.uid = :userId) " +
           "OR (m.matchedAd.user IS NOT NULL AND m.matchedAd.user.uid = :userId)) " +
           "AND m.sourceAd.suspended = false " +
           "AND m.matchedAd.suspended = false " +
           "ORDER BY m.totalScore DESC")
    List<AdMatch> findByUserIdOrderByTotalScoreDesc(@Param("userId") Long userId);

    /**
     * Spring Data JPA varsayılan türetilmiş metodu.
     */
    List<AdMatch> findByUser_UidOrderByTotalScoreDesc(Long userId);

    /**
     * Kullanıcının taraf olduğu, yalnızca eşik değerini geçen ve askıya alınmamış eşleşmeleri getirir.
     *
     * @param userId Kullanıcı ID
     * @return Eşiği geçen aktif eşleşmeler
     */
    @Query("SELECT m FROM AdMatch m WHERE ((m.user IS NOT NULL AND m.user.uid = :userId) " +
           "OR (m.sourceAd.user IS NOT NULL AND m.sourceAd.user.uid = :userId) " +
           "OR (m.matchedAd.user IS NOT NULL AND m.matchedAd.user.uid = :userId)) " +
           "AND m.passedThreshold = true " +
           "AND m.sourceAd.suspended = false " +
           "AND m.matchedAd.suspended = false " +
           "ORDER BY m.totalScore DESC")
    List<AdMatch> findByUserInvolvedAndPassedThresholdTrue(@Param("userId") Long userId);

    /**
     * Belirtilen kullanıcı ID, kaynak ilan ID ve hedef ilan ID üçlüsüne ait benzersiz eşleşmeyi sorgular.
     *
     * @param userId Kullanıcı ID
     * @param sourceAdId Kaynak ilan ID
     * @param matchedAdId Hedef eşleşen ilan ID
     * @return Varsa AdMatch kaydı
     */
    Optional<AdMatch> findByUser_UidAndSourceAd_IdAndMatchedAd_Id(Long userId, Long sourceAdId, Long matchedAdId);

    /**
     * Belirtilen kaynak ve hedef ilan çiftine ait önceden kaydedilmiş benzersiz bir eşleşme olup olmadığını sorgular.
     *
     * @param sourceAdId Kaynak ilan ID
     * @param matchedAdId Hedef eşleşen ilan ID
     * @return Varsa AdMatch kaydı
     */
    Optional<AdMatch> findBySourceAd_IdAndMatchedAd_Id(Long sourceAdId, Long matchedAdId);

    /**
     * Kullanıcı ve eşleşen hedef ilan ID'sine göre eşleşme kaydını getirir.
     *
     * @param userId Kullanıcı ID
     * @param matchedAdId Hedef eşleşen ilan ID
     * @return Varsa AdMatch kaydı
     */
    Optional<AdMatch> findByUser_UidAndMatchedAd_Id(Long userId, Long matchedAdId);
}
