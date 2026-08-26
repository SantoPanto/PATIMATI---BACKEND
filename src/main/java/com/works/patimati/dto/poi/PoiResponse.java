package com.works.patimati.dto.poi;

import com.works.patimati.entity.enums.PoiType;

public record PoiResponse(
        Long id,
        PoiType type,
        String name,
        Double latitude,
        Double longitude,
        String address,
        String phone,
        String openingHours
) {
}
