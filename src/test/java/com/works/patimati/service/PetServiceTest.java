package com.works.patimati.service;

import com.works.patimati.dto.pet.PetResponse;
import com.works.patimati.dto.pet.PetUpsertRequest;
import com.works.patimati.entity.Pet;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.AgeGroup;
import com.works.patimati.entity.enums.PetGender;
import com.works.patimati.entity.enums.Species;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.PetPhotoRepository;
import com.works.patimati.repository.PetRepository;
import com.works.patimati.repository.PetTreatmentNoteRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.storage.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PetServiceTest {

    private static final String SAHIP_EPOSTA = "sahip@ornek.com";

    private PetRepository petRepository;
    private UserRepository userRepository;
    private ImageStorageService imageStorageService;
    private PetPhotoRepository petPhotoRepository;
    private PetTreatmentNoteRepository petTreatmentNoteRepository;
    private PetService service;
    private User sahip;

    @BeforeEach
    void hazirla() {
        petRepository = mock(PetRepository.class);
        userRepository = mock(UserRepository.class);
        imageStorageService = mock(ImageStorageService.class);
        petPhotoRepository = mock(PetPhotoRepository.class);
        petTreatmentNoteRepository = mock(PetTreatmentNoteRepository.class);
        service = new PetService(
                petRepository, userRepository, imageStorageService,
                petPhotoRepository, petTreatmentNoteRepository
        );

        sahip = User.builder().uid(3L).email(SAHIP_EPOSTA).build();
        when(userRepository.findByEmail(SAHIP_EPOSTA)).thenReturn(Optional.of(sahip));
        when(petRepository.save(any(Pet.class))).thenAnswer(cagri -> cagri.getArgument(0));
    }

    @Test
    void hayvanFotografsizOlusturulur() {
        PetResponse yanit = service.create(SAHIP_EPOSTA,
                new PetUpsertRequest(
                        "Boncuk", Species.CAT, "Tekir", PetGender.FEMALE, AgeGroup.YOUNG,
                        null, null, null, null, null
                ), null, null);

        ArgumentCaptor<Pet> kaydedilen = ArgumentCaptor.forClass(Pet.class);
        verify(petRepository).save(kaydedilen.capture());

        assertThat(kaydedilen.getValue().getOwner()).isSameAs(sahip);
        assertThat(kaydedilen.getValue().getName()).isEqualTo("Boncuk");
        assertThat(yanit.name()).isEqualTo("Boncuk");
        assertThat(yanit.photoUrl()).isNull();
        verify(imageStorageService, never()).uploadImages(any(), anyString());
    }

    @Test
    void hayvanFotografliOlusturulur() {
        MockMultipartFile foto = new MockMultipartFile("photo", "kedi.jpg", "image/jpeg", new byte[]{1, 2, 3});
        when(imageStorageService.uploadImages(List.of(foto), "pets")).thenReturn(List.of("pets/kedi.jpg"));
        when(imageStorageService.createTemporaryReadUrl("pets/kedi.jpg")).thenReturn("https://cdn.test/pets/kedi.jpg");

        PetResponse yanit = service.create(SAHIP_EPOSTA,
                new PetUpsertRequest("Boncuk", Species.CAT, null, null, null, null, null, null, null, null),
                foto, null);

        assertThat(yanit.photoUrl()).isEqualTo("https://cdn.test/pets/kedi.jpg");
    }

    @Test
    void baskasininHayvaniniGuncellemeyeCalisirsaReddedilir() {
        User baskasi = User.builder().uid(99L).email("baska@ornek.com").build();
        Pet baskasininHayvani = Pet.builder().id(5L).owner(baskasi).name("Pamuk").species(Species.DOG).build();
        when(petRepository.findById(5L)).thenReturn(Optional.of(baskasininHayvani));

        assertThatThrownBy(() -> service.update(SAHIP_EPOSTA, 5L,
                new PetUpsertRequest("Yeni İsim", Species.DOG, null, null, null, null, null, null, null, null),
                null, null))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void olmayanHayvanGuncellenmeyeCalisilirsaKaynakYokHatasiVerir() {
        when(petRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(SAHIP_EPOSTA, 404L,
                new PetUpsertRequest("İsim", Species.CAT, null, null, null, null, null, null, null, null),
                null, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void kendiHayvaniniSilebilirVeEskiFotografSilinir() {
        Pet hayvan = Pet.builder().id(7L).owner(sahip).name("Boncuk").species(Species.CAT)
                .photoReference("pets/eski.jpg").build();
        when(petRepository.findById(7L)).thenReturn(Optional.of(hayvan));

        service.delete(SAHIP_EPOSTA, 7L);

        verify(petRepository).delete(hayvan);
        verify(imageStorageService).deleteImages(List.of("pets/eski.jpg"));
    }

    @Test
    void kendiHayvanlariniListeler() {
        Pet birinci = Pet.builder().id(1L).owner(sahip).name("Boncuk").species(Species.CAT).build();
        Pet ikinci = Pet.builder().id(2L).owner(sahip).name("Pamuk").species(Species.DOG).build();
        when(petRepository.findByOwner_Uid(3L)).thenReturn(List.of(birinci, ikinci));

        List<PetResponse> yanit = service.listMine(SAHIP_EPOSTA);

        assertThat(yanit).hasSize(2);
        assertThat(yanit).extracting(PetResponse::name).containsExactly("Boncuk", "Pamuk");
    }
}
