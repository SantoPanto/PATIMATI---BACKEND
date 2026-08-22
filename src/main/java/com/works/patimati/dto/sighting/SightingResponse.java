package com.works.patimati.dto.sighting;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Görülmenin dışarı dönen hâli. {@code photoUrl} geçici okuma URL'sidir
 * (kalıcı depolama referansı dışarı sızmaz — AdResponse ile aynı ilke).
 */
public record SightingResponse(
        Long id,
        BigDecimal latitude,
        BigDecimal longitude,
        String note,
        String reporterContact,
        String photoUrl,
        OffsetDateTime createdAt
) {
}
