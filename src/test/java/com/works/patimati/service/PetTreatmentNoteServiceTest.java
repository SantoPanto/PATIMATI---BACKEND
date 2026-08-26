package com.works.patimati.service;

import com.works.patimati.dto.pet.PetTreatmentNoteResponse;
import com.works.patimati.entity.Pet;
import com.works.patimati.entity.PetTreatmentNote;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.NoteAuthorType;
import com.works.patimati.entity.enums.Species;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.PetRepository;
import com.works.patimati.repository.PetTreatmentNoteRepository;
import com.works.patimati.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PetTreatmentNoteServiceTest {

    private static final String SAHIP_EPOSTA = "sahip@ornek.com";
    private static final String VET_EPOSTA = "vet@ornek.com";
    private static final String BASKA_VET_EPOSTA = "baskavet@ornek.com";

    private PetTreatmentNoteRepository petTreatmentNoteRepository;
    private PetRepository petRepository;
    private UserRepository userRepository;
    private VetCustomerService vetCustomerService;
    private PetTreatmentNoteService service;

    private User sahip;
    private User vet;
    private User baskaVet;
    private Pet hayvan;

    @BeforeEach
    void hazirla() {
        petTreatmentNoteRepository = mock(PetTreatmentNoteRepository.class);
        petRepository = mock(PetRepository.class);
        userRepository = mock(UserRepository.class);
        vetCustomerService = mock(VetCustomerService.class);
        service = new PetTreatmentNoteService(petTreatmentNoteRepository, petRepository, userRepository, vetCustomerService);

        sahip = User.builder().uid(1L).email(SAHIP_EPOSTA).build();
        vet = User.builder().uid(2L).email(VET_EPOSTA).firstName("Ayşe").lastName("Vet").build();
        baskaVet = User.builder().uid(3L).email(BASKA_VET_EPOSTA).build();
        hayvan = Pet.builder().id(5L).owner(sahip).name("Boncuk").species(Species.CAT).build();

        when(userRepository.findByEmail(SAHIP_EPOSTA)).thenReturn(Optional.of(sahip));
        when(userRepository.findByEmail(VET_EPOSTA)).thenReturn(Optional.of(vet));
        when(userRepository.findByEmail(BASKA_VET_EPOSTA)).thenReturn(Optional.of(baskaVet));
        when(petRepository.findById(5L)).thenReturn(Optional.of(hayvan));
        when(petTreatmentNoteRepository.save(any(PetTreatmentNote.class))).thenAnswer(c -> c.getArgument(0));
    }

    @Test
    void musterisiOlmayanVetNotEkleyemez() {
        when(vetCustomerService.hasAcceptedRelationship(3L, 1L)).thenReturn(false);

        assertThatThrownBy(() -> service.addNote(BASKA_VET_EPOSTA, 5L, "kontrol yapıldı"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void musterisiOlanVetNotEkleyebilir() {
        when(vetCustomerService.hasAcceptedRelationship(2L, 1L)).thenReturn(true);

        PetTreatmentNoteResponse yanit = service.addNote(VET_EPOSTA, 5L, "Kuduz aşısı yapıldı");

        ArgumentCaptor<PetTreatmentNote> kaydedilen = ArgumentCaptor.forClass(PetTreatmentNote.class);
        verify(petTreatmentNoteRepository).save(kaydedilen.capture());
        assertThat(kaydedilen.getValue().getContent()).isEqualTo("Kuduz aşısı yapıldı");
        assertThat(yanit.vetName()).isEqualTo("Ayşe Vet");
    }

    @Test
    void hayvanSahibiKendiHayvaninInNotlariniGorebilir() {
        when(petTreatmentNoteRepository.findByPet_IdOrderByCreatedAtDesc(5L)).thenReturn(List.of());

        assertThat(service.listNotes(SAHIP_EPOSTA, 5L)).isEmpty();
    }

    @Test
    void musterisiOlanVetNotlariGorebilir() {
        when(vetCustomerService.hasAcceptedRelationship(2L, 1L)).thenReturn(true);
        when(petTreatmentNoteRepository.findByPet_IdOrderByCreatedAtDesc(5L)).thenReturn(List.of());

        assertThat(service.listNotes(VET_EPOSTA, 5L)).isEmpty();
    }

    @Test
    void ilgisizBirVetNotlariGoremez() {
        when(vetCustomerService.hasAcceptedRelationship(3L, 1L)).thenReturn(false);

        assertThatThrownBy(() -> service.listNotes(BASKA_VET_EPOSTA, 5L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void olmayanHayvaninNotuIstenirseKaynakYokHatasiVerir() {
        when(petRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listNotes(SAHIP_EPOSTA, 999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void sahipKendiHayvaninaNotEkleyebilirVeVetIliskisiAranmaz() {
        PetTreatmentNoteResponse yanit = service.addOwnerNote(SAHIP_EPOSTA, 5L, "Bugün iştahsızdı");

        assertThat(yanit.authorType()).isEqualTo(NoteAuthorType.OWNER);
        assertThat(yanit.canEdit()).isTrue();
        assertThat(yanit.vetName()).isEqualTo(sahip.getFirstName() + " " + sahip.getLastName());
    }

    @Test
    void baskasininHayvaninaSahipNotuEklenemez() {
        User baskasi = User.builder().uid(42L).email("baskasi@ornek.com").build();
        when(userRepository.findByEmail("baskasi@ornek.com")).thenReturn(Optional.of(baskasi));

        assertThatThrownBy(() -> service.addOwnerNote("baskasi@ornek.com", 5L, "not"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void notunYazariOlmayanDuzenleyemez() {
        PetTreatmentNote vetNotu = PetTreatmentNote.builder()
                .id(30L).pet(hayvan).vet(vet).authorType(NoteAuthorType.VET).content("eski").build();
        when(petTreatmentNoteRepository.findById(30L)).thenReturn(Optional.of(vetNotu));

        assertThatThrownBy(() -> service.updateNote(BASKA_VET_EPOSTA, 5L, 30L, "yeni"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void notunYazariOlanVetKendiNotunuDuzenleyebilirVeSilebilir() {
        PetTreatmentNote vetNotu = PetTreatmentNote.builder()
                .id(31L).pet(hayvan).vet(vet).authorType(NoteAuthorType.VET).content("eski").build();
        when(petTreatmentNoteRepository.findById(31L)).thenReturn(Optional.of(vetNotu));

        PetTreatmentNoteResponse yanit = service.updateNote(VET_EPOSTA, 5L, 31L, "yeni içerik");
        assertThat(yanit.content()).isEqualTo("yeni içerik");

        service.deleteNote(VET_EPOSTA, 5L, 31L);
        verify(petTreatmentNoteRepository).delete(vetNotu);
    }

    @Test
    void sahipKendiNotunuDuzenleyebilirVetinNotunuDuzenleyemez() {
        PetTreatmentNote sahipNotu = PetTreatmentNote.builder()
                .id(32L).pet(hayvan).authorType(NoteAuthorType.OWNER).content("eski").build();
        when(petTreatmentNoteRepository.findById(32L)).thenReturn(Optional.of(sahipNotu));

        PetTreatmentNoteResponse yanit = service.updateNote(SAHIP_EPOSTA, 5L, 32L, "güncellendi");
        assertThat(yanit.content()).isEqualTo("güncellendi");

        PetTreatmentNote vetNotu = PetTreatmentNote.builder()
                .id(33L).pet(hayvan).vet(vet).authorType(NoteAuthorType.VET).content("eski").build();
        when(petTreatmentNoteRepository.findById(33L)).thenReturn(Optional.of(vetNotu));

        assertThatThrownBy(() -> service.updateNote(SAHIP_EPOSTA, 5L, 33L, "başkasının notu"))
                .isInstanceOf(AccessDeniedException.class);
    }
}
