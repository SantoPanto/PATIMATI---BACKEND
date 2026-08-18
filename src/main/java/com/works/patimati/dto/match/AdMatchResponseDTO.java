package com.works.patimati.dto.match;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Frontend istemcilerine eşleşme verilerini güvenli ve şeffaf şekilde sunan yanıt DTO nesnesi.
 *
 * <p>Oturum açan kullanıcının ilanı {@code myAd}, karşı tarafın ilanı {@code partnerAd} olarak
 * dinamik biçimlendirilir.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdMatchResponseDTO {

    private Long id;

    // Oturum Açan Kullanıcının İlanı (myAd)
    private AdSummaryDTO myAd;
    private Long myAdId;
    private String myAdTitle;

    // Eşleşen Karşı Tarafın İlanı (partnerAd)
    private AdSummaryDTO partnerAd;
    private Long partnerAdId;
    private String partnerAdTitle;

    // Skor Kırılımları
    private Double totalScore;
    private Double visualScore;
    private Double tagScore;
    private Double locationScore;
    private Double thresholdAtTime;

    // Detaylar & Durumlar
    private String matchedPhotoPair;
    private String blockReason;
    private boolean passedThreshold;
    private Instant notificationSentAt;
    private Instant createdAt;

    /**
     * İlan özet bilgilerini tutan iç DTO sınıfı.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdSummaryDTO {
        private Long id;
        private String title;
        private String photoUrl;
        private String species;
        private String breed;
    }
}
