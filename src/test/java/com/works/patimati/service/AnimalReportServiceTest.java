package com.works.patimati.service;

import com.works.patimati.dto.request.AnimalReportCreateRequest;
import com.works.patimati.dto.response.AnimalReportResponse;
import com.works.patimati.entity.AnimalReport;
import com.works.patimati.entity.enums.ReportStatus;
import com.works.patimati.entity.enums.ReportType;
import com.works.patimati.municipality.MunicipalityScopeService;
import com.works.patimati.repository.AnimalReportRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.service.impl.AnimalReportServiceImpl;
import com.works.patimati.storage.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnimalReportServiceTest {

    @Mock
    private AnimalReportRepository animalReportRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ImageStorageService imageStorageService;

    @Mock
    private ReverseGeocodingService reverseGeocodingService;

    @Mock
    private NotificationService notificationService;

    private MunicipalityScopeService municipalityScopeService;

    @InjectMocks
    private AnimalReportServiceImpl animalReportService;

    private GeometryFactory geometryFactory;

    @BeforeEach
    void setUp() {
        geometryFactory = new GeometryFactory();
        municipalityScopeService = mock(MunicipalityScopeService.class, RETURNS_DEEP_STUBS);

        animalReportService = new AnimalReportServiceImpl(
                animalReportRepository,
                userRepository,
                imageStorageService,
                reverseGeocodingService,
                notificationService,
                municipalityScopeService
        );
    }

    @Test
    @DisplayName("İhbar oluşturma başarılı olmalı ve doğru verileri dönmelidir")
    void createPublicReport_Success() {
        AnimalReportCreateRequest request = new AnimalReportCreateRequest(
                "05551234567",
                ReportType.YARALI,
                "Yaralı kedi",
                40.1885,
                29.0610
        );

        when(reverseGeocodingService.cozumle(40.1885, 29.0610))
                .thenReturn(Optional.empty());

        AnimalReport savedReport = AnimalReport.builder()
                .id(1L)
                .reporterContact("05551234567")
                .type(ReportType.YARALI)
                .note("Yaralı kedi")
                .location(geometryFactory.createPoint(new Coordinate(29.0610, 40.1885)))
                .city(null)
                .district(null)
                .status(ReportStatus.YENI)
                .createdAt(LocalDateTime.now())
                .build();

        when(animalReportRepository.save(any(AnimalReport.class))).thenReturn(savedReport);

        AnimalReportResponse response = animalReportService.createPublicReport(request, null, null);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("YENI", response.getStatus());
        verify(animalReportRepository, times(1)).save(any(AnimalReport.class));
    }

    @Test
    @DisplayName("Farklı ilçedeki ihbar güncellenmek istendiğinde AccessDeniedException fırlatılmalıdır")
    void updateStatus_DifferentDistrict_ThrowsAccessDenied() {
        when(municipalityScopeService.mevcutKapsam().ilce()).thenReturn("Nilüfer");
        when(municipalityScopeService.mevcutKapsam().kurumKullaniciId()).thenReturn(5L);

        AnimalReport report = AnimalReport.builder()
                .id(10L)
                .district("Osmangazi")
                .status(ReportStatus.YENI)
                .build();

        when(animalReportRepository.findById(10L)).thenReturn(Optional.of(report));

        assertThrows(AccessDeniedException.class, () ->
                animalReportService.updateStatus(10L, ReportStatus.ISLEME_ALINDI)
        );
    }
}