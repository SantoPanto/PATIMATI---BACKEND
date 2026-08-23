package com.works.patimati.entity.external;

import com.works.patimati.entity.Ad;
import com.works.patimati.entity.enums.AiStatus;
import com.works.patimati.entity.enums.ExternalCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * AI tarafından analiz edilmiş bir Instagram hayvan adayı — bu ASLA bir
 * PatiMati ilanı değildir (Faz 1 kural #1/#2). Bir {@link ExternalSourcePost}
 * ile 1'e N ilişkilidir (çoklu hayvan desteği için {@code petIndex} alanı
 * ayrılmıştır) ama MVP'de her gönderi için tam olarak bir kayıt üretilir
 * (Faz 2 revize blueprint §0 — ayrıştırma modeli kurulmadı, belirsiz
 * durumlar {@code needsReview} ile işaretlenir).
 *
 * <p>{@code aiEmbeddings}/{@code aiLabels}/{@code aiStatus} alanları bilinçli
 * olarak {@link Ad}'daki karşılıklarıyla AYNI şekle sahiptir — hatta
 * {@code aiEmbeddings} aynı {@link Ad.AiPhotoVector} tipini yeniden kullanır
 * ve {@code aiStatus} aynı {@link AiStatus} enum'unu — candidate gathering
 * katmanının iki tabloyu simetrik biçimde okuyabilmesi için.
 */
@Entity
@Table(name = "external_pet_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalPetRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "external_source_post_id", nullable = false)
    private ExternalSourcePost post;

    @Column(name = "pet_index", nullable = false)
    @Builder.Default
    private short petIndex = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Builder.Default
    private ExternalCategory category = ExternalCategory.UNCERTAIN;

    @Column(name = "category_confidence")
    private Float categoryConfidence;

    // AI'dan gelen serbest metin — Ad.aiSpecies/aiBreed ile aynı gerekçeyle
    // enum DEĞİL: AI'ın kelime dağarcığını zorlamak yanlış eşleştirmeye yol açar.
    @Column(length = 16)
    private String species;

    @Column(length = 64)
    private String breed;

    @Column(name = "breed_confidence")
    private Float breedConfidence;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> colors;

    @Column(length = 16)
    private String gender;

    @Column(name = "age_text", length = 64)
    private String ageText;

    @Column(name = "pet_name", length = 64)
    private String petName;

    @Column(name = "location_text", length = 256)
    private String locationText;

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point location;

    @Column(name = "location_confidence")
    private Float locationConfidence;

    @Column(name = "event_date")
    private LocalDate eventDate;

    /** Metindeki göreli ifadeden (örn. "3 gündür kayıp") türetildiyse true. */
    @Column(name = "event_date_estimated", nullable = false)
    @Builder.Default
    private boolean eventDateEstimated = false;

    @Column(name = "distinguishing_features", columnDefinition = "text")
    private String distinguishingFeatures;

    /** Çoklu hayvan belirsizliği ya da düşük güven — otomatik eşleştirmeye girmez. */
    @Column(name = "needs_review", nullable = false)
    @Builder.Default
    private boolean needsReview = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ai_embeddings", columnDefinition = "jsonb")
    private List<Ad.AiPhotoVector> aiEmbeddings;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ai_labels", columnDefinition = "jsonb")
    private List<String> aiLabels;

    @Column(name = "ai_is_pet")
    private Boolean aiIsPet;

    @Column(name = "ai_model_version", length = 64)
    private String aiModelVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "ai_status", nullable = false, length = 16)
    @Builder.Default
    private AiStatus aiStatus = AiStatus.PENDING;

    @Column(name = "ai_processed_at")
    private Instant aiProcessedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
