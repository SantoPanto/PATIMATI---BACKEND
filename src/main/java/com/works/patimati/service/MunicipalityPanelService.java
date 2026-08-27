package com.works.patimati.service;

import com.works.patimati.dto.HeatmapPointDto;
import com.works.patimati.dto.MunicipalityReportStatsDto;
import com.works.patimati.dto.MunicipalityStatsDto;
import com.works.patimati.entity.enums.ReportStatus;
import com.works.patimati.entity.enums.ReportType;
import com.works.patimati.repository.AnimalReportRepository;
import com.works.patimati.entity.Ad.AdType;
import com.works.patimati.entity.enums.AdResolutionStatus;
import com.works.patimati.repository.MunicipalityPanelRepository;
import com.works.patimati.municipality.MunicipalityScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MunicipalityPanelService {

    private final MunicipalityPanelRepository repository;
    private final AnimalReportRepository animalReportRepository;
    private final MunicipalityScopeService municipalityScopeService;

    /** İlçesiz yöneticinin panel başlığı — FE district'i olduğu gibi basıyor. */
    static final String TUM_ILCELER_ETIKETI = "Tüm ilçeler";

    public MunicipalityStatsDto getStats(LocalDateTime startDate, LocalDateTime endDate) {
        // İlçesiz yöneticide null: sorgular süzgeci atlar (repository'e bak).
        String district = municipalityScopeService.mevcutKapsam().ilce();
        Instant start = startDate.atZone(ZoneId.systemDefault()).toInstant();
        Instant end = endDate.atZone(ZoneId.systemDefault()).toInstant();

        long lost = repository.countAdsByDistrictAndType(district, AdType.LOST, start, end);
        long found = repository.countAdsByDistrictAndType(district, AdType.FOUND, start, end);
        long adoption = repository.countAdsByDistrictAndType(district, AdType.ADOPTION, start, end);
        
        List<AdResolutionStatus> happyStatuses = Arrays.asList(AdResolutionStatus.FOUND, AdResolutionStatus.ADOPTED);
        long reunion = repository.countReunionsByDistrict(district, happyStatuses, start, end);

        MunicipalityStatsDto stats = new MunicipalityStatsDto();
        stats.setDistrict(district != null ? district : TUM_ILCELER_ETIKETI);
        stats.setLostCount(lost);
        stats.setFoundCount(found);
        stats.setAdoptionCount(adoption);
        stats.setReunionCount(reunion);
        return stats;
    }

    /**
     * "İhbar analizi" (S6): durum + tür kırılımı ve günlük seri. Kapsam
     * {@link #getStats} ile aynı sözleşmede çözülür.
     */
    public MunicipalityReportStatsDto getReportStats(LocalDateTime startDate, LocalDateTime endDate) {
        String district = municipalityScopeService.mevcutKapsam().ilce();

        MunicipalityReportStatsDto dto = new MunicipalityReportStatsDto();
        dto.setDistrict(district != null ? district : TUM_ILCELER_ETIKETI);

        for (Object[] satir : animalReportRepository.durumaGoreSay(district, startDate, endDate)) {
            long adet = ((Number) satir[1]).longValue();
            switch ((ReportStatus) satir[0]) {
                case YENI -> dto.setYeniCount(adet);
                case ISLEME_ALINDI -> dto.setIslemeAlindiCount(adet);
                case TAMAMLANDI -> dto.setTamamlandiCount(adet);
            }
        }

        for (Object[] satir : animalReportRepository.tureGoreSay(district, startDate, endDate)) {
            long adet = ((Number) satir[1]).longValue();
            switch ((ReportType) satir[0]) {
                case YARALI -> dto.setYaraliCount(adet);
                case SAHIPSIZ -> dto.setSahipsizCount(adet);
                case DIGER -> dto.setDigerCount(adet);
            }
        }

        for (Object[] satir : animalReportRepository.gunlukIhbarSayilari(district, startDate, endDate)) {
            dto.getDaily().add(new MunicipalityReportStatsDto.GunlukSayi(
                    gunecevir(satir[0]), ((Number) satir[1]).longValue()));
        }
        return dto;
    }

    /**
     * Paket-görünür: gerçek-DB testi gün kolonunu sürücünün GERÇEK tipiyle
     * sınar -- {@link #zamanaCevir}'deki 27.08 dersinin tarih hâli.
     */
    static LocalDate gunecevir(Object deger) {
        if (deger instanceof LocalDate g) {
            return g;
        }
        if (deger instanceof java.sql.Date d) {
            return d.toLocalDate();
        }
        throw new IllegalStateException(
                "günlük seri gün kolonu beklenmeyen tipte: " + deger.getClass().getName());
    }

    public List<HeatmapPointDto> getHeatmap(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        String district = municipalityScopeService.mevcutKapsam().ilce();
        Instant start = startDate.atZone(ZoneId.systemDefault()).toInstant();
        Instant end = endDate.atZone(ZoneId.systemDefault()).toInstant();

        List<Object[]> results = repository.getHeatmapPoints(district, start, end, limit);
        List<HeatmapPointDto> heatmap = new ArrayList<>();

        for (Object[] row : results) {
            heatmap.add(satirdanNokta(row));
        }
        return heatmap;
    }

    /** Paket-görünür: gerçek-DB testi bu hattı sürücünün GERÇEK tipiyle koşuyor. */
    static HeatmapPointDto satirdanNokta(Object[] row) {
        Double lat = ((Number) row[0]).doubleValue();
        Double lng = ((Number) row[1]).doubleValue();
        String type = (String) row[2];
        return new HeatmapPointDto(lat, lng, type, zamanaCevir(row[3]));
    }

    /**
     * Native sorgudaki {@code created_at} kolonu {@code timestamp with time zone};
     * Hibernate 6 + PostgreSQL bunu {@code Instant} verir — düz {@code (Timestamp)}
     * cast'i gerçek veritabanında {@code ClassCastException}/500'dü (27.08, yerel
     * uçtan uca ölçüm). Sürücü/ORM sürümüne göre tip değişebildiği için dönüşüm
     * tek tek tanınan tiplere bakar; tanınmayan tipte SESSİZCE bozuk tarih
     * üretmek yerine açık hata fırlatır.
     */
    private static LocalDateTime zamanaCevir(Object deger) {
        if (deger instanceof Instant an) {
            return LocalDateTime.ofInstant(an, ZoneId.systemDefault());
        }
        if (deger instanceof Timestamp ts) {
            return ts.toLocalDateTime();
        }
        if (deger instanceof OffsetDateTime odt) {
            return odt.toLocalDateTime();
        }
        if (deger instanceof LocalDateTime ldt) {
            return ldt;
        }
        throw new IllegalStateException(
                "heatmap created_at beklenmeyen tipte: " + deger.getClass().getName());
    }
}
