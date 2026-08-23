package com.works.patimati.service;

import com.works.patimati.dto.sighting.SightingCreateRequest;
import com.works.patimati.dto.sighting.SightingResponse;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.AdSighting;
import com.works.patimati.entity.User;
import com.works.patimati.exception.BusinessException;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.AdSightingRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.storage.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * "Gördüm" bildirimi: bırakma (girişsiz de olabilir) + sahibine listeleme.
 *
 * <p>Ürün kararları (22.08): bildirimi girişsiz ziyaretçi de bırakabilir
 * (iletişim zorunlu + IP hız sınırı bedeliyle); görülmeleri YALNIZ ilan
 * sahibi görür.
 */
@Service
@RequiredArgsConstructor
public class SightingService {

    static final String BILDIRIM_TIPI = "SIGHTING";
    private static final String BILDIRIM_BASLIGI = "İlanın için görülme bildirimi";

    private static final Logger log = LoggerFactory.getLogger(SightingService.class);

    private final AdSightingRepository adSightingRepository;
    private final AdRepository adRepository;
    private final UserRepository userRepository;
    private final ImageStorageService imageStorageService;
    private final NotificationService notificationService;
    private final SightingRateLimiter rateLimiter;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    /**
     * @param reporterEmail girişli bırakanın e-postası; girişsizde null
     * @param clientIp      hız sınırı anahtarı (girişsiz uç — tek koruma bu)
     */
    @Transactional
    public SightingResponse createSighting(
            Long adId,
            SightingCreateRequest request,
            MultipartFile photo,
            String reporterEmail,
            String clientIp
    ) {
        if (!rateLimiter.izinVer(clientIp)) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Çok sık bildirim gönderildi, lütfen biraz sonra tekrar dene.");
        }

        // Pasif/askıdaki ilan da dışarıya "yok" görünür (getPublicAdById ilkesi).
        Ad ad = adRepository.findByIdAndActiveTrueAndSuspendedFalse(adId)
                .orElseThrow(() -> new ResourceNotFoundException("İlan bulunamadı: " + adId));

        if (ad.getAdType() != Ad.AdType.LOST) {
            throw new BusinessException(
                    "Görülme bildirimi yalnız kayıp ilanlarına bırakılabilir.");
        }

        User reporter = reporterEmail == null
                ? null
                : userRepository.findByEmail(reporterEmail).orElse(null);

        // Foto önce depoya (createAd ile aynı sıra); doğrulamanın tamamı
        // (JPEG imzası, 5MB, boş dosya) ImageStorageService'in içinde.
        String photoReference = null;
        if (photo != null && !photo.isEmpty()) {
            photoReference = imageStorageService.uploadImages(List.of(photo)).get(0);
        }

        AdSighting sighting;
        try {
            sighting = adSightingRepository.saveAndFlush(AdSighting.builder()
                    .ad(ad)
                    .reporter(reporter)
                    .reporterContact(request.reporterContact().trim())
                    .note(request.note() == null || request.note().isBlank()
                            ? null : request.note().trim())
                    .photoUrl(photoReference)
                    .location(toPoint(request.latitude(), request.longitude()))
                    .build());
        } catch (RuntimeException exception) {
            deletePhotoSafely(photoReference);
            throw exception;
        }

        sahibineBildir(ad, sighting);

        return toResponse(sighting);
    }

    /** Görülmeler yalnız ilan sahibine: sahiplik düşerse ilanın varlığı da söylenmez. */
    @Transactional(readOnly = true)
    public List<SightingResponse> listSightings(Long adId, String userEmail) {
        User owner = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Kullanıcı bulunamadı: " + userEmail));

        adRepository.findByIdAndUser_Uid(adId, owner.getUid())
                .orElseThrow(() -> new AccessDeniedException(
                        "Görülmeleri yalnızca ilan sahibi görebilir."));

        return adSightingRepository.findByAd_IdOrderByCreatedAtDescIdDesc(adId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /** Bildirim ilan akışını asla düşürmez (NearbyAlertNotifier ile aynı sözleşme). */
    private void sahibineBildir(Ad ad, AdSighting sighting) {
        try {
            User owner = ad.getUser();
            if (owner == null) {
                return;
            }
            notificationService.createAndSend(
                    owner,
                    BILDIRIM_BASLIGI,
                    "Biri \"" + ad.getTitle() + "\" ilanın için bir görülme bildirdi.",
                    BILDIRIM_TIPI,
                    Map.of(
                            "type", BILDIRIM_TIPI,
                            "adId", String.valueOf(ad.getId()),
                            "sightingId", String.valueOf(sighting.getId())
                    ),
                    BILDIRIM_TIPI + ":" + sighting.getId()
            );
        } catch (RuntimeException exception) {
            log.warn("Görülme kaydedildi ancak sahibine bildirim gönderilemedi. sightingId={}",
                    sighting.getId(), exception);
        }
    }

    private void deletePhotoSafely(String photoReference) {
        if (photoReference == null) {
            return;
        }
        try {
            imageStorageService.deleteImages(List.of(photoReference));
        } catch (RuntimeException exception) {
            log.warn("Görülme kaydı düştü, fotoğraf geri silinemedi: {}", photoReference, exception);
        }
    }

    private Point toPoint(BigDecimal latitude, BigDecimal longitude) {
        // JTS (X, Y) = (boylam, enlem) — AdMapper.toPoint ile aynı sıra.
        return geometryFactory.createPoint(
                new Coordinate(longitude.doubleValue(), latitude.doubleValue()));
    }

    private SightingResponse toResponse(AdSighting sighting) {
        return new SightingResponse(
                sighting.getId(),
                BigDecimal.valueOf(sighting.getLocation().getY()),
                BigDecimal.valueOf(sighting.getLocation().getX()),
                sighting.getNote(),
                sighting.getReporterContact(),
                sighting.getPhotoUrl() == null
                        ? null
                        : imageStorageService.createTemporaryReadUrl(sighting.getPhotoUrl()),
                sighting.getCreatedAt()
        );
    }
}
