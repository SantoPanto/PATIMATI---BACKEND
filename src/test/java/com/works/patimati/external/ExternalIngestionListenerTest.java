package com.works.patimati.external;

import com.works.patimati.entity.enums.ExternalProcessingStatus;
import com.works.patimati.entity.enums.ExternalSource;
import com.works.patimati.entity.external.ExternalSourceEvent;
import com.works.patimati.entity.external.ExternalSourceMedia;
import com.works.patimati.entity.external.ExternalSourcePost;
import com.works.patimati.external.dto.ExternalIngestionEvent;
import com.works.patimati.repository.external.ExternalSourceEventRepository;
import com.works.patimati.repository.external.ExternalSourceMediaRepository;
import com.works.patimati.repository.external.ExternalSourcePostRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GERÇEK PostgreSQL'e karşı çalışır — kanonik kimlik/dedup, DB'deki kısmi
 * benzersiz indekslere dayanır (bkz. {@code PotentialMatchServiceTest}'teki
 * aynı gerekçe). {@code onEvent} doğrudan çağrılır, gerçek bir RabbitMQ
 * mesajı yayınlanmaz — {@code AiIsPetPersistedTest}'teki "dinleyiciyi
 * doğrudan çağır" deseniyle aynı.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "jwt.secret-key=dGhpcy1pcy1hLXRlc3Qtc2VjcmV0LWtleS1mb3ItcGF0aW1hdGktYXBwbGljYXRpb24=",
        // Flyway ACIK kalir (gercek SQL/migration soz dizimini dogrular);
        // yalnizca Hibernate'in acilis-sonrasi kati sema tip denetimi
        // kapatiliyor -- password_reset_tokens.id (V4, bu PR'dan once var,
        // canlida uygulanmis) SERIAL/BIGINT uyusmazligi bu testin konusu
        // DEGIL ve ayri ele aliniyor.
        "spring.jpa.hibernate.ddl-auto=none"
})
@Transactional
class ExternalIngestionListenerTest {

    @Autowired
    private ExternalIngestionListener listener;
    @Autowired
    private ExternalSourcePostRepository postRepository;
    @Autowired
    private ExternalSourceEventRepository eventRepository;
    @Autowired
    private ExternalSourceMediaRepository mediaRepository;

    @Test
    void samePostViaThreeTriggerTypesProducesOnePostAndThreeEvents() {
        String sourcePostId = "shortcode-" + System.nanoTime();

        listener.onEvent(validEvent(sourcePostId, "POST_TAG", null, null));
        listener.onEvent(validEvent(sourcePostId, "CAPTION_MENTION", null, null));
        listener.onEvent(validEvent(sourcePostId, "COMMENT_MENTION", "comment-1", "@patimati bakar mısınız"));

        ExternalSourcePost post = postRepository
                .findBySourceAndSourcePostId(ExternalSource.INSTAGRAM, sourcePostId)
                .orElseThrow();

        List<ExternalSourceEvent> events = eventRepository.findAll().stream()
                .filter(e -> e.getPost().getId().equals(post.getId()))
                .toList();

        assertThat(events)
                .withFailMessage("3 farklı tetikleyici tipi, kanonik gönderi başına 3 event üretmeli, 1 üretti/çoğaldı")
                .hasSize(3);
    }

    @Test
    void redeliveringIdenticalCommentTriggerIsNoOp() {
        String sourcePostId = "shortcode-" + System.nanoTime();
        ExternalIngestionEvent event = validEvent(sourcePostId, "COMMENT_MENTION", "comment-dup", "aynı yorum");

        listener.onEvent(event);
        listener.onEvent(event); // RabbitMQ en-az-bir-kez teslim — aynı mesaj ikinci kez gelebilir.

        ExternalSourcePost post = postRepository
                .findBySourceAndSourcePostId(ExternalSource.INSTAGRAM, sourcePostId)
                .orElseThrow();

        List<ExternalSourceEvent> events = eventRepository.findAll().stream()
                .filter(e -> e.getPost().getId().equals(post.getId()))
                .toList();

        assertThat(events)
                .withFailMessage("Aynı mesajın tekrar teslimi ikinci bir event satırı üretmemeli")
                .hasSize(1);
    }

    @Test
    void mismatchedMediaCountFailsFastWithoutCreatingMediaRows() {
        String sourcePostId = "shortcode-" + System.nanoTime();
        ExternalIngestionEvent event = new ExternalIngestionEvent(
                1, "evt-" + System.nanoTime(), "INSTAGRAM", "POST_TAG",
                sourcePostId, "https://instagram.com/p/" + sourcePostId + "/",
                "someone", "kayıp kedi", null, null,
                Instant.now(), Instant.now(), "Bursa",
                3, // media_count = 3 iddia ediliyor
                List.of(new ExternalIngestionEvent.MediaDescriptor(0, "https://cdn.example/1.jpg")), // ama yalnızca 1 tanımlayıcı
                "corr-" + System.nanoTime());

        listener.onEvent(event);

        ExternalSourcePost post = postRepository
                .findBySourceAndSourcePostId(ExternalSource.INSTAGRAM, sourcePostId)
                .orElseThrow();

        assertThat(post.getProcessingStatus())
                .withFailMessage("media_count/media[] tutarsızlığında gönderi FAILED işaretlenmeli — "
                        + "sonsuza kadar MEDIA_STORED beklememeli")
                .isEqualTo(ExternalProcessingStatus.FAILED);

        List<ExternalSourceMedia> media = mediaRepository.findByPostOrderByOrdinalAsc(post);
        assertThat(media).isEmpty();
    }

    @Test
    void duplicateOrdinalFailsFastWithoutCreatingMediaRows() {
        String sourcePostId = "shortcode-" + System.nanoTime();
        ExternalIngestionEvent event = new ExternalIngestionEvent(
                1, "evt-" + System.nanoTime(), "INSTAGRAM", "POST_TAG",
                sourcePostId, "https://instagram.com/p/" + sourcePostId + "/",
                "someone", "kayıp kedi", null, null,
                Instant.now(), Instant.now(), "Bursa",
                2,
                List.of(
                        new ExternalIngestionEvent.MediaDescriptor(0, "https://cdn.example/1.jpg"),
                        new ExternalIngestionEvent.MediaDescriptor(0, "https://cdn.example/2.jpg")), // tekrar eden ordinal
                "corr-" + System.nanoTime());

        listener.onEvent(event);

        Optional<ExternalSourcePost> post =
                postRepository.findBySourceAndSourcePostId(ExternalSource.INSTAGRAM, sourcePostId);
        assertThat(post).isPresent();
        assertThat(post.get().getProcessingStatus()).isEqualTo(ExternalProcessingStatus.FAILED);
        assertThat(mediaRepository.findByPostOrderByOrdinalAsc(post.get())).isEmpty();
    }

    private ExternalIngestionEvent validEvent(String sourcePostId, String detectionType,
                                               String externalTriggerId, String comment) {
        return new ExternalIngestionEvent(
                1, "evt-" + System.nanoTime(), "INSTAGRAM", detectionType,
                sourcePostId, "https://instagram.com/p/" + sourcePostId + "/",
                "someone", "kayıp kedi", comment, externalTriggerId,
                Instant.now(), Instant.now(), "Bursa",
                2,
                List.of(
                        new ExternalIngestionEvent.MediaDescriptor(0, "https://cdn.example/1.jpg"),
                        new ExternalIngestionEvent.MediaDescriptor(1, "https://cdn.example/2.jpg")),
                "corr-" + System.nanoTime());
    }
}
