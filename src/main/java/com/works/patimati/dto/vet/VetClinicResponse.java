package com.works.patimati.dto.vet;

import com.works.patimati.entity.enums.AnimalType;

import java.util.Set;

/** Vet'in kendi klinik kartı görünümü -- {@code GET/PUT /api/vet/clinic}. */
public record VetClinicResponse(
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
        int reviewCount
) {
}
