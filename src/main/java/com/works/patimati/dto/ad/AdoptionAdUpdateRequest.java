package com.works.patimati.dto.ad;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.works.patimati.entity.enums.AgeGroup;
import com.works.patimati.entity.enums.CoatPattern;
import com.works.patimati.entity.enums.EyeColor;
import com.works.patimati.entity.enums.PetColor;
import com.works.patimati.entity.enums.PetGender;
import com.works.patimati.entity.enums.Species;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Sahiplendirme ilanı güncelleme isteği DTO'su.
 */
public record AdoptionAdUpdateRequest(
        @NotBlank(message = "Başlık zorunludur")
        @Size(max = 150, message = "Başlık en fazla 150 karakter olabilir")
        String title,

        @Size(max = 3000, message = "Açıklama en fazla 3000 karakter olabilir")
        String description,

        @NotNull(message = "Tür zorunludur")
        Species species,

        @NotBlank(message = "Irk/Cins zorunludur")
        @Size(max = 100, message = "Irk en fazla 100 karakter olabilir")
        String breed,

        @NotNull(message = "Cinsiyet zorunludur")
        PetGender gender,

        @NotNull(message = "Yaş grubu zorunludur")
        AgeGroup ageGroup,

        @Size(max = 9, message = "En fazla 9 renk seçilebilir")
        Set<PetColor> colors,

        CoatPattern coatPattern,
        EyeColor eyeColor,

        @Size(max = 32, message = "Mikroçip numarası en fazla 32 karakter olabilir")
        String microchipNumber,

        @NotNull(message = "Enlem (Latitude) zorunludur")
        @DecimalMin(value = "-90.0", message = "Enlem en az -90 olabilir")
        @DecimalMax(value = "90.0", message = "Enlem en fazla 90 olabilir")
        BigDecimal latitude,

        @NotNull(message = "Boylam (Longitude) zorunludur")
        @DecimalMin(value = "-180.0", message = "Boylam en az -180 olabilir")
        @DecimalMax(value = "180.0", message = "Boylam en fazla 180 olabilir")
        BigDecimal longitude
) {
    @JsonIgnore
    @AssertTrue(message = "Tür kedi (CAT) veya köpek (DOG) olmalıdır")
    public boolean isSupportedSpecies() {
        return species == Species.CAT || species == Species.DOG;
    }
}
