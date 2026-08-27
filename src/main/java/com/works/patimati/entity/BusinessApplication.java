package com.works.patimati.entity;

import com.works.patimati.entity.enums.AnimalType;
import com.works.patimati.entity.enums.BusinessApplicationStatus;
import com.works.patimati.entity.enums.BusinessType;
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
 * Kullanıcının veteriner/petshop/barınak işletme sahibi olmak için yaptığı
 * başvuru. Onaylanınca {@code BusinessApplicationService.approve} bu
 * satırdaki iş bilgilerinden {@link VetClinic}/{@link PetShop}/{@link Shelter}
 * satırını oluşturur ve {@link User#getRole()}'ü değiştirir -- bu tablo
 * kalıcı iş kartı DEĞİL, yalnızca başvuru geçmişidir.
 */
@Entity
@Table(name = "business_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_user_id", nullable = false)
    private User applicant;

    @Enumerated(EnumType.STRING)
    @Column(name = "business_type", nullable = false, length = 20)
    private BusinessType businessType;

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

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point location;

    /** Yalnızca {@code businessType = VET} iken doldurulur. */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "business_application_animal_types", joinColumns = @JoinColumn(name = "business_application_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "animal_type", length = 30)
    @Builder.Default
    private Set<AnimalType> animalTypes = new LinkedHashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BusinessApplicationStatus status = BusinessApplicationStatus.BEKLEMEDE;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "decided_at")
    private OffsetDateTime decidedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decided_by_admin_id")
    private User decidedByAdmin;
}
