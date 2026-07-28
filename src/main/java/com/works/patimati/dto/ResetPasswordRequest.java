package com.works.patimati.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank
    private String token;

    @NotBlank
    @Size(min = 6, max = 20, message = "Şifre 6-20 karakter arasında olmalıdır")
    private String newPassword;
}
