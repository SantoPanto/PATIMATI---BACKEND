package com.works.patimati.dto.petshop;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Petshop bilgi kartı kaydet/güncelle isteği -- {@code VetClinicUpsertRequest}
 * eksi {@code animalTypes} (petshop için kapsam dışı). Uç tek ve
 * idempotenttir (PUT): kart yoksa oluşturulur, varsa güncellenir.
 *
 * <p>{@code latitude}/{@code longitude} OPSİYONELDİR -- kart önce konumsuz
 * kaydedilebilmeli. İkisi birlikte gönderilmeli ya da hiç gönderilmemeli
 * ({@link #isLocationConsistent()}).
 */
public record PetShopUpsertRequest(

        @NotBlank(message = "Petshop adı zorunludur")
        @Size(max = 200, message = "Petshop adı en fazla 200 karakter olabilir")
        String name,

        @NotBlank(message = "Adres zorunludur")
        @Size(max = 500, message = "Adres en fazla 500 karakter olabilir")
        String address,

        @NotBlank(message = "İl zorunludur")
        @Size(max = 100, message = "İl en fazla 100 karakter olabilir")
        String city,

        @Size(max = 100, message = "İlçe en fazla 100 karakter olabilir")
        String district,

        @NotBlank(message = "Telefon zorunludur")
        @Size(max = 30, message = "Telefon en fazla 30 karakter olabilir")
        String phone,

        @Size(max = 300, message = "Çalışma saatleri en fazla 300 karakter olabilir")
        String workingHours,

        @DecimalMin(value = "-90.0", message = "Enlem en az -90 olabilir")
        @DecimalMax(value = "90.0", message = "Enlem en fazla 90 olabilir")
        BigDecimal latitude,

        @DecimalMin(value = "-180.0", message = "Boylam en az -180 olabilir")
        @DecimalMax(value = "180.0", message = "Boylam en fazla 180 olabilir")
        BigDecimal longitude
) {
    @JsonIgnore
    @AssertTrue(message = "Enlem ve boylam birlikte gönderilmeli ya da hiç gönderilmemeli")
    public boolean isLocationConsistent() {
        return (latitude == null) == (longitude == null);
    }
}
