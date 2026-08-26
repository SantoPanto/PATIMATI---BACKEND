package com.works.patimati.dto.petshop;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Ürün görünümü -- sahibin kendi listesinde ve herkese açık dizinde ORTAK kullanılır. */
public record PetShopProductResponse(
        Long id,
        Long petShopId,
        String name,
        String description,
        BigDecimal price,
        String photoUrl,
        /** Yorum yoksa {@code null} -- ön yüz "henüz değerlendirme yok" diyebilsin diye {@code 0.0} DEĞİL. */
        Double averageRating,
        int reviewCount,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
