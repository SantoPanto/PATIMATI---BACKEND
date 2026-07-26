package com.works.patimati.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotEmpty
    @NotNull
    @Size(min = 2, max = 40, message = "First Name must be between 2 and 40 characters")
    private String firstName;
    @NotEmpty
    @NotNull
    @Size(min = 2, max = 40, message = "Last Name must be between 2 and 40 characters")
    private String lastName;
    @NotEmpty
    @NotNull
    @Size(min = 5, max = 50, message = "Email must be between 5 and 50 characters")
    @Email(message = "Email should be valid")
    private String email;
    @NotEmpty
    @NotNull
    @Size(min = 6, max = 20, message = "Password must be between 6 and 20 characters")
    private String password;
    @NotNull
    @Size(min = 9, max = 15)
    @NotEmpty
    @Pattern(regexp = "^(?:\\+90\\d{10}|0\\s?\\d{3}\\s?\\d{3}\\s?\\d{2}\\s?\\d{2}|\\d{10})(?:\\s\\+\\d+)?$", message = "Phone number format fail")
    String phone;
}