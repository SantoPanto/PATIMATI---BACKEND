package com.works.patimati.dto.pet;

import com.works.patimati.entity.enums.NoteAuthorType;

import java.time.OffsetDateTime;

public record PetTreatmentNoteResponse(
        Long id,
        String content,
        String vetName,
        NoteAuthorType authorType,
        /** Çağıran bu notun ORİJİNAL yazarı mı — öyleyse düzenle/sil gösterilebilir. */
        boolean canEdit,
        OffsetDateTime createdAt
) {
}
