package com.works.patimati.dto.match;

import com.works.patimati.dto.ad.AdResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI eşleştirme sonucunda eşleşen ilanı ve benzerlik skorunu tutan DTO yanıt nesnesi.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchedAdResponseDTO {

    /**
     * AI tarafından hesaplanan benzerlik skoru (0.0 - 1.0 arası).
     */
    private Double score;

    /**
     * Eşleşen ilanın tam bilgileri ve mutlak/geçici görsel URL'lerini içeren DTO.
     */
    private AdResponse ad;
}
