package com.works.patimati.dto.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

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
    private Point location;
}