package com.works.patimati.dto.vet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Veteriner klinik bilgi kartı kaydet/güncelle isteği. Uç tek ve
 * idempotenttir (PUT): kart yoksa oluşturulur, varsa güncellenir.
 */
public record VetClinicUpsertRequest(

        @NotBlank(message = "Klinik adı zorunludur")
        @Size(max = 200, message = "Klinik adı en fazla 200 karakter olabilir")
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
        String workingHours
) {
}
