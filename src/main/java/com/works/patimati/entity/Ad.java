package com.works.patimati.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.locationtech.jts.geom.Point;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "ad_photo_urls",
            joinColumns = @JoinColumn(name = "ad_id")
    )
    @OrderColumn(name = "photo_order")
    @Column(name = "photo_url", nullable = false, length = 2048)
    @Builder.Default
    private List<String> photoUrls = new ArrayList<>();

    // Coğrafi Konum (GIS) - PostGIS Geography türü tercih edildi
    @Column(columnDefinition = "geography(Point, 4326)")
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

    public enum Species {
        CAT, DOG, BIRD, OTHER
    }
}