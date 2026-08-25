package com.works.patimati.dto;

public record UserStatusResponse(
        Long userId,
        boolean isOnline
) {
}
