package com.works.patimati.dto;

public record SafeUserDTO(
        Long uid,
        String firstName,
        String lastName,
        String email,
        String role
) {
}
