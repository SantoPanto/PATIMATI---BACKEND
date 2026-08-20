package com.works.patimati.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    @NotBlank(message = "Mevcut şifre boş olamaz.")
    private String oldPassword;

    @NotBlank(message = "Yeni şifre boş olamaz.")
    @Size(min = 6, max = 100, message = "Yeni şifre en az 6 karakter olmalıdır.")
    private String newPassword;
}
