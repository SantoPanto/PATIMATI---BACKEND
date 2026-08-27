package com.works.patimati.dto.vet;

import com.works.patimati.entity.enums.AnimalType;

import java.util.Set;

/**
 * Herkese açık veteriner dizini öğesi -- sahip/kullanıcı bilgisi İÇERMEZ,
 * yalnızca kartın kendisi.
 */
public record VetClinicPublicResponse(
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
        Set<AnimalType> animalTypes,
        /** Yorum yoksa {@code null} -- ön yüz "henüz değerlendirme yok" diyebilsin diye {@code 0.0} DEĞİL. */
        Double averageRating,
        int reviewCount,
        /** Müşteri isteği göndermek için hedef ({@code User.uid}) -- klinik kartının kendi {@code id}'sinden FARKLI. */
        Long vetUserId
) {
}
