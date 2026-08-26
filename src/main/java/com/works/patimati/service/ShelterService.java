package com.works.patimati.service;

import com.works.patimati.dto.admin.CreateShelterAccountRequest;
import com.works.patimati.dto.shelter.ShelterPublicResponse;
import com.works.patimati.dto.shelter.ShelterResponse;
import com.works.patimati.dto.shelter.ShelterUpsertRequest;
import com.works.patimati.entity.Shelter;
import com.works.patimati.entity.User;
import com.works.patimati.exception.BusinessException;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.ShelterRepository;
import com.works.patimati.repository.ShelterReviewRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.storage.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Barınak bilgi kartını okur/yazar (kullanıcı başına tek satır; bkz. V36 +
 * {@link Shelter}) ve admin'in barınak hesabı açmasını sağlar --
 * {@link com.works.patimati.service.PetShopService} ile AYNI
 * find-or-create-then-save deseni (hayvan türü işleme YOK), artı
 * {@link com.works.patimati.service.VetClinicService}'in puan hesaplama
 * mantığı ({@code ShelterReviewRepository}'den ortalama puan/sayı).
 */
@Service
@RequiredArgsConstructor
public class ShelterService {

    private static final String PHOTO_KEY_PREFIX = "shelters";
    private static final int WGS_84_SRID = 4326;

    private final ShelterRepository shelterRepository;
    private final UserRepository userRepository;
    private final ImageStorageService imageStorageService;
    private final PasswordEncoder passwordEncoder;
    private final ShelterReviewRepository shelterReviewRepository;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), WGS_84_SRID);

    @Transactional(readOnly = true)
    public ShelterResponse getMine(String userEmail) {
        User user = findUserByEmail(userEmail);
        Shelter shelter = shelterRepository.findByUser_Uid(user.getUid())
                .orElseThrow(() -> new ResourceNotFoundException("Barınak kartı henüz oluşturulmamış"));
        return toResponse(shelter);
    }

    @Transactional
    public ShelterResponse upsertMine(String userEmail, ShelterUpsertRequest request, MultipartFile photo) {
        User user = findUserByEmail(userEmail);

        Shelter shelter = shelterRepository.findByUser_Uid(user.getUid())
                .orElseGet(() -> Shelter.builder().user(user).build());

        shelter.setName(request.name());
        shelter.setAddress(request.address());
        shelter.setCity(request.city());
        shelter.setDistrict(request.district());
        shelter.setPhone(request.phone());
        shelter.setWorkingHours(request.workingHours());

        // Lat/lng ikisi birlikte gönderilmeli ya da hiç gönderilmemeli
        // (DTO'daki @AssertTrue). Yalnızca ikisi birden gönderildiğinde konum
        // güncellenir/oluşturulur -- gönderilmezse mevcut konum korunur
        // (fotoğrafın "gönderilmezse mevcut korunur" mantığıyla tutarlı).
        if (request.latitude() != null && request.longitude() != null) {
            shelter.setLocation(toPoint(request.latitude(), request.longitude()));
        }

        if (photo != null && !photo.isEmpty()) {
            String eskiReferans = shelter.getPhotoReference();
            String yeniReferans = imageStorageService.uploadImages(List.of(photo), PHOTO_KEY_PREFIX).get(0);
            shelter.setPhotoReference(yeniReferans);
            if (eskiReferans != null) {
                imageStorageService.deleteImages(List.of(eskiReferans));
            }
        }

        shelter.setUpdatedAt(OffsetDateTime.now());

        return toResponse(shelterRepository.save(shelter));
    }

    /** {@code ShelterDetailPage} için tekil görünüm -- bulunamazsa 404. */
    @Transactional(readOnly = true)
    public ShelterPublicResponse getPublicById(Long id) {
        Shelter shelter = shelterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Barınak bulunamadı: " + id));
        return toPublicResponse(shelter);
    }

    @Transactional(readOnly = true)
    public Page<ShelterPublicResponse> listPublic(Pageable pageable, String cityFilter) {
        Page<Shelter> page = (cityFilter == null || cityFilter.isBlank())
                ? shelterRepository.findAll(pageable)
                : shelterRepository.findByCityIgnoreCase(cityFilter.trim(), pageable);
        return page.map(this::toPublicResponse);
    }

    /**
     * {@code Ad}'de {@code shelter_id} FK'ı YOK, yalnızca {@code user_id} var
     * -- "bu barınağın ilanları" ucu önce kart id'sini sahip uid'ine
     * çevirmeli (bkz. {@code ShelterDirectoryController#getAdoptions},
     * {@code AdoptionService#getPublicAdoptionAdsByOwner}).
     */
    @Transactional(readOnly = true)
    public Long resolveOwnerUid(Long shelterId) {
        Shelter shelter = shelterRepository.findById(shelterId)
                .orElseThrow(() -> new ResourceNotFoundException("Barınak bulunamadı: " + shelterId));
        return shelter.getUser().getUid();
    }

    @Transactional
    public void createShelterAccount(CreateShelterAccountRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new BusinessException("Bu e-posta adresi zaten kullanımda.");
        }

        User shelterOwner = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(User.Role.BARINAK)
                .enabled(true)
                .build();

        userRepository.save(shelterOwner);
    }

    private String resolvePhotoUrl(String photoReference) {
        return photoReference == null ? null : imageStorageService.createTemporaryReadUrl(photoReference);
    }

    private ShelterResponse toResponse(Shelter shelter) {
        Point location = shelter.getLocation();
        Double averageRating = shelterReviewRepository.findAverageRating(shelter.getId());
        long reviewCount = shelterReviewRepository.countByShelter_Id(shelter.getId());

        return new ShelterResponse(
                shelter.getId(),
                shelter.getName(),
                shelter.getAddress(),
                shelter.getCity(),
                shelter.getDistrict(),
                shelter.getPhone(),
                shelter.getWorkingHours(),
                resolvePhotoUrl(shelter.getPhotoReference()),
                location == null ? null : location.getY(),
                location == null ? null : location.getX(),
                averageRating,
                (int) reviewCount
        );
    }

    private ShelterPublicResponse toPublicResponse(Shelter shelter) {
        Point location = shelter.getLocation();
        Double averageRating = shelterReviewRepository.findAverageRating(shelter.getId());
        long reviewCount = shelterReviewRepository.countByShelter_Id(shelter.getId());

        return new ShelterPublicResponse(
                shelter.getId(),
                shelter.getName(),
                shelter.getAddress(),
                shelter.getCity(),
                shelter.getDistrict(),
                shelter.getPhone(),
                shelter.getWorkingHours(),
                resolvePhotoUrl(shelter.getPhotoReference()),
                location == null ? null : location.getY(),
                location == null ? null : location.getX(),
                averageRating,
                (int) reviewCount
        );
    }

    private Point toPoint(BigDecimal latitude, BigDecimal longitude) {
        Coordinate coordinate = new Coordinate(longitude.doubleValue(), latitude.doubleValue());
        return geometryFactory.createPoint(coordinate);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + email));
    }
}
