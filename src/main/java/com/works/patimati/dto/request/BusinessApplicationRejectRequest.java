package com.works.patimati.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BusinessApplicationRejectRequest(
        @NotBlank(message = "Red sebebi zorunludur")
        @Size(max = 500, message = "Red sebebi en fazla 500 karakter olabilir")
        String reason
) {
}
