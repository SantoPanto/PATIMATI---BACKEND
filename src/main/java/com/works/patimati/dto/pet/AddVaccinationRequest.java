package com.works.patimati.dto.pet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AddVaccinationRequest(
        @NotBlank(message = "Aşı adı zorunludur")
        @Size(max = 200, message = "Aşı adı en fazla 200 karakter olabilir")
        String vaccineName,

        @NotNull(message = "Uygulama tarihi zorunludur")
        @PastOrPresent(message = "Uygulama tarihi gelecekte olamaz")
        LocalDate administeredDate,

        LocalDate nextDueDate,

        @Size(max = 500, message = "Not en fazla 500 karakter olabilir")
        String notes
) {
}
