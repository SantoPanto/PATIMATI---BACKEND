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

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import jakarta.validation.constraints.PastOrPresent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/**
 * Sahiplendirme ilanı oluşturma isteği DTO'su.
 */
public record AdoptionAdCreateRequest(
        @NotBlank(message = "İlan başlığı boş bırakılamaz")
        @Size(max = 150, message = "Başlık en fazla 150 karakter olabilir")
        String title,

        @Size(max = 3000, message = "Açıklama en fazla 3000 karakter olabilir")
        String description,

        @NotNull(message = "Tür zorunludur")
        Species species,

        @Size(max = 100, message = "Irk en fazla 100 karakter olabilir")
        String breed,


        PetGender gender,


        AgeGroup ageGroup,

        @com.fasterxml.jackson.annotation.JsonAlias({"color", "colors"})
        @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.works.patimati.jackson.PetColorSetDeserializer.class)
        @Size(max = 9, message = "En fazla 9 renk seçilebilir")
        Set<PetColor> colors,

        CoatPattern coatPattern,
        EyeColor eyeColor,

        @Size(max = 32, message = "Mikroçip numarası en fazla 32 karakter olabilir")
        String microchipNumber,

        @NotBlank(message = "Tarih alanı boş bırakılamaz")
        @jakarta.validation.constraints.Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Tarih formatı yyyy-MM-dd olmalıdır")
        @JsonAlias({"lostDate", "eventDate", "incidentDate"})
        String date,

        @NotNull(message = "Enlem (Latitude) zorunludur")
        @DecimalMin(value = "-90.0", message = "Enlem en az -90 olabilir")
        @DecimalMax(value = "90.0", message = "Enlem en fazla 90 olabilir")
        BigDecimal latitude,

        @NotNull(message = "Boylam (Longitude) zorunludur")
        @DecimalMin(value = "-180.0", message = "Boylam en az -180 olabilir")
        @DecimalMax(value = "180.0", message = "Boylam en fazla 180 olabilir")
        BigDecimal longitude,

        /**
         * İsteğe bağlı il/ilçe beyanı — sahiplendirme formu zaten soruyor.
         * Verilirse ters geokodlamaya hiç gidilmez; verilmezse sunucu
         * koordinattan çözmeyi dener (V19).
         */
        @Size(max = 100, message = "İl en fazla 100 karakter olabilir")
        String city,

        @Size(max = 100, message = "İlçe en fazla 100 karakter olabilir")
        String district
) {
    @JsonIgnore
    @AssertTrue(message = "Tür kedi (CAT) veya köpek (DOG) olmalıdır")
    public boolean isSupportedSpecies() {
        return species == null || species == Species.CAT || species == Species.DOG;
    }
}
