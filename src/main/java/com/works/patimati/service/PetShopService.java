package com.works.patimati.service;

import com.works.patimati.dto.admin.CreatePetShopAccountRequest;
import com.works.patimati.dto.petshop.PetShopPublicResponse;
import com.works.patimati.dto.petshop.PetShopResponse;
import com.works.patimati.dto.petshop.PetShopUpsertRequest;
import com.works.patimati.entity.PetShop;
import com.works.patimati.entity.User;
import com.works.patimati.exception.BusinessException;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.PetShopRepository;
import com.works.patimati.repository.PetShopReviewRepository;
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
 * Petshop bilgi kartını okur/yazar (kullanıcı başına tek satır; bkz. V35 +
 * {@link PetShop}) ve admin'in petshop hesabı açmasını sağlar --
 * {@link VetClinicService} ile AYNI find-or-create-then-save deseni, hayvan
 * türü işleme YOK. Dükkan bazlı ortalama puan {@link PetShopReviewRepository}
 * ile hesaplanır (ürün bazlı puanlama ayrıca {@link PetShopProductService}'te
 * var, ikisi ayrı kavramlar).
 */
@Service
@RequiredArgsConstructor
public class PetShopService {

    private static final String PHOTO_KEY_PREFIX = "petshops";
    private static final int WGS_84_SRID = 4326;

    private final PetShopRepository petShopRepository;
    private final UserRepository userRepository;
    private final ImageStorageService imageStorageService;
    private final PasswordEncoder passwordEncoder;
    private final PetShopReviewRepository petShopReviewRepository;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), WGS_84_SRID);

    @Transactional(readOnly = true)
    public PetShopResponse getMine(String userEmail) {
        User user = findUserByEmail(userEmail);
        PetShop petShop = petShopRepository.findByUser_Uid(user.getUid())
                .orElseThrow(() -> new ResourceNotFoundException("Petshop kartı henüz oluşturulmamış"));
        return toResponse(petShop);
    }

    @Transactional
    public PetShopResponse upsertMine(String userEmail, PetShopUpsertRequest request, MultipartFile photo) {
        User user = findUserByEmail(userEmail);

        PetShop petShop = petShopRepository.findByUser_Uid(user.getUid())
                .orElseGet(() -> PetShop.builder().user(user).build());

        petShop.setName(request.name());
        petShop.setAddress(request.address());
        petShop.setCity(request.city());
        petShop.setDistrict(request.district());
        petShop.setPhone(request.phone());
        petShop.setWorkingHours(request.workingHours());

        // Lat/lng ikisi birlikte gönderilmeli ya da hiç gönderilmemeli
        // (DTO'daki @AssertTrue). Yalnızca ikisi birden gönderildiğinde konum
        // güncellenir/oluşturulur -- gönderilmezse mevcut konum korunur
        // (fotoğrafın "gönderilmezse mevcut korunur" mantığıyla tutarlı).
        if (request.latitude() != null && request.longitude() != null) {
            petShop.setLocation(toPoint(request.latitude(), request.longitude()));
        }

        if (photo != null && !photo.isEmpty()) {
            String eskiReferans = petShop.getPhotoReference();
            String yeniReferans = imageStorageService.uploadImages(List.of(photo), PHOTO_KEY_PREFIX).get(0);
            petShop.setPhotoReference(yeniReferans);
            if (eskiReferans != null) {
                imageStorageService.deleteImages(List.of(eskiReferans));
            }
        }

        petShop.setUpdatedAt(OffsetDateTime.now());

        return toResponse(petShopRepository.save(petShop));
    }

    /** {@code PetShopDetailPage} için tekil görünüm -- bulunamazsa 404. */
    @Transactional(readOnly = true)
    public PetShopPublicResponse getPublicById(Long id) {
        PetShop petShop = petShopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Petshop bulunamadı: " + id));
        return toPublicResponse(petShop);
    }

    @Transactional(readOnly = true)
    public Page<PetShopPublicResponse> listPublic(Pageable pageable, String cityFilter) {
        Page<PetShop> page = (cityFilter == null || cityFilter.isBlank())
                ? petShopRepository.findAll(pageable)
                : petShopRepository.findByCityIgnoreCase(cityFilter.trim(), pageable);
        return page.map(this::toPublicResponse);
    }

    @Transactional
    public void createPetShopAccount(CreatePetShopAccountRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new BusinessException("Bu e-posta adresi zaten kullanımda.");
        }

        User petshopOwner = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(User.Role.PETSHOP)
                .enabled(true)
                .build();

        userRepository.save(petshopOwner);
    }

    private String resolvePhotoUrl(String photoReference) {
        return photoReference == null ? null : imageStorageService.createTemporaryReadUrl(photoReference);
    }

    private PetShopResponse toResponse(PetShop petShop) {
        Point location = petShop.getLocation();
        Double averageRating = petShopReviewRepository.findAverageRating(petShop.getId());
        long reviewCount = petShopReviewRepository.countByPetShop_Id(petShop.getId());

        return new PetShopResponse(
                petShop.getId(),
                petShop.getName(),
                petShop.getAddress(),
                petShop.getCity(),
                petShop.getDistrict(),
                petShop.getPhone(),
                petShop.getWorkingHours(),
                resolvePhotoUrl(petShop.getPhotoReference()),
                location == null ? null : location.getY(),
                location == null ? null : location.getX(),
                averageRating,
                (int) reviewCount
        );
    }

    private PetShopPublicResponse toPublicResponse(PetShop petShop) {
        Point location = petShop.getLocation();
        Double averageRating = petShopReviewRepository.findAverageRating(petShop.getId());
        long reviewCount = petShopReviewRepository.countByPetShop_Id(petShop.getId());

        return new PetShopPublicResponse(
                petShop.getId(),
                petShop.getName(),
                petShop.getAddress(),
                petShop.getCity(),
                petShop.getDistrict(),
                petShop.getPhone(),
                petShop.getWorkingHours(),
                resolvePhotoUrl(petShop.getPhotoReference()),
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
