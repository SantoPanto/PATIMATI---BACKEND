package com.works.patimati.dto.ad;

import com.works.patimati.entity.Ad;
import com.works.patimati.entity.enums.Species;

public record SharedAdDTO(
        Long id,
        String title,
        Ad.AdType adType,
        Species species,
        String breed,
        String photoUrl,
        boolean active
) {
    public static SharedAdDTO fromEntity(Ad ad) {
        if (ad == null) {
            return null;
        }
        String mainPhoto = (ad.getPhotoUrls() != null && !ad.getPhotoUrls().isEmpty())
                ? ad.getPhotoUrls().get(0)
                : null;

        return new SharedAdDTO(
                ad.getId(),
                ad.getTitle(),
                ad.getAdType(),
                ad.getSpecies(),
                ad.getBreed(),
                mainPhoto,
                ad.isActive()
        );
    }
}
