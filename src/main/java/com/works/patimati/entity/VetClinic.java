package com.works.patimati.entity;

import com.works.patimati.entity.enums.AnimalType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Veteriner klinik bilgi kartı: kullanıcı başına tek satır (user_id UNIQUE,
 * V29) -- {@link AlertSubscription} ile AYNI desen. Yalnızca {@code VET}
 * rolündeki kullanıcılar kendi kartlarını oluşturur/günceller
 * ({@code VetClinicService}), herkese açık dizin bu tablodan okunur.
 */
@Entity
@Table(name = "vet_clinics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VetClinic {

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

    /** Klinik konumu -- nullable (V34): konum ayarlamadan da kart kaydedilebilir. */
    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point location;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "vet_clinic_animal_types", joinColumns = @JoinColumn(name = "vet_clinic_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "animal_type", length = 30)
    @Builder.Default
    private Set<AnimalType> animalTypes = new LinkedHashSet<>();

    // İki damga da INSERT'te veritabanı varsayılanından (now()) dolar;
    // updatedAt'i her güncellemede servis katmanı yeniler (AlertSubscription
    // ile aynı desen).
    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    private OffsetDateTime updatedAt;
}
