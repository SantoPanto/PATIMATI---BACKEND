package com.works.patimati.dto;

import lombok.Data;

@Data
public class GoogleAuthRequest {
    private String idToken;
    private String email;
    private String googleId;
    private String fullName;
    private String fcmToken;
}