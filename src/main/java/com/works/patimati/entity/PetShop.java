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
 * Petshop bilgi kartı: kullanıcı başına tek satır (user_id UNIQUE, V35) --
 * {@link VetClinic} ile AYNI desen, yalnızca hayvan türü seçimi YOK (petshop
 * için kapsam dışı, bkz. plan). Yalnızca {@code PETSHOP} rolündeki
 * kullanıcılar kendi kartlarını oluşturur/günceller ({@code PetShopService}),
 * herkese açık dizin bu tablodan okunur.
 */
@Entity
@Table(name = "petshops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetShop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(length = 100)
    private String district;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(name = "working_hours", length = 300)
    private String workingHours;

    /** Kalıcı depolama referansı ({@code ImageStorageService}) -- ham URL değil. */
    @Column(name = "photo_reference", length = 500)
    private String photoReference;

    /** Dükkan konumu -- nullable: konum ayarlamadan da kart kaydedilebilir. */
    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point location;

    // İki damga da INSERT'te veritabanı varsayılanından (now()) dolar;
    // updatedAt'i her güncellemede servis katmanı yeniler (VetClinic ile
    // aynı desen).
    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    private OffsetDateTime updatedAt;
}
