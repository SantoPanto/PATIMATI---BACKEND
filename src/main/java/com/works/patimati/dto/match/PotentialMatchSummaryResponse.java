package com.works.patimati.dto.match;

import java.time.Instant;

/**
 * Kullanıcının kendi olası eşleşmeleri listesindeki tek bir satır.
 *
 * <p><b>Dil kesinlik iddia etmez</b> (sözleşme §7 kural 3 / Faz 1 kural #11):
 * {@code status = CONFIRMED} yalnızca "kullanıcı bunu onayladı" demektir,
 * "bu kesinlikle aynı hayvan" demek DEĞİLDİR — bu yüzden ne bu DTO'da ne de
 * hiçbir alan adında "confirmedMatch" gibi gerçeklik iddia eden bir isim
 * kullanılmaz.
 *
 * @param status PENDING | NOTIFIED | VIEWED | REJECTED | CONFIRMED | EXPIRED | NOTIFICATION_FAILED
 */
public record PotentialMatchSummaryResponse(
        Long recipientId,
        Long matchId,
        String status,
        double finalScore,
        Instant createdAt,
        Counterparty counterparty
) {
    /**
     * Karşı taraf — native bir ilan ya da bir Instagram kaydı.
     *
     * <p>{@code kind} alanına göre yalnızca ilgili alt küme dolu olur.
     * EXTERNAL için {@code sourceUrl} dolu ve frontend'in bunu PatiMati
     * ilanıymış gibi GÖSTERMEMESİ gerekir (Faz 1 kural #1/#2) — "Instagram'da
     * görüntüle" bağlantısı olarak sunulmalıdır.
     */
    public record Counterparty(
            String kind, // "AD" | "EXTERNAL"
            Long id,
            String title,
            String photoUrl,
            String adType, // yalnızca AD
            String category, // yalnızca EXTERNAL
            String species, // yalnızca EXTERNAL
            String breed, // yalnızca EXTERNAL
            String sourceUrl // yalnızca EXTERNAL — Instagram canonical_url
    ) {
    }
}
