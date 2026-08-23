package com.works.patimati.external;

import com.works.patimati.entity.enums.ExternalProcessingStatus;
import com.works.patimati.entity.enums.ExternalSource;
import com.works.patimati.entity.enums.TriggerType;
import com.works.patimati.entity.external.ExternalSourceEvent;
import com.works.patimati.entity.external.ExternalSourceMedia;
import com.works.patimati.entity.external.ExternalSourcePost;
import com.works.patimati.external.dto.ExternalIngestionEvent;
import com.works.patimati.repository.external.ExternalSourceEventRepository;
import com.works.patimati.repository.external.ExternalSourceMediaRepository;
import com.works.patimati.repository.external.ExternalSourcePostRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Collector'dan gelen ham tespit olayını işler (Faz 2 revize blueprint §5).
 *
 * <p>Yalnızca kanonik gönderi + tetikleyici olay kayıtlarını tutar; hiçbir
 * AI kararı vermez, hiçbir eşleştirme yapmaz, hiçbir PatiMati ilanı
 * OLUŞTURMAZ (Faz 1 kural #1/#2/#14).
 *
 * <p><b>Medya doğrulaması ilk iş:</b> {@code media_count}, {@code media[]}
 * uzunluğuyla uyuşmuyorsa ya da ordinal'ler tekil/aralık dışıysa, bu
 * ÜRETİCİNİN hatasıdır (Collector'ın hatası) — geçici bir arıza değil.
 * Mesaj ACK edilir (yeniden kuyruğa KONULMAZ) ve gönderi FAILED işaretlenir;
 * aksi hâlde gönderi sonsuza kadar medya bekler (§5/§27).
 */
@Component
@RequiredArgsConstructor
public class ExternalIngestionListener {

    private static final Logger log = LoggerFactory.getLogger(ExternalIngestionListener.class);

    private final ExternalSourcePostRepository postRepository;
    private final ExternalSourceEventRepository eventRepository;
    private final ExternalSourceMediaRepository mediaRepository;

    @RabbitListener(queues = ExternalRabbitConfig.INGESTION_QUEUE,
                    containerFactory = "externalListenerContainerFactory")
    @Transactional
    public void onEvent(ExternalIngestionEvent event) {
        if (event == null || event.source() == null || event.sourcePostId() == null) {
            log.error("Geçersiz external ingestion mesajı, atlandı: {}", event);
            return;
        }

        ExternalSource source;
        try {
            source = ExternalSource.valueOf(event.source());
        } catch (IllegalArgumentException e) {
            log.error("Bilinmeyen source: {}", event.source());
            return;
        }

        if (!isMediaDescriptorValid(event)) {
            log.error("Medya tanımlayıcı tutarsız (media_count={}, media.size={}), sourcePostId={} — "
                            + "gönderi FAILED işaretlenip mesaj ACK edilecek (yeniden kuyruğa konulmayacak)",
                    event.mediaCount(), event.media() == null ? 0 : event.media().size(), event.sourcePostId());
            markInvalid(source, event);
            return;
        }

        ExternalSourcePost post = postRepository.findBySourceAndSourcePostId(source, event.sourcePostId())
                .orElseGet(() -> newPost(source, event));

        applyMetadata(post, event);
        post = postRepository.save(post);

        recordEvent(post, event);
        ensurePlaceholderMedia(post, event);

        log.info("External ingestion event işlendi: source={} sourcePostId={} tip={} durum={}",
                source, event.sourcePostId(), event.detectionType(), post.getProcessingStatus());
    }

    /** {@code media_count == media.length}, ordinal'ler [0, media_count) aralığında ve tekil. */
    private boolean isMediaDescriptorValid(ExternalIngestionEvent event) {
        List<ExternalIngestionEvent.MediaDescriptor> media = event.media() == null ? List.of() : event.media();
        if (media.size() != event.mediaCount()) {
            return false;
        }
        Set<Integer> seen = new HashSet<>();
        for (ExternalIngestionEvent.MediaDescriptor descriptor : media) {
            if (descriptor.ordinal() < 0 || descriptor.ordinal() >= event.mediaCount()) {
                return false;
            }
            if (!seen.add(descriptor.ordinal())) {
                return false;
            }
        }
        return true;
    }

    private void markInvalid(ExternalSource source, ExternalIngestionEvent event) {
        ExternalSourcePost post = postRepository.findBySourceAndSourcePostId(source, event.sourcePostId())
                .orElseGet(() -> newPost(source, event));
        post.setProcessingStatus(ExternalProcessingStatus.FAILED);
        post.setFailureReason("media_count (" + event.mediaCount() + ") ile media[] tutarsız veya ordinal hatası");
        postRepository.save(post);
    }

    private ExternalSourcePost newPost(ExternalSource source, ExternalIngestionEvent event) {
        return ExternalSourcePost.builder()
                .source(source)
                .sourcePostId(event.sourcePostId())
                .canonicalUrl(event.canonicalUrl())
                .detectedAt(event.detectedAt() != null ? event.detectedAt() : Instant.now())
                .build();
    }

    /** Yeniden teslim durumunda satır çoğalmaz — mevcut satır güncellenir (idempotent-by-overwrite). */
    private void applyMetadata(ExternalSourcePost post, ExternalIngestionEvent event) {
        post.setCanonicalUrl(event.canonicalUrl());
        post.setAuthorUsername(event.authorUsername());
        post.setCaption(event.caption());
        post.setPublishedAt(event.publishedAt());
        post.setLocationText(event.locationText());
        post.setMediaCount((short) event.mediaCount());
        if (post.getProcessingStatus() == ExternalProcessingStatus.DISCOVERED) {
            post.setProcessingStatus(ExternalProcessingStatus.COLLECTED);
        }
    }

    /**
     * Aynı gönderi POST_TAG + CAPTION_MENTION + COMMENT_MENTION üçüyle
     * tespit edilse bile TEK {@link ExternalSourcePost} kalır — her tespit
     * ayrı bir event satırı olur (§18/§95). Tekillik burada da (DB'deki
     * kısmi indekslerin aynısı) kontrol edilir ki gereksiz exception yerine
     * sessizce atlanmış olsun.
     */
    private void recordEvent(ExternalSourcePost post, ExternalIngestionEvent event) {
        TriggerType type;
        try {
            type = TriggerType.valueOf(event.detectionType());
        } catch (IllegalArgumentException e) {
            log.error("Bilinmeyen detection_type: {}", event.detectionType());
            return;
        }

        if (type == TriggerType.COMMENT_MENTION) {
            if (event.externalTriggerId() != null
                    && eventRepository.existsByPostAndExternalTriggerId(post, event.externalTriggerId())) {
                return;
            }
        } else if (eventRepository.existsByPostAndTriggerType(post, type)) {
            return;
        }

        eventRepository.save(ExternalSourceEvent.builder()
                .post(post)
                .triggerType(type)
                .externalTriggerId(event.externalTriggerId())
                .triggeringComment(event.triggeringComment())
                .build());
    }

    private void ensurePlaceholderMedia(ExternalSourcePost post, ExternalIngestionEvent event) {
        for (ExternalIngestionEvent.MediaDescriptor descriptor : event.media()) {
            short ordinal = (short) descriptor.ordinal();
            if (mediaRepository.findByPostAndOrdinal(post, ordinal).isPresent()) {
                continue;
            }
            mediaRepository.save(ExternalSourceMedia.builder()
                    .post(post)
                    .ordinal(ordinal)
                    .sourceReference(descriptor.sourceReference())
                    .build());
        }
    }
}
