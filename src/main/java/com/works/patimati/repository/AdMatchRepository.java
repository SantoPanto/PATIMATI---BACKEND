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
     * Kullanıcının hem sourceAd (kaynak ilan) hem de matchedAd (hedef ilan) tarafında olabileceğini
     * kontrol eden gelişmiş JPQL sorgusu.
     *
     * <p>Eşleşmeleri toplam skora göre azalan sırada getirir.
     *
     * @param userId Kullanıcı ID (User.uid)
     * @return Kullanıcının taraf olduğu tüm eşleşmeler
     */
    @Query("SELECT m FROM AdMatch m WHERE (m.sourceAd.user IS NOT NULL AND m.sourceAd.user.uid = :userId) " +
           "OR (m.matchedAd.user IS NOT NULL AND m.matchedAd.user.uid = :userId) " +
           "OR (m.user IS NOT NULL AND m.user.uid = :userId) " +
           "ORDER BY m.totalScore DESC")
    List<AdMatch> findByUserIdOrderByTotalScoreDesc(@Param("userId") Long userId);

    /**
     * Spring Data JPA varsayılan türetilmiş metodu.
     */
    List<AdMatch> findByUser_UidOrderByTotalScoreDesc(Long userId);

    /**
     * Kullanıcının taraf olduğu ve yalnızca eşik değerini geçen eşleşmeleri getirir.
     *
     * @param userId Kullanıcı ID
     * @return Eşiği geçen eşleşmeler
     */
    @Query("SELECT m FROM AdMatch m WHERE ((m.sourceAd.user IS NOT NULL AND m.sourceAd.user.uid = :userId) " +
           "OR (m.matchedAd.user IS NOT NULL AND m.matchedAd.user.uid = :userId) " +
           "OR (m.user IS NOT NULL AND m.user.uid = :userId)) " +
           "AND m.passedThreshold = true " +
           "ORDER BY m.totalScore DESC")
    List<AdMatch> findByUserInvolvedAndPassedThresholdTrue(@Param("userId") Long userId);

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
