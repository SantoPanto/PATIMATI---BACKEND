package com.works.patimati.service.impl;

import com.works.patimati.dto.ad.AdResponse;
import java.time.LocalDate;
import com.works.patimati.dto.ad.AdoptionAdCreateRequest;
import com.works.patimati.dto.ad.AdoptionAdUpdateRequest;
import com.works.patimati.dto.ad.ResolveAdoptionAdRequest;
import com.works.patimati.dto.complaint.AdComplaintRequestDTO;
import com.works.patimati.dto.complaint.ComplaintResponse;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.AdoptionComplaint;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.AiStatus;
import com.works.patimati.entity.enums.AdResolutionStatus;
import com.works.patimati.entity.enums.CoatPattern;
import com.works.patimati.entity.enums.ComplaintStatus;
import com.works.patimati.entity.enums.EyeColor;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.AdoptionComplaintRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.service.AdService;
import com.works.patimati.service.AdoptionService;
import com.works.patimati.service.RewardService;
import com.works.patimati.storage.ImageStorageService;
import com.works.patimati.storage.InvalidImageException;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdoptionServiceImpl implements AdoptionService {

    private static final Logger log = LoggerFactory.getLogger(AdoptionServiceImpl.class);
    private static final int WGS_84_SRID = 4326;

    private final AdRepository adRepository;
    private final UserRepository userRepository;
    private final AdoptionComplaintRepository adoptionComplaintRepository;
    private final ImageStorageService imageStorageService;
    private final AdService adService;
    private final RewardService rewardService;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), WGS_84_SRID);

    @Transactional
    @Override
    public AdResponse createAdoptionAd(
            String ownerEmail,
            AdoptionAdCreateRequest request,
            List<MultipartFile> images
    ) {
        // Kural: En az 1 fotoğraf yüklenmelidir.
        if (images == null || images.isEmpty()) {
            throw new InvalidImageException("Sahiplendirme ilanı için en az 1 fotoğraf yüklenmelidir.");
        }

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + ownerEmail));

        List<String> photoReferences = imageStorageService.uploadImages(images);

        Point location = null;
        if (request.latitude() != null && request.longitude() != null) {
            location = geometryFactory.createPoint(
                    new Coordinate(request.longitude().doubleValue(), request.latitude().doubleValue())
            );
        }

        Ad ad = Ad.builder()
                .title(request.title().trim())
                .description(request.description() != null ? request.description().trim() : null)
                .adType(Ad.AdType.ADOPTION)
                .species(request.species())
                .breed(request.breed() != null ? request.breed().trim() : "MIXED_OR_UNKNOWN")
                .gender(request.gender())
                .ageGroup(request.ageGroup())
                .colors(request.colors() != null ? request.colors() : Set.of())
                /*
                 * coatPattern ve eyeColor isteğe bağlı alanlar (DTO'da @NotNull yok)
                 * ama ads tablosunda NOT NULL. Ad entity'sinde @Builder.Default ile
                 * UNKNOWN tanımlı; ancak builder metodunu null ile ÇAĞIRMAK bu
                 * varsayılanı ezer. Bu yüzden alan gönderilmediğinde insert
                 * "null value in column coat_pattern violates not-null constraint"
                 * ile düşüyordu ve sahiplendirme ilanı hiç oluşturulamıyordu.
                 * (AdService bu builder metotlarını hiç çağırmadığı için orada
                 * varsayılanlar çalışıyor.)
                 */
                .coatPattern(request.coatPattern() != null
                        ? request.coatPattern()
                        : CoatPattern.UNKNOWN)
                .eyeColor(request.eyeColor() != null
                        ? request.eyeColor()
                        : EyeColor.UNKNOWN)
                .microchipNumber(request.microchipNumber())
                .lostDate(parseDate(request.date()))
                .location(location)
                .photoUrls(photoReferences)
                .user(owner)
                .active(true)
                .suspended(false)
                .aiStatus(AiStatus.NOT_APPLICABLE) // AI işlemine girmeyecek
                .build();

        Ad savedAd = adRepository.save(ad);
        log.info("Sahiplendirme ilanı oluşturuldu. adId={}, owner={}", savedAd.getId(), ownerEmail);

        return adService.toResponseWithTemporaryPhotoUrls(savedAd);
    }

    @Transactional
    @Override
    public AdResponse updateAdoptionAd(
            String ownerEmail,
            Long adId,
            AdoptionAdUpdateRequest request
    ) {
        Ad ad = adRepository.findByIdAndActiveTrue(adId)
                .orElseThrow(() -> new ResourceNotFoundException("Sahiplendirme ilanı bulunamadı ID: " + adId));

        validateAdOwnerAndType(ad, ownerEmail);

        Point location = null;
        if (request.latitude() != null && request.longitude() != null) {
            location = geometryFactory.createPoint(
                    new Coordinate(request.longitude().doubleValue(), request.latitude().doubleValue())
            );
        }

        ad.setTitle(request.title().trim());
        ad.setDescription(request.description() != null ? request.description().trim() : null);
        ad.setSpecies(request.species());
        ad.setBreed(request.breed() != null ? request.breed().trim() : "MIXED_OR_UNKNOWN");
        ad.setGender(request.gender());
        ad.setAgeGroup(request.ageGroup());
        if (request.colors() != null && !request.colors().isEmpty()) {
            Set<com.works.patimati.entity.enums.PetColor> validColors = request.colors().stream()
                    .filter(java.util.Objects::nonNull)
                    .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
            ad.setColors(validColors);
        } else {
            ad.setColors(new java.util.LinkedHashSet<>());
        }
        if (request.coatPattern() != null) ad.setCoatPattern(request.coatPattern());
        if (request.eyeColor() != null) ad.setEyeColor(request.eyeColor());
        ad.setMicrochipNumber(request.microchipNumber());
        ad.setLocation(location);

        Ad updatedAd = adRepository.save(ad);
        log.info("Sahiplendirme ilanı güncellendi. adId={}", updatedAd.getId());

        return adService.toResponseWithTemporaryPhotoUrls(updatedAd);
    }

    @Transactional
    @Override
    public void deleteAdoptionAd(String ownerEmail, Long adId) {
        Ad ad = adRepository.findByIdAndActiveTrue(adId)
                .orElseThrow(() -> new ResourceNotFoundException("Sahiplendirme ilanı bulunamadı ID: " + adId));

        validateAdOwnerAndType(ad, ownerEmail);

        ad.setActive(false);
        adRepository.save(ad);
        log.info("Sahiplendirme ilanı kaldırıldı (soft delete). adId={}", adId);
    }

    @Transactional
    @Override
    public void resolveAdoptionAd(String ownerEmail, Long adId, ResolveAdoptionAdRequest request) {
        Ad ad = adRepository.findByIdAndActiveTrue(adId)
                .orElseThrow(() -> new ResourceNotFoundException("Sahiplendirme ilanı bulunamadı ID: " + adId));

        validateAdOwnerAndType(ad, ownerEmail);

        ad.setActive(false);
        ad.setResolutionStatus(AdResolutionStatus.ADOPTED);
        adRepository.save(ad);

        Long ownerId = ad.getUser().getUid();
        Long adopterId = (request != null) ? request.adopterId() : null;

        rewardService.awardAdoptionPoints(ownerId, adopterId);
        log.info("Sahiplendirme ilanı sahiplendirildi olarak kapatıldı. adId={}, ownerId={}, adopterId={}", adId, ownerId, adopterId);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<AdResponse> getPublicAdoptionAds(Pageable pageable) {
        Page<Ad> ads = adRepository.findAllByAdTypeAndActiveTrueAndSuspendedFalse(
                Ad.AdType.ADOPTION,
                pageable
        );
        return ads.map(adService::toResponseWithTemporaryPhotoUrls);
    }

    @Transactional
    @Override
    public ComplaintResponse createAdoptionComplaint(
            String reporterEmail,
            Long adId,
            AdComplaintRequestDTO request
    ) {
        User reporter = userRepository.findByEmail(reporterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + reporterEmail));

        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new ResourceNotFoundException("İlan bulunamadı ID: " + adId));

        if (ad.getAdType() != Ad.AdType.ADOPTION) {
            throw new IllegalArgumentException("Belirtilen ilan bir sahiplendirme ilanı değildir.");
        }

        // Mükerrer aktif şikayet kontrolü
        List<ComplaintStatus> activeStatuses = List.of(ComplaintStatus.BEKLEMEDE, ComplaintStatus.INCELEMEDE);
        boolean exists = adoptionComplaintRepository.existsByReporterIdAndAdIdAndStatusIn(
                reporter.getUid(),
                adId,
                activeStatuses
        );

        if (exists) {
            throw new IllegalStateException("Bu sahiplendirme ilanı için zaten incelemede veya beklemede bir şikayetiniz bulunmaktadır.");
        }

        AdoptionComplaint complaint = AdoptionComplaint.builder()
                .reporterId(reporter.getUid())
                .adId(adId)
                .reason(request.getReason())
                .description(request.getDescription().trim())
                .status(ComplaintStatus.BEKLEMEDE)
                .build();

        AdoptionComplaint savedComplaint = adoptionComplaintRepository.save(complaint);
        log.info("Sahiplendirme ilanı şikayeti oluşturuldu. complaintId={}, adId={}", savedComplaint.getId(), adId);

        return new ComplaintResponse(
                savedComplaint.getId(),
                reporter.getUid(),
                reporter.getEmail(),
                adId,
                ad.getUser() != null ? ad.getUser().getUid() : null,
                savedComplaint.getReason(),
                savedComplaint.getDescription(),
                savedComplaint.getStatus(),
                savedComplaint.getCreatedAt()
        );
    }

    @Transactional
    @Override
    public ComplaintResponse resolveAdoptionComplaint(Long complaintId) {
        AdoptionComplaint complaint = adoptionComplaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sahiplendirme ilanı şikayeti bulunamadı ID: " + complaintId));

        complaint.setStatus(ComplaintStatus.COZULDU);
        AdoptionComplaint updatedComplaint = adoptionComplaintRepository.save(complaint);
        log.info("Sahiplendirme ilanı şikayeti çözüldü. complaintId={}", complaintId);

        String reporterEmail = userRepository.findById(updatedComplaint.getReporterId())
                .map(User::getEmail)
                .orElse(null);

        Long adOwnerUid = adRepository.findById(updatedComplaint.getAdId())
                .filter(ad -> ad.getUser() != null)
                .map(ad -> ad.getUser().getUid())
                .orElse(null);

        return new ComplaintResponse(
                updatedComplaint.getId(),
                updatedComplaint.getReporterId(),
                reporterEmail,
                updatedComplaint.getAdId(),
                adOwnerUid,
                updatedComplaint.getReason(),
                updatedComplaint.getDescription(),
                updatedComplaint.getStatus(),
                updatedComplaint.getCreatedAt()
        );
    }

    private void validateAdOwnerAndType(Ad ad, String ownerEmail) {
        if (ad.getAdType() != Ad.AdType.ADOPTION) {
            throw new IllegalArgumentException("İlan bir sahiplendirme ilanı değildir.");
        }
        if (ad.getUser() == null || !ad.getUser().getEmail().equalsIgnoreCase(ownerEmail)) {
            throw new IllegalStateException("Bu sahiplendirme ilanı üzerinde işlem yapma yetkiniz bulunmamaktadır.");
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Tarih formatı yyyy-MM-dd olmalıdır");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Tarih gelecekte bir tarih olamaz");
        }
        return date;
    }
}
