package com.works.patimati.controller;

import com.works.patimati.dto.HeatmapPointDto;
import com.works.patimati.dto.MunicipalityReportStatsDto;
import com.works.patimati.dto.MunicipalityStatsDto;
import com.works.patimati.service.MunicipalityPanelService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.Max;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/municipality/panel")
@RequiredArgsConstructor
@Validated
public class MunicipalityPanelController {

    private final MunicipalityPanelService service;

    @GetMapping("/stats")
    public ResponseEntity<MunicipalityStatsDto> getStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(service.getStats(startDate, endDate));
    }

    @GetMapping("/report-stats")
    public ResponseEntity<MunicipalityReportStatsDto> getReportStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(service.getReportStats(startDate, endDate));
    }

    @GetMapping("/heatmap")
    public ResponseEntity<List<HeatmapPointDto>> getHeatmap(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "500") @Max(2000) int limit) {
        return ResponseEntity.ok(service.getHeatmap(startDate, endDate, limit));
    }
}
