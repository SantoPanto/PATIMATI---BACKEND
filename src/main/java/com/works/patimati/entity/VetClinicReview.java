package com.works.patimati.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/**
 * Bir kullanıcının bir veteriner kliniğine verdiği 1-5 yıldız + yorum.
 * Çift (vetClinic, author) başına TEK satır (UNIQUE, V34) -- upsert edilir,
 * yeni yorum atmak yerine mevcut yorum güncellenir.
 */
@Entity
@Table(name = "vet_clinic_reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VetClinicReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vet_clinic_id", nullable = false)
    private VetClinic vetClinic;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 1000)
    private String comment;

    // AlertSubscription/VetClinic ile AYNI desen: createdAt istemci taraflı
    // (yanıt hemen döner), updatedAt'i her güncellemede servis katmanı yeniler.
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
