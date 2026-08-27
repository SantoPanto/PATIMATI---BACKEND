package com.works.patimati.dto.poi;

import com.works.patimati.entity.enums.PoiSource;
import com.works.patimati.entity.enums.PoiType;

public record PoiResponse(
        Long id,
        PoiType type,
        String name,
        Double latitude,
        Double longitude,
        String address,
        String phone,
        String openingHours,
        PoiSource source,
        /**
         * {@code source=PLATFORM} olduğunda ilgili VetClinic/PetShop/Shelter
         * satırının kendi kimliği -- ön yüz "Hizmete Git" bağlantısını
         * (`/hizmetler/veteriner/{refId}` vb.) bununla kurar. OSM/MANUAL
         * noktalarda gerçek bir hesap/detay sayfası olmadığı için null.
         */
        Long refId,
        /**
         * {@code source=PLATFORM} olduğunda ilgili VetClinic/PetShop/Shelter
         * kartının kendi fotoğrafı (geçici okuma linki). OSM/MANUAL
         * noktalarda fotoğraf verisi hiç olmadığı için null.
         */
        String photoUrl
) {
}
