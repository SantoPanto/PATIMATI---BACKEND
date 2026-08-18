package com.works.patimati.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * AI eşleştirme sonuçlarının ve detaylı skor analizlerinin kalıcı olarak saklandığı varlık sınıfı.
 *
 * <p>Domain Driven Design (DDD) prensiplerine uygun olarak tasarlanmış olup,
 * eşleştirme şeffaflığı, düşük skor analizlerinin tutulması ve bildirim spam koruması sağlar.
 *
 * <p>Veritabanı seviyesinde {@code (user_id, source_ad_id, matched_ad_id)} üzerinde benzersiz kısıt (Unique Constraint)
 * tanımlanarak aynı kullanıcı için aynı eşleşme çiftinin birden fazla kaydı engellenir.
 */
@Entity
@Table(
        name = "ad_match",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ad_match_user_source_matched",
                        columnNames = {"user_id", "source_ad_id", "matched_ad_id"}
                )
        },
        indexes = {
                @Index(name = "idx_ad_match_user_score", columnList = "user_id, total_score DESC"),
                @Index(name = "idx_ad_match_passed_threshold", columnList = "passed_threshold")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Eşleşme sonucunun bildirileceği / ilgilendirdiği kullanıcı.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Eşleştirmeye konu olan ana/kaynak ilan.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_ad_id", nullable = false)
    private Ad sourceAd;

    /**
     * Kaynak ilan ile eşleşen hedef ilan.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "matched_ad_id", nullable = false)
    private Ad matchedAd;

    /**
     * AI tarafından hesaplanan nihai toplam uyum skoru (0.0 - 1.0 veya 0 - 100).
     */
    @Column(name = "total_score", nullable = false)
    private Double totalScore;

    /**
     * Vektörel görsel benzerlik skoru.
     */
    @Column(name = "visual_score")
    private Double visualScore;

    /**
     * Etiket ve öznitelik örtüşme skoru.
     */
    @Column(name = "tag_score")
    private Double tagScore;

    /**
     * Coğrafi konum yakınlık skoru.
     */
    @Column(name = "location_score")
    private Double locationScore;

    /**
     * Eşleştirme yapıldığı andaki sistem bildirim eşik değeri.
     * Eşik değerleri zamanla güncellenebileceği için tarihsel analiz açısından kritiktir.
     */
    @Column(name = "threshold_at_time", nullable = false)
    private Double thresholdAtTime;

    /**
     * Eşleşen fotoğraf çiftine ait referans bilgisi (örneğin URL çifti veya JSON formatı).
     */
    @Column(name = "matched_photo_pair", length = 500)
    private String matchedPhotoPair;

    /**
     * Eşleşmenin engellenme veya elenme sebebi (örn: "SPECIES_MISMATCH", "DISTANCE_OUT_OF_RANGE").
     * Eşik geçildiyse null olabilir.
     */
    @Column(name = "block_reason", length = 255)
    private String blockReason;

    /**
     * Hesaplanan skorun o anki eşik değerini geçip geçmediği bilgisi.
     * Düşük skorlu adayların da saklanmasını sağlar.
     */
    @Column(name = "passed_threshold", nullable = false)
    private boolean passedThreshold;

    /**
     * Kullanıcıya bildirim gönderildiği zaman damgası (Instant).
     * Boş (null) olması henüz bildirim gönderilmediğini ifade eder ve bildirim spam'ini önler.
     */
    @Column(name = "notification_sent_at")
    private Instant notificationSentAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // --- DDD Domain Davranışları (Rich Domain Entity) ---

    /**
     * Kullanıcıya bildirim gönderildiğini varlık üzerinde işaretler.
     */
    public void markNotificationSent() {
        this.notificationSentAt = Instant.now();
    }

    /**
     * Var olan eşleşmenin skorlarını ve analiz metriklerini günceller.
     *
     * @param totalScore nihai skor
     * @param visualScore görsel skor
     * @param tagScore etiket skoru
     * @param locationScore konum skoru
     * @param thresholdAtTime o anki eşik
     * @param matchedPhotoPair eşleşen fotoğraf çifti
     * @param blockReason engellenme sebebi
     * @param passedThreshold eşiği geçti mi
     */
    public void updateMetrics(
            Double totalScore,
            Double visualScore,
            Double tagScore,
            Double locationScore,
            Double thresholdAtTime,
            String matchedPhotoPair,
            String blockReason,
            boolean passedThreshold
    ) {
        this.totalScore = totalScore;
        this.visualScore = visualScore;
        this.tagScore = tagScore;
        this.locationScore = locationScore;
        this.thresholdAtTime = thresholdAtTime;
        this.matchedPhotoPair = matchedPhotoPair;
        this.blockReason = blockReason;
        this.passedThreshold = passedThreshold;
    }
}
