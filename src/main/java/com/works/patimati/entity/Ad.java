package com.works.patimati.entity;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

@Entity
@Table(
        name = "ads",
        indexes = {
                @Index(name = "idx_ads_location", columnList = "location") // GIST İndeksi (3. Kısım)
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

    // KISIM 3: Coğrafi Konum (GIS)
    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point location;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private boolean active = true;

    public enum AdType {
        LOST, FOUND, ADOPTION
    }
}