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
 * Bir kullanıcının bir petshop'a verdiği 1-5 yıldız + yorum -- {@link ShelterReview}'ın
 * birebir aynısı, {@code shelter} yerine {@code petShop}. Çift (petShop,
 * author) başına TEK satır (UNIQUE, V37) -- upsert edilir, yeni yorum atmak
 * yerine mevcut yorum güncellenir. {@link PetShopProductReview} ile
 * KARIŞTIRILMAMALI: bu, ürün değil DÜKKANIN kendisi için verilen puan.
 */
@Entity
@Table(name = "petshop_reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetShopReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "petshop_id", nullable = false)
    private PetShop petShop;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 1000)
    private String comment;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
