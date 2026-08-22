package com.works.patimati.external;

import com.works.patimati.ai.AiRabbitConfig;
import com.works.patimati.ai.dto.AiAnalysisRequest;
import com.works.patimati.entity.enums.ExternalProcessingStatus;
import com.works.patimati.entity.external.ExternalPetRecord;
import com.works.patimati.entity.external.ExternalSourceEvent;
import com.works.patimati.entity.external.ExternalSourceMedia;
import com.works.patimati.entity.external.ExternalSourcePost;
import com.works.patimati.repository.external.ExternalPetRecordRepository;
import com.works.patimati.repository.external.ExternalSourceEventRepository;
import com.works.patimati.repository.external.ExternalSourceMediaRepository;
import com.works.patimati.repository.external.ExternalSourcePostRepository;
import com.works.patimati.storage.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * AŞAMA 1 — yalnızca analiz (Faz 2 revize blueprint §1/§2). Adaylar HER ZAMAN
 * boş gönderilir: bir external kayıt henüz analiz edilmeden kategori/tür
 * bilinemez, dolayısıyla bu aşamada aday süzmesi YAPILMAZ — taslaktaki
 * dairesel bağımlılığın düzeltildiği yer burasıdır.
 *
 * <p>Mevcut {@code ai.analysis.request}/{@code result} sözleşmesini olduğu
 * gibi kullanır (yeni kuyruk YOK); yalnızca {@code source}/
 * {@code external_record_id}/{@code caption}/{@code triggering_comment}
 * alanları eklenmiştir.
 */
@Service
@RequiredArgsConstructor
public class ExternalAnalysisPublisher {

    private static final Logger log = LoggerFactory.getLogger(ExternalAnalysisPublisher.class);

    /** Python tarafı ad_type'ı eşleştirme kararı için KULLANMIYOR (süzme Java'da yapılıyor,
     * bkz. sözleşme §5) — bu yüzden burada zararsız bir varsayılan gönderiliyor. Aşama 1'de
     * candidates boş olduğu için zaten hiçbir eşleştirme denemesi yapılmaz. */
    private static final String HARMLESS_AD_TYPE_PLACEHOLDER = "LOST";

    private final RabbitTemplate aiRabbitTemplate;
    private final ExternalPetRecordRepository externalPetRecordRepository;
    private final ExternalSourceMediaRepository mediaRepository;
    private final ExternalSourceEventRepository eventRepository;
    private final ExternalSourcePostRepository postRepository;
    private final ImageStorageService imageStorageService;

    /**
     * Analiz kuyruğuna gönderir. Kaydedilmiş medya sayısı beklenenle
     * eşleştiğinde {@code ExternalMediaIngestionService} tarafından çağrılır.
     *
     * <p>Native {@code AiAnalysisPublisher.publish} gibi hiçbir zaman istisna
     * fırlatmaz — AI bir ektir.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publish(ExternalSourcePost post) {
        try {
            ExternalPetRecord record = externalPetRecordRepository
                    .findByPostAndPetIndex(post, (short) 0)
                    .orElseGet(() -> externalPetRecordRepository.save(
                            ExternalPetRecord.builder().post(post).petIndex((short) 0).build()));

            List<String> photoUrls = toDownloadableUrls(mediaRepository.findByPostOrderByOrdinalAsc(post));
            if (photoUrls.isEmpty()) {
                log.warn("Gönderi {} için indirilebilir fotoğraf adresi üretilemedi, analiz atlandı",
                        post.getId());
                return;
            }

            String triggeringComment = eventRepository
                    .findFirstByPostAndTriggeringCommentIsNotNullOrderByDetectedAtDesc(post)
                    .map(ExternalSourceEvent::getTriggeringComment)
                    .orElse(null);

            AiAnalysisRequest request = new AiAnalysisRequest(
                    AiRabbitConfig.SCHEMA_VERSION,
                    UUID.randomUUID().toString(),
                    null,
                    HARMLESS_AD_TYPE_PLACEHOLDER,
                    null,
                    photoUrls,
                    List.of(), // Aşama 1: adaylar HER ZAMAN boş.
                    "INSTAGRAM",
                    record.getId(),
                    post.getCaption(),
                    triggeringComment,
                    null,
                    true);

            aiRabbitTemplate.convertAndSend(
                    AiRabbitConfig.EXCHANGE,
                    AiRabbitConfig.REQUEST_ROUTING_KEY,
                    request,
                    message -> {
                        message.getMessageProperties().setDeliveryMode(
                                org.springframework.amqp.core.MessageDeliveryMode.PERSISTENT);
                        message.getMessageProperties().setCorrelationId(request.requestId());
                        return message;
                    });

            post.setProcessingStatus(ExternalProcessingStatus.ANALYZING);
            postRepository.save(post);

            log.info("External analiz isteği yayınlandı: postId={} externalRecordId={} fotoğraf={}",
                    post.getId(), record.getId(), photoUrls.size());

        } catch (Exception e) {
            log.error("External analiz isteği yayınlanamadı (postId={}): {}", post.getId(), e.getMessage(), e);
        }
    }

    /** {@code AiAnalysisPublisher.toDownloadableUrls} ile aynı gerekçe: ya hepsi ya hiçbiri. */
    private List<String> toDownloadableUrls(List<ExternalSourceMedia> media) {
        List<String> urls = new ArrayList<>(media.size());
        for (ExternalSourceMedia item : media) {
            if (item.getStorageKey() == null) {
                log.warn("Medya {} henüz depolanmamış (storageKey yok), analiz atlanıyor", item.getId());
                return List.of();
            }
            try {
                urls.add(imageStorageService.createTemporaryReadUrl(item.getStorageKey()));
            } catch (RuntimeException e) {
                log.warn("Fotoğraf adresi üretilemedi ({}): {}", item.getStorageKey(), e.getMessage());
                return List.of();
            }
        }
        return urls;
    }
}
