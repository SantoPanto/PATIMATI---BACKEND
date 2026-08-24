package com.works.patimati.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Giriş yapmış kullanıcının şifre değiştirme isteğini taşır.
 *
 * Entity doğrudan dışarı açılmaz. İstemcinin gönderebileceği
 * alanlar yalnızca bu DTO üzerinden belirlenir.
 */
@Data
public class ChangePasswordRequest {

    /**
     * Kullanıcının kimliğini doğrulamak için mevcut şifre istenir.
     * Eski zayıf şifreli kullanıcılar için bu alanda katı boy/desen doğrulaması
     * yapılmaz; yalnızca boş geçilemez kuralı uygulanır.
     */
    @NotBlank(message = "Mevcut şifre boş bırakılamaz")
    @com.fasterxml.jackson.annotation.JsonAlias({"oldPassword", "currentPassword"})
    private String currentPassword;

    public String getOldPassword() {
        return currentPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.currentPassword = oldPassword;
    }

    /**
     * Yeni şifre, Frontend'de kullanılan güvenlik kurallarıyla
     * aynı şekilde doğrulanır.
     */
    @NotBlank(message = "Yeni şifre boş bırakılamaz")
    @Size(
            min = 8,
            max = 20,
            message = "Yeni şifre 8-20 karakter arasında olmalıdır"
    )
    @Pattern(
            regexp = "^(?=.*[A-ZÇĞİÖŞÜ])(?=.*[a-zçğıöşü])(?=.*\\d).+$",
            message = "Yeni şifre en az bir büyük harf, bir küçük harf ve bir rakam içermelidir"
    )
    private String newPassword;

    /**
     * Yazım hatalarını engellemek için yeni şifre tekrarı alınabilir.
     */
    private String confirmPassword;

    public String getConfirmPassword() {
        return confirmPassword != null ? confirmPassword : newPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
