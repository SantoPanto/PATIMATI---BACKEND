package com.works.patimati.dto.alert;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Uyarı aboneliği kaydet/güncelle isteği. Uç tek ve idempotenttir (PUT):
 * ön yüz her kaydetmede tam durumu gönderir, satır yoksa oluşturulur.
 *
 * <p>Yarıçap KM cinsinden alınır (kullanıcının gördüğü birim), metreye
 * çevrimi servis yapar. 1-50 km sınırının veritabanı katındaki eşi V21'in
 * CHECK kısıtıdır.
 */
public record AlertSubscriptionUpsertRequest(

        @NotNull(message = "Enlem zorunludur")
        @DecimalMin(value = "-90", message = "Enlem -90 ile 90 arasında olmalıdır")
        @DecimalMax(value = "90", message = "Enlem -90 ile 90 arasında olmalıdır")
        BigDecimal latitude,

        @NotNull(message = "Boylam zorunludur")
        @DecimalMin(value = "-180", message = "Boylam -180 ile 180 arasında olmalıdır")
        @DecimalMax(value = "180", message = "Boylam -180 ile 180 arasında olmalıdır")
        BigDecimal longitude,

        @NotNull(message = "Yarıçap zorunludur")
        @DecimalMin(value = "1", message = "Yarıçap 1 ile 50 km arasında olmalıdır")
        @DecimalMax(value = "50", message = "Yarıçap 1 ile 50 km arasında olmalıdır")
        BigDecimal radiusKm,

        @NotNull(message = "Açık/kapalı bilgisi zorunludur")
        Boolean enabled
) {
}
