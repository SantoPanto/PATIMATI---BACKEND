package com.works.patimati.service;

import com.works.patimati.dto.petshop.PetShopReviewResponse;
import com.works.patimati.dto.petshop.PetShopReviewUpsertRequest;
import com.works.patimati.entity.PetShop;
import com.works.patimati.entity.PetShopReview;
import com.works.patimati.entity.User;
import com.works.patimati.exception.BusinessException;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.PetShopRepository;
import com.works.patimati.repository.PetShopReviewRepository;
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
 * {@code ShelterReviewServiceTest} ile birebir aynı kalıp: find-or-create-
 * then-save (yazar başına tek satır, upsert) + sahiplik kontrolü, {@code petShop}
 * alanına göre.
 */
class PetShopReviewServiceTest {

    private static final String YAZAR_EPOSTA = "musteri@ornek.com";
    private static final String SAHIP_EPOSTA = "petshop@ornek.com";

    private PetShopReviewRepository petShopReviewRepository;
    private PetShopRepository petShopRepository;
    private UserRepository userRepository;
    private NotificationService notificationService;
    private PetShopReviewService service;

    private User yazar;
    private User petshopSahibi;
    private PetShop petshop;

    @BeforeEach
    void hazirla() {
        petShopReviewRepository = mock(PetShopReviewRepository.class);
        petShopRepository = mock(PetShopRepository.class);
        userRepository = mock(UserRepository.class);
        notificationService = mock(NotificationService.class);
        service = new PetShopReviewService(petShopReviewRepository, petShopRepository, userRepository,
                notificationService);

        yazar = User.builder().uid(1L).email(YAZAR_EPOSTA).firstName("Ali").lastName("Yılmaz").build();
        petshopSahibi = User.builder().uid(2L).email(SAHIP_EPOSTA).role(User.Role.PETSHOP)
                .firstName("Ayşe").lastName("Petshop").build();
        petshop = PetShop.builder().id(5L).user(petshopSahibi).name("Pati Petshop").build();

        when(userRepository.findByEmail(YAZAR_EPOSTA)).thenReturn(Optional.of(yazar));
        when(userRepository.findByEmail(SAHIP_EPOSTA)).thenReturn(Optional.of(petshopSahibi));
        when(petShopRepository.findById(5L)).thenReturn(Optional.of(petshop));
        when(petShopReviewRepository.save(any(PetShopReview.class)))
                .thenAnswer(cagri -> cagri.getArgument(0));
    }

    @Test
    void ilkYorumOlusturulurVePetshopaBildirimGider() {
        when(petShopReviewRepository.findByPetShop_IdAndAuthor_Uid(5L, 1L)).thenReturn(Optional.empty());

        PetShopReviewResponse yanit = service.upsertMine(YAZAR_EPOSTA, 5L,
                new PetShopReviewUpsertRequest(5, "Çok ilgililer"));

        assertThat(yanit.rating()).isEqualTo(5);
        assertThat(yanit.comment()).isEqualTo("Çok ilgililer");
        assertThat(yanit.canEdit()).isTrue();
        verify(notificationService).createAndSend(
                eq(petshopSahibi), any(), any(), eq("PETSHOP_REVIEW"), any());
    }

    @Test
    void mevcutYorumGuncellenirYeniSatirAcilmazVeIkinciDuzenlemedeBildirimTekrarGitmez() {
        PetShopReview mevcut = PetShopReview.builder()
                .id(9L).petShop(petshop).author(yazar).rating(3).comment("Fena değil").build();
        when(petShopReviewRepository.findByPetShop_IdAndAuthor_Uid(5L, 1L)).thenReturn(Optional.of(mevcut));

        PetShopReviewResponse yanit = service.upsertMine(YAZAR_EPOSTA, 5L,
                new PetShopReviewUpsertRequest(4, "Daha da iyi"));

        assertThat(yanit.id()).isEqualTo(9L);
        assertThat(yanit.rating()).isEqualTo(4);
        assertThat(yanit.comment()).isEqualTo("Daha da iyi");
        verify(petShopReviewRepository, never()).save(org.mockito.ArgumentMatchers.argThat(
                r -> r != mevcut));
        verify(notificationService, never()).createAndSend(any(), any(), any(), any(), any());
    }

