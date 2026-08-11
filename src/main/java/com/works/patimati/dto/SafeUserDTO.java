package com.works.patimati.dto;

public record SafeUserDTO(
        Long uid,
        String firstName,
        String lastName,
        String email,
        String role,
        int lostPoints,
        int adoptionPoints,
        int lostBadgeLevel,
        int adoptionBadgeLevel
) {
}
