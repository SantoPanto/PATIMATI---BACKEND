package com.works.patimati.entity;

import com.works.patimati.entity.enums.ComplaintReason;
import com.works.patimati.entity.enums.ComplaintStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Sahiplendirme İlanı Şikayeti Entity'si (Adoption Complaint Domain Entity).
 *
 * DDD (Domain-Driven Design) ve Decoupled Tables prensiplerine uygun olarak:
 * 1. Veritabanı tablosu 'adoption_complaints' olarak tamamen ayrılmıştır.
 * 2. JPA ilişki bağı yerine primitive 'adId' ve 'reporterId' alanları kullanılmıştır.
 */
@Entity
@Table(
        name = "adoption_complaints",
        indexes = {
                @Index(name = "idx_adoption_complaint_reporter_ad_status", columnList = "reporter_id, ad_id, status"),
                @Index(name = "idx_adoption_complaint_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdoptionComplaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Şikayet eden kullanıcının benzersiz kimlik numarası (uid).
     */
    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    /**
     * Şikayet edilen sahiplendirme ilanın ID'si.
     */
    @Column(name = "ad_id", nullable = false)
    private Long adId;

    /**
     * Şikayet sebebi (Enum).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ComplaintReason reason;

    /**
     * Şikayet açıklaması / detay metni.
     */
    @Column(length = 1000, nullable = false)
    private String description;

    /**
     * Şikayet durumu (BEKLEMEDE, INCELEMEDE, COZULDU vb.).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ComplaintStatus status = ComplaintStatus.BEKLEMEDE;

    /**
     * Şikayet kayıt tarihi.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
