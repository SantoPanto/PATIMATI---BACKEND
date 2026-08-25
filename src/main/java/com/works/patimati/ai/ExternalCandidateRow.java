package com.works.patimati.ai;

/**
 * {@link com.works.patimati.ai.AiCandidateRow} ile aynı gerekçe, yalnızca
 * external_pet_records tablosu için: hafif satır (kimlik + mesafe), entity'ler
 * toplu olarak ayrı sorguda yüklenir (N+1'den kaçınma).
 */
public interface ExternalCandidateRow {

    Long getId();

    Double getDistanceKm();
}
