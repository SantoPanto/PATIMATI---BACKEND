package com.works.patimati.service.impl;

import com.works.patimati.dto.admin.InstagramPublishQueueAdminResponse;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.AdInstagramPublication;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.InstagramPublishStatus;
import com.works.patimati.entity.enums.PetColor;
import com.works.patimati.entity.enums.Species;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.instagram.InstagramGraphClient;
import com.works.patimati.instagram.InstagramGraphResult;
import com.works.patimati.repository.AdInstagramPublicationRepository;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.instagram.InstagramCaptionAiClient;
import com.works.patimati.service.InstagramPublishService;
import com.works.patimati.storage.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstagramPublishServiceImpl implements InstagramPublishService {

    private static final Logger log = LoggerFactory.getLogger(InstagramPublishServiceImpl.class);

    private static final Set<Ad.AdType> ELIGIBLE_AD_TYPES =
            Set.of(Ad.AdType.LOST, Ad.AdType.FOUND, Ad.AdType.ADOPTION, Ad.AdType.HELP);

    private static final Map<PetColor, String> RENK_TR = new EnumMap<>(PetColor.class);
    static {
        RENK_TR.put(PetColor.BLACK, "Siyah");
        RENK_TR.put(PetColor.WHITE, "Beyaz");
        RENK_TR.put(PetColor.GRAY, "Gri");
        RENK_TR.put(PetColor.BROWN, "Kahverengi");
        RENK_TR.put(PetColor.ORANGE, "Turuncu");
        RENK_TR.put(PetColor.CREAM, "Krem");
        RENK_TR.put(PetColor.GOLDEN, "Altın Sarısı");
        RENK_TR.put(PetColor.BEIGE, "Bej");
        RENK_TR.put(PetColor.OTHER, "Diğer");
    }

    private final AdInstagramPublicationRepository publicationRepository;
    private final AdRepository adRepository;
    private final UserRepository userRepository;
    private final ImageStorageService imageStorageService;
    private final InstagramCaptionAiClient instagramCaptionAiClient;
    private final InstagramGraphClient instagramGraphClient;

    @Value("${instagram.publish.enabled:false}")
    private boolean publishEnabled;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void queueForReview(Ad savedAd) {
        try {
            if (!publishEnabled || !savedAd.isInstagramShareConsent()
                    || !ELIGIBLE_AD_TYPES.contains(savedAd.getAdType())) {
                return;
            }

            String caption = onerilenCaptionUret(savedAd);

            AdInstagramPublication publication = AdInstagramPublication.builder()
                    .adId(savedAd.getId())
                    .status(InstagramPublishStatus.PENDING)
                    .suggestedCaption(caption)
                    .build();
            publicationRepository.save(publication);

            log.info("İlan Instagram kuyruğuna eklendi: adId={}", savedAd.getId());
        } catch (Exception e) {
            // Yutulan istisna bilinçli -- AiAnalysisPublisher.publish() ile
            // AYNI ilke: bu bir ek, ilan oluşturma akışını asla etkilememeli.
            log.error("İlan Instagram kuyruğuna eklenemedi (adId={}): {}",
                    savedAd.getId(), e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Page<InstagramPublishQueueAdminResponse> listQueue(Pageable pageable, InstagramPublishStatus status) {
        Page<AdInstagramPublication> page = status != null
                ? publicationRepository.findAllByStatus(status, pageable)
                : publicationRepository.findAll(pageable);
        return page.map(this::toAdminResponse);
    }

    private InstagramPublishQueueAdminResponse toAdminResponse(AdInstagramPublication publication) {
        Ad ad = adRepository.findById(publication.getAdId()).orElse(null);
        if (ad == null) {
            // İlan sonradan (admin tarafından) kalıcı silinmiş olabilir --
            // kuyruk kaydı yine de görünsün, çökmesin.
            return new InstagramPublishQueueAdminResponse(
                    publication.getId(), publication.getAdId(), "(İlan silinmiş)",
                    null, null, null, publication.getSuggestedCaption(),
                    publication.getStatus(), publication.getFailureReason(),
                    publication.getCreatedAt());
        }

        String firstPhotoUrl = ad.getPhotoUrls() == null || ad.getPhotoUrls().isEmpty()
                ? null
                : imageStorageService.createTemporaryReadUrl(ad.getPhotoUrls().get(0));

        return new InstagramPublishQueueAdminResponse(
                publication.getId(),
                ad.getId(),
                ad.getTitle(),
                ad.getAdType(),
                ownerDisplayName(ad.getUser()),
                firstPhotoUrl,
                publication.getSuggestedCaption(),
                publication.getStatus(),
                publication.getFailureReason(),
                publication.getCreatedAt()
        );
    }

    private String ownerDisplayName(User owner) {
        if (owner == null) return null;
        String displayName = (owner.getFirstName() != null ? owner.getFirstName() : "")
                + " " + (owner.getLastName() != null ? owner.getLastName() : "");
        displayName = displayName.trim();
        return displayName.isBlank() ? null : displayName;
    }

    @Transactional
    @Override
    public boolean publish(Long queueItemId, String finalCaption, String adminEmail) {
        AdInstagramPublication publication = publicationRepository.findById(queueItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Instagram kuyruk kaydı bulunamadı: " + queueItemId));
        Ad ad = adRepository.findById(publication.getAdId())
                .orElseThrow(() -> new ResourceNotFoundException("İlan bulunamadı: " + publication.getAdId()));

        InstagramGraphResult result;
        try {
            List<String> photoUrls = toDownloadableUrls(ad.getPhotoUrls());
            result = instagramGraphClient.publish(photoUrls, finalCaption);
        } catch (RuntimeException e) {
            // Fotoğraf adresi üretilemedi gibi Graph API'ye HİÇ ULAŞMADAN
            // oluşan hatalar da FAILED'a düşmeli -- admin panelde sessizce
            // kaybolmasın, tekrar denenebilsin.
            log.error("İlan Instagram'a gönderilirken beklenmeyen hata (adId={}): {}",
                    ad.getId(), e.getMessage(), e);
            result = InstagramGraphResult.failure("Beklenmeyen hata: " + e.getMessage());
        }

        Long adminId = userRepository.findByEmail(adminEmail).map(User::getUid).orElse(null);
        publication.setFinalCaption(finalCaption);
        publication.setPublishedByAdminId(adminId);

        if (result.success()) {
            publication.setStatus(InstagramPublishStatus.PUBLISHED);
            publication.setIgMediaId(result.mediaId());
            publication.setIgPermalink(result.permalink());
            publication.setFailureReason(null);
            log.info("İlan Instagram'da yayınlandı: adId={}, igMediaId={}", ad.getId(), result.mediaId());
        } else {
            publication.setStatus(InstagramPublishStatus.FAILED);
            publication.setFailureReason(result.errorMessage());
            log.warn("İlan Instagram'da yayınlanamadı: adId={}, sebep={}", ad.getId(), result.errorMessage());
        }

        publicationRepository.save(publication);
        return result.success();
    }

    @Transactional
    @Override
    public void skip(Long queueItemId, String adminEmail) {
        AdInstagramPublication publication = publicationRepository.findById(queueItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Instagram kuyruk kaydı bulunamadı: " + queueItemId));

        Long adminId = userRepository.findByEmail(adminEmail).map(User::getUid).orElse(null);
        publication.setStatus(InstagramPublishStatus.SKIPPED);
        publication.setPublishedByAdminId(adminId);
        publicationRepository.save(publication);
    }

    /**
     * AI'dan bir caption ister; sağlayıcı yapılandırılmamışsa ya da çağrı
     * başarısız olursa (InstagramCaptionAiClient.generateCaption null döner
     * -- asla fırlatmaz) basit bir Türkçe şablona düşülür.
     */
    private String onerilenCaptionUret(Ad ad) {
        String tur = turTr(ad.getSpecies());
        String irk = "MIXED_OR_UNKNOWN".equalsIgnoreCase(ad.getBreed()) ? null : ad.getBreed();
        String renkler = ad.getColors() == null || ad.getColors().isEmpty()
                ? null
                : ad.getColors().stream().map(c -> RENK_TR.getOrDefault(c, c.name()))
                        .collect(Collectors.joining(", "));
        String ilIlce = ad.getCity() == null ? null
                : ad.getDistrict() == null ? ad.getCity() : ad.getCity() + "/" + ad.getDistrict();
        List<String> ayirtEdici = ad.getDistinctiveMarks() == null || ad.getDistinctiveMarks().isBlank()
                ? List.of()
                : List.of(ad.getDistinctiveMarks());

        String aiCaption;
        try {
            aiCaption = instagramCaptionAiClient.generateCaption(
                    ad.getAdType().name(), tur, irk, renkler, ilIlce, ad.getDescription(), ayirtEdici);
        } catch (RuntimeException e) {
            // InstagramCaptionAiClient sözleşmesi zaten fırlatmaz -- bu, o
            // sözleşme ihlal edilse bile queueForReview'un YİNE DE kuyruğa
            // eklemeye devam etmesini sağlayan ikinci bir güvenlik katmanı.
            log.warn("Instagram caption istemcisi beklenmedik şekilde fırlattı, şablona düşülüyor: {}", e.toString());
            aiCaption = null;
        }

        return aiCaption != null ? aiCaption : sablonCaptionUret(ad, tur, irk, ilIlce);
    }

    /** AI yapılandırılmamışsa/başarısız olursa güvenli varsayılan. */
    private String sablonCaptionUret(Ad ad, String tur, String irk, String ilIlce) {
        String ilanTuruTr = switch (ad.getAdType()) {
            case LOST -> "Kayıp";
            case FOUND -> "Bulundu";
            case ADOPTION -> "Sahiplendirme";
            case HELP -> "Yardım";
        };

        StringBuilder metin = new StringBuilder();
        metin.append(ilanTuruTr).append(" ilanı: ").append(tur);
        if (irk != null) {
            metin.append(" (").append(irk).append(")");
        }
        if (ilIlce != null) {
            metin.append(" -- ").append(ilIlce);
        }
        if (ad.getDescription() != null && !ad.getDescription().isBlank()) {
            metin.append("\n\n").append(ad.getDescription());
        }
        metin.append("\n\nDetaylar ve iletişim için profildeki PatiMati linkine bak.\n#patimati");
        return metin.toString();
    }

    private String turTr(Species species) {
        if (species == null) return "Hayvan";
        return switch (species) {
            case CAT -> "Kedi";
            case DOG -> "Köpek";
            case UNKNOWN -> "Hayvan";
        };
    }

    /** {@code AiAnalysisPublisher.toDownloadableUrls} ile AYNI amaç/desen. */
    private List<String> toDownloadableUrls(List<String> storageReferences) {
        return storageReferences.stream()
                .map(imageStorageService::createTemporaryReadUrl)
                .collect(Collectors.toList());
    }
}
