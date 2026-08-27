package com.works.patimati.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Belediye paneli "İhbar analizi" bölümünün cevabı (S6, 27.08).
 *
 * <p>Kapsam {@code MunicipalityStatsDto} ile aynı sözleşmede çözülür: kurum
 * yalnız kendi ilçesini, ilçesiz yönetici tümünü görür; {@code district}
 * panel başlığındaki değerin aynısıdır ("Tüm ilçeler" dahil).
 */
@Data
public class MunicipalityReportStatsDto {

    private String district;

    // Durum kırılımı (ReportStatus) — süreç sağlığı: ne kadarı bekliyor?
    private long yeniCount;
    private long islemeAlindiCount;
    private long tamamlandiCount;

    // Tür kırılımı (ReportType) — yükün niteliği: acil mi, bakım mı?
    private long yaraliCount;
    private long sahipsizCount;
    private long digerCount;

    /** Gün gün ihbar sayısı (aralıktaki boş günler LİSTEDE YOK — FE doldurur). */
    private List<GunlukSayi> daily = new ArrayList<>();

    /** Tek günün ihbar sayısı; {@code date} ISO (yyyy-MM-dd) serileşir. */
    public record GunlukSayi(LocalDate date, long count) {
    }
}
