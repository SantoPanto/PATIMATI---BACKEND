package com.works.patimati.service;

import com.works.patimati.ai.AiMatchNotifier;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.PotentialMatch;
import com.works.patimati.entity.PotentialMatchRecipient;
import com.works.patimati.entity.User;
import com.works.patimati.entity.external.ExternalPetRecord;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.PotentialMatchRecipientRepository;
import com.works.patimati.repository.PotentialMatchRepository;
import com.works.patimati.repository.external.ExternalPetRecordRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Eşleşmelerin kalıcılığı ve bildirim teslimatı — Faz 1 taslağının en büyük
 * eksiğini kapatır: mevcut native↔native eşleştirme hiçbir zaman kalıcı
 * değildi, her yeniden analizde aynı çifte tekrar bildirim gidiyordu.
 *
 * <p>Faz 2 revize blueprint §2/§3/§4'teki düzeltilmiş tasarım:
 * <ul>
 *   <li>Eşleşme KİMLİĞİ ({@link PotentialMatch}) ve bildirim HEDEFİ
 *       ({@link PotentialMatchRecipient}) ayrı satırlardır — native↔native'de
 *       iki bağımsız alıcı satırı, native↔external'da bir tane.</li>
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
    private final AiMatchNotifier notifier;

    @Value("${potential-match.max-send-attempts:5}")
    private int maxSendAttempts;

    @Value("${potential-match.retry-grace-period:PT2M}")
    private Duration retryGracePeriod;

    /**
     * Native↔native bir eşleşmeyi kaydeder. {@code (adAId, adBId)} kanonik
     * sırada (küçük id önce) tutulur — hangi ilanın analizi eşleşmeyi
     * bulduğuna bakılmaksızın DAİMA aynı satıra düşsün diye; DB'deki
     * LEAST/GREATEST indeksi bunu ikinci bir güvenlik katmanı olarak zaten
     * garanti eder, buradaki sıralama okunabilirlik + tutarlılık içindir.
     */
    @Transactional
    public void recordAdMatch(Long subjectAdId, Long candidateAdId,
                               float visual, float label, float location, float finalScore,
                               String matchingVersion) {
        if (subjectAdId == null || candidateAdId == null || subjectAdId.equals(candidateAdId)) {
            return;
        }
        Long adAId = Math.min(subjectAdId, candidateAdId);
        Long adBId = Math.max(subjectAdId, candidateAdId);

        Optional<Long> insertedId = potentialMatchRepository.insertAdMatchIfAbsent(
                adAId, adBId, visual, label, location, finalScore, matchingVersion);

        if (insertedId.isEmpty()) {
            // Zaten var — muhtemelen daha önceki bir analiz turu. Alıcı
            // satırları da zaten var demektir; tekrar oluşturmuyoruz (bu,
            // "duplicate match = recipient sayısı değişmez" gereksinimidir).
            log.debug("AD-AD eşleşmesi zaten mevcut: {} <-> {}", adAId, adBId);
            return;
        }

        Optional<Ad> adA = adRepository.findById(adAId);
        Optional<Ad> adB = adRepository.findById(adBId);
        if (adA.isEmpty() || adB.isEmpty()) {
            log.warn("Yeni PotentialMatch (AD) için ilanlardan biri bulunamadı: {} / {}", adAId, adBId);
            return;
        }

        PotentialMatch match = potentialMatchRepository.findById(insertedId.get()).orElse(null);
        if (match == null) {
            return;
        }

        createRecipientAndSend(match, adA.get().getUser(), PotentialMatchRecipient.Role.OWNER_A);
        createRecipientAndSend(match, adB.get().getUser(), PotentialMatchRecipient.Role.OWNER_B);

        log.info("Yeni PotentialMatch (AD<->AD): {} <-> {} skor={}", adAId, adBId, finalScore);
    }

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

        createRecipientAndSend(match, ad.get().getUser(), PotentialMatchRecipient.Role.OWNER);

        log.info("Yeni PotentialMatch (AD<->EXTERNAL): ad={} external={} skor={}",
                adId, externalRecordId, finalScore);
    }

    private void createRecipientAndSend(PotentialMatch match, User recipient, PotentialMatchRecipient.Role role) {
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
        boolean sent = notifier.sendOne(recipient);
        if (sent) {
            recipientRepository.markNotifiedIfPending(recipientId, Instant.now());
        } else {
            recipientRepository.incrementSendAttempts(recipientId);
        }
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
}
