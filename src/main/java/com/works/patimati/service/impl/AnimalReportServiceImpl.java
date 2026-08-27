package com.works.patimati.service.impl;

import com.works.patimati.dto.request.AnimalReportCreateRequest;
import com.works.patimati.dto.response.AnimalReportResponse;
import com.works.patimati.entity.AnimalReport;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.ReportStatus;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.municipality.MunicipalityScopeService;
import com.works.patimati.repository.AnimalReportRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.service.AnimalReportService;
import com.works.patimati.service.NotificationService;
import com.works.patimati.service.ReverseGeocodingService;
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
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnimalReportServiceImpl implements AnimalReportService {

    private final AnimalReportRepository animalReportRepository;
    private final UserRepository userRepository;
    private final ImageStorageService imageStorageService;
    private final ReverseGeocodingService reverseGeocodingService;
    private final NotificationService notificationService;
    private final MunicipalityScopeService municipalityScopeService;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Override
    @Transactional
    public AnimalReportResponse createPublicReport(AnimalReportCreateRequest request, MultipartFile photo, String userEmail) {
        String photoReference = null;

        if (photo != null && !photo.isEmpty()) {
            photoReference = imageStorageService.uploadImages(List.of(photo)).get(0);
        }

        try {
            Point point = geometryFactory.createPoint(new Coordinate(request.getLongitude(), request.getLatitude()));

            String city = null;
            String district = null;
            var cozum = reverseGeocodingService.cozumle(request.getLatitude(), request.getLongitude());
            if (cozum.isPresent()) {
                city = cozum.get().il();
                district = cozum.get().ilce();
            }

            User reporter = null;
            if (userEmail != null && !userEmail.isBlank() && !"anonymousUser".equals(userEmail)) {
                reporter = userRepository.findByEmail(userEmail).orElse(null);
            }

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

            kurumaBildirimGonder(district, saved);

            return mapToResponse(saved);

        } catch (Exception ex) {
            deletePhotoSafely(photoReference);
            log.error("İhbar kaydedilirken hata oluştu, yüklenen fotoğraf temizlendi: {}", photoReference, ex);
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AnimalReportResponse> getReportsForMunicipality(ReportStatus status, Pageable pageable) {
        var scope = municipalityScopeService.mevcutKapsam();

        // İlçesiz yönetici (tumIlceler) bütün kuyruğu görür; kurum hesabı
        // her zaman kendi ilçesine kilitli (MunicipalityScopeService'e bak).
        Page<AnimalReport> reports;
        if (scope.tumIlceler()) {
            reports = (status != null)
                    ? animalReportRepository.findAllByStatus(status, pageable)
                    : animalReportRepository.findAll(pageable);
        } else {
            String ilce = scope.ilce();
            reports = (status != null)
                    ? animalReportRepository.findAllByDistrictIgnoreCaseAndStatus(ilce, status, pageable)
                    : animalReportRepository.findAllByDistrictIgnoreCase(ilce, pageable);
        }

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

        String reportDistrict = report.getDistrict();
        boolean districtMatches = reportDistrict != null
                && kurumIlcesi != null
                && reportDistrict.toLowerCase(java.util.Locale.forLanguageTag("tr"))
                .equals(kurumIlcesi.toLowerCase(java.util.Locale.forLanguageTag("tr")));

        // İlçesiz yönetici (tumIlceler) her ilçenin ihbarına müdahale edebilir.
        if (!scope.tumIlceler() && !districtMatches) {
            throw new AccessDeniedException("Bu ilçeye ait ihbara müdahale yetkiniz bulunmamaktadır");
        }

        User handledByUser = userRepository.findById(kurumKullaniciId).orElse(null);
        report.setStatus(newStatus);
        report.setHandledByUser(handledByUser);

        log.info("İhbar durumu güncellendi. reportId={}, newStatus={}, handledBy={}", reportId, newStatus, kurumKullaniciId);

        return mapToResponse(report);
    }

    /**
     * İlçeye atanmış kurum hesaplarına yeni ihbar bildirimi.
     *
     * <p>{@code NotificationService.notifyMunicipality(...)} diye bir uç yok;
     * mevcut {@code createAndSend(...)} kullanılıyor. {@code notifications.type}
     * serbest String olduğu için göç gerekmiyor.
     *
     * <p>Bildirim gönderilemezse ihbar yine de kaydolur: kaydın kendisi
     * bildirimin başarısına bağlanmaz. İlçe çözülemediyse kimseye gitmez.
     */
    private void kurumaBildirimGonder(String district, AnimalReport report) {
        if (district == null || district.isBlank()) {
            log.warn("İhbar {} için ilçe çözülemedi; kuruma bildirim gönderilmedi.", report.getId());
            return;
        }
        try {
            List<User> kurumlar = userRepository
                    .findByRoleAndInstitutionDistrictIgnoreCase(User.Role.INSTITUTION, district);
            if (kurumlar.isEmpty()) {
                log.info("{} ilçesine atanmış kurum hesabı yok; ihbar {} bildirimsiz kaydedildi.",
                        district, report.getId());
                return;
            }
            for (User kurum : kurumlar) {
                notificationService.createAndSend(
                        kurum,
                        "Yeni hayvan ihbarı",
                        district + " ilçesinde yeni bir ihbar var: " + report.getType().name(),
                        "ANIMAL_REPORT",
                        Map.of("reportId", String.valueOf(report.getId())),
                        report.getId());
            }
        } catch (Exception e) {
            log.warn("Kuruma ihbar bildirimi gönderilemedi. reportId={}", report.getId(), e);
        }
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