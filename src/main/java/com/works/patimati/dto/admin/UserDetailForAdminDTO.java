package com.works.patimati.dto.admin;

import com.works.patimati.entity.User;

import java.time.Instant;

/**
 * Admin paneli için kullanıcı detay transfer objesi.
 * Şifre veya token gibi hassas veriler KESİNLİKLE yer almamalıdır.
 */
public record UserDetailForAdminDTO(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        boolean isEmailVerified,
        boolean enabled,
        User.Role role,
        Instant createdAt
) {
}
