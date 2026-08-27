package com.works.patimati.dto.petshop;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Ürün oluştur/güncelle isteği. Uç tek ve idempotenttir (PUT/POST): yeni
 * ürün için POST, mevcut ürün için PUT (controller katmanında ayrılır).
 */
public record PetShopProductUpsertRequest(

        @NotBlank(message = "Ürün adı zorunludur")
        @Size(max = 200, message = "Ürün adı en fazla 200 karakter olabilir")
        String name,

        @Size(max = 1000, message = "Açıklama en fazla 1000 karakter olabilir")
        String description,

        @NotNull(message = "Fiyat zorunludur")
        @DecimalMin(value = "0.0", inclusive = false, message = "Fiyat 0'dan büyük olmalıdır")
        BigDecimal price
) {
}
