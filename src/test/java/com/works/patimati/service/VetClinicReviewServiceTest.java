package com.works.patimati.service;

import com.works.patimati.dto.vet.VetClinicReviewResponse;
import com.works.patimati.dto.vet.VetClinicReviewUpsertRequest;
import com.works.patimati.entity.User;
import com.works.patimati.entity.VetClinic;
import com.works.patimati.entity.VetClinicReview;
import com.works.patimati.exception.BusinessException;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.repository.VetClinicRepository;
import com.works.patimati.repository.VetClinicReviewRepository;
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
 * {@code VetCustomerServiceTest} ile AYNI kalıp: find-or-create-then-save
 * (yazar başına tek satır, upsert) + sahiplik kontrolü.
 */
class VetClinicReviewServiceTest {

    private static final String YAZAR_EPOSTA = "musteri@ornek.com";
    private static final String SAHIP_EPOSTA = "vet@ornek.com";

    private VetClinicReviewRepository vetClinicReviewRepository;
    private VetClinicRepository vetClinicRepository;
    private UserRepository userRepository;
    private NotificationService notificationService;
    private VetClinicReviewService service;

    private User yazar;
    private User klinikSahibi;
    private VetClinic klinik;

    @BeforeEach
    void hazirla() {
        vetClinicReviewRepository = mock(VetClinicReviewRepository.class);
        vetClinicRepository = mock(VetClinicRepository.class);
        userRepository = mock(UserRepository.class);
        notificationService = mock(NotificationService.class);
        service = new VetClinicReviewService(vetClinicReviewRepository, vetClinicRepository, userRepository,
                notificationService);

        yazar = User.builder().uid(1L).email(YAZAR_EPOSTA).firstName("Ali").lastName("Yılmaz").build();
        klinikSahibi = User.builder().uid(2L).email(SAHIP_EPOSTA).role(User.Role.VET)
                .firstName("Ayşe").lastName("Vet").build();
        klinik = VetClinic.builder().id(5L).user(klinikSahibi).name("Pati Veteriner").build();

        when(userRepository.findByEmail(YAZAR_EPOSTA)).thenReturn(Optional.of(yazar));
        when(userRepository.findByEmail(SAHIP_EPOSTA)).thenReturn(Optional.of(klinikSahibi));
        when(vetClinicRepository.findById(5L)).thenReturn(Optional.of(klinik));
        when(vetClinicReviewRepository.save(any(VetClinicReview.class)))
                .thenAnswer(cagri -> cagri.getArgument(0));
    }

    @Test
    void ilkYorumOlusturulurVeVeteBildirimGider() {
        when(vetClinicReviewRepository.findByVetClinic_IdAndAuthor_Uid(5L, 1L)).thenReturn(Optional.empty());

        VetClinicReviewResponse yanit = service.upsertMine(YAZAR_EPOSTA, 5L,
                new VetClinicReviewUpsertRequest(5, "Çok ilgililer"));

        assertThat(yanit.rating()).isEqualTo(5);
        assertThat(yanit.comment()).isEqualTo("Çok ilgililer");
        assertThat(yanit.canEdit()).isTrue();
        verify(notificationService).createAndSend(
                eq(klinikSahibi), any(), any(), eq("VET_CLINIC_REVIEW"), any());
    }

    @Test
    void mevcutYorumGuncellenirYeniSatirAcilmazVeIkinciDuzenlemedeBildirimTekrarGitmez() {
        VetClinicReview mevcut = VetClinicReview.builder()
                .id(9L).vetClinic(klinik).author(yazar).rating(3).comment("Fena değil").build();
        when(vetClinicReviewRepository.findByVetClinic_IdAndAuthor_Uid(5L, 1L)).thenReturn(Optional.of(mevcut));

        VetClinicReviewResponse yanit = service.upsertMine(YAZAR_EPOSTA, 5L,
                new VetClinicReviewUpsertRequest(4, "Daha da iyi"));

        assertThat(yanit.id()).isEqualTo(9L);
        assertThat(yanit.rating()).isEqualTo(4);
        assertThat(yanit.comment()).isEqualTo("Daha da iyi");
        verify(vetClinicReviewRepository, never()).save(org.mockito.ArgumentMatchers.argThat(
                r -> r != mevcut));
        verify(notificationService, never()).createAndSend(any(), any(), any(), any(), any());
    }

