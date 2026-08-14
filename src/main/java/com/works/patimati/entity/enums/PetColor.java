package com.works.patimati.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PetColor {
    BLACK,
    WHITE,
    GRAY,
    BROWN,
    ORANGE,
    CREAM,
    GOLDEN,
    BEIGE,
    OTHER;

    @JsonCreator
    public static PetColor fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (PetColor color : PetColor.values()) {
            if (color.name().equalsIgnoreCase(value.trim())) {
                return color;
            }
        }
        return null;
    }
}
