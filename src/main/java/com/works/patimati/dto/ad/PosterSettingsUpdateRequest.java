package com.works.patimati.dto.ad;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * İlan sahibinin afiş gizlilik ve KVKK izin ayarlarını güncelleme isteği DTO'su.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PosterSettingsUpdateRequest {

    /**
     * İlan afişinin (PDF/görsel) oluşturulmasına izin verilip verilmediği.
     */
    private Boolean isPosterAllowed;

    /**
     * Afiş üzerinde ilan sahibinin e-posta adresinin gösterilip gösterilmeyeceği.
     */
    private Boolean showEmailOnPoster;

    /**
     * Afiş üzerinde ilan sahibinin telefon numarasının gösterilip gösterilmeyeceği.
     */
    private Boolean showPhoneOnPoster;
}
