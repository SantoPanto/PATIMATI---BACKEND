package com.works.patimati.dto.shelter;

/**
 * Herkese açık barınak dizini öğesi -- sahip/kullanıcı bilgisi İÇERMEZ,
 * yalnızca kartın kendisi. {@code PetShopPublicResponse}'daki gibi hedeflenen
 * bir "müşteri isteği" alanı YOK -- iletişim gerekirse ilan sahibi zaten
 * {@code AdResponse} üzerinden görünür (bkz. plan kapsam kararları).
 */
public record ShelterPublicResponse(
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
