package com.works.patimati.service;

import com.works.patimati.dto.admin.CreateVetAccountRequest;
import com.works.patimati.dto.vet.VetClinicPublicResponse;
import com.works.patimati.dto.vet.VetClinicResponse;
import com.works.patimati.dto.vet.VetClinicUpsertRequest;
import com.works.patimati.entity.User;
import com.works.patimati.entity.VetClinic;
import com.works.patimati.exception.BusinessException;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.repository.VetClinicRepository;
import com.works.patimati.storage.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Veteriner klinik bilgi kartını okur/yazar (kullanıcı başına tek satır;
 * bkz. V29 + {@link VetClinic}) ve admin'in vet hesabı açmasını sağlar.
 * {@link AlertSubscriptionService} ile AYNI find-or-create-then-save deseni.
 */
@Service
@RequiredArgsConstructor
public class VetClinicService {

    private static final String PHOTO_KEY_PREFIX = "vet-clinics";

    private final VetClinicRepository vetClinicRepository;
    private final UserRepository userRepository;
    private final ImageStorageService imageStorageService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public VetClinicResponse getMine(String userEmail) {
        User user = findUserByEmail(userEmail);
        VetClinic clinic = vetClinicRepository.findByUser_Uid(user.getUid())
                .orElseThrow(() -> new ResourceNotFoundException("Klinik bilgi kartı henüz oluşturulmamış"));
        return toResponse(clinic);
    }

    @Transactional
    public VetClinicResponse upsertMine(String userEmail, VetClinicUpsertRequest request, MultipartFile photo) {
        User user = findUserByEmail(userEmail);

        VetClinic clinic = vetClinicRepository.findByUser_Uid(user.getUid())
                .orElseGet(() -> VetClinic.builder().user(user).build());

        clinic.setName(request.name());
        clinic.setAddress(request.address());
        clinic.setCity(request.city());
        clinic.setDistrict(request.district());
        clinic.setPhone(request.phone());
        clinic.setWorkingHours(request.workingHours());

        if (photo != null && !photo.isEmpty()) {
            String eskiReferans = clinic.getPhotoReference();
            String yeniReferans = imageStorageService.uploadImages(List.of(photo), PHOTO_KEY_PREFIX).get(0);
            clinic.setPhotoReference(yeniReferans);
            if (eskiReferans != null) {
                imageStorageService.deleteImages(List.of(eskiReferans));
            }
        }

        clinic.setUpdatedAt(OffsetDateTime.now());

        return toResponse(vetClinicRepository.save(clinic));
    }

    /** {@code VetDetailPage} için tekil görünüm -- bulunamazsa 404. */
    @Transactional(readOnly = true)
    public VetClinicPublicResponse getPublicById(Long id) {
        VetClinic clinic = vetClinicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Klinik bulunamadı: " + id));
        return toPublicResponse(clinic);
    }

    @Transactional(readOnly = true)
    public Page<VetClinicPublicResponse> listPublic(Pageable pageable, String cityFilter) {
        Page<VetClinic> page = (cityFilter == null || cityFilter.isBlank())
                ? vetClinicRepository.findAll(pageable)
                : vetClinicRepository.findByCityIgnoreCase(cityFilter.trim(), pageable);
        return page.map(this::toPublicResponse);
    }

    @Transactional
    public void createVetAccount(CreateVetAccountRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new BusinessException("Bu e-posta adresi zaten kullanımda.");
        }

        User vet = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(User.Role.VET)
                .enabled(true)
                .build();

        userRepository.save(vet);
    }

    private String resolvePhotoUrl(String photoReference) {
        return photoReference == null ? null : imageStorageService.createTemporaryReadUrl(photoReference);
    }

    private VetClinicResponse toResponse(VetClinic clinic) {
        return new VetClinicResponse(
                clinic.getId(),
                clinic.getName(),
                clinic.getAddress(),
                clinic.getCity(),
                clinic.getDistrict(),
                clinic.getPhone(),
                clinic.getWorkingHours(),
                resolvePhotoUrl(clinic.getPhotoReference())
        );
    }

    private VetClinicPublicResponse toPublicResponse(VetClinic clinic) {
        return new VetClinicPublicResponse(
                clinic.getId(),
                clinic.getName(),
                clinic.getAddress(),
                clinic.getCity(),
                clinic.getDistrict(),
                clinic.getPhone(),
                clinic.getWorkingHours(),
                resolvePhotoUrl(clinic.getPhotoReference()),
                clinic.getUser().getUid()
        );
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + email));
    }
}
