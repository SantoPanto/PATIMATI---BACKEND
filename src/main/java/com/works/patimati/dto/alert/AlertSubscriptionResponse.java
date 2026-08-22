package com.works.patimati.dto.alert;

import java.math.BigDecimal;

/** Uyarı aboneliğinin ön yüze dönen hâli; yarıçap KM cinsindendir. */
public record AlertSubscriptionResponse(
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal radiusKm,
        boolean enabled
) {
}
