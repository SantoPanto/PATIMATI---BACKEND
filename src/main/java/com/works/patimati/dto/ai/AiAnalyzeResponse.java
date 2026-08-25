package com.works.patimati.dto.ai;

import com.works.patimati.entity.enums.CoatPattern;
import com.works.patimati.entity.enums.PetColor;
import com.works.patimati.entity.enums.Species;

import java.util.Set;

/**
 * İlan oluşturma ekranında AI fotoğraf analizinin frontend'e sunulan yanıt DTO'su.
 *
 * <p>AI servisinin ham JSON çıktısını frontend form alanlarının sözleşmesine
 * (büyük harf enum'lar, `coatPattern` ismi, `soft:color_X` etiketlerinden renk
 * kümesi, camelCase güven skorları) adapte eder.
 */
public record AiAnalyzeResponse(
        Species species,
        Double speciesConfidence,
        String breed,
        Double breedConfidence,
        CoatPattern coatPattern,
        Set<PetColor> colors,
        Boolean isPet
) {
}
