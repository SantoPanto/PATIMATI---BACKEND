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
 * Bir kullanıcının bir barınağa verdiği 1-5 yıldız + yorum -- {@link VetClinicReview}'ın
 * birebir aynısı, {@code vetClinic} yerine {@code shelter}. Çift (shelter,
 * author) başına TEK satır (UNIQUE, V36) -- upsert edilir, yeni yorum atmak
 * yerine mevcut yorum güncellenir.
 */
@Entity
@Table(name = "shelter_reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShelterReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shelter_id", nullable = false)
    private Shelter shelter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 1000)
    private String comment;

    // VetClinicReview ile AYNI desen: createdAt istemci taraflı (yanıt hemen
    // döner), updatedAt'i her güncellemede servis katmanı yeniler.
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
