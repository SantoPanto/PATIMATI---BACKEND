package com.works.patimati.service;

import com.works.patimati.dto.vetcustomer.VetCustomerRequestResponse;
import com.works.patimati.dto.vetcustomer.VetCustomerResponse;
import com.works.patimati.entity.Pet;
import com.works.patimati.entity.User;
import com.works.patimati.entity.VetCustomerRequest;
import com.works.patimati.entity.enums.Species;
import com.works.patimati.entity.enums.VetCustomerRequestStatus;
import com.works.patimati.exception.BusinessException;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.PetRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.repository.VetCustomerRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VetCustomerServiceTest {

    private static final String REQUESTER_EPOSTA = "sahip@ornek.com";
    private static final String VET_EPOSTA = "vet@ornek.com";

    private VetCustomerRequestRepository vetCustomerRequestRepository;
    private UserRepository userRepository;
    private PetRepository petRepository;
    private PetService petService;
    private NotificationService notificationService;
    private VetCustomerService service;

    private User requester;
    private User vet;

    @BeforeEach
    void hazirla() {
        vetCustomerRequestRepository = mock(VetCustomerRequestRepository.class);
        userRepository = mock(UserRepository.class);
        petRepository = mock(PetRepository.class);
        petService = mock(PetService.class);
        notificationService = mock(NotificationService.class);
        service = new VetCustomerService(vetCustomerRequestRepository, userRepository, petRepository,
                petService, notificationService);

        requester = User.builder().uid(1L).email(REQUESTER_EPOSTA).firstName("Ali").lastName("Yılmaz").build();
        vet = User.builder().uid(2L).email(VET_EPOSTA).firstName("Ayşe").lastName("Vet").role(User.Role.VET).build();

        when(userRepository.findByEmail(REQUESTER_EPOSTA)).thenReturn(Optional.of(requester));
        when(userRepository.findByEmail(VET_EPOSTA)).thenReturn(Optional.of(vet));
        when(userRepository.findById(2L)).thenReturn(Optional.of(vet));
        when(vetCustomerRequestRepository.save(any(VetCustomerRequest.class)))
                .thenAnswer(cagri -> cagri.getArgument(0));
    }

    @Test
    void yeniIstekPendingOlarakOlusturulurVeVeteVetBildirimGider() {
        when(vetCustomerRequestRepository.findByRequester_UidAndVet_Uid(1L, 2L)).thenReturn(Optional.empty());

        VetCustomerRequestResponse yanit = service.sendRequest(REQUESTER_EPOSTA, 2L);

        assertThat(yanit.status()).isEqualTo(VetCustomerRequestStatus.PENDING);
        verify(notificationService).createAndSend(
                org.mockito.ArgumentMatchers.eq(vet), any(), any(),
                org.mockito.ArgumentMatchers.eq("VET_CUSTOMER_REQUEST"), any());
    }

    @Test
    void veterinerOlmayanKullaniciyaIstekGonderilemez() {
        User normalKullanici = User.builder().uid(9L).role(User.Role.USER).build();
        when(userRepository.findById(9L)).thenReturn(Optional.of(normalKullanici));

        assertThatThrownBy(() -> service.sendRequest(REQUESTER_EPOSTA, 9L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void kendineIstekGonderilemez() {
        assertThatThrownBy(() -> service.sendRequest(VET_EPOSTA, 2L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void zatenBekleyenIstekVarsaTekrarGonderilemez() {
        VetCustomerRequest mevcut = VetCustomerRequest.builder()
                .requester(requester).vet(vet).status(VetCustomerRequestStatus.PENDING).build();
        when(vetCustomerRequestRepository.findByRequester_UidAndVet_Uid(1L, 2L)).thenReturn(Optional.of(mevcut));

        assertThatThrownBy(() -> service.sendRequest(REQUESTER_EPOSTA, 2L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void reddedilmisIstekTekrarPendingeDoner() {
        VetCustomerRequest reddedilmis = VetCustomerRequest.builder()
                .requester(requester).vet(vet).status(VetCustomerRequestStatus.REJECTED).build();
        when(vetCustomerRequestRepository.findByRequester_UidAndVet_Uid(1L, 2L)).thenReturn(Optional.of(reddedilmis));

        VetCustomerRequestResponse yanit = service.sendRequest(REQUESTER_EPOSTA, 2L);

        assertThat(yanit.status()).isEqualTo(VetCustomerRequestStatus.PENDING);
    }

    @Test
    void istekYoksaDurumSorgusuKaynakYokHatasiVerir() {
        when(vetCustomerRequestRepository.findByRequester_UidAndVet_Uid(1L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMyRequestStatus(REQUESTER_EPOSTA, 2L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void baskaVetinIstegKabulEtmeyeCalisirsaReddedilir() {
        User baskaVet = User.builder().uid(77L).email("baskavet@ornek.com").role(User.Role.VET).build();
        when(userRepository.findByEmail("baskavet@ornek.com")).thenReturn(Optional.of(baskaVet));
        VetCustomerRequest istek = VetCustomerRequest.builder()
                .id(10L).requester(requester).vet(vet).status(VetCustomerRequestStatus.PENDING).build();
        when(vetCustomerRequestRepository.findById(10L)).thenReturn(Optional.of(istek));

        assertThatThrownBy(() -> service.accept("baskavet@ornek.com", 10L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void kabulEdilenIstekMusteriyeBildirimGonderirVeAcceptedOlur() {
        VetCustomerRequest istek = VetCustomerRequest.builder()
                .id(11L).requester(requester).vet(vet).status(VetCustomerRequestStatus.PENDING).build();
        when(vetCustomerRequestRepository.findById(11L)).thenReturn(Optional.of(istek));

        VetCustomerRequestResponse yanit = service.accept(VET_EPOSTA, 11L);

        assertThat(yanit.status()).isEqualTo(VetCustomerRequestStatus.ACCEPTED);
        assertThat(istek.getDecidedAt()).isNotNull();
        verify(notificationService).createAndSend(
                org.mockito.ArgumentMatchers.eq(requester), any(), any(),
                org.mockito.ArgumentMatchers.eq("VET_CUSTOMER_ACCEPTED"), any());
    }

    @Test
    void zatenKararaBaglanmisIstekTekrarKararaBaglanamaz() {
        VetCustomerRequest istek = VetCustomerRequest.builder()
                .id(12L).requester(requester).vet(vet).status(VetCustomerRequestStatus.ACCEPTED).build();
        when(vetCustomerRequestRepository.findById(12L)).thenReturn(Optional.of(istek));

        assertThatThrownBy(() -> service.accept(VET_EPOSTA, 12L)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.reject(VET_EPOSTA, 12L)).isInstanceOf(BusinessException.class);
    }

    @Test
    void musterisiOlmayanBirininHayvanlariniGormeyeCalisirsaReddedilir() {
        when(vetCustomerRequestRepository.findByRequester_UidAndVet_Uid(5L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCustomerPets(VET_EPOSTA, 5L))
                .isInstanceOf(AccessDeniedException.class);
        verify(petRepository, never()).findByOwner_Uid(any());
    }

    @Test
    void kabulEdilmisMusterininHayvanlariGorulebilir() {
        VetCustomerRequest kabulEdilmis = VetCustomerRequest.builder()
                .requester(requester).vet(vet).status(VetCustomerRequestStatus.ACCEPTED).build();
        when(vetCustomerRequestRepository.findByRequester_UidAndVet_Uid(1L, 2L)).thenReturn(Optional.of(kabulEdilmis));
        Pet hayvan = Pet.builder().id(1L).owner(requester).name("Boncuk").species(Species.CAT).build();
        when(petRepository.findByOwner_Uid(1L)).thenReturn(List.of(hayvan));

        assertThat(service.getCustomerPets(VET_EPOSTA, 1L)).hasSize(1);
    }

    @Test
    void musterilerListesiSadeceAcceptedOlanlariDoner() {
        Pageable pageable = PageRequest.of(0, 20);
        VetCustomerRequest kabulEdilmis = VetCustomerRequest.builder()
                .id(20L).requester(requester).vet(vet).status(VetCustomerRequestStatus.ACCEPTED).build();
        when(vetCustomerRequestRepository.findByVet_UidAndStatus(2L, VetCustomerRequestStatus.ACCEPTED, pageable))
                .thenReturn(new PageImpl<>(List.of(kabulEdilmis)));

        List<VetCustomerResponse> yanit = service.listCustomers(VET_EPOSTA, pageable);

        assertThat(yanit).hasSize(1);
        assertThat(yanit.get(0).requesterId()).isEqualTo(1L);
    }
}
