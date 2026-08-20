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
 *
 * <p><b>Listeleme sorguları neden yalnız {@code m.user}'a bakar:</b> "Çözüm B"
 * mimarisinde ({@code b49751f}) bir eşleşme çifti için <b>alıcı başına ayrı satır</b>
 * yazılır — {@code AdMatchService.saveOrUpdateMatch} javadoc'u ve
 * {@code AiMatchNotifier.kaydet} bunu açıkça söyler, çünkü her alıcının kendi
 * {@code notification_sent_at} damgası olmalıdır. İki satırın {@code source_ad_id}
 * ve {@code matched_ad_id} değerleri <b>aynıdır</b>, yalnız {@code user_id} farklıdır.
 *
 * <p>Bu yüzden listelemeye {@code sourceAd.user} / {@code matchedAd.user} koşulları
 * <b>eklenemez</b>: eklenirse kullanıcı hem kendi satırını hem de karşı tarafın
 * satırını görür ve <b>aynı çift listede iki kez çıkar</b>. Bu koşullar
 * {@code user_id} sütunu var olmadan önceki mimariden kalmıştı ve kusur buydu.
 */
@Repository
public interface AdMatchRepository extends JpaRepository<AdMatch, Long> {

    /**
     * Kullanıcının <b>kendi adına yazılmış</b>, eşleşen ilanların askıya alınmamış
     * (suspended = false) olduğu eşleşmeleri toplam skora göre azalan sırada getirir.
     *
     * <p>Ölçüt satır sahipliğidir; gerekçesi sınıf javadoc'unda. Kullanıcının taraf
     * olduğu her çift için kendi adına <b>zaten</b> bir satır vardır, dolayısıyla
     * ilan sahipliğine bakmamak hiçbir eşleşmeyi gizlemez.
     *
     * @param userId Kullanıcı ID (User.uid)
     * @return Kullanıcının aktif eşleşmeleri
     */
    @Query("SELECT m FROM AdMatch m WHERE m.user IS NOT NULL AND m.user.uid = :userId " +
           "AND m.sourceAd.suspended = false " +
           "AND m.matchedAd.suspended = false " +
           "ORDER BY m.totalScore DESC")
    List<AdMatch> findByUserIdOrderByTotalScoreDesc(@Param("userId") Long userId);

    /**
     * Spring Data JPA varsayılan türetilmiş metodu.
     */
    List<AdMatch> findByUser_UidOrderByTotalScoreDesc(Long userId);

    /**
     * Kullanıcının kendi adına yazılmış, yalnızca eşik değerini geçen ve askıya
     * alınmamış eşleşmelerini getirir.
     *
     * <p>Ölçüt burada da satır sahipliğidir — sebebi sınıf javadoc'unda.
     * <b>Şu an hiçbir yerden çağrılmıyor</b>; yukarıdaki sorguyla aynı biçimde
     * tutuluyor ki ileride bağlandığında mükerrer listeleme kusuru geri gelmesin.
     *
     * @param userId Kullanıcı ID
     * @return Eşiği geçen aktif eşleşmeler
     */
    @Query("SELECT m FROM AdMatch m WHERE m.user IS NOT NULL AND m.user.uid = :userId " +
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
