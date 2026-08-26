package com.works.patimati.dto.pet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** "Ben Neyim?" raporunun ham JSON'unu ({@code JSON.stringify}) hayvana kaydeder. */
public record SaveAiReportRequest(
        @NotBlank(message = "Rapor içeriği boş olamaz")
        @Size(max = 8000, message = "Rapor çok büyük")
        String reportJson
) {
}
