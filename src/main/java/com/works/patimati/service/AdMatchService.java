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
     * Yeni bir eşleşme kaydeder veya önceden var olan benzersiz eşleşme çiftinin skorunu günceller.
     *
     * <p>Ayna çift kayıtlarını (örn: 5 <-> 12 ve 12 <-> 5) engellemek amacıyla ID'ler kaydedilmeden
     * hemen önce matematiksel olarak sıralanır (Kanatsal / Canonical ID Sorting):
     * {@code sourceAdId = Math.min(id1, id2)}, {@code matchedAdId = Math.max(id1, id2)}.
     *
     * @param request Eşleşme kaydetme/güncelleme talebi
     * @return Kaydedilen/Güncellenen eşleşmenin DTO karşılığı
     */
    @Transactional
    public AdMatchResponseDTO saveOrUpdateMatch(AdMatchSaveRequest request) {
        Long rawSourceId = request.getSourceAdId();
        Long rawMatchedId = request.getMatchedAdId();

        if (rawSourceId == null || rawMatchedId == null) {
            throw new IllegalArgumentException("İlan ID değerleri boş olamaz.");
        }

        // İlan ID'lerinin yönünü sabitleme (Math.min / Math.max sorting)
        Long firstAdId = Math.min(rawSourceId, rawMatchedId);
        Long secondAdId = Math.max(rawSourceId, rawMatchedId);

        Ad firstAd = adRepository.findById(firstAdId)
                .orElseThrow(() -> new ResourceNotFoundException("İlan bulunamadı: " + firstAdId));

        Ad secondAd = adRepository.findById(secondAdId)
                .orElseThrow(() -> new ResourceNotFoundException("İlan bulunamadı: " + secondAdId));

        User targetUser;
        if (request.getUserId() != null) {
            targetUser = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + request.getUserId()));
        } else {
            targetUser = firstAd.getUser() != null ? firstAd.getUser() : secondAd.getUser();
            if (targetUser == null) {
                throw new IllegalStateException("İlanların sahibi bulunamadı.");
            }
        }

        // Veritabanında sabitlenen (firstAdId, secondAdId) çifti kontrolü
        Optional<AdMatch> existingMatchOpt = adMatchRepository.findBySourceAd_IdAndMatchedAd_Id(
                firstAdId,
                secondAdId
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
            log.info("Var olan eşleşme skoru güncellendi (kanonik ID sırasıyla): matchId={} firstAdId={} secondAdId={}",
                    matchEntity.getId(), firstAdId, secondAdId);
        } else {
            // Yeni kayıt oluştur (sourceAd = firstAd, matchedAd = secondAd)
            matchEntity = AdMatch.builder()
                    .user(targetUser)
                    .sourceAd(firstAd)
                    .matchedAd(secondAd)
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

            log.info("Yeni eşleşme kaydı oluşturuldu (kanonik ID sırasıyla): firstAdId={} secondAdId={}",
                    firstAdId, secondAdId);
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

        return AdMatchResponseDTO.AdSummaryDTO.builder()
                .id(ad.getId())
                .title(ad.getTitle())
                .photoUrl(photoUrl)
                .species(ad.getSpecies() != null ? ad.getSpecies().name() : null)
                .breed(ad.getBreed())
                .build();
    }
}
