package com.works.patimati.dto;

/**
 * {@code /topic/user-status} üzerinden yayınlanan çevrimiçi/çevrimdışı olayı.
 * <p>
 * {@code lastSeen}, yalnızca {@code online=false} olduğunda (kullanıcının SON
 * oturumu da kapandığında) doldurulur.
 */
public record UserStatusEvent(
        Long userId,
        boolean online,
        String status,
        String lastSeen
) {
}
