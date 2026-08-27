package com.works.patimati.dto.petshop;

/**
 * Petshop sahibinin kendi kart görünümü -- {@code GET/PUT /api/petshop/card}.
 * {@code VetClinicResponse} eksi {@code animalTypes}. Dükkan seviyesinde
 * puanlama {@link PetShopReview} ile eklendi -- {@code ShelterResponse} ile
 * AYNI desen ({@code averageRating}/{@code reviewCount}).
 */
public record PetShopResponse(
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
