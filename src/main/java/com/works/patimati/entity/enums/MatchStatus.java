package com.works.patimati.entity.enums;

/**
 * potential_match_recipients.status — bir alıcı için bildirim/karar durumu.
 *
 * <p>Bilinçli olarak match kimliğinde ({@code PotentialMatch}) değil, alıcı
 * satırında ({@code PotentialMatchRecipient}) yaşar: native↔native bir
 * eşleşmede iki alıcının durumu birbirinden tamamen bağımsızdır — biri
 * CONFIRMED derken diğeri henüz PENDING olabilir (Faz 2 revize blueprint §3).
 *
 * <p>{@code NOTIFICATION_FAILED}, sınırlı deneme sayısı tükendikten sonraki
 * gözlemlenebilir uç durumdur — {@code EXPIRED}'dan (iş kuralı zaman aşımı)
 * kasıtlı olarak ayrı: biri teslimat hatası, diğeri kullanıcı hiç bakmadı.
 */
public enum MatchStatus {
    PENDING,
    NOTIFIED,
    VIEWED,
    REJECTED,
    CONFIRMED,
    EXPIRED,
    NOTIFICATION_FAILED
}
