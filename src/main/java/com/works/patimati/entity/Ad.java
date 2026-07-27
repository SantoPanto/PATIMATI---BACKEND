package com.works.patimati.entity;

import com.works.patimati.entity.enums.AgeGroup;
import com.works.patimati.entity.enums.CoatPattern;
import com.works.patimati.entity.enums.EyeColor;
import com.works.patimati.entity.enums.PetColor;
import com.works.patimati.entity.enums.PetGender;
import com.works.patimati.entity.enums.PresenceStatus;
import com.works.patimati.entity.enums.Species;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.locationtech.jts.geom.Point;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(
        name = "ads",
        indexes = {
                @Index(name = "idx_ads_location", columnList = "location")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private AdType adType; // LOST, FOUND, ADOPTION

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Species species;

    @Column(nullable = false, length = 100)
    @Builder.Default
    private String breed = "MIXED_OR_UNKNOWN";

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "ad_colors",
            joinColumns = @JoinColumn(name = "ad_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "color", nullable = false, length = 30)
    @Builder.Default
    private Set<PetColor> colors = new LinkedHashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PetGender gender = PetGender.UNKNOWN;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_group", nullable = false, length = 20)
    @Builder.Default
    private AgeGroup ageGroup = AgeGroup.UNKNOWN;

    @Enumerated(EnumType.STRING)
    @Column(name = "coat_pattern", nullable = false, length = 30)
    @Builder.Default
    private CoatPattern coatPattern = CoatPattern.UNKNOWN;

    @Enumerated(EnumType.STRING)
    @Column(name = "collar_status", nullable = false, length = 20)
    @Builder.Default
    private PresenceStatus collarStatus = PresenceStatus.UNKNOWN;

    @Enumerated(EnumType.STRING)
    @Column(name = "collar_color", length = 30)
    private PetColor collarColor;

    @Column(name = "collar_tag_text", length = 255)
    private String collarTagText;

    @Enumerated(EnumType.STRING)
    @Column(name = "eye_color", nullable = false, length = 30)
    @Builder.Default
    private EyeColor eyeColor = EyeColor.UNKNOWN;

    @Enumerated(EnumType.STRING)
    @Column(name = "ear_tag_status", nullable = false, length = 20)
    @Builder.Default
    private PresenceStatus earTagStatus = PresenceStatus.UNKNOWN;

    @Enumerated(EnumType.STRING)
    @Column(name = "ear_notch_status", nullable = false, length = 20)
    @Builder.Default
    private PresenceStatus earNotchStatus = PresenceStatus.UNKNOWN;

    @Column(name = "microchip_number", length = 32)
    private String microchipNumber;

    @Column(name = "lost_date")
    private LocalDate lostDate;

    @Column(name = "distinctive_marks", length = 1000)
    private String distinctiveMarks;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "ad_photo_urls",
            joinColumns = @JoinColumn(name = "ad_id")
    )
    @OrderColumn(name = "photo_order")
    @Column(name = "photo_url", nullable = false, length = 2048)
    @Builder.Default
    private List<String> photoUrls = new ArrayList<>();

    // KISIM 3: Coğrafi Konum (GIS)
    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public enum AdType {
        LOST, FOUND, ADOPTION
    }
}
