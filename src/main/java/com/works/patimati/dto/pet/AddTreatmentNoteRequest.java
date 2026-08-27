package com.works.patimati.dto.pet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddTreatmentNoteRequest(
        @NotBlank(message = "Not içeriği boş olamaz")
        @Size(max = 4000, message = "Not en fazla 4000 karakter olabilir")
        String content
) {
}
