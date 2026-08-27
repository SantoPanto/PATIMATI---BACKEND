package com.works.patimati.dto.vet;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Klinik değerlendirme kaydet/güncelle isteği. Uç tek ve idempotenttir
 * (PUT): yorum yoksa oluşturulur, varsa güncellenir (yazar başına tek satır).
 */
public record VetClinicReviewUpsertRequest(
        @NotNull(message = "Puan zorunludur")
        @Min(value = 1, message = "Puan en az 1 olabilir")
        @Max(value = 5, message = "Puan en fazla 5 olabilir")
        Integer rating,

        @Size(max = 1000, message = "Yorum en fazla 1000 karakter olabilir")
        String comment
) {
}
