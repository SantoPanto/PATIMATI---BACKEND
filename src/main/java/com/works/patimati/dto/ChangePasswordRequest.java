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
     */
    @NotBlank(message = "Mevcut şifre boş bırakılamaz")
    @Size(
            min = 6,
            max = 20,
            message = "Mevcut şifre 6-20 karakter arasında olmalıdır"
    )
    private String currentPassword;

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
     * Yazım hatalarını engellemek için yeni şifre tekrar alınır.
     */
    @NotBlank(message = "Yeni şifre tekrarı boş bırakılamaz")
    @Size(
            min = 8,
            max = 20,
            message = "Yeni şifre tekrarı 8-20 karakter arasında olmalıdır"
    )
    private String confirmPassword;
}
