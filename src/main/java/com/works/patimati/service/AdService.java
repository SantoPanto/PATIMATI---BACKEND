package com.works.patimati.service;

import com.works.patimati.ai.AiAnalysisPublisher;
import com.works.patimati.dto.ad.AdCreateRequest;
import com.works.patimati.dto.ad.AdCountersResponse;
import com.works.patimati.dto.ad.AdResponse;
import com.works.patimati.dto.ad.AdUpdateRequest;
import com.works.patimati.dto.ad.PosterSettingsUpdateRequest;
import com.works.patimati.dto.ad.ResolveLostAdRequest;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.AiStatus;
import com.works.patimati.entity.enums.AdResolutionStatus;
import com.works.patimati.exception.BusinessException;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.mapper.AdMapper;
import com.works.patimati.notification.NearbyAlertNotifier;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.storage.ImageStorageService;
import com.works.patimati.storage.InvalidImageException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class AdService {

    private static final Logger log = LoggerFactory.getLogger(AdService.class);
    private static final List<AdResolutionStatus> HAPPY_ENDING_STATUSES = List.of(
            AdResolutionStatus.FOUND,
            AdResolutionStatus.ADOPTED
    );

    private final AdRepository adRepository;
    private final UserRepository userRepository;
    private final AdMapper adMapper;
    private final ImageStorageService imageStorageService;
    private final AiAnalysisPublisher aiAnalysisPublisher;
    private final RewardService rewardService;
    private final NotificationService notificationService;
    private final ReverseGeocodingService reverseGeocodingService;
    private final NearbyAlertNotifier nearbyAlertNotifier;
    private final InstagramPublishService instagramPublishService;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    /**
     * İlanı, yüklenen fotoğraflarıyla birlikte oluşturur.
     *
     * <p>Fotoğraflar önce depoya yüklenir, dönen kalıcı referanslar ilana
     * yazılır. Veritabanına kayıt başarısız olursa yüklenen dosyalar geri
     * silinir — aksi hâlde depoda sahipsiz nesneler birikir.
     */
    @Transactional
    public AdResponse createAd(
            String ownerEmail,
            AdCreateRequest request,
            List<MultipartFile> images
    ) {
        if (request != null && request.adType() == Ad.AdType.ADOPTION) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sahiplendirme ilanları bu adresten oluşturulamaz. Lütfen '/api/adoptions' adresini kullanın."
            );
        }

        if (images == null || images.isEmpty()) {
            throw new InvalidImageException(
                    "İlan oluşturmak için en az bir fotoğraf yüklenmelidir"
            );
        }

        User owner = findUserByEmail(ownerEmail);

        List<String> photoReferences = imageStorageService.uploadImages(images);

        Ad savedAd;
        try {
            Ad ad = adMapper.toEntity(request);
            konumBilgisiniDoldur(ad, request.city(), request.district());
            ad.setUser(owner);
            ad.setActive(true);

            ad.setAiStatus(AiStatus.PENDING);

            ad.setPhotoUrls(new ArrayList<>(photoReferences));

            savedAd = adRepository.saveAndFlush(ad);
        } catch (RuntimeException exception) {
            deleteImagesSafely(photoReferences);
            throw exception;
        }

        AdResponse response = toResponseWithTemporaryPhotoUrls(savedAd);

        // Uyarı abonelerine bildirim (yalnız KAYIP ilanlar; asla fırlatmaz).
        // Eski aboneliksiz notifyNearbyUsersSafely'nin yerine geçti —
        // gerekçe NearbyAlertNotifier sınıf yorumunda.
        nearbyAlertNotifier.yeniIlaniBildir(savedAd);

        // AI analizini KUYRUĞA bırakır ve beklemez (entegrasyon sözleşmesi §1).
        // Fotoğraf analizi 1-3 saniye sürüyor; senkron çağrı kullanıcıyı
        // bekletirdi. Yayınlama hata verse bile ilan kaydedilmiş kalır:
        // ai_status PENDING'de durur ve sonradan yeniden denenebilir.
        aiAnalysisPublisher.publish(savedAd);

        // Instagram kuyruğuna ekleme de AYNI ilkeyle beklenmez/asla fırlatmaz
        // (bkz. InstagramPublishService.queueForReview javadoc'u).
        instagramPublishService.queueForReview(savedAd);

        return response;
    }

    /**
     * İl/ilçeyi doldurur (V19): form beyanı öncelikli; beyan yoksa
     * koordinattan ters geokodlama denenir. Geokodlama servisi istisna
     * fırlatmaz — başarısızlıkta alanlar boş kalır, ilan kaydı hiçbir
     * durumda engellenmez.
     *
     * <p>Kayıp/bulundu akışı {@code createAd} içinden, sahiplendirme akışı
     * {@code AdoptionServiceImpl.createAdoptionAd} içinden çağırır.
     */
    public void konumBilgisiniDoldur(Ad ad, String beyanIl, String beyanIlce) {
        String il = normalizeBlank(beyanIl);
        String ilce = normalizeBlank(beyanIlce);

        if (il == null && ad.getLocation() != null) {
            var cozum = reverseGeocodingService.cozumle(
                    ad.getLocation().getY(),
                    ad.getLocation().getX()
            );
            if (cozum.isPresent()) {
                il = cozum.get().il();
                if (ilce == null) {
                    ilce = cozum.get().ilce();
                }
            }
        }

        ad.setCity(il);
        ad.setDistrict(ilce);
    }

    private static String normalizeBlank(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private void deleteImagesSafely(List<String> photoReferences) {
        try {
            imageStorageService.deleteImages(photoReferences);
        } catch (RuntimeException exception) {
            log.warn("Yüklenen fotoğraflar silinemedi: {}", photoReferences, exception);
        }
    }

    @Transactional(readOnly = true)
    public AdResponse getPublicActiveAd(Long adId) {
        Ad ad = adRepository.findByIdAndActiveTrueAndSuspendedFalse(adId)
                .orElseThrow(() -> adNotFound(adId));

        return toResponseWithTemporaryPhotoUrls(ad);
    }

    @Transactional(readOnly = true)
    public Page<AdResponse> getPublicActiveAds(
            Ad.AdType adType,
            String search,
            Pageable pageable
    ) {
        String aramaMetni = temizleAramaMetni(search);

        Page<Ad> ads;
        if (aramaMetni == null) {
            ads = adType == null
                    ? adRepository.findAllByActiveTrueAndSuspendedFalse(pageable)
                    : adRepository.findAllByAdTypeAndActiveTrueAndSuspendedFalse(
                    adType,
                    pageable
            );
        } else {
            ads = adType == null
                    ? adRepository.searchPublicActiveAds(aramaMetni, pageable)
                    : adRepository.searchPublicActiveAdsByAdType(
                    adType,
                    aramaMetni,
                    pageable
            );
        }

        return ads.map(this::toResponseWithTemporaryPhotoUrls);
    }

    /**
     * LIKE joker karakterleri (%, _, \) kullanıcı girdisinden atılır — desen
     * olarak değil düz metin olarak aransınlar diye; kaçış zinciri (ESCAPE)
     * kurmaktan bilerek kaçınıldı. Temizlik sonrası boş kalan arama, hiç arama
     * yokmuş gibi davranır ki liste boş desenle daralmasın.
     */
    private static String temizleAramaMetni(String search) {
        if (search == null) {
            return null;
        }
        String temiz = search.replaceAll("[%_\\\\]", "").trim();
        return temiz.isEmpty() ? null : temiz;
    }

    @Transactional(readOnly = true)
    public AdResponse getActiveAd(Long adId) {
        Ad ad = adRepository.findByIdAndActiveTrue(adId)
                .orElseThrow(() -> adNotFound(adId));

        if (ad.isSuspended() && !isOwnerOrAdmin(ad)) {
            throw new AccessDeniedException("Askıya alınmış ilanı görüntüleme yetkiniz yoktur.");
        }

        return toResponseWithTemporaryPhotoUrls(ad);
    }

    @Transactional(readOnly = true)
    public Page<AdResponse> getActiveAds(
            Ad.AdType adType,
            Pageable pageable
    ) {
        User currentUser = getCurrentUser();
        boolean isAdmin = isCurrentUserAdmin(currentUser);

        Page<Ad> ads;
        if (isAdmin) {
            ads = adType == null
                    ? adRepository.findAllByActive(true, pageable)
                    : adRepository.findAllByAdTypeAndActive(
                    adType,
                    true,
                    pageable
            );
        } else if (currentUser != null) {
            ads = adType == null
                    ? adRepository.findAllActiveForUser(currentUser.getUid(), pageable)
                    : adRepository.findAllByAdTypeActiveForUser(
                    adType,
                    currentUser.getUid(),
                    pageable
            );
        } else {
            ads = adType == null
                    ? adRepository.findAllByActiveTrueAndSuspendedFalse(pageable)
                    : adRepository.findAllByAdTypeAndActiveTrueAndSuspendedFalse(
                    adType,
                    pageable
            );
        }

        return ads.map(this::toResponseWithTemporaryPhotoUrls);
    }

    private Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private User getCurrentUser() {
        Authentication auth = getCurrentAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        String email = auth.getName();
        if (email == null || email.isBlank()) {
            return null;
        }
        return userRepository.findByEmail(email).orElse(null);
    }

    private boolean isCurrentUserAdmin(User currentUser) {
        Authentication auth = getCurrentAuthentication();
        if (auth != null && auth.getAuthorities() != null) {
            boolean hasAdminRole = auth.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ADMIN".equals(a.getAuthority()));
            if (hasAdminRole) {
                return true;
            }
        }
        return currentUser != null && currentUser.getRole() == User.Role.ADMIN;
    }

    private boolean isOwnerOrAdmin(Ad ad) {
        User currentUser = getCurrentUser();
        if (isCurrentUserAdmin(currentUser)) {
            return true;
        }
        if (currentUser == null) {
            return false;
        }
        return ad.getUser() != null && currentUser.getUid() != null && currentUser.getUid().equals(ad.getUser().getUid());
    }

    @Transactional(readOnly = true)
    /**
     * Kullanıcının kendi ilanları.
     *
     * <p>{@code active} <b>null olabilir</b> ve null "hepsi" demektir. Eskiden
     * parametre {@code boolean}du ve ucun varsayılanı {@code true}ydu; ön yüz
     * "Tümü" sekmesinde alanı hiç göndermediği için sunucu sessizce
     * <i>yalnız yayındakileri</i> döndürüyordu. Sonuç: bulundu olarak kapanan
     * ilan "Tümü"de kayboluyordu (ölçüldü, 21.08 canlı).
     */
    public Page<AdResponse> getUserAds(
            String ownerEmail,
            Boolean active,
            Pageable pageable
    ) {
        User owner = findUserByEmail(ownerEmail);

        Page<Ad> sayfa = (active == null)
                ? adRepository.findAllByUser_Uid(owner.getUid(), pageable)
                : adRepository.findAllByUser_UidAndActive(
                        owner.getUid(), active, pageable);

        return sayfa.map(this::toResponseWithTemporaryPhotoUrls);
    }

    @Transactional
    public AdResponse updateAd(
            String ownerEmail,
            Long adId,
            AdUpdateRequest request
    ) {
        User owner = findUserByEmail(ownerEmail);
        Ad ad = findActiveOwnedAd(adId, owner.getUid());

        if (ad.getAdType() == Ad.AdType.LOST || ad.getAdType() == Ad.AdType.FOUND) {
            throw new BusinessException(
                    "Kayıp ve Bulundu ilanlarında bilgi bütünlüğünü korumak amacıyla temel bilgilerin güncellenmesine izin verilmemektedir."
            );
        }

        adMapper.updateEntity(ad, request);
        Ad updatedAd = adRepository.saveAndFlush(ad);

        return toResponseWithTemporaryPhotoUrls(updatedAd);
    }

    @Transactional
    public AdResponse updatePosterSettings(
            Long adId,
            PosterSettingsUpdateRequest request,
            String currentUserEmail
    ) {
        User owner = findUserByEmail(currentUserEmail);
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> adNotFound(adId));

        if (ad.getUser() == null || !ad.getUser().getUid().equals(owner.getUid())) {
            throw new AccessDeniedException("Bu ilanın afiş ayarlarını yalnızca ilan sahibi güncelleyebilir.");
        }

        if (request != null) {
            if (request.getIsPosterAllowed() != null) {
                ad.setIsPosterAllowed(request.getIsPosterAllowed());
            }
            if (request.getShowEmailOnPoster() != null) {
                ad.setShowEmailOnPoster(request.getShowEmailOnPoster());
            }
            if (request.getShowPhoneOnPoster() != null) {
                ad.setShowPhoneOnPoster(request.getShowPhoneOnPoster());
            }
        }

        Ad updatedAd = adRepository.saveAndFlush(ad);
        return toResponseWithTemporaryPhotoUrls(updatedAd);
    }

    /**
     * Yayından kaldırılmış bir ilanı sahibi yeniden yayına alır.
     * <p>
     * <b>NEDEN AYRI BİR İŞLEM:</b> {@link #updateAd} bunu yapamaz, iki sebepten —
     * {@code AdUpdateRequest} içinde {@code active} alanı YOK, ve zaten
     * {@code findActiveOwnedAd} yalnız AKTİF ilanı buluyor, yani pasif ilana
     * hiçbir şekilde dokunulamıyordu. İlan yaşam döngüsü tek yönlüydü:
     * yayından kaldırılan ilan kalıcı olarak öyle kalıyordu.
     * <p>
     * <b>ASKIYA ALINMIŞ İLAN YENİDEN YAYINLANAMAZ.</b> {@code suspended} alanını
     * yalnız yönetici değiştiriyor ({@code AdminServiceImpl}); sahibin bu işlemle
     * yönetici kararını geçersiz kılabilmesi yetki aşımı olurdu.
     * <p>
     * İşlem <b>etkisiz-tekrarlanabilir</b> (idempotent): zaten yayında olan ilan
     * için hata değil, aynı sonuç döner. Çift tıklama ya da tekrar gönderilen
     * istek kullanıcıya hata göstermemeli.
     */
    @Transactional
    public AdResponse republishAd(String ownerEmail, Long adId) {
        User owner = findUserByEmail(ownerEmail);
        // adNotFound() bilerek kullanılmadı: onun metni "Aktif ilan bulunamadı"
        // diyor ve bu akışta aktiflik zaten aranmıyor — yanıltıcı olurdu.
        Ad ad = adRepository.findByIdAndUser_Uid(adId, owner.getUid())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "İlan bulunamadı: " + adId
                ));

        if (ad.isSuspended()) {
            throw new AccessDeniedException(
                    "Askıya alınmış ilan yeniden yayınlanamaz."
            );
        }

        if (!ad.isActive()) {
            ad.setActive(true);
            ad = adRepository.saveAndFlush(ad);
        }

        return toResponseWithTemporaryPhotoUrls(ad);
    }

    @Transactional
    public void deactivateAd(String ownerEmail, Long adId) {
        User owner = findUserByEmail(ownerEmail);
        Ad ad = findActiveOwnedAd(adId, owner.getUid());

        ad.setActive(false);
        adRepository.saveAndFlush(ad);
    }

    /**
     * FAILED (ya da takılı PENDING) kalmış bir ilanı sahibi yeniden analize gönderir.
     * <p>
     * <b>NEDEN VAR:</b> {@link AiAnalysisPublisher#publish} yalnız ilan
     * <b>oluşturulurken</b> çağrılıyor ve LOST/FOUND ilanlarında düzenleme
     * bilerek kapalı ({@link #updateAd}). AI servisi arızalıyken açılan bir
     * ilan bu yüzden <b>kalıcı olarak</b> FAILED kalıyordu: aday sorgusu
     * {@code ai_status='DONE'} istediği için ilan hiçbir eşleştirmeye
     * giremiyor ve sahibi bunu düzeltemiyordu (21.08'de canlıda 7 ilan bu
     * durumdaydı).
     * <p>
     * <b>DONE ilan yeniden analize sokulmaz.</b> Sebep yük değil, bildirim:
     * yeniden analiz aynı çift için ters yönde ikinci bir eşleşme satırı ve
     * yeni bildirim üretebilir (bkz. {@code AdMatchRepository} sınıf notu).
     * FAILED/PENDING ilanın eşleşme satırı zaten yoktur, risk de yoktur.
     * PENDING'in kabul sebebi: kuyruk mesajı kaybolursa (broker kesintisi,
     * 15 dakikalık fotoğraf adresinin ölmesi) ilan PENDING'te takılı kalıyor —
     * bu işlem o durumu da kurtarır ve tekrarlanabilir (idempotent-benzeri):
     * başarısız olursa ilan yine PENDING/FAILED kalır, tekrar denenebilir.
     */
    @Transactional
    public AdResponse reanalyzeAd(String ownerEmail, Long adId) {
        User owner = findUserByEmail(ownerEmail);
        Ad ad = findActiveOwnedAd(adId, owner.getUid());

        yenidenAnalizeGonder(ad);

        return toResponseWithTemporaryPhotoUrls(ad);
    }

    /**
     * Analize uygunluğu denetler, durumu PENDING'e çeker ve kuyruğa yazar.
     * Tekil ({@link #reanalyzeAd}) ve toplu ({@link #reanalyzeAllFailed})
     * yolun ikisi de buradan geçer ki kurallar tek yerde yaşasın.
     */
    private void yenidenAnalizeGonder(Ad ad) {
        if (ad.getAdType() == Ad.AdType.ADOPTION) {
            throw new BusinessException(
                    "Sahiplendirme ilanları AI eşleştirmesine girmez.");
        }
        if (ad.getAiStatus() == AiStatus.DONE) {
            throw new BusinessException(
                    "İlan zaten analiz edilmiş; yeniden analiz yalnız "
                            + "başarısız ya da takılı kalmış ilanlar içindir.");
        }
        if (ad.getPhotoUrls() == null || ad.getPhotoUrls().isEmpty()) {
            // Publisher fotoğrafsız ilanı sessizce atlar; burada sessiz
            // kalınsaydı ilan PENDING'e çekilip kuyruğa hiç yazılmaz ve
            // "takılı PENDING" elle üretilmiş olurdu.
            throw new BusinessException(
                    "Fotoğrafsız ilan analiz edilemez.");
        }

        ad.setAiStatus(AiStatus.PENDING);
        adRepository.saveAndFlush(ad);
        aiAnalysisPublisher.publish(ad);
    }

    /**
     * FAILED durumundaki tüm aktif ilanları yeniden analize gönderir (yönetici).
     * <p>
     * Toplu yol tek tek {@link #yenidenAnalizeGonder} çağırmaz çünkü oradaki
     * kural ihlalleri burada hata değil <b>atlama</b> sebebidir: arızalı tek
     * bir kayıt (örn. fotoğrafsız FAILED ilan) tüm kurtarmayı durdurmamalı.
     *
     * @return kuyruğa yazılan ilan sayısı
     */
    @Transactional
    public int reanalyzeAllFailed() {
        List<Ad> basarisizlar = adRepository.findAllByAiStatusAndActiveTrue(AiStatus.FAILED);

        int kuyruklanan = 0;
        for (Ad ad : basarisizlar) {
            if (ad.getAdType() == Ad.AdType.ADOPTION
                    || ad.getPhotoUrls() == null || ad.getPhotoUrls().isEmpty()) {
                log.warn("Yeniden analiz atlandı (uygun değil): adId={} tip={} fotoğraf={}",
                        ad.getId(), ad.getAdType(),
                        ad.getPhotoUrls() == null ? 0 : ad.getPhotoUrls().size());
                continue;
            }
            ad.setAiStatus(AiStatus.PENDING);
            adRepository.saveAndFlush(ad);
            aiAnalysisPublisher.publish(ad);
            kuyruklanan++;
        }

        log.info("Toplu yeniden analiz: {} ilan bulundu, {} kuyruğa yazıldı",
                basarisizlar.size(), kuyruklanan);
        return kuyruklanan;
    }

    @Transactional
    public void resolveLostAd(String ownerEmail, Long adId, ResolveLostAdRequest request) {
        User owner = findUserByEmail(ownerEmail);
        Ad ad = findActiveOwnedAd(adId, owner.getUid());

        if (ad.getAdType() != Ad.AdType.LOST) {
            throw new IllegalArgumentException("İlan bir kayıp ilanı değildir.");
        }

        Long finderId = (request != null) ? request.finderId() : null;
        Long foundAdId = (request != null) ? request.foundAdId() : null;

        // Eşleşen ilan bildirildiyse GERÇEKTEN var mı diye bakılır. Yoksa
        // sessizce null bırakmak, ölçüm sorgusunda "bağ kurulmamış" ile
        // "yanlış id gönderilmiş" durumlarını ayırt edilemez yapardı.
        if (foundAdId != null) {
            if (foundAdId.equals(adId)) {
                throw new IllegalArgumentException(
                        "Bir ilan kendisiyle eşleştirilemez.");
            }
            Ad eslesen = adRepository.findById(foundAdId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Eşleşen ilan bulunamadı: " + foundAdId));
            if (eslesen.getAdType() != Ad.AdType.FOUND) {
                throw new IllegalArgumentException(
                        "Eşleşen ilan bir 'bulundu' ilanı olmalı.");
            }
            ad.setResolvedByAdId(foundAdId);
        }

        ad.setFinderUserId(finderId);
        ad.setActive(false);
        ad.setResolutionStatus(AdResolutionStatus.FOUND);
        adRepository.saveAndFlush(ad);

        rewardService.awardLostPoint(finderId);
        log.info("Kayıp ilanı bulundu olarak işaretlendi. adId={}, ownerId={}, "
                        + "finderId={}, eslesenIlanId={}",
                adId, owner.getUid(), finderId, foundAdId);
    }

    @Transactional(readOnly = true)
    public AdCountersResponse getAdCounters() {
        long activeAds = adRepository.countByActiveTrueAndSuspendedFalse();
        long happyEndings = adRepository.countByResolutionStatusIn(HAPPY_ENDING_STATUSES);

        return new AdCountersResponse(activeAds, happyEndings);
    }

    @Transactional(readOnly = true)
    public List<AdResponse> findNearbyAds(
            double latitude,
            double longitude,
            double radiusInMeters
    ) {
        Point userPoint = geometryFactory.createPoint(new Coordinate(longitude, latitude));

        return adRepository.findNearbyAds(userPoint, radiusInMeters)
                .stream()
                .map(this::toResponseWithTemporaryPhotoUrls)
                .toList();
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Oturum sahibine ait kullanıcı kaydı bulunamadı"
                ));
    }

    private Ad findActiveOwnedAd(Long adId, Long ownerId) {
        return adRepository.findByIdAndUser_UidAndActiveTrue(adId, ownerId)
                .orElseThrow(() -> adNotFound(adId));
    }

    private ResourceNotFoundException adNotFound(Long adId) {
        return new ResourceNotFoundException(
                "Aktif ilan bulunamadı: " + adId
        );
    }


        /**
         * Public harita için aktif ve askıda olmayan yakın ilanları getirir.
         *
         * <p>Koordinat oluşturulurken JTS sıralaması gereği önce boylam (X),
         * sonra enlem (Y) verilir.</p>
         */
        @Transactional(readOnly = true)
        public List<AdResponse> findPublicNearbyAds(
                double latitude,
                double longitude,
                double radiusInMeters
        ) {
            // JTS Coordinate sırası longitude (X), latitude (Y) şeklindedir.
            Point userPoint = geometryFactory.createPoint(
                    new Coordinate(longitude, latitude)
            );

            return adRepository.findPublicNearbyAds(
                            userPoint,
                            radiusInMeters
                    )
                    .stream()
                    // Entity doğrudan açılmaz; ilan güvenli DTO yanıtına dönüştürülür.
                    .map(this::toResponseWithTemporaryPhotoUrls)
                    .toList();
        }

    public AdResponse toResponseWithTemporaryPhotoUrls(Ad ad) {
        List<String> temporaryPhotoUrls = ad.getPhotoUrls() == null
                ? List.of()
                : ad.getPhotoUrls()
                .stream()
                .map(imageStorageService::createTemporaryReadUrl)
                .toList();

        return adMapper.toResponse(ad, temporaryPhotoUrls);
    }

}
