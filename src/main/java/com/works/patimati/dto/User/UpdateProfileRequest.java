package com.works.patimati.dto.User;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String city;

    @DecimalMin(value = "-90.0", message = "Enlem (latitude) -90 ile 90 arasında olmalıdır")
    @DecimalMax(value = "90.0", message = "Enlem (latitude) -90 ile 90 arasında olmalıdır")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "Boylam (longitude) -180 ile 180 arasında olmalıdır")
    @DecimalMax(value = "180.0", message = "Boylam (longitude) -180 ile 180 arasında olmalıdır")
    private Double longitude;
}