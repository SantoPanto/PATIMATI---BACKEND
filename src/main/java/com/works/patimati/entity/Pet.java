package com.works.patimati.entity;

import com.works.patimati.entity.enums.AgeGroup;
import com.works.patimati.entity.enums.PetGender;
import com.works.patimati.entity.enums.Species;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Kullanıcının kendi hayvanı ("Evcil Hayvanlarım") -- ilan (Ad) sisteminden
 * BAĞIMSIZ, kalıcı bir kayıt. Kullanıcı başına birden fazla satır olabilir
 * (owner_id UNIQUE DEĞİL) -- {@link FavoriteAd} ile AYNI "kullanıcı sahipli
 * çoklu kayıt" deseni.
 */
@Entity
@Table(name = "pets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Species species;

    @Column(length = 100)
    private String breed;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PetGender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_group", length = 20)
    private AgeGroup ageGroup;

    /** Kalıcı depolama referansı ({@code ImageStorageService}) -- ham URL değil. */
    @Column(name = "photo_reference", length = 500)
    private String photoReference;

    // Yapılandırılmış sağlık alanları (26.08) — öncesinde her şey
    // PetTreatmentNote.content serbest metnine sıkışmıştı, veteriner için
    // taranabilir/aranabilir değildi.
    @Column(name = "birth_date")
    private LocalDate birthDate;

    private Boolean sterilized;

    @Column(name = "microchip_number", length = 50)
    private String microchipNumber;

    @Column(name = "chronic_conditions", length = 1000)
    private String chronicConditions;

    @Column(length = 1000)
    private String allergies;

    /**
     * "Ben Neyim?" AI raporunun ({@code PetReportResult}) ham JSON'u — alanlar
     * burada yeniden modellenmez, AI'ya eklenen yeni alan sessizce düşmesin
     * diye ({@code PublicPetAnalysisController} ile AYNI gerekçe).
     */
    @Column(name = "ai_report", columnDefinition = "TEXT")
    private String aiReport;

    @Column(name = "ai_report_at")
    private OffsetDateTime aiReportAt;

    // İki damga da INSERT'te veritabanı varsayılanından (now()) dolar;
    // updatedAt'i her güncellemede servis katmanı yeniler (VetClinic ile
    // aynı desen).
    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    private OffsetDateTime updatedAt;
}
