package com.works.patimati.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank
    private String token;

    /**
     * Kayıt formuyla (RegisterRequest) AYNI şifre politikası. Eskiden 6-20 ve
     * desensizdi: sıfırlama yoluyla, kayıt formunun reddedeceği bir şifre
     * konabiliyordu.
     */
    @NotBlank
    @Size(
            min = 8,
            max = 20,
            message = "Şifre 8-20 karakter arasında olmalıdır"
    )
    @Pattern(
            regexp = "^(?=.*[A-ZÇĞİÖŞÜ])(?=.*[a-zçğıöşü])(?=.*\\d).+$",
            message = "Şifre en az bir büyük harf, bir küçük harf ve bir rakam içermelidir"
    )
    private String newPassword;
}
