package com.works.patimati.dto.vet;

/** Vet'in kendi klinik kartı görünümü -- {@code GET/PUT /api/vet/clinic}. */
public record VetClinicResponse(
        Long id,
        String name,
        String address,
        String city,
        String district,
        String phone,
        String workingHours,
        String photoUrl
) {
}
