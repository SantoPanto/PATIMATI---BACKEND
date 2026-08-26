package com.works.patimati.service.impl;

import com.works.patimati.dto.request.AnimalReportCreateRequest;
import com.works.patimati.dto.response.AnimalReportResponse;
import com.works.patimati.entity.AnimalReport;
import com.works.patimati.entity.User;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.AnimalReportRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.service.AnimalReportService;
import com.works.patimati.service.GeocodingService;
import com.works.patimati.service.MunicipalityScopeService;
import com.works.patimati.service.NotificationService;
import com.works.patimati.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnimalReportServiceImpl implements AnimalReportService {

    private final AnimalReportRepository animalReportRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final GeocodingService geocodingService; // İlanlardaki ters geokodlamanın aynısı
    private final NotificationService notificationService;
    private final MunicipalityScopeService municipalityScopeService; // Oturumdan ilçe çözen hazır servis
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Override
    @Transactional
    public AnimalReportResponse createPublicReport(AnimalReportCreateRequest request, MultipartFile photo, String userEmail) {
        String photoUrl = null;
        if (photo != null && !photo.isEmpty()) {
            photoUrl = storageService.uploadFile(photo, "reports");
        }

        Point point = geometryFactory.createPoint(new Coordinate(request.getLongitude(), request.getLatitude()));

        // İlanlarla birebir aynı ters geokodlama servisinden il ve ilçe türetimi
        String city = geocodingService.extractCity(request.getLatitude(), request.getLongitude());
        String district = geocodingService.extractDistrict(request.getLatitude(), request.getLongitude());

        User reporter = null;
        if (userEmail != null && !userEmail.isBlank() && !"anonymousUser".equals(userEmail)) {
            reporter = userRepository.findByEmail(userEmail).orElse(null);
        }

        AnimalReport report = AnimalReport.builder()
                .reporter(reporter)
                .reporterContact(request.getReporterContact())
                .type(request.getType())
                .note(request.getNote())
                .photoUrl(photoUrl)
                .location(point)
                .city(city)
                .district(district)
                .status(AnimalReport.ReportStatus.YENI)
                .build();

        AnimalReport saved = animalReportRepository.save(report);
        log.info("Yeni ihbar kaydedildi. id={}, district={}", saved.getId(), district);

        notificationService.notifyMunicipality(district, "Yeni bir hayvan ihbarı alındı: " + request.getType());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AnimalReportResponse> getReportsForMunicipality(AnimalReport.ReportStatus status, Pageable pageable) {
        // İlçe bilgisi istek parametresinden değil, doğrudan hazır servisten çözülür
        var scope = municipalityScopeService.mevcutKapsam();
        String ilce = scope.ilce();

        Page<AnimalReport> reports = (status != null)
                ? animalReportRepository.findAllByDistrictIgnoreCaseAndStatus(ilce, status, pageable)
                : animalReportRepository.findAllByDistrictIgnoreCase(ilce, pageable);

        return reports.map(this::mapToResponse);
    }

    @Override
    @Transactional
    public AnimalReportResponse updateStatus(Long reportId, AnimalReport.ReportStatus newStatus) {
        var scope = municipalityScopeService.mevcutKapsam();
        String kurumIlcesi = scope.ilce();
        Long kurumKullaniciId = scope.kurumKullaniciId();

        AnimalReport report = animalReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("İhbar bulunamadı ID: " + reportId));

        // Kurumun sadece kendi ilçesine müdahale edebilme güvencesi
        if (!report.getDistrict().equalsIgnoreCase(kurumIlcesi)) {
            throw new AccessDeniedException("Bu ilçeye ait ihbara müdahale yetkiniz bulunmamaktadır");
        }

        User handledByUser = userRepository.findById(kurumKullaniciId).orElse(null);
        report.setStatus(newStatus);
        report.setHandledByUser(handledByUser);

        log.info("İhbar durumu güncellendi. reportId={}, newStatus={}, handledBy={}", reportId, newStatus, kurumKullaniciId);

        return mapToResponse(report);
    }

    private AnimalReportResponse mapToResponse(AnimalReport report) {
        return AnimalReportResponse.builder()
                .id(report.getId())
                .reporterContact(report.getReporterContact())
                .type(report.getType().name())
                .note(report.getNote())
                .photoUrl(report.getPhotoUrl())
                .latitude(report.getLocation().getY())
                .longitude(report.getLocation().getX())
                .city(report.getCity())
                .district(report.getDistrict())
                .status(report.getStatus().name())
                .createdAt(report.getCreatedAt())
                .build();
    }
}