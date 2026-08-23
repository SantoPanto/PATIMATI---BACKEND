package com.works.patimati.mapper;

import com.works.patimati.dto.ai.AiAnalyzeResponse;
import com.works.patimati.entity.enums.CoatPattern;
import com.works.patimati.entity.enums.PetColor;
import com.works.patimati.entity.enums.Species;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * AI servisinin ham JSON cevabını ({@code Map<String, Object>}) frontend'in
 * ilan otomatik doldurma ekranında beklediği tiplere ve form alanlarına dönüştüren adapter.
 */
@Component
public class AiAnalyzeMapper {

    private static final String COLOR_LABEL_PREFIX = "soft:color_";

    public AiAnalyzeResponse toResponse(Map<String, Object> raw) {
        if (raw == null || raw.isEmpty()) {
            return new AiAnalyzeResponse(null, null, null, null, null, Set.of(), null);
        }

        Species species = parseSpecies(raw.get("species"));
        Double speciesConfidence = toDouble(raw.get("species_confidence"));
        if (speciesConfidence == null) {
            speciesConfidence = toDouble(raw.get("speciesConfidence"));
        }

        String breed = parseBreed((String) raw.get("breed"));
        Double breedConfidence = toDouble(raw.get("breed_confidence"));
        if (breedConfidence == null) {
            breedConfidence = toDouble(raw.get("breedConfidence"));
        }

        CoatPattern coatPattern = parseCoatPattern(raw.get("pattern"));
        if (coatPattern == null) {
            coatPattern = parseCoatPattern(raw.get("coatPattern"));
        }

        Set<PetColor> colors = parseColorsFromLabels(raw.get("labels"));

        Boolean isPet = raw.get("is_pet") instanceof Boolean b ? b :
                (raw.get("isPet") instanceof Boolean b2 ? b2 : null);

        return new AiAnalyzeResponse(
                species,
                speciesConfidence,
                breed,
                breedConfidence,
                coatPattern,
                colors,
                isPet
        );
    }

    private Species parseSpecies(Object rawSpecies) {
        if (rawSpecies == null) {
            return null;
        }
        String str = rawSpecies.toString().trim().toUpperCase(Locale.ROOT);
        try {
            return Species.valueOf(str);
        } catch (IllegalArgumentException e) {
            return Species.UNKNOWN;
        }
    }

    private String parseBreed(String rawBreed) {
        if (rawBreed == null || rawBreed.isBlank() || "null".equalsIgnoreCase(rawBreed.trim())) {
            return null;
        }
        return rawBreed.trim();
    }

    private CoatPattern parseCoatPattern(Object rawPattern) {
        if (rawPattern == null) {
            return null;
        }
        String str = rawPattern.toString().trim().toUpperCase(Locale.ROOT);
        try {
            return CoatPattern.valueOf(str);
        } catch (IllegalArgumentException e) {
            return CoatPattern.UNKNOWN;
        }
    }

    private Set<PetColor> parseColorsFromLabels(Object rawLabels) {
        if (!(rawLabels instanceof List<?> labels)) {
            return LinkedHashSet.newLinkedHashSet(0);
        }

        Set<PetColor> colorSet = new LinkedHashSet<>();
        for (Object item : labels) {
            if (item == null) continue;
            String label = item.toString().trim().toLowerCase(Locale.ROOT);
            if (label.startsWith(COLOR_LABEL_PREFIX)) {
                String colorStr = label.substring(COLOR_LABEL_PREFIX.length());
                PetColor petColor = PetColor.fromString(colorStr);
                if (petColor != null) {
                    colorSet.add(petColor);
                }
            }
        }
        return colorSet;
    }

    private Double toDouble(Object obj) {
        if (obj instanceof Number n) {
            return n.doubleValue();
        }
        return null;
    }
}
