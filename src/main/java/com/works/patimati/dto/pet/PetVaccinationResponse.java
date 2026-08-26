package com.works.patimati.dto.pet;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record PetVaccinationResponse(
        Long id,
        String vaccineName,
        LocalDate administeredDate,
        LocalDate nextDueDate,
        String notes,
        String recordedByName,
        OffsetDateTime createdAt
) {
}
