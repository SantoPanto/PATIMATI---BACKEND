package com.works.patimati.dto.pet;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AddWeightLogRequest(
        @NotNull(message = "Ağırlık zorunludur")
        @DecimalMin(value = "0.01", message = "Ağırlık 0'dan büyük olmalı")
        @DecimalMax(value = "999.99", message = "Ağırlık çok büyük")
        BigDecimal weightKg,

        @NotNull(message = "Tarih zorunludur")
        @PastOrPresent(message = "Tarih gelecekte olamaz")
        LocalDate recordedAt
) {
}
