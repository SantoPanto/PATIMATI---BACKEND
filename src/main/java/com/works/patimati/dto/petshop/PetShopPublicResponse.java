package com.works.patimati.dto.petshop;

/**
 * Herkese açık petshop dizini öğesi -- sahip/kullanıcı bilgisi İÇERMEZ,
 * yalnızca kartın kendisi. Vet'teki {@code vetUserId}'nin karşılığı YOK --
 * dizin kartından hedeflenen bir "müşteri isteği" eylemi olmadığı için
 * gerekmiyor (YAGNI). Dükkan seviyesinde puanlama {@link PetShopReview} ile
 * eklendi -- {@code ShelterPublicResponse} ile AYNI desen.
 */
public record PetShopPublicResponse(
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
