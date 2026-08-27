package com.works.patimati.service;

import com.works.patimati.dto.request.BusinessApplicationCreateRequest;
import com.works.patimati.dto.response.BusinessApplicationResponse;
import com.works.patimati.entity.BusinessApplication;
import com.works.patimati.entity.PetShop;
import com.works.patimati.entity.Shelter;
import com.works.patimati.entity.User;
import com.works.patimati.entity.VetClinic;
import com.works.patimati.entity.enums.BusinessApplicationStatus;
import com.works.patimati.entity.enums.BusinessType;
import com.works.patimati.exception.BusinessException;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.BusinessApplicationRepository;
import com.works.patimati.repository.PetShopRepository;
import com.works.patimati.repository.ShelterRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.repository.VetClinicRepository;
import com.works.patimati.storage.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Kullanıcının veteriner/petshop/barınak sahibi olma başvurusunu alır, admin
 * onay/red kararını uygular. Onayda {@link User#getRole()} değişir VE
 * {@link VetClinic}/{@link PetShop}/{@link Shelter} kartı başvurudaki
 * bilgilerden otomatik oluşturulur -- {@code VetClinicService.createVetAccount}'un
 * aksine (orada kart boş kalır, kullanıcı panelden kendi doldurur).
 */
@Service
@RequiredArgsConstructor
public class BusinessApplicationService {

    private static final String PHOTO_KEY_PREFIX = "business-applications";
    private static final int WGS_84_SRID = 4326;

    private final BusinessApplicationRepository businessApplicationRepository;
    private final UserRepository userRepository;
    private final VetClinicRepository vetClinicRepository;
    private final PetShopRepository petShopRepository;
    private final ShelterRepository shelterRepository;
    private final ImageStorageService imageStorageService;
    private final NotificationService notificationService;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), WGS_84_SRID);

    @Transactional
    public BusinessApplicationResponse submit(String applicantEmail, BusinessApplicationCreateRequest request, MultipartFile photo) {
        User applicant = findUserByEmail(applicantEmail);

        if (applicant.getRole() == User.Role.VET || applicant.getRole() == User.Role.PETSHOP
                || applicant.getRole() == User.Role.BARINAK || applicant.getRole() == User.Role.ADMIN) {
            throw new BusinessException("Zaten bir işletme/yönetici hesabınız var.");
        }
        if (applicant.getRole() == User.Role.INSTITUTION) {
            // 27.08 inceleme bulgusu: kurum (belediye) hesabı başvurabiliyordu
            // ve ONAY rolün üstüne yazdığı için belediye yetkisi sessizce
            // silinirdi. Belediye hesabı işletmeye dönüştürülmez.
            throw new BusinessException("Kurum (belediye) hesabı işletme başvurusu yapamaz.");
        }
        if (businessApplicationRepository.existsByApplicant_UidAndStatus(applicant.getUid(), BusinessApplicationStatus.BEKLEMEDE)) {
            throw new BusinessException("Zaten incelenmekte olan bir başvurunuz var.");
        }

        BusinessApplication application = BusinessApplication.builder()
                .applicant(applicant)
                .businessType(request.businessType())
                .name(request.name())
                .address(request.address())
                .city(request.city())
                .district(request.district())
                .phone(request.phone())
                .workingHours(request.workingHours())
                .animalTypes(request.businessType() == BusinessType.VET ? request.animalTypes() : Set.of())
                .status(BusinessApplicationStatus.BEKLEMEDE)
                .build();

        if (request.latitude() != null && request.longitude() != null) {
            application.setLocation(toPoint(request.latitude(), request.longitude()));
        }

        if (photo != null && !photo.isEmpty()) {
            application.setPhotoReference(imageStorageService.uploadImages(List.of(photo), PHOTO_KEY_PREFIX).get(0));
        }

        BusinessApplication saved = businessApplicationRepository.save(application);

        for (User admin : userRepository.findAllByRole(User.Role.ADMIN)) {
            notificationService.createAndSend(
                    admin,
                    "Yeni kurum başvurusu",
                    applicant.getFirstName() + " " + applicant.getLastName() + " işletme sahibi olmak için başvurdu: " + request.name(),
                    "BUSINESS_APPLICATION",
                    Map.of("applicationId", String.valueOf(saved.getId()))
            );
        }

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public BusinessApplicationResponse getMine(String applicantEmail) {
        User applicant = findUserByEmail(applicantEmail);
        BusinessApplication application = businessApplicationRepository
                .findFirstByApplicant_UidOrderByCreatedAtDesc(applicant.getUid())
                .orElseThrow(() -> new ResourceNotFoundException("Başvuru bulunamadı"));
        return toResponse(application);
    }

    @Transactional(readOnly = true)
    public Page<BusinessApplicationResponse> listForAdmin(BusinessApplicationStatus status, BusinessType type, Pageable pageable) {
        Page<BusinessApplication> page;
        if (status == null && type == null) {
            page = businessApplicationRepository.findAll(pageable);
        } else if (type == null) {
            page = businessApplicationRepository.findAllByStatus(status, pageable);
        } else if (status == null) {
            // 27.08 inceleme bulgusu: yalnız tür süzülürken durum sessizce
            // BEKLEMEDE'ye zorlanıyordu — "tüm VET başvuruları" yalnız
            // bekleyenleri gösteriyordu.
            page = businessApplicationRepository.findAllByBusinessType(type, pageable);
        } else {
            page = businessApplicationRepository.findAllByStatusAndBusinessType(status, type, pageable);
        }
        return page.map(this::toResponse);
    }

    @Transactional
    public BusinessApplicationResponse approve(Long id, String adminEmail) {
        User admin = findUserByEmail(adminEmail);
        BusinessApplication application = findPendingById(id);

        application.setStatus(BusinessApplicationStatus.ONAYLANDI);
        application.setDecidedAt(OffsetDateTime.now());
        application.setDecidedByAdmin(admin);

        User applicant = application.getApplicant();
        if (applicant.getRole() != User.Role.USER) {
            // Başvuru ile onay arasında rol değişmiş olabilir (örn. hesap bu
            // arada kuruma yükseltildi ya da başka başvurusu onaylandı).
            // Rolün üstüne sessizce yazmak yetki siler — açık hata daha doğru.
            throw new BusinessException(
                    "Başvuru sahibinin rolü bu arada değişmiş (" + applicant.getRole()
                            + "); onaylamadan önce başvuruyu reddedin ya da hesabı kontrol edin.");
        }
        applicant.setRole(toRole(application.getBusinessType()));
        userRepository.save(applicant);

        createBusinessCard(application);

        BusinessApplication saved = businessApplicationRepository.save(application);

        notificationService.createAndSend(
                applicant,
                "Başvurunuz onaylandı",
                "İşletme başvurunuz onaylandı. Tekrar giriş yaparak panelinize ulaşabilirsiniz.",
                "BUSINESS_APPLICATION_APPROVED",
                Map.of("applicationId", String.valueOf(saved.getId()))
        );

        return toResponse(saved);
    }

    @Transactional
    public BusinessApplicationResponse reject(Long id, String adminEmail, String reason) {
        User admin = findUserByEmail(adminEmail);
        BusinessApplication application = findPendingById(id);

        application.setStatus(BusinessApplicationStatus.REDDEDILDI);
        application.setRejectionReason(reason);
        application.setDecidedAt(OffsetDateTime.now());
        application.setDecidedByAdmin(admin);

        BusinessApplication saved = businessApplicationRepository.save(application);

        notificationService.createAndSend(
                application.getApplicant(),
                "Başvurunuz reddedildi",
                "İşletme başvurunuz reddedildi. Sebep: " + reason,
                "BUSINESS_APPLICATION_REJECTED",
                Map.of("applicationId", String.valueOf(saved.getId()))
        );

        return toResponse(saved);
    }

    /**
     * Onaylanmış bir başvuruyu kurumun kendisiyle birlikte tamamen kaldırır:
     * {@link #createBusinessCard} ile oluşturulan VetClinic/PetShop/Shelter
     * kartı (ve buna bağlı yorum/ürün satırları, DB'de {@code ON DELETE CASCADE})
     * silinir, kullanıcının rolü tekrar {@code USER}'a döner, ardından başvuru
     * kaydı silinir.
     */
    @Transactional
    public void delete(Long id) {
        BusinessApplication application = businessApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Başvuru bulunamadı: " + id));
        if (application.getStatus() != BusinessApplicationStatus.ONAYLANDI) {
            throw new BusinessException("Yalnızca onaylanmış başvurular silinebilir.");
        }

        User owner = application.getApplicant();
        deleteBusinessCard(application.getBusinessType(), owner);

        // Sadece hâlâ bu başvurunun verdiği roldeyse USER'a döndür -- rol
        // aradan geçen sürede başka bir yolla değiştiyse (ör. ADMIN'e
        // yükseltildiyse) üzerine yazmayalım.
        if (owner.getRole() == toRole(application.getBusinessType())) {
            owner.setRole(User.Role.USER);
            userRepository.save(owner);
        }

        businessApplicationRepository.delete(application);
    }

    private void deleteBusinessCard(BusinessType businessType, User owner) {
        switch (businessType) {
            case VET -> vetClinicRepository.findByUser_Uid(owner.getUid())
                    .ifPresent(vetClinicRepository::delete);
            case PETSHOP -> petShopRepository.findByUser_Uid(owner.getUid())
                    .ifPresent(petShopRepository::delete);
            case BARINAK -> shelterRepository.findByUser_Uid(owner.getUid())
                    .ifPresent(shelterRepository::delete);
        }
    }

    private void createBusinessCard(BusinessApplication application) {
        User owner = application.getApplicant();
        switch (application.getBusinessType()) {
            case VET -> vetClinicRepository.save(VetClinic.builder()
                    .user(owner)
                    .name(application.getName())
                    .address(application.getAddress())
                    .city(application.getCity())
                    .district(application.getDistrict())
                    .phone(application.getPhone())
                    .workingHours(application.getWorkingHours())
                    .photoReference(application.getPhotoReference())
                    .location(application.getLocation())
                    // Yüklenmiş entity'nin PersistentSet'i ikinci bir entity'ye
                    // verilemez ("shared references to a collection") — kopya.
                    .animalTypes(new java.util.LinkedHashSet<>(application.getAnimalTypes()))
                    .build());
            case PETSHOP -> petShopRepository.save(PetShop.builder()
                    .user(owner)
                    .name(application.getName())
                    .address(application.getAddress())
                    .city(application.getCity())
                    .district(application.getDistrict())
                    .phone(application.getPhone())
                    .workingHours(application.getWorkingHours())
                    .photoReference(application.getPhotoReference())
                    .location(application.getLocation())
                    .build());
            case BARINAK -> shelterRepository.save(Shelter.builder()
                    .user(owner)
                    .name(application.getName())
                    .address(application.getAddress())
                    .city(application.getCity())
                    .district(application.getDistrict())
                    .phone(application.getPhone())
                    .workingHours(application.getWorkingHours())
                    .photoReference(application.getPhotoReference())
                    .location(application.getLocation())
                    .build());
        }
    }

    private User.Role toRole(BusinessType businessType) {
        return switch (businessType) {
            case VET -> User.Role.VET;
            case PETSHOP -> User.Role.PETSHOP;
            case BARINAK -> User.Role.BARINAK;
        };
    }

    private BusinessApplication findPendingById(Long id) {
        BusinessApplication application = businessApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Başvuru bulunamadı: " + id));
        if (application.getStatus() != BusinessApplicationStatus.BEKLEMEDE) {
            throw new BusinessException("Bu başvuru zaten karara bağlanmış.");
        }
        return application;
    }

    private Point toPoint(BigDecimal latitude, BigDecimal longitude) {
        Coordinate coordinate = new Coordinate(longitude.doubleValue(), latitude.doubleValue());
        return geometryFactory.createPoint(coordinate);
    }

    private String resolvePhotoUrl(String photoReference) {
        return photoReference == null ? null : imageStorageService.createTemporaryReadUrl(photoReference);
    }

    private BusinessApplicationResponse toResponse(BusinessApplication application) {
        Point location = application.getLocation();
        User applicant = application.getApplicant();
        return new BusinessApplicationResponse(
                application.getId(),
                application.getBusinessType(),
                application.getName(),
                application.getAddress(),
                application.getCity(),
                application.getDistrict(),
                application.getPhone(),
                application.getWorkingHours(),
                resolvePhotoUrl(application.getPhotoReference()),
                location == null ? null : location.getY(),
                location == null ? null : location.getX(),
                application.getAnimalTypes(),
                application.getStatus(),
                application.getRejectionReason(),
                application.getCreatedAt(),
                application.getDecidedAt(),
                applicant.getUid(),
                applicant.getFirstName() + " " + applicant.getLastName(),
                applicant.getEmail()
        );
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + email));
    }
}
