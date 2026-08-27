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
 * Bir kullanıcının bir petshop ÜRÜNÜNE verdiği 1-5 yıldız + yorum (dükkan
 * bazında DEĞİL, ürün bazında). Çift (product, author) başına TEK satır
 * (UNIQUE, V35) -- upsert edilir, yeni yorum atmak yerine mevcut yorum
 * güncellenir. {@link VetClinicReview} ile birebir AYNI desen.
 */
@Entity
@Table(name = "petshop_product_reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetShopProductReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private PetShopProduct product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 1000)
    private String comment;

    // AlertSubscription/VetClinicReview ile AYNI desen: createdAt istemci
    // taraflı (yanıt hemen döner), updatedAt'i her güncellemede servis
    // katmanı yeniler.
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
