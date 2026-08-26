package com.works.patimati.service;

import com.works.patimati.dto.pet.PetTreatmentNoteResponse;
import com.works.patimati.entity.Pet;
import com.works.patimati.entity.PetTreatmentNote;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.NoteAuthorType;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.PetRepository;
import com.works.patimati.repository.PetTreatmentNoteRepository;
import com.works.patimati.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Bir hayvana girilen notlar -- ya hayvanın sahibiyle ACCEPTED ilişkisi olan
 * bir VET, ya da hayvanın sahibinin KENDİSİ ekler (26.08: sahip de kendi
 * gözlemini girebilsin). Notu yalnızca ORİJİNAL yazarı düzenleyebilir/silebilir.
 */
@Service
@RequiredArgsConstructor
public class PetTreatmentNoteService {

    private final PetTreatmentNoteRepository petTreatmentNoteRepository;
    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final VetCustomerService vetCustomerService;

    @Transactional
    public PetTreatmentNoteResponse addNote(String vetEmail, Long petId, String content) {
        User vet = findUserByEmail(vetEmail);
        Pet pet = findPet(petId);

        if (!vetCustomerService.hasAcceptedRelationship(vet.getUid(), pet.getOwner().getUid())) {
            throw new AccessDeniedException("Bu hayvanın sahibi müşteriniz değil");
        }

        PetTreatmentNote note = PetTreatmentNote.builder()
                .pet(pet)
                .vet(vet)
                .authorType(NoteAuthorType.VET)
                .content(content)
                .build();

        return toResponse(petTreatmentNoteRepository.save(note), vet.getUid());
    }

    /** Sahip kendi hayvanına kendi gözlemini ekler -- vet ilişkisi ARANMAZ. */
    @Transactional
    public PetTreatmentNoteResponse addOwnerNote(String ownerEmail, Long petId, String content) {
        User owner = findUserByEmail(ownerEmail);
        Pet pet = findPet(petId);

        if (!pet.getOwner().getUid().equals(owner.getUid())) {
            throw new AccessDeniedException("Bu hayvan size ait değil");
        }

        PetTreatmentNote note = PetTreatmentNote.builder()
                .pet(pet)
                .authorType(NoteAuthorType.OWNER)
                .content(content)
                .build();

        return toResponse(petTreatmentNoteRepository.save(note), owner.getUid());
    }

    /**
     * Çağıran ya hayvanın sahibi OLMALI ya da sahiple ACCEPTED ilişkisi olan
     * bir VET OLMALI -- ikisi de değilse reddedilir.
     */
    @Transactional(readOnly = true)
    public List<PetTreatmentNoteResponse> listNotes(String callerEmail, Long petId) {
        User caller = findUserByEmail(callerEmail);
        Pet pet = findPet(petId);

        boolean isOwner = pet.getOwner().getUid().equals(caller.getUid());
        boolean isAcceptedVet = !isOwner
                && vetCustomerService.hasAcceptedRelationship(caller.getUid(), pet.getOwner().getUid());

        if (!isOwner && !isAcceptedVet) {
            throw new AccessDeniedException("Bu hayvanın notlarını görme yetkiniz yok");
        }

        return petTreatmentNoteRepository.findByPet_IdOrderByCreatedAtDesc(petId).stream()
                .map(n -> toResponse(n, caller.getUid()))
                .toList();
    }

    @Transactional
    public PetTreatmentNoteResponse updateNote(String callerEmail, Long petId, Long noteId, String content) {
        User caller = findUserByEmail(callerEmail);
        PetTreatmentNote note = findOwnNote(caller, petId, noteId);
        note.setContent(content);
        return toResponse(petTreatmentNoteRepository.save(note), caller.getUid());
    }

    @Transactional
    public void deleteNote(String callerEmail, Long petId, Long noteId) {
        User caller = findUserByEmail(callerEmail);
        PetTreatmentNote note = findOwnNote(caller, petId, noteId);
        petTreatmentNoteRepository.delete(note);
    }

    /** Notu ORİJİNAL yazarı çağırıyor mu doğrular -- başka vet/sahip düzenleyemez/silemez. */
    private PetTreatmentNote findOwnNote(User caller, Long petId, Long noteId) {
        PetTreatmentNote note = petTreatmentNoteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Not bulunamadı: " + noteId));

        if (!note.getPet().getId().equals(petId)) {
            throw new ResourceNotFoundException("Not bulunamadı: " + noteId);
        }
        if (!yazariMi(note, caller.getUid())) {
            throw new AccessDeniedException("Bu notu düzenleme/silme yetkiniz yok");
        }
        return note;
    }

    private boolean yazariMi(PetTreatmentNote note, Long callerUid) {
        if (note.getAuthorType() == NoteAuthorType.VET) {
            return note.getVet() != null && note.getVet().getUid().equals(callerUid);
        }
        return note.getPet().getOwner().getUid().equals(callerUid);
    }

    private Pet findPet(Long petId) {
        return petRepository.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Hayvan bulunamadı: " + petId));
    }

    private PetTreatmentNoteResponse toResponse(PetTreatmentNote note, Long callerUid) {
        String yazarAdi = note.getAuthorType() == NoteAuthorType.VET
                ? note.getVet().getFirstName() + " " + note.getVet().getLastName()
                : note.getPet().getOwner().getFirstName() + " " + note.getPet().getOwner().getLastName();

        return new PetTreatmentNoteResponse(
                note.getId(),
                note.getContent(),
                yazarAdi,
                note.getAuthorType(),
                yazariMi(note, callerUid),
                note.getCreatedAt()
        );
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + email));
    }
}
