package com.works.patimati.entity;

import com.works.patimati.entity.enums.PoiSource;
import com.works.patimati.entity.enums.PoiType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.locationtech.jts.geom.Point;

import java.time.Instant;

/**
 * Haritadaki sabit nokta — veteriner, petshop veya barınak.
 *
 * <p>{@code source=OSM} kayıtları {@code PoiSyncService} tarafından
 * OpenStreetMap Overpass API'sinden periyodik çekilir ve {@code osmId} ile
 * eşleştirilerek üzerine yazılır (upsert). {@code source=MANUAL} kayıtlar
 * elle/admin panelinden eklenir; bunlarda {@code osmId} null kalır.</p>
 */
@Entity
@Table(
        name = "points_of_interest",
        indexes = {
                @Index(name = "idx_pois_location", columnList = "location"),
                @Index(name = "idx_pois_type", columnList = "type")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_pois_source_osm_id", columnNames = {"source", "osm_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointOfInterest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PoiType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PoiSource source;

    /** OSM node/way kimliği — yalnız source=OSM'de dolu, senkronizasyonun upsert anahtarı. */
    @Column(name = "osm_id")
    private Long osmId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "geometry(Point, 4326)", nullable = false)
    private Point location;

    private String address;
    private String phone;

    @Column(name = "opening_hours")
    private String openingHours;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
