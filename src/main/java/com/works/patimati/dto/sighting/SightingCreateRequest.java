package com.works.patimati.dto.sighting;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * "Gördüm" bildirimi bırakma isteği (multipart'ın "sighting" parçası;
 * opsiyonel foto ayrı "photo" parçasında gelir).
 *
 * <p>İletişim zorunlu: bildirimi girişsiz ziyaretçi de bırakabildiği için
 * sahibinin bildirene ulaşabileceği tek yol bu alandır.
 */
public record SightingCreateRequest(

        @NotNull(message = "Enlem zorunludur")
        @DecimalMin(value = "-90", message = "Enlem -90 ile 90 arasında olmalıdır")
        @DecimalMax(value = "90", message = "Enlem -90 ile 90 arasında olmalıdır")
        BigDecimal latitude,

        @NotNull(message = "Boylam zorunludur")
        @DecimalMin(value = "-180", message = "Boylam -180 ile 180 arasında olmalıdır")
        @DecimalMax(value = "180", message = "Boylam -180 ile 180 arasında olmalıdır")
        BigDecimal longitude,

        @Size(max = 1000, message = "Not en fazla 1000 karakter olabilir")
        String note,

        @NotBlank(message = "İletişim bilgisi zorunludur (telefon ya da e-posta)")
        @Size(min = 5, max = 255, message = "İletişim bilgisi 5-255 karakter olmalıdır")
        String reporterContact
) {
}
