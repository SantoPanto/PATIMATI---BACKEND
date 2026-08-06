package com.works.patimati.dto.ad;

import com.works.patimati.entity.Ad;
import com.works.patimati.entity.enums.AgeGroup;
import com.works.patimati.entity.enums.CoatPattern;
import com.works.patimati.entity.enums.EyeColor;
import com.works.patimati.entity.enums.PetColor;
import com.works.patimati.entity.enums.PetGender;
import com.works.patimati.entity.enums.PresenceStatus;
import com.works.patimati.entity.enums.Species;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record AdResponse(
        Long id,
        String title,
        String description,
        Ad.AdType adType,
        Species species,
        String breed,
        Set<PetColor> colors,
        PetGender gender,
        AgeGroup ageGroup,
        CoatPattern coatPattern,
        PresenceStatus collarStatus,
        PetColor collarColor,
        String collarTagText,
        EyeColor eyeColor,
        PresenceStatus earTagStatus,
        PresenceStatus earNotchStatus,
        boolean microchipped,
        LocalDate lostDate,
        String distinctiveMarks,
        List<String> photoUrls,
        Double latitude,
        Double longitude,
        Long ownerId,
        String ownerDisplayName,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
