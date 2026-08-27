package com.works.patimati.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.works.patimati.entity.enums.AnimalType;
import com.works.patimati.entity.enums.BusinessType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * İşletme sahibi olma başvurusu -- {@code VetClinicUpsertRequest} ile aynı
 * iş-bilgisi alanları + hangi işletme türüne başvurulduğu ({@link #businessType()}).
 */
public record BusinessApplicationCreateRequest(

        @NotNull(message = "İşletme türü zorunludur")
        BusinessType businessType,

        @NotBlank(message = "İşletme adı zorunludur")
        @Size(max = 200, message = "İşletme adı en fazla 200 karakter olabilir")
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
        BigDecimal longitude,

        @Size(max = 10, message = "En fazla 10 hayvan türü seçilebilir")
        Set<AnimalType> animalTypes
) {
    public BusinessApplicationCreateRequest {
        if (animalTypes == null) {
            animalTypes = new LinkedHashSet<>();
        }
    }

    @JsonIgnore
    @AssertTrue(message = "Enlem ve boylam birlikte gönderilmeli ya da hiç gönderilmemeli")
    public boolean isLocationConsistent() {
        return (latitude == null) == (longitude == null);
    }
}
