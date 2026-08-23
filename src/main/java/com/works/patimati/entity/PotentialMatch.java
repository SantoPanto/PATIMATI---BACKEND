package com.works.patimati.entity;

import com.works.patimati.entity.external.ExternalPetRecord;
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
 * Bir hayvan çiftinin eşleştiği <b>gerçeği</b> — kime bildirileceğiyle
 * ilgisi yoktur, o {@link PotentialMatchRecipient}'ta yaşar.
 *
 * <p>Faz 2 revize blueprint §2/§3'te düzeltilen tasarım: taslak sürüm
 * {@code source_ad_id}'yi hem "eşleşen çift" hem de "kime bildirilecek"
 * anlamında kullanıyordu — bu, native↔native eşleşmelerde iki sahipten
 * birinin kaybolmasına yol açabilirdi. Artık bu sınıf yalnızca KİMLİKtir:
 *
 * <ul>
 *   <li>AD↔AD: {@code adAId}/{@code adBId} kanonik sırada (küçük id önce) —
 *       hangi ilanın analizi eşleşmeyi bulduğuna bakılmaksızın DAİMA aynı
 *       satıra düşer (bkz. ilgili migration'daki LEAST/GREATEST indeksi).</li>
 *   <li>AD↔EXTERNAL: {@code adAId} tek ilan, {@code externalRecordId} dolu,
 *       {@code adBId} null.</li>
 * </ul>
 */
@Entity
@Table(name = "potential_matches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PotentialMatch {

    public enum CandidateKind {
        AD,
        EXTERNAL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(name = "candidate_kind", nullable = false, length = 16)
    private CandidateKind candidateKind;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_a_id", nullable = false)
    private Ad adA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_b_id")
    private Ad adB;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "external_record_id")
    private ExternalPetRecord externalRecord;

    @Column(name = "visual_score", nullable = false)
    private float visualScore;

    @Column(name = "label_score", nullable = false)
    private float labelScore;

    @Column(name = "location_score", nullable = false)
    private float locationScore;

    @Column(name = "final_score", nullable = false)
    private float finalScore;

    @Column(name = "matching_version", nullable = false, length = 64)
    private String matchingVersion;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
