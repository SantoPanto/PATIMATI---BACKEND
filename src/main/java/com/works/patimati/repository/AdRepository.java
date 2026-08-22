package com.works.patimati.repository;

import com.works.patimati.ai.AiCandidateRow;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.enums.AdResolutionStatus;
import com.works.patimati.entity.enums.AiStatus;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdRepository extends JpaRepository<Ad, Long> {

    /**
     * V19 backfill'i: il'i hiç yazılmamış ama koordinatı olan ilanlar.
     * İl dolu olanlara (beyan ya da önceki backfill) dokunulmaz.
     */
    List<Ad> findByCityIsNullAndLocationIsNotNull();

    // Yalnızca aktif bir ilanı getirir.
    Optional<Ad> findByIdAndActiveTrue(Long adId);

    // Yalnızca halka açık (aktif ve askıda olmayan) bir ilanı getirir.
    Optional<Ad> findByIdAndActiveTrueAndSuspendedFalse(Long adId);

    // İlanın hem aktif olduğunu hem de belirtilen kullanıcıya ait olduğunu kontrol eder.
    Optional<Ad> findByIdAndUser_UidAndActiveTrue(Long adId, Long userId);

    // Sahiplik kontrolü YAPAR ama aktiflik ŞARTI ARAMAZ.
    // Yalnızca "yeniden yayınla" akışı için var: yayından kaldırılmış (active=false)
    // bir ilan, tanımı gereği yukarıdaki sorguyla BULUNAMAZ — o yüzden onunla
    // pasif ilana dokunmak imkânsızdı. Başka yerlerde bunu kullanmayın; aktif
    // ilan bekleyen akışlar aktifliği sorgunun kendisinde şart koşmalı.
    Optional<Ad> findByIdAndUser_Uid(Long adId, Long userId);

    /**
     * Belirli AI durumundaki aktif ilanlar — toplu yeniden analiz bunun
     * üzerinden FAILED kalanları toplar. Pasif ilan bilerek dışarıda: sahibi
     * yayından kaldırdığı ilanın analizini de istemiyordur.
     */
    List<Ad> findAllByAiStatusAndActiveTrue(AiStatus aiStatus);

    // İlanları aktiflik durumuna göre sayfalı biçimde listeler.
    Page<Ad> findAllByActive(
            boolean active,
            Pageable pageable
    );

    // Halka açık aktif ve askıda olmayan ilanları listeler.
    Page<Ad> findAllByActiveTrueAndSuspendedFalse(Pageable pageable);

    long countByActiveTrueAndSuspendedFalse();

    // Bu kullanıcının HALKA AÇIK bir ilanı var mı?
    // Sohbet odası açma yetkisi buna bakıyor (bkz. MessageService.createOrGetRoom):
    // halka açık ilanı olan kullanıcının adı zaten AdResponse.ownerDisplayName ile
    // herkese görünüyor, dolayısıyla onunla oda açmak yeni bir bilgi sızdırmaz.
    // Aynı gerekçeyle filtre "aktif ve askıda değil" — askıya alınmış ya da
    // kapatılmış ilan halka görünmediği için sahibinin adı da görünmüyor.
    boolean existsByUser_UidAndActiveTrueAndSuspendedFalse(Long userId);

    long countByResolutionStatusIn(Collection<AdResolutionStatus> resolutionStatuses);

    // İlanları türüne ve aktiflik durumuna göre filtreler.
    Page<Ad> findAllByAdTypeAndActive(
            Ad.AdType adType,
            boolean active,
            Pageable pageable
    );

    // Halka açık aktif ve askıda olmayan ilanları türüne göre listeler.
    Page<Ad> findAllByAdTypeAndActiveTrueAndSuspendedFalse(
            Ad.AdType adType,
            Pageable pageable
    );

    // Halka açık listede metin araması: başlık + ırk + açıklama.
    // Açıklama bilerek dahil — şehir/semt bilgisi ayrı bir sütunda YOK;
    // bulundu ve sahiplendirme formları Nominatim adresini açıklamaya kattığı
    // için "Bursa" gibi bir yer araması ancak açıklama üzerinden tutabiliyor.
    // LOWER iki tarafta da veritabanının kendi katlamasıyla çalışır: ASCII
    // güvenli, Türkçe İ/ı kenarında (ör. "izmir" ↔ "İzmir") kaçırma olabilir.
    @Query("""
            SELECT a FROM Ad a
            WHERE a.active = TRUE AND a.suspended = FALSE
              AND (LOWER(a.title) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(a.breed) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(a.description) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Ad> searchPublicActiveAds(
            @Param("search") String search,
            Pageable pageable
    );

    // Aynı arama, ilan türü süzgeciyle. Tür null olamaz; null tür için üstteki
    // kullanılır (null parametreli tek sorgu, enum bağlamada tip belirsizliğine
    // düşebildiği için bilerek iki ayrı metot — mevcut listeleme çifti gibi).
    @Query("""
            SELECT a FROM Ad a
            WHERE a.active = TRUE AND a.suspended = FALSE
              AND a.adType = :adType
              AND (LOWER(a.title) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(a.breed) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(a.description) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Ad> searchPublicActiveAdsByAdType(
            @Param("adType") Ad.AdType adType,
            @Param("search") String search,
            Pageable pageable
    );

    // Bir kullanıcıya ait ilanları aktiflik durumuna göre listeler.
    Page<Ad> findAllByUser_UidAndActive(
            Long userId,
            boolean active,
            Pageable pageable
    );

    // Bir kullanıcının TÜM ilanları — aktiflik süzgeci olmadan.
    // "İlanlarım > Tümü" sekmesinin karşılığı: kapanmış ilan da görünmeli.
    Page<Ad> findAllByUser_Uid(
            Long userId,
            Pageable pageable
    );

    // Standart kullanıcının görebileceği aktif ilanlar (askıda olmayanlar VEYA kullanıcının kendi askıdaki ilanları)
    @Query("SELECT a FROM Ad a WHERE a.active = true AND (a.suspended = false OR (a.user IS NOT NULL AND a.user.uid = :userId))")
    Page<Ad> findAllActiveForUser(@Param("userId") Long userId, Pageable pageable);

    // Türüne göre standart kullanıcının görebileceği aktif ilanlar (askıda olmayanlar VEYA kullanıcının kendi askıdaki ilanları)
    @Query("SELECT a FROM Ad a WHERE a.active = true AND a.adType = :adType AND (a.suspended = false OR (a.user IS NOT NULL AND a.user.uid = :userId))")
    Page<Ad> findAllByAdTypeActiveForUser(
            @Param("adType") Ad.AdType adType,
            @Param("userId") Long userId,
            Pageable pageable
    );


    // KISIM 3
    // PARAMETREDE ::geography YAZILAMAZ — Hibernate parametre adını
    // "userPoint::geography" diye okur ve sorgu çalışma anında patlar.
    // Sütunda (a.location::geography) sorun yok.
    @Query(
            value = "SELECT * FROM ads a WHERE a.active = true AND ST_DWithin(a.location::geography, CAST(:userPoint AS geography), :distanceInMeters) = true", nativeQuery = true)
    List<Ad> findNearbyAds(@Param("userPoint") Point userPoint, @Param("distanceInMeters") double distanceInMeters);

    /**
     * Herkese açık haritada gösterilebilecek yakın ilanları getirir.
     *
     * <p>Public uç nokta kimlik bilgisi taşımadığı için sahip ve yönetici
     * istisnası uygulanmaz; yalnızca aktif ve askıda olmayan ilanlar döner.</p>
     */
    @Query(value = """
        SELECT *
          FROM ads a
         WHERE a.active = TRUE
           AND a.suspended = FALSE
           AND a.location IS NOT NULL
           AND ST_DWithin(
                   a.location::geography,
                   CAST(:userPoint AS geography),
                   :distanceInMeters
               ) = TRUE
        """, nativeQuery = true)
        List<Ad> findPublicNearbyAds(
                @Param("userPoint") Point userPoint,
                @Param("distanceInMeters") double distanceInMeters
    );

    /**
     * AI eşleştirmesine girecek adayları süzer (entegrasyon sözleşmesi §5).
     *
     * <p>Süzmeyi <b>Java yapar</b>, AI değil: PostGIS, ilan tipi ve tarih
     * bilgisi bizde; AI'ın veritabanına erişimi yok.
     *
     * <p>Uygulanan kurallar:
     * <ul>
     *   <li><b>Karşıt ilan tipi</b> — kayıp ilanını başka bir kayıp ilanıyla
     *       eşleştirmek anlamsız. ADOPTION hiç aday olmaz.</li>
     *   <li><b>ai_status = DONE ve vektör dolu</b> — vektörü olmayan aday
     *       kıyaslanamaz.</li>
     *   <li><b>Askıya alınmamış</b> — askıdaki ilanı yalnızca sahibi ve
     *       yöneticiler görebilir. Aday havuzuna girerse eşleşme üzerinden
     *       başlığı, açıklaması ve sahibinin adı üçüncü bir kişiye açılır.</li>
     *   <li><b>Yarıçap</b> — bildirim yarıçapından (5 km) farklıdır ve olmalıdır:
     *       kaybolan hayvan yürür, günlerce uzaklaşabilir.</li>
     *   <li><b>Zaman penceresi</b> — eski ilanlar gürültü yaratır.</li>
     *   <li><b>Kendisi hariç</b> — ilan kendisiyle %100 eşleşirdi.</li>
     * </ul>
     *
     * <p>Sıralama mesafeye göre yakından uzağa; üst sınır çağıran tarafta
     * uygulanır (sözleşmede 100).
     *
     * <p><b>Neden {@code ::geography}:</b> {@code geometry(Point,4326)} üzerinde
     * ST_DWithin mesafeyi <b>derece</b> cinsinden ölçer — 25000 yazarsanız tüm
     * dünyayı kapsar. {@code geography}'e çevirince metre olur. Bu, projede
     * gerçekten yaşanmış bir hatadır.
     */
    @Query(value = """
            SELECT a.id AS adId,
                   ST_Distance(a.location::geography, CAST(:origin AS geography)) / 1000.0 AS distanceKm
              FROM ads a
             WHERE a.active = TRUE
               AND a.id <> :selfAdId
               AND a.ad_type = :oppositeAdType
               AND a.ai_status = 'DONE'
               AND a.ai_embeddings IS NOT NULL
               AND a.suspended = FALSE
               AND a.created_at >= :since
               AND a.location IS NOT NULL
               AND ST_DWithin(a.location::geography, CAST(:origin AS geography), :radiusMeters)
             ORDER BY a.location <-> :origin
            """, nativeQuery = true)
    List<AiCandidateRow> findAiCandidates(
            @Param("selfAdId") Long selfAdId,
            @Param("oppositeAdType") String oppositeAdType,
            @Param("origin") Point origin,
            @Param("radiusMeters") double radiusMeters,
            @Param("since") Instant since);

    @Query(value = """
             SELECT a.id AS adId, 0.0 AS distanceKm
               FROM ads a
              WHERE a.ad_type = :oppositeAdType
                AND a.active = true
                AND a.ai_status = 'DONE'
                AND a.ai_embeddings IS NOT NULL
                AND a.suspended = FALSE
                AND a.created_at >= :since
              ORDER BY a.created_at DESC
            """, nativeQuery = true)
    List<AiCandidateRow> findAiCandidatesWithoutLocation(
            @Param("oppositeAdType") String oppositeAdType,
            @Param("since") Instant since);
}
