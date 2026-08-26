package com.works.patimati.service;

import com.works.patimati.dto.petshop.PetShopProductReviewResponse;
import com.works.patimati.dto.petshop.PetShopProductReviewUpsertRequest;
import com.works.patimati.entity.PetShop;
import com.works.patimati.entity.PetShopProduct;
import com.works.patimati.entity.PetShopProductReview;
import com.works.patimati.entity.User;
import com.works.patimati.exception.BusinessException;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.PetShopProductRepository;
import com.works.patimati.repository.PetShopProductReviewRepository;
import com.works.patimati.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
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
 * {@code VetClinicReviewServiceTest}'in (product, author) çiftine göre
 * birebir kopyası.
 */
class PetShopProductReviewServiceTest {

    private static final String YAZAR_EPOSTA = "musteri@ornek.com";
    private static final String SAHIP_EPOSTA = "petshop@ornek.com";

    private PetShopProductReviewRepository petShopProductReviewRepository;
    private PetShopProductRepository petShopProductRepository;
    private UserRepository userRepository;
    private NotificationService notificationService;
    private PetShopProductReviewService service;

    private User yazar;
    private User dukkanSahibi;
    private PetShop petShop;
    private PetShopProduct urun;

    @BeforeEach
    void hazirla() {
        petShopProductReviewRepository = mock(PetShopProductReviewRepository.class);
        petShopProductRepository = mock(PetShopProductRepository.class);
        userRepository = mock(UserRepository.class);
        notificationService = mock(NotificationService.class);
        service = new PetShopProductReviewService(petShopProductReviewRepository, petShopProductRepository,
                userRepository, notificationService);

        yazar = User.builder().uid(1L).email(YAZAR_EPOSTA).firstName("Ali").lastName("Yılmaz").build();
        dukkanSahibi = User.builder().uid(2L).email(SAHIP_EPOSTA).role(User.Role.PETSHOP)
                .firstName("Ayşe").lastName("Petshop").build();
        petShop = PetShop.builder().id(3L).user(dukkanSahibi).name("Pati Petshop").build();
        urun = PetShopProduct.builder().id(5L).petShop(petShop).name("Mama").price(new BigDecimal("10.00")).build();

        when(userRepository.findByEmail(YAZAR_EPOSTA)).thenReturn(Optional.of(yazar));
        when(userRepository.findByEmail(SAHIP_EPOSTA)).thenReturn(Optional.of(dukkanSahibi));
        when(petShopProductRepository.findById(5L)).thenReturn(Optional.of(urun));
        when(petShopProductReviewRepository.save(any(PetShopProductReview.class)))
                .thenAnswer(cagri -> cagri.getArgument(0));
    }

    @Test
    void ilkYorumOlusturulurVeDukkanSahibineBildirimGider() {
        when(petShopProductReviewRepository.findByProduct_IdAndAuthor_Uid(5L, 1L)).thenReturn(Optional.empty());

        PetShopProductReviewResponse yanit = service.upsertMine(YAZAR_EPOSTA, 5L,
                new PetShopProductReviewUpsertRequest(5, "Çok kaliteli"));

        assertThat(yanit.rating()).isEqualTo(5);
        assertThat(yanit.comment()).isEqualTo("Çok kaliteli");
        assertThat(yanit.canEdit()).isTrue();
        verify(notificationService).createAndSend(
                eq(dukkanSahibi), any(), any(), eq("PETSHOP_PRODUCT_REVIEW"), any());
    }

    @Test
    void mevcutYorumGuncellenirYeniSatirAcilmazVeIkinciDuzenlemedeBildirimTekrarGitmez() {
        PetShopProductReview mevcut = PetShopProductReview.builder()
                .id(9L).product(urun).author(yazar).rating(3).comment("Fena değil").build();
        when(petShopProductReviewRepository.findByProduct_IdAndAuthor_Uid(5L, 1L)).thenReturn(Optional.of(mevcut));

        PetShopProductReviewResponse yanit = service.upsertMine(YAZAR_EPOSTA, 5L,
                new PetShopProductReviewUpsertRequest(4, "Daha da iyi"));

        assertThat(yanit.id()).isEqualTo(9L);
        assertThat(yanit.rating()).isEqualTo(4);
        assertThat(yanit.comment()).isEqualTo("Daha da iyi");
        verify(petShopProductReviewRepository, never()).save(org.mockito.ArgumentMatchers.argThat(
                r -> r != mevcut));
        verify(notificationService, never()).createAndSend(any(), any(), any(), any(), any());
    }

