package com.works.patimati.entity.enums;

import com.works.patimati.entity.Ad;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AiStatusTest {

    @Test
    @DisplayName("AiStatus enum geçerli değerleri tam olarak içermeli ve silinen atıl değerleri barındırmamalı")
    void shouldContainOnlyValidValues() {
        AiStatus[] values = AiStatus.values();

        assertThat(values)
                .containsExactlyInAnyOrder(
                        AiStatus.PENDING,
                        AiStatus.DONE,
                        AiStatus.FAILED,
                        AiStatus.NOT_APPLICABLE
                );
    }

    @Test
    @DisplayName("APPROVED ve REJECTED değerleri AiStatus içinden silinmiş olmalı ve valueOf ile erişilememeli")
    void shouldNotContainApprovedOrRejected() {
        assertThrows(IllegalArgumentException.class, () -> AiStatus.valueOf("APPROVED"));
        assertThrows(IllegalArgumentException.class, () -> AiStatus.valueOf("REJECTED"));
    }

    @Test
    @DisplayName("NOT_APPLICABLE durumu bir ilana başarıyla atanabilmeli")
    void shouldAssignNotApplicableStatusToAd() {
        Ad ad = Ad.builder()
                .title("Sahiplendirme İlanı")
                .aiStatus(AiStatus.NOT_APPLICABLE)
                .build();

        assertThat(ad.getAiStatus()).isEqualTo(AiStatus.NOT_APPLICABLE);
    }
}
