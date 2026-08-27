package com.works.patimati.service;

import com.works.patimati.dto.pet.AddWeightLogRequest;
import com.works.patimati.dto.pet.PetWeightLogResponse;
import com.works.patimati.entity.Pet;
import com.works.patimati.entity.PetWeightLog;
import com.works.patimati.entity.User;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.PetRepository;
import com.works.patimati.repository.PetWeightLogRepository;
import com.works.patimati.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Bir hayvanın ağırlık geçmişi -- yetki kontrolü {@code PetVaccinationService} ile AYNI ilke. */
@Service
@RequiredArgsConstructor
public class PetWeightLogService {

    private final PetWeightLogRepository petWeightLogRepository;
    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final VetCustomerService vetCustomerService;

    @Transactional(readOnly = true)
    public List<PetWeightLogResponse> list(String callerEmail, Long petId) {
        Pet pet = findPet(petId);
        assertOwnerOrAcceptedVet(callerEmail, pet);

        return petWeightLogRepository.findByPet_IdOrderByRecordedAtAsc(petId).stream()
                .map(w -> new PetWeightLogResponse(w.getId(), w.getWeightKg(), w.getRecordedAt()))
                .toList();
    }

    @Transactional
    public PetWeightLogResponse add(String callerEmail, Long petId, AddWeightLogRequest request) {
        User caller = findUserByEmail(callerEmail);
        Pet pet = findPet(petId);
        assertOwnerOrAcceptedVet(callerEmail, pet);

        PetWeightLog log = PetWeightLog.builder()
                .pet(pet)
                .recordedBy(caller)
                .weightKg(request.weightKg())
                .recordedAt(request.recordedAt())
                .build();

        PetWeightLog saved = petWeightLogRepository.save(log);
        return new PetWeightLogResponse(saved.getId(), saved.getWeightKg(), saved.getRecordedAt());
    }

    private void assertOwnerOrAcceptedVet(String callerEmail, Pet pet) {
        User caller = findUserByEmail(callerEmail);
        boolean isOwner = pet.getOwner().getUid().equals(caller.getUid());
        boolean isAcceptedVet = !isOwner
                && vetCustomerService.hasAcceptedRelationship(caller.getUid(), pet.getOwner().getUid());

        if (!isOwner && !isAcceptedVet) {
            throw new AccessDeniedException("Bu hayvanın kilo kayıtlarına erişim yetkiniz yok");
        }
    }

    private Pet findPet(Long petId) {
        return petRepository.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Hayvan bulunamadı: " + petId));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + email));
    }
}
