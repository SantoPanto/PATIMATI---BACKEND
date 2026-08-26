package com.works.patimati.service.impl;

import com.works.patimati.dto.request.AnimalReportCreateRequest;
import com.works.patimati.dto.response.AnimalReportResponse;
import com.works.patimati.entity.AnimalReport;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.ReportStatus;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.AnimalReportRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.service.AnimalReportService;
import com.works.patimati.service.GeocodingService;
import com.works.patimati.service.MunicipalityScopeService;
import com.works.patimati.service.NotificationService;
import com.works.patimati.storage.ImageStorageService;
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

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnimalReportServiceImpl implements AnimalReportService {

    private final AnimalReportRepository animalReportRepository;
    private final UserRepository userRepository;
    private final ImageStorageService imageStorageService;
    private final GeocodingService geocodingService;
    private final NotificationService notificationService;
    private final MunicipalityScopeService municipalityScopeService;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Override
    @Transactional
    public AnimalReportResponse createPublicReport(AnimalReportCreateRequest request, MultipartFile photo, String userEmail) {
        String photoReference = null;

        // 1. Fotoğraf Yükleme (Doğrulama ve boyut kontrolü servisin içinde)
        if (photo != null && !photo.isEmpty()) {
            photoReference = imageStorageService.uploadImages(List.of(photo)).get(0);
        }

        try {
            // 2. Nokta (Geometry Point) Üretimi
            Point point = geometryFactory.createPoint(new Coordinate(request.getLongitude(), request.getLatitude()));

            // 3. İlanlardaki Ters Geokodlamanın Aynısı ile İl/İlçe Türetimi
            String city = geocodingService.extractCity(request.getLatitude(), request.getLongitude());
            String district = geocodingService.extractDistrict(request.getLatitude(), request.getLongitude());

            // 4. Giriş Yapmış Kullanıcı Varsa Bağlama
            User reporter = null;
            if (userEmail != null && !userEmail.isBlank() && !"anonymousUser".equals(userEmail)) {
                reporter = userRepository.findByEmail(userEmail).orElse(null);
            }

            // 5. İhbar Kaydını Oluşturma
            AnimalReport report = AnimalReport.builder()
                    .reporter(reporter)
                    .reporterContact(request.getReporterContact())
                    .type(request.getType())
                    .note(request.getNote())
                    .photoUrl(photoReference)
                    .location(point)
                    .city(city)
                    .district(district)
                    .status(ReportStatus.YENI)
                    .build();

            AnimalReport saved = animalReportRepository.save(report);
            log.info("Yeni ihbar kaydedildi. id={}, district={}", saved.getId(), district);

            // 6. Belediyeye Asenkron Bildirim Gönderme
            notificationService.notifyMunicipality(district, "Yeni bir hayvan ihbarı alındı: " + request.getType());

            return mapToResponse(saved);

        } catch (Exception ex) {
            // Hata durumunda depoda sahipsiz dosya kalmaması için güvenli silme
            deletePhotoSafely(photoReference);
            log.error("İhbar kaydedilirken hata oluştu, yüklenen fotoğraf temizlendi: {}", photoReference, ex);
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AnimalReportResponse> getReportsForMunicipality(ReportStatus status, Pageable pageable) {
        var scope = municipalityScopeService.mevcutKapsam();
        String ilce = scope.ilce();

        Page<AnimalReport> reports = (status != null)
                ? animalReportRepository.findAllByDistrictIgnoreCaseAndStatus(ilce, status, pageable)
                : animalReportRepository.findAllByDistrictIgnoreCase(ilce, pageable);

        return reports.map(this::mapToResponse);
    }

    @Override
    @Transactional
    public AnimalReportResponse updateStatus(Long reportId, ReportStatus newStatus) {
        var scope = municipalityScopeService.mevcutKapsam();
        String kurumIlcesi = scope.ilce();
        Long kurumKullaniciId = scope.kurumKullaniciId();

        AnimalReport report = animalReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("İhbar bulunamadı ID: " + reportId));

        if (!report.getDistrict().equalsIgnoreCase(kurumIlcesi)) {
            throw new AccessDeniedException("Bu ilçeye ait ihbara müdahale yetkiniz bulunmamaktadır");
        }

        User handledByUser = userRepository.findById(kurumKullaniciId).orElse(null);
        report.setStatus(newStatus);
        report.setHandledByUser(handledByUser);

        log.info("İhbar durumu güncellendi. reportId={}, newStatus={}, handledBy={}", reportId, newStatus, kurumKullaniciId);

        return mapToResponse(report);
    }

    private void deletePhotoSafely(String photoReference) {
        if (photoReference != null && !photoReference.isBlank()) {
            try {
                imageStorageService.deleteImages(List.of(photoReference));
            } catch (Exception e) {
                log.warn("Fotoğraf silinirken hata oluştu: {}", photoReference, e);
            }
        }
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