package com.works.patimati.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Bir petshop'a ait tek bir ürün -- {@code petshop_id} UNIQUE DEĞİL (bir
 * dükkanın çok ürünü olur). Puanlama/yorum ürün BAZINDA yapılır (dükkan
 * bazında DEĞİL, bkz. {@link PetShopProductReview}).
 */
@Entity
@Table(name = "petshop_products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetShopProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "petshop_id", nullable = false)
    private PetShop petShop;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /** Kalıcı depolama referansı ({@code ImageStorageService}) -- ham URL değil. */
    @Column(name = "photo_reference", length = 500)
    private String photoReference;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    private OffsetDateTime updatedAt;
}
