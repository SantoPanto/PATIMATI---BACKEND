package com.works.patimati.entity.external;

import com.works.patimati.entity.enums.ExternalProcessingStatus;
import com.works.patimati.entity.enums.ExternalSource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Bir Instagram gönderisinin kanonik kimliği (Faz 2 revize blueprint §2).
 *
 * <p>{@code (source, sourcePostId)} çifti benzersizdir — aynı gönderi
 * POST_TAG, CAPTION_MENTION ve COMMENT_MENTION üçüyle birden tespit edilse
 * bile tek satır kalır; her tespit ayrı bir {@link ExternalSourceEvent}
 * olarak tutulur.
 */
@Entity
@Table(name = "external_source_posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalSourcePost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ExternalSource source;

    @Column(name = "source_post_id", nullable = false, length = 64)
    private String sourcePostId;

    @Column(name = "canonical_url", nullable = false, length = 512)
    private String canonicalUrl;

    @Column(name = "author_username", length = 128)
    private String authorUsername;

    @Column(columnDefinition = "text")
    private String caption;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "detected_at", nullable = false)
    private Instant detectedAt;

    @Column(name = "location_text", length = 256)
    private String locationText;

    /** Collector'ın bildirdiği beklenen carousel uzunluğu — medya doğrulaması için. */
    @Column(name = "media_count", nullable = false)
    @Builder.Default
    private short mediaCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false, length = 16)
    @Builder.Default
    private ExternalProcessingStatus processingStatus = ExternalProcessingStatus.DISCOVERED;

    /** Gönderi silinmiş/erişilemez hâle gelmiş olabilir (blueprint §87). */
    @Column(name = "source_unavailable", nullable = false)
    @Builder.Default
    private boolean sourceUnavailable = false;

    @Column(name = "failure_reason", length = 512)
    private String failureReason;

    @Column(name = "analysis_version", length = 64)
    private String analysisVersion;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
