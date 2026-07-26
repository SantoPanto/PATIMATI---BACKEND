package com.works.patimati.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleAuthRequest {

    @NotBlank
    private String idToken;

    @NotBlank
    private String email;

    @NotBlank
    private String googleId;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    // Bildirim (Push Notification) izni verilmeme ihtimaline karşı
    // FCM Token null veya boş gelebilir. Bu yüzden doğrulama koymuyoruz.
    private String fcmToken;
}