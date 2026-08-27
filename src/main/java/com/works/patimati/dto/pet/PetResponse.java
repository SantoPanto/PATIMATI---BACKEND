package com.works.patimati.dto.pet;

import com.works.patimati.entity.enums.AgeGroup;
import com.works.patimati.entity.enums.PetGender;
import com.works.patimati.entity.enums.Species;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/** "Evcil Hayvanlarım" kartı görünümü. */
public record PetResponse(
        Long id,
        String name,
        Species species,
        String breed,
        PetGender gender,
        AgeGroup ageGroup,
        String photoUrl,

        /** Kapak dahil TÜM fotoğraflar, sıralı; {@code photoUrl} her zaman ilk eleman. */
        List<String> photoUrls,

        LocalDate birthDate,
        Boolean sterilized,
        String microchipNumber,
        String chronicConditions,
        String allergies,

        /** "Ben Neyim?" raporunun ham JSON'u — frontend JSON.parse eder. */
        String aiReport,
        OffsetDateTime aiReportAt,

        /** Vet panelindeki müşteri/hayvan listesi özeti (26.08). */
        long treatmentNoteCount,
        OffsetDateTime lastTreatmentAt
) {
}