    @Test
    void kendiUrununeYorumYapilamaz() {
        when(petShopProductReviewRepository.findByProduct_IdAndAuthor_Uid(5L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.upsertMine(SAHIP_EPOSTA, 5L,
                new PetShopProductReviewUpsertRequest(5, "Kendi ürünüm")))
                .isInstanceOf(BusinessException.class);

        verify(petShopProductReviewRepository, never()).save(any());
    }

    @Test
    void olmayanUruneYorumYapilamazKaynakYokHatasiVerir() {
        when(petShopProductRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.upsertMine(YAZAR_EPOSTA, 999L,
                new PetShopProductReviewUpsertRequest(5, "Yorum")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listedeCanEditYalnizcaYazaraTrueDoner() {
        PetShopProductReview yazarinYorumu = PetShopProductReview.builder()
                .id(1L).product(urun).author(yazar).rating(5).comment("Harika").build();
        User baskaKullanici = User.builder().uid(3L).firstName("Veli").lastName("Demir").build();
        PetShopProductReview baskasininYorumu = PetShopProductReview.builder()
                .id(2L).product(urun).author(baskaKullanici).rating(2).comment("İdare eder").build();

        Pageable pageable = PageRequest.of(0, 20);
        when(petShopProductReviewRepository.findByProduct_IdOrderByCreatedAtDesc(5L, pageable))
                .thenReturn(new PageImpl<>(java.util.List.of(yazarinYorumu, baskasininYorumu)));

        var sayfa = service.listForProduct(5L, pageable, 1L);

        assertThat(sayfa.getContent()).hasSize(2);
        assertThat(sayfa.getContent().get(0).canEdit()).isTrue();
        assertThat(sayfa.getContent().get(1).canEdit()).isFalse();
    }

    @Test
    void anonimGoruntuleyicideHicbirYorumDuzenlenemezGorunur() {
        PetShopProductReview yazarinYorumu = PetShopProductReview.builder()
                .id(1L).product(urun).author(yazar).rating(5).comment("Harika").build();
        Pageable pageable = PageRequest.of(0, 20);
        when(petShopProductReviewRepository.findByProduct_IdOrderByCreatedAtDesc(5L, pageable))
                .thenReturn(new PageImpl<>(java.util.List.of(yazarinYorumu)));

        var sayfa = service.listForProduct(5L, pageable, null);

        assertThat(sayfa.getContent().get(0).canEdit()).isFalse();
    }

    @Test
    void getMineYorumYoksaKaynakYokHatasiVerir() {
        when(petShopProductReviewRepository.findByProduct_IdAndAuthor_Uid(5L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMine(YAZAR_EPOSTA, 5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getMineYorumVarsaDoner() {
        PetShopProductReview mevcut = PetShopProductReview.builder()
                .id(9L).product(urun).author(yazar).rating(3).comment("Fena değil").build();
        when(petShopProductReviewRepository.findByProduct_IdAndAuthor_Uid(5L, 1L)).thenReturn(Optional.of(mevcut));

        PetShopProductReviewResponse yanit = service.getMine(YAZAR_EPOSTA, 5L);

        assertThat(yanit.id()).isEqualTo(9L);
        assertThat(yanit.canEdit()).isTrue();
    }

    @Test
    void silmeYalnizcaProductIdVeCallerUidIleSinirli() {
        PetShopProductReview mevcut = PetShopProductReview.builder()
                .id(9L).product(urun).author(yazar).rating(3).comment("Fena değil").build();
        when(petShopProductReviewRepository.findByProduct_IdAndAuthor_Uid(5L, 1L)).thenReturn(Optional.of(mevcut));

        service.deleteMine(YAZAR_EPOSTA, 5L);

        ArgumentCaptor<PetShopProductReview> silinen = ArgumentCaptor.forClass(PetShopProductReview.class);
        verify(petShopProductReviewRepository).delete(silinen.capture());
        assertThat(silinen.getValue().getId()).isEqualTo(9L);
        verify(petShopProductReviewRepository, never()).deleteById(any());
        verify(petShopProductReviewRepository, never()).findById(any());
    }

    @Test
    void silmeBaskasininYorumunaErisemezKaynakYokHatasiVerir() {
        when(petShopProductReviewRepository.findByProduct_IdAndAuthor_Uid(5L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteMine(YAZAR_EPOSTA, 5L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(petShopProductReviewRepository, never()).delete(any());
    }
}