    @Test
    void kendiPetshopunaYorumYapilamaz() {
        when(petShopReviewRepository.findByPetShop_IdAndAuthor_Uid(5L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.upsertMine(SAHIP_EPOSTA, 5L,
                new PetShopReviewUpsertRequest(5, "Kendi petshopum")))
                .isInstanceOf(BusinessException.class);

        verify(petShopReviewRepository, never()).save(any());
    }

    @Test
    void olmayanPetshopaYorumYapilamazKaynakYokHatasiVerir() {
        when(petShopRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.upsertMine(YAZAR_EPOSTA, 999L,
                new PetShopReviewUpsertRequest(5, "Yorum")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listedeCanEditYalnizcaYazaraTrueDoner() {
        PetShopReview yazarinYorumu = PetShopReview.builder()
                .id(1L).petShop(petshop).author(yazar).rating(5).comment("Harika").build();
        User baskaKullanici = User.builder().uid(3L).firstName("Veli").lastName("Demir").build();
        PetShopReview baskasininYorumu = PetShopReview.builder()
                .id(2L).petShop(petshop).author(baskaKullanici).rating(2).comment("İdare eder").build();

        Pageable pageable = PageRequest.of(0, 20);
        when(petShopReviewRepository.findByPetShop_IdOrderByCreatedAtDesc(5L, pageable))
                .thenReturn(new PageImpl<>(java.util.List.of(yazarinYorumu, baskasininYorumu)));

        var sayfa = service.listForPetShop(5L, pageable, 1L);

        assertThat(sayfa.getContent()).hasSize(2);
        assertThat(sayfa.getContent().get(0).canEdit()).isTrue();
        assertThat(sayfa.getContent().get(1).canEdit()).isFalse();
    }

    @Test
    void anonimGoruntuleyicideHicbirYorumDuzenlenemezGorunur() {
        PetShopReview yazarinYorumu = PetShopReview.builder()
                .id(1L).petShop(petshop).author(yazar).rating(5).comment("Harika").build();
        Pageable pageable = PageRequest.of(0, 20);
        when(petShopReviewRepository.findByPetShop_IdOrderByCreatedAtDesc(5L, pageable))
                .thenReturn(new PageImpl<>(java.util.List.of(yazarinYorumu)));

        var sayfa = service.listForPetShop(5L, pageable, null);

        assertThat(sayfa.getContent().get(0).canEdit()).isFalse();
    }

    @Test
    void getMineYorumYoksaKaynakYokHatasiVerir() {
        when(petShopReviewRepository.findByPetShop_IdAndAuthor_Uid(5L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMine(YAZAR_EPOSTA, 5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getMineYorumVarsaDoner() {
        PetShopReview mevcut = PetShopReview.builder()
                .id(9L).petShop(petshop).author(yazar).rating(3).comment("Fena değil").build();
        when(petShopReviewRepository.findByPetShop_IdAndAuthor_Uid(5L, 1L)).thenReturn(Optional.of(mevcut));

        PetShopReviewResponse yanit = service.getMine(YAZAR_EPOSTA, 5L);

        assertThat(yanit.id()).isEqualTo(9L);
        assertThat(yanit.canEdit()).isTrue();
    }

    @Test
    void silmeYalnizcaPetShopIdVeCallerUidIleSinirli() {
        PetShopReview mevcut = PetShopReview.builder()
                .id(9L).petShop(petshop).author(yazar).rating(3).comment("Fena değil").build();
        when(petShopReviewRepository.findByPetShop_IdAndAuthor_Uid(5L, 1L)).thenReturn(Optional.of(mevcut));

        service.deleteMine(YAZAR_EPOSTA, 5L);

        ArgumentCaptor<PetShopReview> silinen = ArgumentCaptor.forClass(PetShopReview.class);
        verify(petShopReviewRepository).delete(silinen.capture());
        assertThat(silinen.getValue().getId()).isEqualTo(9L);
        verify(petShopReviewRepository, never()).deleteById(any());
        verify(petShopReviewRepository, never()).findById(any());
    }

    @Test
    void silmeBaskasininYorumunaErisemezKaynakYokHatasiVerir() {
        when(petShopReviewRepository.findByPetShop_IdAndAuthor_Uid(5L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteMine(YAZAR_EPOSTA, 5L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(petShopReviewRepository, never()).delete(any());
    }
}
