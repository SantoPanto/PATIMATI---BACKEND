package com.works.patimati.service;

import com.works.patimati.dto.match.PotentialMatchDecisionRequest;
import com.works.patimati.dto.match.PotentialMatchSummaryResponse;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.PotentialMatch;
import com.works.patimati.entity.PotentialMatchRecipient;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.MatchStatus;
import com.works.patimati.entity.external.ExternalPetRecord;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.notification.PushNotificationService;
import com.works.patimati.notification.PushResult;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.PotentialMatchRecipientRepository;
import com.works.patimati.repository.PotentialMatchRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.repository.external.ExternalPetRecordRepository;
import com.works.patimati.repository.external.ExternalSourceMediaRepository;
import com.works.patimati.storage.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Native↔external (Instagram) eşleşmelerin kalıcılığı ve bildirim teslimatı.
 *
 * <p><b>Yalnızca native↔external:</b> native↔native eşleşmeler bilerek burada
 * DEĞİL — o akış {@code ad_matches} tablosu ve {@link com.works.patimati.ai.AiMatchNotifier}
 * üzerinden yürüyor (develop'taki B6 düzeltmesi, canlıda zaten oturmuş).
 * Kaynağa göre ayrım: aynı eşleşmeyi iki sistemin birden yazması hem çift
 * bildirime hem çakışan dedup anahtarlarına yol açardı. Tam ikame (native↔native
 * eşleşmelerin de buraya taşınması) ayrı bir PR'ın konusu.
 *
 * <p>Faz 2 revize blueprint §2/§3/§4'teki tasarım:
 * <ul>
 *   <li>Eşleşme KİMLİĞİ ({@link PotentialMatch}) ve bildirim HEDEFİ
 *       ({@link PotentialMatchRecipient}) ayrı satırlardır — native↔external'da
 *       tek alıcı: ilan sahibi.</li>
 *   <li>Teslimat sırası GÖNDER-sonra-işaretle'dir (taslaktaki
 *       işaretle-sonra-gönder'in TERSİ): çökme anında sessiz kayıp yerine
 *       nadir/sınırlı bir tekrar bildirim tercih edilir — bu proje için
 *       kaybolan bir eşleşme, tekrar edilen bir bildirimden daha kötüdür.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class PotentialMatchService {

    private static final Logger log = LoggerFactory.getLogger(PotentialMatchService.class);

    private final PotentialMatchRepository potentialMatchRepository;
    private final PotentialMatchRecipientRepository recipientRepository;
    private final AdRepository adRepository;
    private final ExternalPetRecordRepository externalPetRecordRepository;
    private final PushNotificationService pushNotificationService;
    private final UserRepository userRepository;
    private final ExternalSourceMediaRepository externalSourceMediaRepository;
    private final ImageStorageService imageStorageService;

    @Value("${potential-match.max-send-attempts:5}")
    private int maxSendAttempts;

    @Value("${potential-match.retry-grace-period:PT2M}")
    private Duration retryGracePeriod;

    /** Native↔external bir eşleşmeyi kaydeder — tek alıcı: ilan sahibi. */
    @Transactional
    public void recordExternalMatch(Long adId, Long externalRecordId,
                                     float visual, float label, float location, float finalScore,
                                     String matchingVersion) {
        if (adId == null || externalRecordId == null) {
            return;
        }

        Optional<Long> insertedId = potentialMatchRepository.insertExternalMatchIfAbsent(
                adId, externalRecordId, visual, label, location, finalScore, matchingVersion);

        if (insertedId.isEmpty()) {
            log.debug("AD-EXTERNAL eşleşmesi zaten mevcut: ad={} external={}", adId, externalRecordId);
            return;
        }

        Optional<Ad> ad = adRepository.findById(adId);
        Optional<ExternalPetRecord> record = externalPetRecordRepository.findById(externalRecordId);
        if (ad.isEmpty() || record.isEmpty()) {
            log.warn("Yeni PotentialMatch (EXTERNAL) için taraflardan biri bulunamadı: ad={} external={}",
                    adId, externalRecordId);
            return;
        }

        PotentialMatch match = potentialMatchRepository.findById(insertedId.get()).orElse(null);
        if (match == null) {
            return;
        }

        createRecipientAndSend(match, ad.get().getUser(), PotentialMatchRecipient.RecipientRole.OWNER);

        log.info("Yeni PotentialMatch (AD<->EXTERNAL): ad={} external={} skor={}",
                adId, externalRecordId, finalScore);
    }

    private void createRecipientAndSend(PotentialMatch match, User recipient, PotentialMatchRecipient.RecipientRole role) {
        if (recipient == null) {
            return;
        }
        Optional<Long> recipientId = recipientRepository.insertIfAbsent(
                match.getId(), recipient.getUid(), role.name());
        // insertIfAbsent boş dönerse bu (match, kullanıcı) çifti için zaten
        // bir satır var — aynı kullanıcıya asla ikinci kez bildirim
        // ATILMAZ (blueprint §12/§39).
        recipientId.ifPresent(this::attemptSend);
    }

    /**
     * GÖNDER-sonra-işaretle. Yalnızca hâlâ PENDING ise NOTIFIED'e geçer —
     * eşzamanlı süpürücüyle yarışsa bile ikinci geçiş 0 satır etkiler.
     */
    private void attemptSend(Long recipientId) {
        PotentialMatchRecipient recipient = recipientRepository.findById(recipientId).orElse(null);
        if (recipient == null) {
            return;
        }
        boolean sent = sendOne(recipient);
        if (sent) {
            recipientRepository.markNotifiedIfPending(recipientId, Instant.now());
        } else {
            recipientRepository.incrementSendAttempts(recipientId);
        }
    }

    /**
     * Tek bir alıcıya tek bir FCM bildirimi gönderir. {@code collapseKey},
     * alıcı satırının kendi kimliğine bağlanır: aynı bildirim (nadir de olsa)
     * iki kez tetiklenirse cihaz/OS seviyesinde tek bildirime düşürülür —
     * bu, {@code potential_matches}/{@code potential_match_recipients}
     * üzerindeki DB seviyesindeki dedup'ın YERİNE geçmez, ikincil bir savunmadır.
     *
     * @return true ise gönderim BAŞARILI (ya da gönderilecek alıcı/token
     *         yoktu — bu durumda tekrar denemenin bir anlamı kalmaz); false
     *         ise BAŞARISIZ, çağıran {@code sendAttempts} artırıp PENDING'de bırakmalı.
     */
    boolean sendOne(PotentialMatchRecipient recipient) {
        User user = recipient.getRecipient();
        if (user == null) {
            log.warn("Eşleşme bildirimi atlandı (recipientId={}): ilgili kullanıcı yok", recipient.getId());
            return true;
        }

        PushResult sonuc = pushNotificationService.send(
                user.getFcmToken(),
                "Olası eşleşme bulundu",
                buildBody(recipient.getPotentialMatch(), recipient.getRole()),
                Map.of(
                        "type", "POTENTIAL_MATCH",
                        "potentialMatchId", String.valueOf(recipient.getPotentialMatch().getId()),
                        "recipientId", String.valueOf(recipient.getId())
                ),
                "potential-match-" + recipient.getId()
        );

        return switch (sonuc) {
            case SENT -> true;
            case NO_TOKEN, NO_RECIPIENT -> true;
            case PUSH_DISABLED, FAILED -> {
                log.warn("Eşleşme bildirimi gönderilemedi (recipientId={}): {}",
                        recipient.getId(), sonuc.aciklama());
                yield false;
            }
        };
    }

    /**
     * Hiçbir zaman kesinlik iddia etmez (sözleşme §7 kural 3): "bulundu"
     * değil "benzeyen bir kayıt tespit edildi" dili kullanılır.
     */
    private String buildBody(PotentialMatch match, PotentialMatchRecipient.RecipientRole role) {
        if (match.getCandidateKind() == PotentialMatch.CandidateKind.EXTERNAL) {
            return "Evcil hayvanınıza benzeyen bir hayvan tespit ettik. "
                    + "Kaydı inceleyerek aynı hayvan olup olmadığını kontrol edebilirsiniz.";
        }
        Ad other = role == PotentialMatchRecipient.RecipientRole.OWNER_B ? match.getAdA() : match.getAdB();
        String title = other != null && other.getTitle() != null ? other.getTitle() : "bir ilan";
        return "İlanınıza benzeyen bir ilan var: " + title + ". Siz de bakar mısınız?";
    }

    /**
     * Süpürücü: çökme anında (insert-sonra-çökme VEYA gönder-sonra-işaretle
     * arasında çökme) PENDING kalmış satırları yeniden dener. Max deneme
     * sayısı aşılanlar {@code NOTIFICATION_FAILED}'e geçirilir — sessizce
     * sonsuza kadar kaybolmaz, gözlemlenebilir bir uç duruma düşer.
     */
    // initialDelay: uygulama açılışında henüz veri kaynağı tam ısınmadan
    // t=0'da tetiklenip gürültülü (ama zararsız) bir hatayla loglanmasın diye.
    @Scheduled(fixedDelayString = "${potential-match.retry-interval:PT2M}",
               initialDelayString = "${potential-match.retry-interval:PT2M}")
    @Transactional
    public void retryPendingNotifications() {
        Instant cutoff = Instant.now().minus(retryGracePeriod);
        List<PotentialMatchRecipient> pending = recipientRepository.findPendingForRetry(maxSendAttempts, cutoff);
        for (PotentialMatchRecipient recipient : pending) {
            attemptSend(recipient.getId());
        }

        List<PotentialMatchRecipient> exhausted = recipientRepository.findExhaustedPending(maxSendAttempts);
        for (PotentialMatchRecipient recipient : exhausted) {
            int updated = recipientRepository.markNotificationFailedIfPending(recipient.getId());
            if (updated > 0) {
                log.warn("Bildirim teslim edilemedi (max deneme aşıldı): recipientId={} matchId={}",
                        recipient.getId(), recipient.getPotentialMatch().getId());
            }
        }
    }

    // ------------------------------------------------------------------
    // Okuma/karar uçları — GET /api/me/potential-matches, POST .../decision
    // (Faz 2 revize blueprint §10 — frontend minimum). Yalnızca çağıranın
    // KENDİ alıcı satırları görünür/değiştirilebilir.
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<PotentialMatchSummaryResponse> findForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));
        return recipientRepository.findByRecipient_UidOrderByCreatedAtDesc(user.getUid()).stream()
                .map(this::toSummary)
                .toList();
    }

    /**
     * @throws AccessDeniedException kayıt başka bir kullanıcıya aitse (403)
     * @throws IllegalStateException geçerli durum NOTIFIED/VIEWED değilse (409) — örn.
     *         zaten karar verilmiş ya da EXPIRED bir kayıt için tekrar karar verilemez
     */
    @Transactional
    public PotentialMatchSummaryResponse recordDecision(
            String email, Long recipientId, PotentialMatchDecisionRequest.Decision decision) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        PotentialMatchRecipient recipient = recipientRepository.findById(recipientId)
                .orElseThrow(() -> new ResourceNotFoundException("Eşleşme kaydı bulunamadı"));

        if (!recipient.getRecipient().getUid().equals(user.getUid())) {
            throw new AccessDeniedException("Bu eşleşme kaydı size ait değil");
        }

        if (recipient.getStatus() != MatchStatus.NOTIFIED && recipient.getStatus() != MatchStatus.VIEWED) {
            throw new IllegalStateException(
                    "Bu kayıt şu anda '" + recipient.getStatus() + "' durumunda — yalnızca NOTIFIED/VIEWED "
                            + "durumundaki bir kayıt için karar verilebilir");
        }

        MatchStatus newStatus = decision == PotentialMatchDecisionRequest.Decision.CONFIRMED
                ? MatchStatus.CONFIRMED : MatchStatus.REJECTED;
        recipient.setStatus(newStatus);
        recipient.setDecidedAt(Instant.now());
        recipientRepository.save(recipient);

        return toSummary(recipient);
    }

    private PotentialMatchSummaryResponse toSummary(PotentialMatchRecipient recipient) {
        PotentialMatch match = recipient.getPotentialMatch();
        PotentialMatchSummaryResponse.Counterparty counterparty;

        if (match.getCandidateKind() == PotentialMatch.CandidateKind.AD) {
            // role=OWNER_B -> çağıran adB sahibi -> karşı taraf adA; aksi hâlde tersi.
            Ad other = recipient.getRole() == PotentialMatchRecipient.RecipientRole.OWNER_B
                    ? match.getAdA() : match.getAdB();
            counterparty = new PotentialMatchSummaryResponse.Counterparty(
                    "AD", other.getId(), other.getTitle(), representativeAdPhoto(other),
                    other.getAdType() != null ? other.getAdType().name() : null,
                    null, null, null, null);
        } else {
            ExternalPetRecord record = match.getExternalRecord();
            counterparty = new PotentialMatchSummaryResponse.Counterparty(
                    "EXTERNAL", record.getId(), null, representativeExternalPhoto(record),
                    null,
                    record.getCategory() != null ? record.getCategory().name() : null,
                    record.getSpecies(), record.getBreed(),
                    record.getPost() != null ? record.getPost().getCanonicalUrl() : null);
        }

        return new PotentialMatchSummaryResponse(
                recipient.getId(), match.getId(), recipient.getStatus().name(),
                match.getFinalScore(), recipient.getCreatedAt(), counterparty);
    }

    private String representativeAdPhoto(Ad ad) {
        if (ad.getPhotoUrls() == null || ad.getPhotoUrls().isEmpty()) {
            return null;
        }
        try {
            return imageStorageService.createTemporaryReadUrl(ad.getPhotoUrls().get(0));
        } catch (RuntimeException e) {
            log.warn("İlan {} için fotoğraf adresi üretilemedi: {}", ad.getId(), e.getMessage());
            return null;
        }
    }

    private String representativeExternalPhoto(ExternalPetRecord record) {
        if (record.getPost() == null) {
            return null;
        }
        return externalSourceMediaRepository.findByPostOrderByOrdinalAsc(record.getPost()).stream()
                .filter(m -> m.getStorageKey() != null)
                .findFirst()
                .map(m -> {
                    try {
                        return imageStorageService.createTemporaryReadUrl(m.getStorageKey());
                    } catch (RuntimeException e) {
                        log.warn("External kayıt {} için fotoğraf adresi üretilemedi: {}",
                                record.getId(), e.getMessage());
                        return null;
                    }
                })
                .orElse(null);
    }
}
