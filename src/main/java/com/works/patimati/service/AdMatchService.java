package com.works.patimati.service;

import com.works.patimati.dto.match.AdMatchResponseDTO;
import com.works.patimati.dto.match.AdMatchSaveRequest;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.AdMatch;
import com.works.patimati.entity.User;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.AdMatchRepository;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.storage.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * AdMatch (Eşleşme) modülünün iş mantığını (Business Logic) yürüten servis katmanı.
 *
 * <p>SOLID prensiplerine ve DDD standartlarına uygun olarak tasarlanmıştır.
 * Oturum açmış kullanıcının ilanını {@code myAd}, karşı tarafın ilanını {@code partnerAd} olarak mapler.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdMatchService {

    private static final Logger log = LoggerFactory.getLogger(AdMatchService.class);

    private final AdMatchRepository adMatchRepository;
    private final AdRepository adRepository;
    private final UserRepository userRepository;
    private final ImageStorageService imageStorageService;

    /**
     * Oturum açmış kullanıcının taraf olduğu (sourceAd veya matchedAd) tüm eşleşmeleri
     * toplam skor değerine göre azalan sırada getirir.
     *
     * @param userEmail İstekte bulunan kullanıcının e-posta adresi
     * @return Eşleşme yanıt DTO listesi (myAd ve partnerAd şeklinde ayrıştırılmış)
     */
    public List<AdMatchResponseDTO> getUserMatches(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + userEmail));

        Long currentUserId = user.getUid();
        List<AdMatch> matches = adMatchRepository.findByUserIdOrderByTotalScoreDesc(currentUserId);

        return matches.stream()
                .map(match -> mapToDTO(match, currentUserId))
                .toList();
    }

    /**
     * Yeni bir eşleşme kaydeder veya önceden var olan benzersiz eşleşmenin skorunu günceller.
     *
     * <p>Her satır bildirim gidecek olan {@code userId}'ye aittir. Her iki taraf için (kaybeden ve bulan)
     * kendi kullanıcı ID'leriyle ayrı satırlar oluşturulur ve bildirim damgaları (notification_sent_at)
     * bağımsız olarak tutulur.
     *
     * @param request Eşleşme kaydetme/güncelleme talebi
     * @return Kaydedilen/Güncellenen eşleşmenin DTO karşılığı
     */
    @Transactional
    public AdMatchResponseDTO saveOrUpdateMatch(AdMatchSaveRequest request) {
        Long sourceAdId = request.getSourceAdId();
        Long matchedAdId = request.getMatchedAdId();

        if (sourceAdId == null || matchedAdId == null) {
            throw new IllegalArgumentException("İlan ID değerleri boş olamaz.");
        }

        Ad sourceAd = adRepository.findById(sourceAdId)
                .orElseThrow(() -> new ResourceNotFoundException("Kaynak ilan bulunamadı: " + sourceAdId));

        Ad matchedAd = adRepository.findById(matchedAdId)
                .orElseThrow(() -> new ResourceNotFoundException("Eşleşen ilan bulunamadı: " + matchedAdId));

        User targetUser;
        if (request.getUserId() != null) {
            targetUser = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + request.getUserId()));
        } else {
            targetUser = sourceAd.getUser() != null ? sourceAd.getUser() : matchedAd.getUser();
            if (targetUser == null) {
                throw new IllegalStateException("İlanların sahibi bulunamadı.");
            }
        }

        // Veritabanında (user_id, source_ad_id, matched_ad_id) üçlüsüne göre kayıt kontrolü
        Optional<AdMatch> existingMatchOpt = adMatchRepository.findByUser_UidAndSourceAd_IdAndMatchedAd_Id(
                targetUser.getUid(),
                sourceAdId,
                matchedAdId
        );

        AdMatch matchEntity;
        if (existingMatchOpt.isPresent()) {
            // Var olan kaydın skorlarını ve metriklerini güncelle (Idempotent Update)
            matchEntity = existingMatchOpt.get();
            matchEntity.updateMetrics(
                    request.getTotalScore(),
                    request.getVisualScore(),
                    request.getTagScore(),
                    request.getLocationScore(),
                    request.getThresholdAtTime(),
                    request.getMatchedPhotoPair(),
                    request.getBlockReason(),
                    request.isPassedThreshold()
            );
            log.info("Var olan eşleşme skoru güncellendi: matchId={} userId={} sourceAdId={} matchedAdId={}",
                    matchEntity.getId(), targetUser.getUid(), sourceAdId, matchedAdId);
        } else {
            // Yeni kayıt oluştur (sourceAd -> matchedAd yönüyle)
            matchEntity = AdMatch.builder()
                    .user(targetUser)
                    .sourceAd(sourceAd)
                    .matchedAd(matchedAd)
                    .totalScore(request.getTotalScore())
                    .visualScore(request.getVisualScore())
                    .tagScore(request.getTagScore())
                    .locationScore(request.getLocationScore())
                    .thresholdAtTime(request.getThresholdAtTime())
                    .matchedPhotoPair(request.getMatchedPhotoPair())
                    .blockReason(request.getBlockReason())
                    .passedThreshold(request.isPassedThreshold())
                    .notificationSentAt(null)
                    .build();

            log.info("Yeni eşleşme kaydı oluşturuldu: userId={} sourceAdId={} matchedAdId={}",
                    targetUser.getUid(), sourceAdId, matchedAdId);
        }

        AdMatch saved = adMatchRepository.save(matchEntity);
        return mapToDTO(saved, targetUser.getUid());
    }

    /**
     * Eşleşme kaydı için kullanıcıya bildirim gönderildiğini işaretler (SRP Uygun Metot).
     *
     * @param matchId Eşleşme kayıt ID
     * @return Güncellenmiş DTO
     */
    @Transactional
    public AdMatchResponseDTO markNotificationAsSent(Long matchId) {
        AdMatch match = adMatchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Eşleşme kaydı bulunamadı: " + matchId));

        match.markNotificationSent();
        AdMatch updated = adMatchRepository.save(match);

        log.info("Eşleşme bildirimi gönderildi olarak işaretlendi: matchId={} notificationSentAt={}",
                updated.getId(), updated.getNotificationSentAt());

        Long currentUserId = updated.getUser() != null ? updated.getUser().getUid() : null;
        return mapToDTO(updated, currentUserId);
    }

    /**
     * Entity nesnesini oturum açmış kullanıcıya göre `myAd` ve `partnerAd` olarak ayrıştırıp
     * DTO yapısına dönüştüren yardımcı metot.
     *
     * @param match AdMatch nesnesi
     * @param currentUserId Oturum açmış kullanıcının ID'si
     * @return Dönüştürülmüş AdMatchResponseDTO
     */
    private AdMatchResponseDTO mapToDTO(AdMatch match, Long currentUserId) {
        Ad source = match.getSourceAd();
        Ad matched = match.getMatchedAd();

        Ad myAdEntity;
        Ad partnerAdEntity;

        // Oturum açan kullanıcının ilanı myAd, karşı tarafın ilanı partnerAd olarak belirlenir
        if (currentUserId != null && source != null && source.getUser() != null && currentUserId.equals(source.getUser().getUid())) {
            myAdEntity = source;
            partnerAdEntity = matched;
        } else if (currentUserId != null && matched != null && matched.getUser() != null && currentUserId.equals(matched.getUser().getUid())) {
            myAdEntity = matched;
            partnerAdEntity = source;
        } else {
            // Varsayılan olarak sourceAd -> myAd, matchedAd -> partnerAd
            myAdEntity = source;
            partnerAdEntity = matched;
        }

        AdMatchResponseDTO.AdSummaryDTO myAdSummary = mapToSummary(myAdEntity);
        AdMatchResponseDTO.AdSummaryDTO partnerAdSummary = mapToSummary(partnerAdEntity);

        return AdMatchResponseDTO.builder()
                .id(match.getId())
                .myAd(myAdSummary)
                .myAdId(myAdSummary != null ? myAdSummary.getId() : null)
                .myAdTitle(myAdSummary != null ? myAdSummary.getTitle() : null)
                .partnerAd(partnerAdSummary)
                .partnerAdId(partnerAdSummary != null ? partnerAdSummary.getId() : null)
                .partnerAdTitle(partnerAdSummary != null ? partnerAdSummary.getTitle() : null)
                .totalScore(match.getTotalScore())
                .visualScore(match.getVisualScore())
                .tagScore(match.getTagScore())
                .locationScore(match.getLocationScore())
                .thresholdAtTime(match.getThresholdAtTime())
                .matchedPhotoPair(match.getMatchedPhotoPair())
                .blockReason(match.getBlockReason())
                .passedThreshold(match.isPassedThreshold())
                .notificationSentAt(match.getNotificationSentAt())
                .createdAt(match.getCreatedAt())
                .build();
    }

    private AdMatchResponseDTO.AdSummaryDTO mapToSummary(Ad ad) {
        if (ad == null) {
            return null;
        }
        String photoUrl = (ad.getPhotoUrls() != null && !ad.getPhotoUrls().isEmpty())
                ? ad.getPhotoUrls().get(0)
                : null;

        if (photoUrl != null && !photoUrl.isBlank()) {
            try {
                photoUrl = imageStorageService.createTemporaryReadUrl(photoUrl);
            } catch (Exception e) {
                log.warn("Fotoğraf için geçici indirme URL'si oluşturulamadı: {}", photoUrl, e);
            }
        }

        return AdMatchResponseDTO.AdSummaryDTO.builder()
                .id(ad.getId())
                .title(ad.getTitle())
                .photoUrl(photoUrl)
                .species(ad.getSpecies() != null ? ad.getSpecies().name() : null)
                .breed(ad.getBreed())
                .build();
    }
}
