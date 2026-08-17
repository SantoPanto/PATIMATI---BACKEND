package com.works.patimati.entity.external;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
 * Bir carousel fotoğrafının izleme kaydı — Faz 2 revize blueprint §4 medya
 * transfer sırasını uygular.
 *
 * <p>{@code sourceReference} yalnızca denetim amaçlıdır ve asla yeniden
 * indirilmez; gerçek erişim {@code storageKey} üzerinden, bizim S3
 * kovamızdan yapılır. {@code contentSha256}, Collector'ın aynı fotoğrafı
 * iki kez yüklemesini engelleyen idempotency anahtarıdır.
 */
@Entity
@Table(name = "external_source_media")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalSourceMedia {

    public enum ProcessingState {
        PENDING,
        STORED,
        FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "external_source_post_id", nullable = false)
    private ExternalSourcePost post;

    @Column(nullable = false)
    private short ordinal;

    @Column(name = "media_type", nullable = false, length = 16)
    @Builder.Default
    private String mediaType = "IMAGE";

    @Column(name = "source_reference", length = 512)
    private String sourceReference;

    @Column(name = "storage_key", length = 512)
    private String storageKey;

    @Column(name = "content_sha256", length = 64)
    private String contentSha256;

    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(name = "processing_state", nullable = false, length = 16)
    @Builder.Default
    private ProcessingState processingState = ProcessingState.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
