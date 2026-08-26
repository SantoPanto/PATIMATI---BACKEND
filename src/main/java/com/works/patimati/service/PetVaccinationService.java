package com.works.patimati.service;

import com.works.patimati.dto.pet.AddVaccinationRequest;
import com.works.patimati.dto.pet.PetVaccinationResponse;
import com.works.patimati.entity.Pet;
import com.works.patimati.entity.PetVaccination;
import com.works.patimati.entity.User;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.PetRepository;
import com.works.patimati.repository.PetVaccinationRepository;
import com.works.patimati.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Bir hayvanın aşı kayıtları -- yetki kontrolü {@code PetTreatmentNoteService}
 * ile AYNI ilke (sahip ya da ACCEPTED ilişkideki vet), ekleme de İKİSİNE de
 * açık (aşı, vet notundan farklı olarak sahip tarafından da GEÇMİŞTE
 * yaptırılmış olabilir).
 */
@Service
@RequiredArgsConstructor
public class PetVaccinationService {

    private final PetVaccinationRepository petVaccinationRepository;
    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final VetCustomerService vetCustomerService;

    @Transactional(readOnly = true)
    public List<PetVaccinationResponse> list(String callerEmail, Long petId) {
        Pet pet = findPet(petId);
        assertOwnerOrAcceptedVet(callerEmail, pet);

        return petVaccinationRepository.findByPet_IdOrderByAdministeredDateDesc(petId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PetVaccinationResponse add(String callerEmail, Long petId, AddVaccinationRequest request) {
        User caller = findUserByEmail(callerEmail);
        Pet pet = findPet(petId);
        assertOwnerOrAcceptedVet(callerEmail, pet);

        PetVaccination vaccination = PetVaccination.builder()
                .pet(pet)
                .recordedBy(caller)
                .vaccineName(request.vaccineName())
                .administeredDate(request.administeredDate())
                .nextDueDate(request.nextDueDate())
                .notes(request.notes())
                .build();

        return toResponse(petVaccinationRepository.save(vaccination));
    }

    private void assertOwnerOrAcceptedVet(String callerEmail, Pet pet) {
        User caller = findUserByEmail(callerEmail);
        boolean isOwner = pet.getOwner().getUid().equals(caller.getUid());
        boolean isAcceptedVet = !isOwner
                && vetCustomerService.hasAcceptedRelationship(caller.getUid(), pet.getOwner().getUid());

        if (!isOwner && !isAcceptedVet) {
            throw new AccessDeniedException("Bu hayvanın aşı kayıtlarına erişim yetkiniz yok");
        }
    }

    private Pet findPet(Long petId) {
        return petRepository.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Hayvan bulunamadı: " + petId));
    }

    private PetVaccinationResponse toResponse(PetVaccination vaccination) {
        String kaydeden = vaccination.getRecordedBy() == null
                ? null
                : vaccination.getRecordedBy().getFirstName() + " " + vaccination.getRecordedBy().getLastName();

        return new PetVaccinationResponse(
                vaccination.getId(),
                vaccination.getVaccineName(),
                vaccination.getAdministeredDate(),
                vaccination.getNextDueDate(),
                vaccination.getNotes(),
                kaydeden,
                vaccination.getCreatedAt()
        );
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + email));
    }
}
