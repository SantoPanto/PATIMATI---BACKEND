package com.works.patimati.service;

import com.works.patimati.dto.HeatmapPointDto;
import com.works.patimati.dto.MunicipalityStatsDto;
import com.works.patimati.entity.Ad.AdType;
import com.works.patimati.entity.enums.AdResolutionStatus;
import com.works.patimati.repository.MunicipalityPanelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MunicipalityPanelService {

    private final MunicipalityPanelRepository repository;
    private final MunicipalityScopeService municipalityScopeService;

    public MunicipalityStatsDto getStats(LocalDateTime startDate, LocalDateTime endDate) {
        // District bilgisini disaridan degil, mevcut oturumdan (session) aliyoruz
        String district = municipalityScopeService.mevcutKapsam().ilce();
        
        Instant start = startDate.atZone(ZoneId.systemDefault()).toInstant();
        Instant end = endDate.atZone(ZoneId.systemDefault()).toInstant();

        long lost = repository.countAdsByDistrictAndType(district, AdType.LOST, start, end);
        long found = repository.countAdsByDistrictAndType(district, AdType.FOUND, start, end);
        long adoption = repository.countAdsByDistrictAndType(district, AdType.ADOPTION, start, end);
        
        long reunion = repository.countReunionsByDistrict(district, AdResolutionStatus.FOUND, start, end);

        MunicipalityStatsDto stats = new MunicipalityStatsDto();
        stats.setDistrict(district);
        stats.setLostCount(lost);
        stats.setFoundCount(found);
        stats.setAdoptionCount(adoption);
        stats.setReunionCount(reunion);
        return stats;
    }

    public List<HeatmapPointDto> getHeatmap(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        // District bilgisini disaridan degil, mevcut oturumdan (session) aliyoruz
        String district = municipalityScopeService.mevcutKapsam().ilce();
        
        Instant start = startDate.atZone(ZoneId.systemDefault()).toInstant();
        Instant end = endDate.atZone(ZoneId.systemDefault()).toInstant();

        List<Object[]> results = repository.getHeatmapPoints(district, start, end, limit);
        List<HeatmapPointDto> heatmap = new ArrayList<>();
        
        for (Object[] row : results) {
            Double lat = ((Number) row[0]).doubleValue();
            Double lng = ((Number) row[1]).doubleValue();
            String type = (String) row[2];
            LocalDateTime createdAt = ((Timestamp) row[3]).toLocalDateTime();
            
            heatmap.add(new HeatmapPointDto(lat, lng, type, createdAt));
        }
        return heatmap;
    }
}
