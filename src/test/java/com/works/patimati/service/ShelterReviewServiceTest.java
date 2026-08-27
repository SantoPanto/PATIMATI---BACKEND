package com.works.patimati.service;

import com.works.patimati.dto.shelter.ShelterReviewResponse;
import com.works.patimati.dto.shelter.ShelterReviewUpsertRequest;
import com.works.patimati.entity.Shelter;
import com.works.patimati.entity.ShelterReview;
import com.works.patimati.entity.User;
import com.works.patimati.exception.BusinessException;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.ShelterRepository;
import com.works.patimati.repository.ShelterReviewRepository;
import com.works.patimati.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@code VetClinicReviewServiceTest} ile birebir aynı kalıp: find-or-create-
 * then-save (yazar başına tek satır, upsert) + sahiplik kontrolü, {@code shelter}
 * alanına göre.
 */
class ShelterReviewServiceTest {

    private static final String YAZAR_EPOSTA = "musteri@ornek.com";
    private static final String SAHIP_EPOSTA = "barinak@ornek.com";

    private ShelterReviewRepository shelterReviewRepository;
    private ShelterRepository shelterRepository;
    private UserRepository userRepository;
    private NotificationService notificationService;
    private ShelterReviewService service;

    private User yazar;
    private User barinakSahibi;
    private Shelter barinak;

    @BeforeEach
    void hazirla() {
        shelterReviewRepository = mock(ShelterReviewRepository.class);
        shelterRepository = mock(ShelterRepository.class);
        userRepository = mock(UserRepository.class);
        notificationService = mock(NotificationService.class);
        service = new ShelterReviewService(shelterReviewRepository, shelterRepository, userRepository,
                notificationService);

        yazar = User.builder().uid(1L).email(YAZAR_EPOSTA).firstName("Ali").lastName("Yılmaz").build();
        barinakSahibi = User.builder().uid(2L).email(SAHIP_EPOSTA).role(User.Role.BARINAK)
                .firstName("Ayşe").lastName("Barınak").build();
        barinak = Shelter.builder().id(5L).user(barinakSahibi).name("Pati Barınağı").build();

        when(userRepository.findByEmail(YAZAR_EPOSTA)).thenReturn(Optional.of(yazar));
        when(userRepository.findByEmail(SAHIP_EPOSTA)).thenReturn(Optional.of(barinakSahibi));
        when(shelterRepository.findById(5L)).thenReturn(Optional.of(barinak));
        when(shelterReviewRepository.save(any(ShelterReview.class)))
                .thenAnswer(cagri -> cagri.getArgument(0));
    }

    @Test
    void ilkYorumOlusturulurVeBarinagaBildirimGider() {
        when(shelterReviewRepository.findByShelter_IdAndAuthor_Uid(5L, 1L)).thenReturn(Optional.empty());

        ShelterReviewResponse yanit = service.upsertMine(YAZAR_EPOSTA, 5L,
                new ShelterReviewUpsertRequest(5, "Çok ilgililer"));

        assertThat(yanit.rating()).isEqualTo(5);
        assertThat(yanit.comment()).isEqualTo("Çok ilgililer");
        assertThat(yanit.canEdit()).isTrue();
        verify(notificationService).createAndSend(
                eq(barinakSahibi), any(), any(), eq("SHELTER_REVIEW"), any());
    }

    @Test
    void mevcutYorumGuncellenirYeniSatirAcilmazVeIkinciDuzenlemedeBildirimTekrarGitmez() {
        ShelterReview mevcut = ShelterReview.builder()
                .id(9L).shelter(barinak).author(yazar).rating(3).comment("Fena değil").build();
        when(shelterReviewRepository.findByShelter_IdAndAuthor_Uid(5L, 1L)).thenReturn(Optional.of(mevcut));

        ShelterReviewResponse yanit = service.upsertMine(YAZAR_EPOSTA, 5L,
                new ShelterReviewUpsertRequest(4, "Daha da iyi"));

        assertThat(yanit.id()).isEqualTo(9L);
        assertThat(yanit.rating()).isEqualTo(4);
        assertThat(yanit.comment()).isEqualTo("Daha da iyi");
        verify(shelterReviewRepository, never()).save(org.mockito.ArgumentMatchers.argThat(
                r -> r != mevcut));
        verify(notificationService, never()).createAndSend(any(), any(), any(), any(), any());
    }

