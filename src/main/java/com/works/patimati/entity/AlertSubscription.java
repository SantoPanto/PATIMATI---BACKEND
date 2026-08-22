package com.works.patimati.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

import java.time.OffsetDateTime;

/**
 * Konum tabanlı uyarı aboneliği: kullanıcının "çevremde kayıp ilanı çıkınca
 * bildirim al" tercihi. Kullanıcı başına tek satır (user_id UNIQUE, V21).
 *
 * <p>Yarıçap METRE cinsindendir; sorgu {@code ST_DWithin(...::geography)}
 * metreyle çalıştığı için birim dönüşümü uç katmanında (km &#8596; m) yapılır.
 */
@Entity
@Table(
        name = "alert_subscriptions",
        indexes = {
                @Index(name = "idx_alert_subscriptions_location", columnList = "location")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(columnDefinition = "geometry(Point, 4326)", nullable = false)
    private Point location;

    @Column(name = "radius_meters", nullable = false)
    private double radiusMeters;

    @Column(nullable = false)
    private boolean enabled;

    // İki damga da INSERT'te veritabanı varsayılanından (CURRENT_TIMESTAMP)
    // dolar; updatedAt'i her güncellemede servis katmanı yeniler.
    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    private OffsetDateTime updatedAt;
}
