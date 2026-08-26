package com.works.patimati.dto.vet;

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
        /** Müşteri isteği göndermek için hedef ({@code User.uid}) -- klinik kartının kendi {@code id}'sinden FARKLI. */
        Long vetUserId
) {
}
