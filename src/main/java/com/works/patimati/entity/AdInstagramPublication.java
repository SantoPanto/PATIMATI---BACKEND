package com.works.patimati.entity;

import com.works.patimati.entity.enums.InstagramPublishStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Bir ilanın PatiMati'nin Instagram hesabında paylaşılma kuyruğu/geçmişi.
 *
 * <p>İlan başına en fazla bir satır -- {@code queueForReview} yalnızca ilan
 * oluşturulduğunda bir kez ekler ({@code status=PENDING}); admin
 * yayınladığında/atladığında AYNI satır güncellenir ({@code PUBLISHED} /
 * {@code SKIPPED} / {@code FAILED}). {@code FAILED} sonrası admin tekrar
 * denerse yine bu satır güncellenir -- ayrı bir deneme geçmişi tutulmaz,
 * yalnızca son durum (kullanıcı kararı, {@code AdService.reanalyzeAd}'ın
 * "yeniden dene" deseniyle AYNI basitlik).
 */
@Entity
@Table(name = "ad_instagram_publications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdInstagramPublication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_id", nullable = false)
    private Ad ad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private InstagramPublishStatus status = InstagramPublishStatus.PENDING;

    @Column(name = "suggested_caption", columnDefinition = "TEXT")
    private String suggestedCaption;

    @Column(name = "final_caption", columnDefinition = "TEXT")
    private String finalCaption;

    @Column(name = "ig_media_id", length = 64)
    private String igMediaId;

    @Column(name = "ig_permalink", length = 512)
    private String igPermalink;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    @Column(name = "published_by_admin_id")
    private Long publishedByAdminId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
