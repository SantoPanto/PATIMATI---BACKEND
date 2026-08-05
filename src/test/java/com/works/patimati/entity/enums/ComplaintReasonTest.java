package com.works.patimati.entity.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ComplaintReasonTest {

    @ParameterizedTest
    @ValueSource(strings = {"SAHTE_ILAN", "sahte_ilan", "Sahte_Ilan", " sahte_ilan "})
    @DisplayName("Farklı harf boyutları ve boşluklarla gelen sahte_ilan string'i SAHTE_ILAN enum'ına dönüşmeli")
    void shouldParseSahteIlanCaseInsensitively(String input) {
        ComplaintReason result = ComplaintReason.fromString(input);
        assertEquals(ComplaintReason.SAHTE_ILAN, result);
    }

    @Test
    @DisplayName("Null veya boş string null dönmeli")
    void shouldReturnNullForBlankOrNullInput() {
        assertNull(ComplaintReason.fromString(null));
        assertNull(ComplaintReason.fromString("   "));
    }

    @Test
    @DisplayName("Geçersiz enum string'inde açıklayıcı IllegalArgumentException fırlatılmalı")
    void shouldThrowIllegalArgumentExceptionForInvalidReason() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ComplaintReason.fromString("GECERSIZ_NEDEN")
        );

        assertTrue(exception.getMessage().contains("Geçersiz şikayet sebebi: 'GECERSIZ_NEDEN'"));
        assertTrue(exception.getMessage().contains("SAHTE_ILAN"));
    }
}
