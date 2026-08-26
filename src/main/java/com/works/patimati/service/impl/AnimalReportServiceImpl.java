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
    private final GeocodingService geocodingService;
    private final NotificationService notificationService;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Override
    @Transactional
    public AnimalReportResponse createPublicReport(AnimalReportCreateRequest request, MultipartFile photo, String userEmail) {
        // 1. Fotoğraf Yükleme (Opsiyonel / Varsa S3'e kaydet)
        String photoUrl = null;
        if (photo != null && !photo.isEmpty()) {
            photoUrl = storageService.uploadFile(photo, "reports");
        }

        // 2. Nokta (Geometry Point) Üretimi
        Point point = geometryFactory.createPoint(new Coordinate(request.getLongitude(), request.getLatitude()));

        // 3. Ters Geokodlama ile İl/İlçe Bilgisini Sunucuda Türetme
        String city = geocodingService.extractCity(request.getLatitude(), request.getLongitude());
        String district = geocodingService.extractDistrict(request.getLatitude(), request.getLongitude());

        // 4. Giriş Yapmış Kullanıcı Varsa İlişkilendirme
        User reporter = null;
        if (userEmail != null && !userEmail.isBlank() && !"anonymousUser".equals(userEmail)) {
            reporter = userRepository.findByEmail(userEmail).orElse(null);
        }

        // 5. İhbar Nesnesini Kaydetme
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

        // 6. İlgili İlçe Belediyesine Asenkron Bildirim Gönderme
        notificationService.notifyMunicipality(district, "Yeni bir hayvan ihbarı alındı: " + request.getType());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AnimalReportResponse> getReportsForMunicipality(String userEmail, AnimalReport.ReportStatus status, Pageable pageable) {
        User institution = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kurum kullanıcısı bulunamadı"));

        String district = institution.getInstitutionDistrict();
        if (district == null || district.isBlank()) {
            throw new AccessDeniedException("Yetkili ilçe bilgisi bulunamadı");
        }

        Page<AnimalReport> reports = (status != null)
                ? animalReportRepository.findAllByDistrictIgnoreCaseAndStatus(district, status, pageable)
                : animalReportRepository.findAllByDistrictIgnoreCase(district, pageable);

        return reports.map(this::mapToResponse);
    }

    @Override
    @Transactional
    public AnimalReportResponse updateStatus(Long reportId, AnimalReport.ReportStatus newStatus, String userEmail) {
        User institution = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        AnimalReport report = animalReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("İhbar bulunamadı ID: " + reportId));

        // Güvenlik: Kurum yalnızca kendi ilçesindeki ihbarın durumunu değiştirebilir
        if (!report.getDistrict().equalsIgnoreCase(institution.getInstitutionDistrict())) {
            throw new AccessDeniedException("Bu ilçeye ait ihbara müdahale yetkiniz yok");
        }

        report.setStatus(newStatus);
        report.setHandledByUser(institution);
        log.info("İhbar durumu güncellendi. reportId={}, newStatus={}, user={}", reportId, newStatus, userEmail);

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