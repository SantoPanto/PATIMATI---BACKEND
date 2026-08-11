package com.works.patimati.dto.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Long uid;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String phone;
    private boolean enabled;
    private Double latitude;
    private Double longitude;
    private int lostPoints;
    private int adoptionPoints;
    private int lostBadgeLevel;
    private int adoptionBadgeLevel;
}