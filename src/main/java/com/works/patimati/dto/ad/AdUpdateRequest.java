package com.works.patimati.dto.ad;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.enums.AgeGroup;
import com.works.patimati.entity.enums.CoatPattern;
import com.works.patimati.entity.enums.EyeColor;
import com.works.patimati.entity.enums.PetColor;
import com.works.patimati.entity.enums.PetGender;
import com.works.patimati.entity.enums.PresenceStatus;
import com.works.patimati.entity.enums.Species;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public record AdUpdateRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 150, message = "Title can contain at most 150 characters")
        String title,

        @Size(max = 3000, message = "Description can contain at most 3000 characters")
        String description,

        @NotNull(message = "Ad type is required")
        Ad.AdType adType,

        @NotNull(message = "Species is required")
        Species species,

        @Size(max = 100, message = "Breed can contain at most 100 characters")
        String breed,

        @com.fasterxml.jackson.annotation.JsonAlias({"color", "colors"})
        @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.works.patimati.jackson.PetColorSetDeserializer.class)
        @Size(max = 9, message = "At most 9 colors can be selected")
        Set<PetColor> colors,

        PetGender gender,
        AgeGroup ageGroup,
        CoatPattern coatPattern,
        PresenceStatus collarStatus,
        PetColor collarColor,

        @Size(max = 255, message = "Collar tag text can contain at most 255 characters")
        String collarTagText,

        EyeColor eyeColor,
        PresenceStatus earTagStatus,
        PresenceStatus earNotchStatus,

        @Size(max = 32, message = "Microchip number can contain at most 32 characters")
        String microchipNumber,

        @jakarta.validation.constraints.Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Tarih formatı yyyy-MM-dd olmalıdır")
        @com.fasterxml.jackson.annotation.JsonAlias({"date", "eventDate", "incidentDate"})
        String lostDate,

        @Size(max = 1000, message = "Distinctive marks can contain at most 1000 characters")
        String distinctiveMarks,

        @NotNull(message = "Latitude is required")
        @DecimalMin(value = "-90.0", message = "Latitude must be at least -90")
        @DecimalMax(value = "90.0", message = "Latitude must be at most 90")
        BigDecimal latitude,

        @NotNull(message = "Longitude is required")
        @DecimalMin(value = "-180.0", message = "Longitude must be at least -180")
        @DecimalMax(value = "180.0", message = "Longitude must be at most 180")
        BigDecimal longitude
) {

    @JsonIgnore
    @AssertTrue(message = "Species must be CAT or DOG")
    public boolean isSupportedSpecies() {
        return species == Species.CAT || species == Species.DOG;
    }
}
