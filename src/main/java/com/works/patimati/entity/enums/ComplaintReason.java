package com.works.patimati.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Locale;

public enum ComplaintReason {
    SAHTE_ILAN,
    UYGUNSUZ_ICERIK,
    DOLANDIRICILIK,
    KOTU_DIL_KULLANIMI,
    DIGER;

    @JsonCreator
    public static ComplaintReason fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ENGLISH);
        for (ComplaintReason reason : ComplaintReason.values()) {
            if (reason.name().equalsIgnoreCase(normalized)) {
                return reason;
            }
        }
        throw new IllegalArgumentException(
                "Geçersiz şikayet sebebi: '" + value + "'. Geçerli değerler: " + Arrays.toString(ComplaintReason.values())
        );
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}

