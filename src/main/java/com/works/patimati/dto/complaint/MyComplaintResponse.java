package com.works.patimati.dto.complaint;

import com.works.patimati.entity.enums.ComplaintReason;
import com.works.patimati.entity.enums.ComplaintStatus;

import java.time.Instant;

/**
 * "Şikayetlerim" listesinin tek satırı (S7, 27.08).
 *
 * <p>Üç ayrı şikayet tablosu (kullanıcı / ilan / sahiplendirme) tek listede
 * birleşir; {@code tur} hangi tablodan geldiğini söyler, {@code hedefId} o
 * türün hedefidir (KULLANICI → şikayet edilen kullanıcı, diğerleri → ilan).
 * Ekran hedef başlığını ayrıca çekmez — ILAN/SAHIPLENDIRME satırları ilana
 * bağlantı verir.
 */
public record MyComplaintResponse(
        Long id,
        /** "KULLANICI" | "ILAN" | "SAHIPLENDIRME" */
        String tur,
        Long hedefId,
        ComplaintReason reason,
        String description,
        ComplaintStatus status,
        Instant createdAt
) {
}
