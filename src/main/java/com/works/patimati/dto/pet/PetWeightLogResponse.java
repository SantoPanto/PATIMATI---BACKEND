package com.works.patimati.dto.pet;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PetWeightLogResponse(
        Long id,
        BigDecimal weightKg,
        LocalDate recordedAt
) {
}
