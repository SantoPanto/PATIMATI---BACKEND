package com.works.patimati.mapper;

import com.works.patimati.dto.ad.AdCreateRequest;
import com.works.patimati.dto.ad.AdResponse;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.PetColor;
import com.works.patimati.entity.enums.PetGender;
import com.works.patimati.entity.enums.PresenceStatus;
import com.works.patimati.entity.enums.Species;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AdMapperTest {

    private AdMapper adMapper;

    @BeforeEach
    void setUp() {
        adMapper = new AdMapper();
    }

    @Test
    void shouldMapCreateRequestToEntityWithoutSystemManagedFields() {
        AdCreateRequest request = new AdCreateRequest(
                "  Lost tabby cat  ",
                " ",
                Ad.AdType.LOST,
                Species.CAT,
                null,
                null,
                null,
                null,
                null,
                PresenceStatus.NO,
                PetColor.BROWN,
                "Should be cleared",
                null,
                null,
                null,
                "123 456 789",
                LocalDate.now(),
                "  Notch on the left ear  ",
                new BigDecimal("40.195000"),
                new BigDecimal("29.060000")
        );

        Ad ad = adMapper.toEntity(request);

        assertThat(ad.getTitle()).isEqualTo("Lost tabby cat");
        assertThat(ad.getDescription()).isNull();
        assertThat(ad.getBreed()).isEqualTo("MIXED_OR_UNKNOWN");
        assertThat(ad.getColors()).isEmpty();
        assertThat(ad.getGender()).isEqualTo(PetGender.UNKNOWN);
        assertThat(ad.getCollarStatus()).isEqualTo(PresenceStatus.NO);
        assertThat(ad.getCollarColor()).isNull();
        assertThat(ad.getCollarTagText()).isNull();
        assertThat(ad.getMicrochipNumber()).isEqualTo("123456789");
        assertThat(ad.getDistinctiveMarks()).isEqualTo("Notch on the left ear");

        assertThat(ad.getLocation().getX()).isEqualTo(29.060000);
        assertThat(ad.getLocation().getY()).isEqualTo(40.195000);
        assertThat(ad.getLocation().getSRID()).isEqualTo(4326);

        assertThat(ad.getId()).isNull();
        assertThat(ad.getUser()).isNull();
        assertThat(ad.isActive()).isTrue();
        assertThat(ad.getCreatedAt()).isNull();
        assertThat(ad.getUpdatedAt()).isNull();
        assertThat(ad.getPhotoUrls()).isEmpty();
    }

    @Test
    void shouldMapEntityToSafeResponseCoordinatesAndOwnerSummary() {
        GeometryFactory geometryFactory =
                new GeometryFactory(new PrecisionModel(), 4326);

        User owner = User.builder()
                .uid(42L)
                .firstName("Zahid")
                .lastName("Yavaş")
                .build();

        Ad ad = Ad.builder()
                .id(7L)
                .title("Lost cat")
                .adType(Ad.AdType.LOST)
                .species(Species.CAT)
                .colors(Set.of(PetColor.WHITE, PetColor.ORANGE))
                .photoUrls(List.of("https://example.com/1.jpg"))
                .location(geometryFactory.createPoint(new Coordinate(29.060000, 40.195000)))
                .user(owner)
                .build();
        ad.setCreatedAt(Instant.parse("2026-07-27T09:00:00Z"));
        ad.setUpdatedAt(Instant.parse("2026-07-27T10:00:00Z"));

        AdResponse response = adMapper.toResponse(ad);

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.latitude()).isEqualTo(40.195000);
        assertThat(response.longitude()).isEqualTo(29.060000);
        assertThat(response.ownerId()).isEqualTo(42L);
        assertThat(response.ownerDisplayName()).isEqualTo("Zahid Yavaş");
        assertThat(response.photoUrls())
                .containsExactly("https://example.com/1.jpg");
    }
}
