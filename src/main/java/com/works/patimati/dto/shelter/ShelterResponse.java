package com.works.patimati.dto.shelter;

/**
 * Barınak sahibinin kendi kart görünümü -- {@code GET/PUT /api/shelter/card}.
 * {@code VetClinicResponse} eksi {@code animalTypes} -- {@code PetShopResponse}'un
 * aksine {@code averageRating}/{@code reviewCount} VAR (barınak seviyesinde
 * puanlama, bkz. plan kapsam kararları).
 */
public record ShelterResponse(
        Long id,
        String name,
        String address,
        String city,
        String district,
        String phone,
        String workingHours,
        String photoUrl,
        Double latitude,
        Double longitude,
        /** Yorum yoksa {@code null} -- ön yüz "henüz değerlendirme yok" diyebilsin diye {@code 0.0} DEĞİL. */
        Double averageRating,
        int reviewCount
) {
}
