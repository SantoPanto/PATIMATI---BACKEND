package com.works.patimati.dto.ad;

/**
 * İlanın afiş gizlilik ve KVKK ayarlarını döndüren DTO.
 */
public record PosterSettingsResponse(
        Long adId,
        Boolean isPosterAllowed,
        Boolean showEmailOnPoster,
        Boolean showPhoneOnPoster
) {
}
