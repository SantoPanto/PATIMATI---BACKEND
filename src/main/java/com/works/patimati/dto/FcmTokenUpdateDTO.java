package com.works.patimati.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FcmTokenUpdateDTO {

    @NotBlank(message = "FCM token boş olamaz.")
    private String fcmToken;
}
