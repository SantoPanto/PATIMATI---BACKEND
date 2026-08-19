package com.works.patimati.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.works.patimati.ai.AiAnalysisPublisher;
import com.works.patimati.dto.ad.AdCreateRequest;
import com.works.patimati.dto.ad.AdResponse;
import com.works.patimati.dto.ad.AdUpdateRequest;
import com.works.patimati.dto.ad.ResolveLostAdRequest;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.AiStatus;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.mapper.AdMapper;
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
    private static final double DEFAULT_NOTIFICATION_RADIUS_METERS = 5_000.0;

    private final AdRepository adRepository;
    private final UserRepository userRepository;
    private final AdMapper adMapper;
    private final ImageStorageService imageStorageService;
    private final AiAnalysisPublisher aiAnalysisPublisher;
    private final RewardService rewardService;
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

        notifyNearbyUsersSafely(savedAd, owner.getUid());

        // AI analizini KUYRUĞA bırakır ve beklemez (entegrasyon sözleşmesi §1).
        // Fotoğraf analizi 1-3 saniye sürüyor; senkron çağrı kullanıcıyı
        // bekletirdi. Yayınlama hata verse bile ilan kaydedilmiş kalır:
        // ai_status PENDING'de durur ve sonradan yeniden denenebilir.
        aiAnalysisPublisher.publish(savedAd);

        return response;
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
            Pageable pageable
    ) {
        Page<Ad> ads = adType == null
                ? adRepository.findAllByActiveTrueAndSuspendedFalse(pageable)
                : adRepository.findAllByAdTypeAndActiveTrueAndSuspendedFalse(
                adType,
                pageable
        );

        return ads.map(this::toResponseWithTemporaryPhotoUrls);
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
    public Page<AdResponse> getUserAds(
            String ownerEmail,
            boolean active,
            Pageable pageable
    ) {
        User owner = findUserByEmail(ownerEmail);

        return adRepository.findAllByUser_UidAndActive(
                        owner.getUid(),
                        active,
                        pageable
                )
                .map(this::toResponseWithTemporaryPhotoUrls);
    }

    @Transactional
    public AdResponse updateAd(
            String ownerEmail,
            Long adId,
            AdUpdateRequest request
    ) {
        User owner = findUserByEmail(ownerEmail);
        Ad ad = findActiveOwnedAd(adId, owner.getUid());

        adMapper.updateEntity(ad, request);
        Ad updatedAd = adRepository.saveAndFlush(ad);

        return toResponseWithTemporaryPhotoUrls(updatedAd);
    }

    @Transactional
    public void deactivateAd(String ownerEmail, Long adId) {
        User owner = findUserByEmail(ownerEmail);
        Ad ad = findActiveOwnedAd(adId, owner.getUid());

        ad.setActive(false);
        adRepository.saveAndFlush(ad);
    }

    @Transactional
    public void resolveLostAd(String ownerEmail, Long adId, ResolveLostAdRequest request) {
        User owner = findUserByEmail(ownerEmail);
        Ad ad = findActiveOwnedAd(adId, owner.getUid());

        if (ad.getAdType() != Ad.AdType.LOST) {
            throw new IllegalArgumentException("İlan bir kayıp ilanı değildir.");
        }

        ad.setActive(false);
        adRepository.saveAndFlush(ad);

        Long finderId = (request != null) ? request.finderId() : null;
        rewardService.awardLostPoint(finderId);
        log.info("Kayıp ilanı bulundu olarak işaretlendi. adId={}, ownerId={}, finderId={}", adId, owner.getUid(), finderId);
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
                .map(url -> {
                    // TEST BYPASS EKLENTİSİ
                    if (url != null && url.contains("dummyimage.com")) {
                        return url; // Sahte URL ise direkt döndür
                    }
                    // Gerçek URL ise S3 servisine (createTemporaryReadUrl) yolla
                    return imageStorageService.createTemporaryReadUrl(url);
                })
                .toList();

        return adMapper.toResponse(ad, temporaryPhotoUrls);
    }

    private void notifyNearbyUsersSafely(Ad ad, Long ownerUid) {
        if (ad.getLocation() == null) {
            return;
        }

        try {
            List<User> nearbyUsers = userRepository.findUsersNearby(
                    ad.getLocation(),
                    DEFAULT_NOTIFICATION_RADIUS_METERS
            );

            String notificationTitle = "Olası Eşleşme!";
            String notificationBody = "Kayıp ilanınızla uyuşabilecek yeni bir ilan var: " + ad.getTitle();

            for (User nearbyUser : nearbyUsers) {

                if (nearbyUser.getUid() != null && nearbyUser.getUid().equals(ownerUid)) {
                    continue;
                }

                String fcmToken = nearbyUser.getFcmToken();
                if (fcmToken != null && !fcmToken.isBlank()) {
                    sendPushNotification(
                            fcmToken,
                            notificationTitle,
                            notificationBody
                    );
                }
            }
        } catch (RuntimeException exception) {
            log.warn(
                    "İlan oluşturuldu ancak yakındaki kullanıcılar belirlenemedi. adId={}",
                    ad.getId(),
                    exception
            );
        }
    }

    private void sendPushNotification(String token, String title, String body) {
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();
            FirebaseMessaging.getInstance().send(message);
        } catch (Exception e) {
            log.error("Push notification gönderilemedi: {}", e.getMessage());
        }
    }
}