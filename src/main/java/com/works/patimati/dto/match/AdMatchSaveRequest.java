package com.works.patimati.dto.match;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI eşleştirme pipeline'ından veya eşleştirme servisinden gelen yeni eşleşme kaydetme/güncelleme istek DTO'su.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdMatchSaveRequest {

    @NotNull(message = "Kaynak ilan ID boş olamaz")
    private Long sourceAdId;

    @NotNull(message = "Eşleşen ilan ID boş olamaz")
    private Long matchedAdId;

    /**
     * Eşleşmenin atanacağı hedef kullanıcı ID (opsiyoneldir, verilmezse kaynak ilanın sahibine atanır).
     */
    private Long userId;

    @NotNull(message = "Toplam skor boş olamaz")
    private Double totalScore;

    private Double visualScore;
    private Double tagScore;
    private Double locationScore;

    @NotNull(message = "Threshold zaman değeri boş olamaz")
    private Double thresholdAtTime;

    private String matchedPhotoPair;
    private String blockReason;
    private boolean passedThreshold;
}
