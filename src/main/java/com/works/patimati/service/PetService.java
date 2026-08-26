package com.works.patimati.service;

import com.works.patimati.dto.pet.PetResponse;
import com.works.patimati.dto.pet.PetUpsertRequest;
import com.works.patimati.entity.Pet;
import com.works.patimati.entity.PetPhoto;
import com.works.patimati.entity.User;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.PetPhotoRepository;
import com.works.patimati.repository.PetRepository;
import com.works.patimati.repository.PetTreatmentNoteRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.storage.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Kullanıcının kendi "Evcil Hayvanlarım" kayıtlarını okur/yazar --
 * {@link FavoriteAdService} ile AYNI "kullanıcı sahipli çoklu kayıt" deseni,
 * fotoğraf yükleme için {@code VetClinicService} ile AYNI
 * {@link ImageStorageService} deseni.
 *
 * <p>{@link #toResponse(Pet)} public'tir -- {@code VetCustomerService} de
 * hayvan yanıtı üretirken bunu çağırır, iki katmanda aynı eşleme kopyalanmaz.</p>
 */
@Service
@RequiredArgsConstructor
public class PetService {

    private static final String PHOTO_KEY_PREFIX = "pets";

    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final ImageStorageService imageStorageService;
    private final PetPhotoRepository petPhotoRepository;
    private final PetTreatmentNoteRepository petTreatmentNoteRepository;

    @Transactional(readOnly = true)
    public List<PetResponse> listMine(String ownerEmail) {
        User owner = findUserByEmail(ownerEmail);
        return petRepository.findByOwner_Uid(owner.getUid()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PetResponse create(
            String ownerEmail,
            PetUpsertRequest request,
            MultipartFile photo,
            List<MultipartFile> extraPhotos
    ) {
        User owner = findUserByEmail(ownerEmail);

        Pet pet = Pet.builder()
                .owner(owner)
                .name(request.name())
                .species(request.species())
                .breed(request.breed())
                .gender(request.gender())
                .ageGroup(request.ageGroup())
                .birthDate(request.birthDate())
                .sterilized(request.sterilized())
                .microchipNumber(request.microchipNumber())
                .chronicConditions(request.chronicConditions())
                .allergies(request.allergies())
                .build();

        if (photo != null && !photo.isEmpty()) {
            pet.setPhotoReference(imageStorageService.uploadImages(List.of(photo), PHOTO_KEY_PREFIX).get(0));
        }

        Pet saved = petRepository.save(pet);
        saveExtraPhotos(saved, extraPhotos);

        return toResponse(saved);
    }

    @Transactional
    public PetResponse update(
            String ownerEmail,
            Long petId,
            PetUpsertRequest request,
            MultipartFile photo,
            List<MultipartFile> extraPhotos
    ) {
        Pet pet = findOwnedPet(ownerEmail, petId);

        pet.setName(request.name());
        pet.setSpecies(request.species());
        pet.setBreed(request.breed());
        pet.setGender(request.gender());
        pet.setAgeGroup(request.ageGroup());
        pet.setBirthDate(request.birthDate());
        pet.setSterilized(request.sterilized());
        pet.setMicrochipNumber(request.microchipNumber());
        pet.setChronicConditions(request.chronicConditions());
        pet.setAllergies(request.allergies());

        if (photo != null && !photo.isEmpty()) {
            String eskiReferans = pet.getPhotoReference();
            pet.setPhotoReference(imageStorageService.uploadImages(List.of(photo), PHOTO_KEY_PREFIX).get(0));
            if (eskiReferans != null) {
                imageStorageService.deleteImages(List.of(eskiReferans));
            }
        }

        pet.setUpdatedAt(OffsetDateTime.now());
        Pet saved = petRepository.save(pet);

        saveExtraPhotos(saved, extraPhotos);

        return toResponse(saved);
    }

    @Transactional
    public void delete(String ownerEmail, Long petId) {
        Pet pet = findOwnedPet(ownerEmail, petId);
        List<String> tumFotograflar = new ArrayList<>();
        if (pet.getPhotoReference() != null) {
            tumFotograflar.add(pet.getPhotoReference());
        }
        petPhotoRepository.findByPet_IdOrderBySortOrderAsc(petId)
                .forEach(p -> tumFotograflar.add(p.getPhotoReference()));

        if (!tumFotograflar.isEmpty()) {
            imageStorageService.deleteImages(tumFotograflar);
        }
        petRepository.delete(pet);
    }

    /**
     * "Ben Neyim?" AI raporunu (ham JSON) hayvana kaydeder -- yalnızca sahip.
     * Alanlar burada modellenmez, {@code PetResponse.aiReport} olduğu gibi
     * geri döner (bkz. entity javadoc'u).
     */
    @Transactional
    public PetResponse saveAiReport(String ownerEmail, Long petId, String reportJson) {
        Pet pet = findOwnedPet(ownerEmail, petId);
        pet.setAiReport(reportJson);
        pet.setAiReportAt(OffsetDateTime.now());
        return toResponse(petRepository.save(pet));
    }

    private void saveExtraPhotos(Pet pet, List<MultipartFile> extraPhotos) {
        if (extraPhotos == null || extraPhotos.isEmpty()) {
            return;
        }
        List<MultipartFile> doluOlanlar = extraPhotos.stream().filter(f -> f != null && !f.isEmpty()).toList();
        if (doluOlanlar.isEmpty()) {
            return;
        }

        List<String> referanslar = imageStorageService.uploadImages(doluOlanlar, PHOTO_KEY_PREFIX);
        int mevcutMaxSira = petPhotoRepository.findByPet_IdOrderBySortOrderAsc(pet.getId()).stream()
                .mapToInt(PetPhoto::getSortOrder)
                .max()
                .orElse(-1);

        for (int i = 0; i < referanslar.size(); i++) {
            petPhotoRepository.save(PetPhoto.builder()
                    .pet(pet)
                    .photoReference(referanslar.get(i))
                    .sortOrder(mevcutMaxSira + 1 + i)
                    .build());
        }
    }

    /** Sahiplik doğrulamalı okuma -- {@code AdService}'teki desenle AYNI. */
    private Pet findOwnedPet(String ownerEmail, Long petId) {
        User owner = findUserByEmail(ownerEmail);
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Hayvan bulunamadı: " + petId));
        if (!pet.getOwner().getUid().equals(owner.getUid())) {
            throw new AccessDeniedException("Bu hayvan size ait değil");
        }
        return pet;
    }

    PetResponse toResponse(Pet pet) {
        String photoUrl = pet.getPhotoReference() == null
                ? null
                : imageStorageService.createTemporaryReadUrl(pet.getPhotoReference());

        List<String> galeri = new ArrayList<>();
        if (photoUrl != null) {
            galeri.add(photoUrl);
        }
        petPhotoRepository.findByPet_IdOrderBySortOrderAsc(pet.getId()).forEach(
                p -> galeri.add(imageStorageService.createTemporaryReadUrl(p.getPhotoReference()))
        );

        long notSayisi = pet.getId() == null ? 0 : petTreatmentNoteRepository.countByPet_Id(pet.getId());
        OffsetDateTime sonZiyaret = pet.getId() == null
                ? null
                : petTreatmentNoteRepository.findTopByPet_IdOrderByCreatedAtDesc(pet.getId())
                        .map(n -> n.getCreatedAt())
                        .orElse(null);

        return new PetResponse(
                pet.getId(), pet.getName(), pet.getSpecies(), pet.getBreed(),
                pet.getGender(), pet.getAgeGroup(), photoUrl,
                Collections.unmodifiableList(galeri),
                pet.getBirthDate(), pet.getSterilized(), pet.getMicrochipNumber(),
                pet.getChronicConditions(), pet.getAllergies(),
                pet.getAiReport(), pet.getAiReportAt(),
                notSayisi, sonZiyaret
        );
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + email));
    }
}