    @Test
    void kendiKlinigineYorumYapilamaz() {
        when(vetClinicReviewRepository.findByVetClinic_IdAndAuthor_Uid(5L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.upsertMine(SAHIP_EPOSTA, 5L,
                new VetClinicReviewUpsertRequest(5, "Kendi klinigim")))
                .isInstanceOf(BusinessException.class);

        verify(vetClinicReviewRepository, never()).save(any());
    }

    @Test
    void olmayanKlinigeYorumYapilamazKaynakYokHatasiVerir() {
        when(vetClinicRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.upsertMine(YAZAR_EPOSTA, 999L,
                new VetClinicReviewUpsertRequest(5, "Yorum")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listedeCanEditYalnizcaYazaraTrueDoner() {
        VetClinicReview yazarinYorumu = VetClinicReview.builder()
                .id(1L).vetClinic(klinik).author(yazar).rating(5).comment("Harika").build();
        User baskaKullanici = User.builder().uid(3L).firstName("Veli").lastName("Demir").build();
        VetClinicReview baskasininYorumu = VetClinicReview.builder()
                .id(2L).vetClinic(klinik).author(baskaKullanici).rating(2).comment("İdare eder").build();

        Pageable pageable = PageRequest.of(0, 20);
        when(vetClinicReviewRepository.findByVetClinic_IdOrderByCreatedAtDesc(5L, pageable))
                .thenReturn(new PageImpl<>(java.util.List.of(yazarinYorumu, baskasininYorumu)));

        var sayfa = service.listForClinic(5L, pageable, 1L);

        assertThat(sayfa.getContent()).hasSize(2);
        assertThat(sayfa.getContent().get(0).canEdit()).isTrue();
        assertThat(sayfa.getContent().get(1).canEdit()).isFalse();
    }

    @Test
    void anonimGoruntuleyicideHicbirYorumDuzenlenemezGorunur() {
        VetClinicReview yazarinYorumu = VetClinicReview.builder()
                .id(1L).vetClinic(klinik).author(yazar).rating(5).comment("Harika").build();
        Pageable pageable = PageRequest.of(0, 20);
        when(vetClinicReviewRepository.findByVetClinic_IdOrderByCreatedAtDesc(5L, pageable))
                .thenReturn(new PageImpl<>(java.util.List.of(yazarinYorumu)));

        var sayfa = service.listForClinic(5L, pageable, null);

        assertThat(sayfa.getContent().get(0).canEdit()).isFalse();
    }

    @Test
    void getMineYorumYoksaKaynakYokHatasiVerir() {
        when(vetClinicReviewRepository.findByVetClinic_IdAndAuthor_Uid(5L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMine(YAZAR_EPOSTA, 5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getMineYorumVarsaDoner() {
        VetClinicReview mevcut = VetClinicReview.builder()
                .id(9L).vetClinic(klinik).author(yazar).rating(3).comment("Fena değil").build();
        when(vetClinicReviewRepository.findByVetClinic_IdAndAuthor_Uid(5L, 1L)).thenReturn(Optional.of(mevcut));

        VetClinicReviewResponse yanit = service.getMine(YAZAR_EPOSTA, 5L);

        assertThat(yanit.id()).isEqualTo(9L);
        assertThat(yanit.canEdit()).isTrue();
    }

    @Test
    void silmeYalnizcaClinicIdVeCallerUidIleSinirli() {
        VetClinicReview mevcut = VetClinicReview.builder()
                .id(9L).vetClinic(klinik).author(yazar).rating(3).comment("Fena değil").build();
        when(vetClinicReviewRepository.findByVetClinic_IdAndAuthor_Uid(5L, 1L)).thenReturn(Optional.of(mevcut));

        service.deleteMine(YAZAR_EPOSTA, 5L);

        ArgumentCaptor<VetClinicReview> silinen = ArgumentCaptor.forClass(VetClinicReview.class);
        verify(vetClinicReviewRepository).delete(silinen.capture());
        assertThat(silinen.getValue().getId()).isEqualTo(9L);
        verify(vetClinicReviewRepository, never()).deleteById(any());
        verify(vetClinicReviewRepository, never()).findById(any());
    }

    @Test
    void silmeBaskasininYorumunaErisemezKaynakYokHatasiVerir() {
        when(vetClinicReviewRepository.findByVetClinic_IdAndAuthor_Uid(5L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteMine(YAZAR_EPOSTA, 5L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(vetClinicReviewRepository, never()).delete(any());
    }
}
