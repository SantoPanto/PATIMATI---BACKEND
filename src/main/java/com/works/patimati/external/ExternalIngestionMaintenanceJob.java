package com.works.patimati.external;

import com.works.patimati.entity.enums.ExternalProcessingStatus;
import com.works.patimati.entity.external.ExternalPetRecord;
import com.works.patimati.entity.external.ExternalSourcePost;
import com.works.patimati.repository.external.ExternalPetRecordRepository;
import com.works.patimati.repository.external.ExternalSourcePostRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * İki bağımsız süpürücü (Faz 2 revize blueprint §5/§11):
 *
 * <ol>
 *   <li>{@link #sweepStalePosts()} — carousel'i hiç tamamlanmayan (Collector
 *       çökmüş, ağ kopmuş) gönderileri sonsuza kadar beklemekten kurtarır.</li>
 *   <li>{@link #retryStuckMatching()} — Aşama 2 (eşleştirme) geçici olarak
 *       başarısız olup {@code ANALYZED}'de bırakılmış kayıtları yeniden
 *       dener; Aşama 1'in sonucu bu süreçte hiç kaybolmaz.</li>
 * </ol>
 */
@Component
@RequiredArgsConstructor
public class ExternalIngestionMaintenanceJob {

    private static final Logger log = LoggerFactory.getLogger(ExternalIngestionMaintenanceJob.class);

    private final ExternalSourcePostRepository postRepository;
    private final ExternalPetRecordRepository externalPetRecordRepository;
    private final ExternalMatchingService matchingService;

    @Value("${external.maintenance.stale-grace-period:PT6H}")
    private Duration staleGracePeriod;

    /** Bu süreden uzun süre ANALYZED'de takılı kalan kayıt artık yeniden denenmez, NEEDS_REVIEW'e düşer. */
    @Value("${external.maintenance.stage2-retry-max-age:P1D}")
    private Duration stage2RetryMaxAge;

    @Scheduled(fixedDelayString = "${external.maintenance.sweep-interval:PT15M}",
               initialDelayString = "${external.maintenance.sweep-interval:PT15M}")
    @Transactional
    public void sweepStalePosts() {
        Instant cutoff = Instant.now().minus(staleGracePeriod);
        for (ExternalProcessingStatus status : List.of(
                ExternalProcessingStatus.DISCOVERED,
                ExternalProcessingStatus.COLLECTED,
                ExternalProcessingStatus.MEDIA_STORED,
                ExternalProcessingStatus.ANALYZING)) {
            List<ExternalSourcePost> stuck = postRepository.findStuckAt(status, cutoff);
            for (ExternalSourcePost post : stuck) {
                post.setProcessingStatus(ExternalProcessingStatus.FAILED);
                post.setFailureReason("Süpürücü: " + status + " durumunda " + staleGracePeriod + " süresinden "
                        + "uzun süre takılı kaldı (medya asla tamamlanmadı ya da analiz hiç dönmedi)");
                postRepository.save(post);
                log.warn("Takılı kalmış gönderi FAILED işaretlendi: postId={} önceki durum={}",
                        post.getId(), status);
            }
        }
    }

    @Scheduled(fixedDelayString = "${external.maintenance.stage2-retry-interval:PT5M}",
               initialDelayString = "${external.maintenance.stage2-retry-interval:PT5M}")
    @Transactional
    public void retryStuckMatching() {
        Instant retryCutoff = Instant.now().minus(Duration.ofMinutes(2)); // Aşama 2'nin ilk denemesine yetecek pay
        Instant giveUpCutoff = Instant.now().minus(stage2RetryMaxAge);

        List<ExternalSourcePost> analyzed = postRepository.findStuckAt(ExternalProcessingStatus.ANALYZED, retryCutoff);
        for (ExternalSourcePost post : analyzed) {
            if (post.getUpdatedAt() != null && post.getUpdatedAt().isBefore(giveUpCutoff)) {
                post.setProcessingStatus(ExternalProcessingStatus.NEEDS_REVIEW);
                post.setFailureReason("Aşama 2, " + stage2RetryMaxAge + " boyunca tekrar tekrar başarısız oldu");
                postRepository.save(post);
                log.warn("Aşama 2 tekrar denemesi vazgeçildi, NEEDS_REVIEW: postId={}", post.getId());
                continue;
            }

            externalPetRecordRepository.findByPostAndPetIndex(post, (short) 0)
                    .ifPresent(this::retryOne);
        }
    }

    private void retryOne(ExternalPetRecord record) {
        log.info("Aşama 2 yeniden deneniyor: externalRecordId={}", record.getId());
        matchingService.attemptMatching(record);
    }
}
