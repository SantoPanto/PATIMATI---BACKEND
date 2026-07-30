package com.works.patimati.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.works.patimati.ai.AiAnalysisPublisher;
import com.works.patimati.dto.ad.AdCreateRequest;
import com.works.patimati.dto.ad.AdResponse;
import com.works.patimati.dto.ad.AdUpdateRequest;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.User;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.mapper.AdMapper;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.storage.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
        User owner = findUserByEmail(ownerEmail);

        Ad ad = adMapper.toEntity(request);

        // Güvenli atamalar
        ad.setUser(owner);
        ad.setActive(true);

        // MapStruct spatial point dönüşümünü yapmadıysa manuel fallback
        if (ad.getLocation() == null && request.latitude() != null && request.longitude() != null) {
            Point location = geometryFactory.createPoint(
                    new Coordinate(request.longitude().doubleValue(), request.latitude().doubleValue())
            );
            ad.setLocation(location);
        }

        List<String> storedReferences = images == null || images.isEmpty()
                ? List.of()
                : imageStorageService.uploadImages(images);
        if (!storedReferences.isEmpty()) {
            ad.setPhotoUrls(new ArrayList<>(storedReferences));
        }

        Ad savedAd;
        try {
            savedAd = adRepository.saveAndFlush(ad);
        } catch (RuntimeException exception) {
            // Kayıt başarısızsa yüklenen dosyalar depoda kalmasın: kimse
            // referanslarını bilmediği için sonsuza kadar yer kaplarlar.
            if (!storedReferences.isEmpty()) {
                imageStorageService.deleteImages(storedReferences);
            }
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

    @Transactional(readOnly = true)
    public AdResponse getActiveAd(Long adId) {
        Ad ad = adRepository.findByIdAndActiveTrue(adId)
                .orElseThrow(() -> adNotFound(adId));

        return toResponseWithTemporaryPhotoUrls(ad);
    }

    @Transactional(readOnly = true)
    public Page<AdResponse> getActiveAds(
            Ad.AdType adType,
            Pageable pageable
    ) {
        Page<Ad> ads = adType == null
                ? adRepository.findAllByActive(true, pageable)
                : adRepository.findAllByAdTypeAndActive(
                adType,
                true,
                pageable
        );

        return ads.map(this::toResponseWithTemporaryPhotoUrls);
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

    private AdResponse toResponseWithTemporaryPhotoUrls(Ad ad) {
        List<String> temporaryPhotoUrls = ad.getPhotoUrls() == null
                ? List.of()
                : ad.getPhotoUrls()
                .stream()
                .map(imageStorageService::createTemporaryReadUrl)
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
                            .setTitle("Yakınınızda Yeni İlan: " + title)
                            .setBody(body)
                            .build())
                    .build();
            FirebaseMessaging.getInstance().send(message);
        } catch (Exception e) {
            log.error("Push notification gönderilemedi: {}", e.getMessage());
        }
    }
}