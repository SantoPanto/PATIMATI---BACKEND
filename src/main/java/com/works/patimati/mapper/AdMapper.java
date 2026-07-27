package com.works.patimati.mapper;

import com.works.patimati.dto.ad.AdCreateRequest;
import com.works.patimati.dto.ad.AdResponse;
import com.works.patimati.dto.ad.AdUpdateRequest;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.AgeGroup;
import com.works.patimati.entity.enums.CoatPattern;
import com.works.patimati.entity.enums.EyeColor;
import com.works.patimati.entity.enums.PetColor;
import com.works.patimati.entity.enums.PetGender;
import com.works.patimati.entity.enums.PresenceStatus;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class AdMapper {

    private static final int WGS_84_SRID = 4326;
    private static final String UNKNOWN_BREED = "MIXED_OR_UNKNOWN";

    private final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), WGS_84_SRID);

    public Ad toEntity(AdCreateRequest request) {
        Ad ad = Ad.builder()
                .title(request.title().trim())
                .description(normalizeNullable(request.description()))
                .adType(request.adType())
                .species(request.species())
                .breed(normalizeBreed(request.breed()))
                .colors(copyColors(request.colors()))
                .gender(defaultValue(request.gender(), PetGender.UNKNOWN))
                .ageGroup(defaultValue(request.ageGroup(), AgeGroup.UNKNOWN))
                .coatPattern(defaultValue(request.coatPattern(), CoatPattern.UNKNOWN))
                .eyeColor(defaultValue(request.eyeColor(), EyeColor.UNKNOWN))
                .earTagStatus(defaultValue(request.earTagStatus(), PresenceStatus.UNKNOWN))
                .earNotchStatus(defaultValue(request.earNotchStatus(), PresenceStatus.UNKNOWN))
                .microchipNumber(normalizeMicrochipNumber(request.microchipNumber()))
                .lostDate(request.lostDate())
                .distinctiveMarks(normalizeNullable(request.distinctiveMarks()))
                .location(toPoint(request.latitude(), request.longitude()))
                .build();

        applyCollarFields(
                ad,
                request.collarStatus(),
                request.collarColor(),
                request.collarTagText()
        );

        return ad;
    }

    public void updateEntity(Ad ad, AdUpdateRequest request) {
        ad.setTitle(request.title().trim());
        ad.setDescription(normalizeNullable(request.description()));
        ad.setAdType(request.adType());
        ad.setSpecies(request.species());
        ad.setBreed(normalizeBreed(request.breed()));
        ad.setColors(copyColors(request.colors()));
        ad.setGender(defaultValue(request.gender(), PetGender.UNKNOWN));
        ad.setAgeGroup(defaultValue(request.ageGroup(), AgeGroup.UNKNOWN));
        ad.setCoatPattern(defaultValue(request.coatPattern(), CoatPattern.UNKNOWN));
        ad.setEyeColor(defaultValue(request.eyeColor(), EyeColor.UNKNOWN));
        ad.setEarTagStatus(defaultValue(request.earTagStatus(), PresenceStatus.UNKNOWN));
        ad.setEarNotchStatus(defaultValue(request.earNotchStatus(), PresenceStatus.UNKNOWN));
        ad.setMicrochipNumber(normalizeMicrochipNumber(request.microchipNumber()));
        ad.setLostDate(request.lostDate());
        ad.setDistinctiveMarks(normalizeNullable(request.distinctiveMarks()));
        ad.setLocation(toPoint(request.latitude(), request.longitude()));

        applyCollarFields(
                ad,
                request.collarStatus(),
                request.collarColor(),
                request.collarTagText()
        );
    }

    public AdResponse toResponse(Ad ad) {
        return toResponse(ad, immutablePhotoUrls(ad.getPhotoUrls()));
    }

    public AdResponse toResponse(Ad ad, List<String> photoUrls) {
        Point location = ad.getLocation();
        User owner = ad.getUser();

        return new AdResponse(
                ad.getId(),
                ad.getTitle(),
                ad.getDescription(),
                ad.getAdType(),
                ad.getSpecies(),
                ad.getBreed(),
                immutableColors(ad.getColors()),
                ad.getGender(),
                ad.getAgeGroup(),
                ad.getCoatPattern(),
                ad.getCollarStatus(),
                ad.getCollarColor(),
                ad.getCollarTagText(),
                ad.getEyeColor(),
                ad.getEarTagStatus(),
                ad.getEarNotchStatus(),
                ad.getMicrochipNumber() != null && !ad.getMicrochipNumber().isBlank(),
                ad.getLostDate(),
                ad.getDistinctiveMarks(),
                immutablePhotoUrls(photoUrls),
                location == null ? null : location.getY(),
                location == null ? null : location.getX(),
                owner == null ? null : owner.getUid(),
                ownerDisplayName(owner),
                ad.isActive(),
                ad.getCreatedAt(),
                ad.getUpdatedAt()
        );
    }

    private void applyCollarFields(
            Ad ad,
            PresenceStatus collarStatus,
            PetColor collarColor,
            String collarTagText
    ) {
        PresenceStatus normalizedStatus =
                defaultValue(collarStatus, PresenceStatus.UNKNOWN);

        ad.setCollarStatus(normalizedStatus);

        if (normalizedStatus == PresenceStatus.YES) {
            ad.setCollarColor(collarColor);
            ad.setCollarTagText(normalizeNullable(collarTagText));
            return;
        }

        ad.setCollarColor(null);
        ad.setCollarTagText(null);
    }

    private Point toPoint(BigDecimal latitude, BigDecimal longitude) {
        Coordinate coordinate =
                new Coordinate(longitude.doubleValue(), latitude.doubleValue());
        return geometryFactory.createPoint(coordinate);
    }

    private Set<PetColor> copyColors(Set<PetColor> colors) {
        return colors == null ? new LinkedHashSet<>() : new LinkedHashSet<>(colors);
    }

    private Set<PetColor> immutableColors(Set<PetColor> colors) {
        if (colors == null || colors.isEmpty()) {
            return Set.of();
        }

        return Collections.unmodifiableSet(new LinkedHashSet<>(colors));
    }

    private List<String> immutablePhotoUrls(List<String> photoUrls) {
        return photoUrls == null ? List.of() : List.copyOf(photoUrls);
    }

    private String normalizeBreed(String breed) {
        String normalizedBreed = normalizeNullable(breed);
        return normalizedBreed == null ? UNKNOWN_BREED : normalizedBreed;
    }

    private String normalizeMicrochipNumber(String microchipNumber) {
        String normalizedMicrochipNumber = normalizeNullable(microchipNumber);
        return normalizedMicrochipNumber == null
                ? null
                : normalizedMicrochipNumber.replaceAll("\\s+", "");
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private <T> T defaultValue(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    private String ownerDisplayName(User owner) {
        if (owner == null) {
            return null;
        }

        String displayName = Stream.of(owner.getFirstName(), owner.getLastName())
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .collect(Collectors.joining(" "));

        return displayName.isBlank() ? null : displayName;
    }
}
