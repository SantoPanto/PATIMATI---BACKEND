package com.works.patimati.entity;

import com.works.patimati.entity.enums.NoteAuthorType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/**
 * Bir hayvana girilen not ("ne yaptık / ne yapacağız") — ya hayvanın
 * sahibiyle ACCEPTED durumda {@link VetCustomerRequest} ilişkisi olan bir
 * VET, ya da hayvanın sahibinin KENDİSİ yazar (26.08: sahip de kendi
 * gözlemini girebilsin). {@code authorType} hangisi olduğunu ayırt eder;
 * {@code vet} yalnızca {@code authorType=VET} iken doludur. Notlar hem ilgili
 * vet hem de hayvanın sahibi tarafından görülebilir (bkz.
 * {@code PetTreatmentNoteService}).
 */
@Entity
@Table(name = "pet_treatment_notes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetTreatmentNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    /** Yalnızca {@code authorType=VET} iken dolu — sahip notunda null kalır. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vet_id")
    private User vet;

    @Enumerated(EnumType.STRING)
    @Column(name = "author_type", nullable = false, length = 20)
    private NoteAuthorType authorType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // VetCustomerRequest ile AYNI gerekçe: addNote() bu alanı HEMEN
    // döndürülen yanıtta gösteriyor, DB varsayılanına bırakılsaydı satır
    // yeniden okunmadan null görünürdü (canlı testte yakalandı).
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
