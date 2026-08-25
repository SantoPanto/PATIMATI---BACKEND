package com.works.patimati.external;

import com.works.patimati.entity.enums.ExternalSource;
import com.works.patimati.entity.external.ExternalSourceMedia;
import com.works.patimati.entity.external.ExternalSourcePost;
import com.works.patimati.repository.external.ExternalSourceMediaRepository;
import com.works.patimati.repository.external.ExternalSourcePostRepository;
import com.works.patimati.storage.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

/**
 * Aşama 4 medya transfer sırasının backend tarafı (Faz 2 revize blueprint
 * §4): Collector'ın kendi oturumuyla indirdiği baytları, mevcut
 * {@code ImageStorageService} üzerinden kalıcı depolamaya taşır.
 *
 * <p>Instagram CDN adresine ASLA güvenilmez, RabbitMQ'ya bayt KONULMAZ,
 * Collector S3 kimlik bilgisi ALMAZ.
 */
@Service
@RequiredArgsConstructor
public class ExternalMediaIngestionService {

    private static final Logger log = LoggerFactory.getLogger(ExternalMediaIngestionService.class);

    private final ExternalSourcePostRepository postRepository;
    private final ExternalSourceMediaRepository mediaRepository;
    private final ImageStorageService imageStorageService;
    private final ExternalAnalysisPublisher analysisPublisher;

    public record Result(Long mediaId, String storageKey, String processingState) {
    }

    @Transactional
    public Result attach(String sourceRaw, String sourcePostId, int ordinal, String contentSha256, MultipartFile file) {
        ExternalSource source;
        try {
            source = ExternalSource.valueOf(sourceRaw);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bilinmeyen source: " + sourceRaw);
        }

        // Ebeveyn gönderi henüz kaydedilmemiş olabilir (event mesajı hâlâ
        // kuyrukta) — bu KALICI bir hata değil, Collector kısa bir bekleme
        // sonrası yeniden dener (§4 sıra adım 6).
        ExternalSourcePost post = postRepository.findBySourceAndSourcePostId(source, sourcePostId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "Gönderi henüz kaydedilmedi, kısa süre sonra yeniden deneyin"));

        short ord = (short) ordinal;
        ExternalSourceMedia mediaRow = mediaRepository.findByPostAndOrdinal(post, ord)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "Bu ordinal için tanımlayıcı yok — event ile media[] tutarsız olabilir"));

        // İdempotency: aynı içerik (hash) zaten depolanmışsa yeniden yüklemeden dön.
        if (mediaRow.getProcessingState() == ExternalSourceMedia.ProcessingState.STORED
                && mediaRow.getStorageKey() != null) {
            return new Result(mediaRow.getId(), mediaRow.getStorageKey(), mediaRow.getProcessingState().name());
        }
        if (contentSha256 != null) {
            Optional<ExternalSourceMedia> byHash = mediaRepository
                    .findByPostAndContentSha256AndProcessingState(post, contentSha256,
                            ExternalSourceMedia.ProcessingState.STORED);
            if (byHash.isPresent()) {
                return new Result(byHash.get().getId(), byHash.get().getStorageKey(),
                        byHash.get().getProcessingState().name());
            }
        }

        List<String> stored = imageStorageService.uploadImages(List.of(file), "external");
        String storageKey = stored.get(0);

        try {
            mediaRow.setStorageKey(storageKey);
            mediaRow.setContentSha256(contentSha256);
            mediaRow.setProcessingState(ExternalSourceMedia.ProcessingState.STORED);
            mediaRepository.save(mediaRow);
        } catch (RuntimeException e) {
            // AdService.createAd'daki S3 geri alma deseniyle aynı: DB satırı
            // yazılamazsa, az önce yüklenen nesne sahipsiz kalmasın.
            imageStorageService.deleteImages(stored);
            throw e;
        }

        maybeTriggerAnalysis(post);

        return new Result(mediaRow.getId(), storageKey, mediaRow.getProcessingState().name());
    }

    /**
     * {@code analysisPublisher.publish} kendi transaction'ını
     * ({@code REQUIRES_NEW}) açar — bu metod hâlâ AÇIK olan {@code attach()}
     * transaction'ı içinden senkron çağrılırsa, yeni transaction az önce
     * burada yazılan (henüz COMMIT OLMAMIŞ) {@code storageKey}'i asla
     * göremez ve analiz sessizce atlanır (gerçek bir üretim hatası olarak
     * bulundu — bkz. commit geçmişi). Bu yüzden tetikleme, mevcut
     * transaction COMMIT OLDUKTAN SONRAYA ertelenir; o noktada gönderiyi
     * ID'sinden taze okuyup öyle yayınlar.
     */
    private void maybeTriggerAnalysis(ExternalSourcePost post) {
        long storedCount = mediaRepository.countByPostAndProcessingState(
                post, ExternalSourceMedia.ProcessingState.STORED);
        if (post.getMediaCount() > 0 && storedCount >= post.getMediaCount()) {
            log.info("Gönderi {} için tüm medya ({}) depolandı, analiz tetikleniyor",
                    post.getId(), storedCount);
            Long postId = post.getId();
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    postRepository.findById(postId).ifPresent(analysisPublisher::publish);
                }
            });
        }
    }
}
