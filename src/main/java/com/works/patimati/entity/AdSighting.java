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
 * "Gördüm" bildirimi: üçüncü kişinin bir KAYIP ilanına bıraktığı
 * konum + opsiyonel foto + not (V22).
 *
 * <p>{@code reporter} null olabilir — bildirimi girişsiz ziyaretçi de
 * bırakabilir (afiş QR'ı senaryosu); o yüzden {@code reporterContact}
 * zorunludur. Görülmeler yalnız ilan sahibine listelenir.
 */
@Entity
@Table(
        name = "ad_sightings",
        indexes = {
                @Index(name = "idx_ad_sightings_ad_created", columnList = "ad_id, created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdSighting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ad_id", nullable = false)
    private Ad ad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private User reporter;

    @Column(name = "reporter_contact", nullable = false, length = 255)
    private String reporterContact;

    @Column(columnDefinition = "TEXT")
    private String note;

    // Kalıcı depolama referansı (ImageStorageService.uploadImages dönüşü);
    // dışarı verilirken createTemporaryReadUrl ile geçici URL'ye çevrilir.
    @Column(name = "photo_url", length = 2048)
    private String photoUrl;

    @Column(columnDefinition = "geometry(Point, 4326)", nullable = false)
    private Point location;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}
