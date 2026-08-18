package com.works.patimati.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.works.patimati.entity.enums.PetColor;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Frontend'den gelebilecek null, "" (boş string), tekil string veya dizi biçimindeki
 * renk verilerini güvenle {@code Set<PetColor>} kümesine dönüştürür.
 */
public class PetColorSetDeserializer extends JsonDeserializer<Set<PetColor>> {

    @Override
    public Set<PetColor> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken token = p.currentToken();

        if (token == JsonToken.VALUE_NULL) {
            return new LinkedHashSet<>();
        }

        if (token == JsonToken.VALUE_STRING) {
            String text = p.getText();
            PetColor color = parseColor(text);
            Set<PetColor> colors = new LinkedHashSet<>();
            if (color != null) {
                colors.add(color);
            }
            return colors;
        }

        if (token == JsonToken.START_ARRAY) {
            Set<PetColor> colors = new LinkedHashSet<>();
            while (p.nextToken() != JsonToken.END_ARRAY) {
                if (p.currentToken() == JsonToken.VALUE_STRING) {
                    PetColor color = parseColor(p.getText());
                    if (color != null) {
                        colors.add(color);
                    }
                }
            }
            return colors;
        }

        return new LinkedHashSet<>();
    }

    private PetColor parseColor(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return PetColor.valueOf(text.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
