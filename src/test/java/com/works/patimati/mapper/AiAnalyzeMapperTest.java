package com.works.patimati.mapper;

import com.works.patimati.dto.ai.AiAnalyzeResponse;
import com.works.patimati.entity.enums.CoatPattern;
import com.works.patimati.entity.enums.PetColor;
import com.works.patimati.entity.enums.Species;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AiAnalyzeMapperTest {

    private final AiAnalyzeMapper mapper = new AiAnalyzeMapper();

    @Test
    @DisplayName("AI ham cevabı frontend sözleşmesine doğru dönüştürülmeli (cat, solid, cream/gray)")
    void aiResponseCorrectlyMappedToFrontendDto() {
        Map<String, Object> raw = new LinkedHashMap<>();
        raw.put("species", "cat");
        raw.put("species_confidence", 0.9955);
        raw.put("breed", null);
        raw.put("breed_confidence", 0.4843);
        raw.put("pattern", "solid");
        raw.put("is_pet", true);
        raw.put("labels", List.of(
                "hard:species_cat",
                "hard:fur_length_short_fur",
                "soft:pattern_solid",
                "soft:color_cream",
                "soft:color_gray"
        ));

        AiAnalyzeResponse response = mapper.toResponse(raw);

        assertThat(response).isNotNull();
        assertThat(response.species()).isEqualTo(Species.CAT);
        assertThat(response.speciesConfidence()).isEqualTo(0.9955);
        assertThat(response.breed()).isNull();
        assertThat(response.breedConfidence()).isEqualTo(0.4843);
        assertThat(response.coatPattern()).isEqualTo(CoatPattern.SOLID);
        assertThat(response.isPet()).isTrue();
        assertThat(response.colors()).containsExactlyInAnyOrder(PetColor.CREAM, PetColor.GRAY);
    }

    @Test
    @DisplayName("Büyük harf, köpek türü ve farklı desenler doğru haritalanmalı")
    void dogAndStripedPatternMappedCorrectly() {
        Map<String, Object> raw = new LinkedHashMap<>();
        raw.put("species", "DOG");
        raw.put("species_confidence", 0.98);
        raw.put("breed", "Golden Retriever");
        raw.put("breed_confidence", 0.92);
        raw.put("pattern", "striped");
        raw.put("labels", List.of("soft:color_golden", "soft:color_white"));

        AiAnalyzeResponse response = mapper.toResponse(raw);

        assertThat(response.species()).isEqualTo(Species.DOG);
        assertThat(response.breed()).isEqualTo("Golden Retriever");
        assertThat(response.breedConfidence()).isEqualTo(0.92);
        assertThat(response.coatPattern()).isEqualTo(CoatPattern.STRIPED);
        assertThat(response.colors()).containsExactlyInAnyOrder(PetColor.GOLDEN, PetColor.WHITE);
    }

    @Test
    @DisplayName("Boş veya eksik alanlarda güvenli varsayılanlar dönmeli")
    void emptyOrNullMapHandledGracefully() {
        AiAnalyzeResponse response = mapper.toResponse(Map.of());

        assertThat(response.species()).isNull();
        assertThat(response.speciesConfidence()).isNull();
        assertThat(response.breed()).isNull();
        assertThat(response.breedConfidence()).isNull();
        assertThat(response.coatPattern()).isNull();
        assertThat(response.colors()).isEmpty();
        assertThat(response.isPet()).isNull();
    }
}
