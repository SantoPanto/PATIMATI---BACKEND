package com.works.patimati.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 40, message = "First Name must be between 2 and 40 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 40, message = "Last Name must be between 2 and 40 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Size(min = 5, max = 50, message = "Email must be between 5 and 50 characters")
    @Email(message = "Email should be valid")
    private String email;

//    @NotBlank(message = "Password is required")
//    @Size(min = 6, max = 20, message = "Password must be between 6 and 20 characters")
//    private String password;

    /**
     * Kayıt sırasında gönderilen şifrenin frontend ile aynı
     * güvenlik kurallarını sağlaması gerekir.
     */
    @NotBlank(message = "Password is required")
    @Size(
            min = 8,
            max = 20,
            message = "The password must be between 8 and 20 characters long"
    )
    @Pattern(
            regexp = "^(?=.*[A-ZÇĞİÖŞÜ])(?=.*[a-zçğıöşü])(?=.*\\d).+$",
            message = "The password must contain at least one uppercase letter, one lowercase letter, and one number."
    )
    private String password;

    @NotBlank(message = "Telefon numarası boş bırakılamaz")
    @Size(min = 9, max = 15, message = "Phone number must be between 9 and 15 characters")
    @Pattern(regexp = "^(?:\\+90\\d{10}|0\\s?\\d{3}\\s?\\d{3}\\s?\\d{2}\\s?\\d{2}|\\d{10})(?:\\s\\+\\d+)?$", message = "Phone number format fail")
    private String phone;
}