    @Test
    void kendiBarinaginaYorumYapilamaz() {
        when(shelterReviewRepository.findByShelter_IdAndAuthor_Uid(5L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.upsertMine(SAHIP_EPOSTA, 5L,
                new ShelterReviewUpsertRequest(5, "Kendi barinagim")))
                .isInstanceOf(BusinessException.class);

        verify(shelterReviewRepository, never()).save(any());
    }

    @Test
    void olmayanBarinagaYorumYapilamazKaynakYokHatasiVerir() {
        when(shelterRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.upsertMine(YAZAR_EPOSTA, 999L,
                new ShelterReviewUpsertRequest(5, "Yorum")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listedeCanEditYalnizcaYazaraTrueDoner() {
        ShelterReview yazarinYorumu = ShelterReview.builder()
                .id(1L).shelter(barinak).author(yazar).rating(5).comment("Harika").build();
        User baskaKullanici = User.builder().uid(3L).firstName("Veli").lastName("Demir").build();
        ShelterReview baskasininYorumu = ShelterReview.builder()
                .id(2L).shelter(barinak).author(baskaKullanici).rating(2).comment("İdare eder").build();

        Pageable pageable = PageRequest.of(0, 20);
        when(shelterReviewRepository.findByShelter_IdOrderByCreatedAtDesc(5L, pageable))
                .thenReturn(new PageImpl<>(java.util.List.of(yazarinYorumu, baskasininYorumu)));

        var sayfa = service.listForShelter(5L, pageable, 1L);

        assertThat(sayfa.getContent()).hasSize(2);
        assertThat(sayfa.getContent().get(0).canEdit()).isTrue();
        assertThat(sayfa.getContent().get(1).canEdit()).isFalse();
    }

    @Test
    void anonimGoruntuleyicideHicbirYorumDuzenlenemezGorunur() {
        ShelterReview yazarinYorumu = ShelterReview.builder()
                .id(1L).shelter(barinak).author(yazar).rating(5).comment("Harika").build();
        Pageable pageable = PageRequest.of(0, 20);
        when(shelterReviewRepository.findByShelter_IdOrderByCreatedAtDesc(5L, pageable))
                .thenReturn(new PageImpl<>(java.util.List.of(yazarinYorumu)));

        var sayfa = service.listForShelter(5L, pageable, null);

        assertThat(sayfa.getContent().get(0).canEdit()).isFalse();
    }

    @Test
    void getMineYorumYoksaKaynakYokHatasiVerir() {
        when(shelterReviewRepository.findByShelter_IdAndAuthor_Uid(5L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMine(YAZAR_EPOSTA, 5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getMineYorumVarsaDoner() {
        ShelterReview mevcut = ShelterReview.builder()
                .id(9L).shelter(barinak).author(yazar).rating(3).comment("Fena değil").build();
        when(shelterReviewRepository.findByShelter_IdAndAuthor_Uid(5L, 1L)).thenReturn(Optional.of(mevcut));

        ShelterReviewResponse yanit = service.getMine(YAZAR_EPOSTA, 5L);

        assertThat(yanit.id()).isEqualTo(9L);
        assertThat(yanit.canEdit()).isTrue();
    }

    @Test
    void silmeYalnizcaShelterIdVeCallerUidIleSinirli() {
        ShelterReview mevcut = ShelterReview.builder()
                .id(9L).shelter(barinak).author(yazar).rating(3).comment("Fena değil").build();
        when(shelterReviewRepository.findByShelter_IdAndAuthor_Uid(5L, 1L)).thenReturn(Optional.of(mevcut));

        service.deleteMine(YAZAR_EPOSTA, 5L);

        ArgumentCaptor<ShelterReview> silinen = ArgumentCaptor.forClass(ShelterReview.class);
        verify(shelterReviewRepository).delete(silinen.capture());
        assertThat(silinen.getValue().getId()).isEqualTo(9L);
        verify(shelterReviewRepository, never()).deleteById(any());
        verify(shelterReviewRepository, never()).findById(any());
    }

    @Test
    void silmeBaskasininYorumunaErisemezKaynakYokHatasiVerir() {
        when(shelterReviewRepository.findByShelter_IdAndAuthor_Uid(5L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteMine(YAZAR_EPOSTA, 5L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(shelterReviewRepository, never()).delete(any());
    }
}
