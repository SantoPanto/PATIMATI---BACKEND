package com.works.patimati.dto.pet;

import com.works.patimati.entity.enums.AgeGroup;
import com.works.patimati.entity.enums.PetGender;
import com.works.patimati.entity.enums.Species;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Hayvan ekle/güncelle isteği ("Evcil Hayvanlarım"). */
public record PetUpsertRequest(

        @NotBlank(message = "Hayvanın adı zorunludur")
        @Size(max = 100, message = "İsim en fazla 100 karakter olabilir")
        String name,

        @NotNull(message = "Tür zorunludur")
        Species species,

        @Size(max = 100, message = "Irk en fazla 100 karakter olabilir")
        String breed,

        PetGender gender,

        AgeGroup ageGroup,

        @PastOrPresent(message = "Doğum tarihi gelecekte olamaz")
        LocalDate birthDate,

        Boolean sterilized,

        @Size(max = 50, message = "Mikroçip no en fazla 50 karakter olabilir")
        String microchipNumber,

        @Size(max = 1000, message = "Kronik hastalık/alerji bilgisi en fazla 1000 karakter olabilir")
        String chronicConditions,

        @Size(max = 1000, message = "Alerji bilgisi en fazla 1000 karakter olabilir")
        String allergies
) {
}
