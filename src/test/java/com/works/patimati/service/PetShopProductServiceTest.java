package com.works.patimati.service;

import com.works.patimati.dto.petshop.PetShopProductResponse;
import com.works.patimati.dto.petshop.PetShopProductUpsertRequest;
import com.works.patimati.entity.PetShop;
import com.works.patimati.entity.PetShopProduct;
import com.works.patimati.entity.User;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.PetShopProductRepository;
import com.works.patimati.repository.PetShopProductReviewRepository;
import com.works.patimati.repository.PetShopRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.storage.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PetShopProductServiceTest {

    private static final String SAHIP_EPOSTA = "petshop@ornek.com";

    private PetShopProductRepository petShopProductRepository;
    private PetShopRepository petShopRepository;
    private UserRepository userRepository;
    private ImageStorageService imageStorageService;
    private PetShopProductReviewRepository petShopProductReviewRepository;
    private PetShopProductService service;

    private User sahip;
    private PetShop petShop;

    @BeforeEach
    void hazirla() {
        petShopProductRepository = mock(PetShopProductRepository.class);
        petShopRepository = mock(PetShopRepository.class);
        userRepository = mock(UserRepository.class);
        imageStorageService = mock(ImageStorageService.class);
        petShopProductReviewRepository = mock(PetShopProductReviewRepository.class);
        service = new PetShopProductService(petShopProductRepository, petShopRepository, userRepository,
                imageStorageService, petShopProductReviewRepository);

        sahip = User.builder().uid(7L).email(SAHIP_EPOSTA).role(User.Role.PETSHOP).build();
        petShop = PetShop.builder().id(3L).user(sahip).name("Pati Petshop").build();

        when(userRepository.findByEmail(SAHIP_EPOSTA)).thenReturn(Optional.of(sahip));
        when(petShopProductRepository.save(any(PetShopProduct.class))).thenAnswer(cagri -> cagri.getArgument(0));
    }

    @Test
    void kartYokkenUrunOlusturmaKaynakYokHatasiVerir() {
        when(petShopRepository.findByUser_Uid(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createMine(SAHIP_EPOSTA,
                new PetShopProductUpsertRequest("Mama", "Açıklama", new BigDecimal("99.90")), null))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(petShopProductRepository, never()).save(any());
    }

    @Test
    void kendiDukkaninaUrunEklemeBasarili() {
        when(petShopRepository.findByUser_Uid(7L)).thenReturn(Optional.of(petShop));
        when(petShopProductReviewRepository.findAverageRating(any())).thenReturn(null);
        when(petShopProductReviewRepository.countByProduct_Id(any())).thenReturn(0L);

        PetShopProductResponse yanit = service.createMine(SAHIP_EPOSTA,
                new PetShopProductUpsertRequest("Mama", "Açıklama", new BigDecimal("99.90")), null);

        ArgumentCaptor<PetShopProduct> kaydedilen = ArgumentCaptor.forClass(PetShopProduct.class);
        verify(petShopProductRepository).save(kaydedilen.capture());
        assertThat(kaydedilen.getValue().getPetShop()).isSameAs(petShop);
        assertThat(kaydedilen.getValue().getName()).isEqualTo("Mama");
        assertThat(kaydedilen.getValue().getPrice()).isEqualByComparingTo("99.90");
        assertThat(yanit.name()).isEqualTo("Mama");
    }

    @Test
    void baskasininUrununuGuncellemeKaynakYokHatasiVerir() {
        when(petShopRepository.findByUser_Uid(7L)).thenReturn(Optional.of(petShop));
        when(petShopProductRepository.findByPetShop_IdAndId(3L, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateMine(SAHIP_EPOSTA, 99L,
                new PetShopProductUpsertRequest("Yeni", "Açıklama", new BigDecimal("10.00")), null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void baskasininUrununuSilmeKaynakYokHatasiVerir() {
        when(petShopRepository.findByUser_Uid(7L)).thenReturn(Optional.of(petShop));
        when(petShopProductRepository.findByPetShop_IdAndId(3L, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteMine(SAHIP_EPOSTA, 99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(petShopProductRepository, never()).delete(any());
    }

    @Test
    void kendiUrununuGuncellemeBasarili() {
        PetShopProduct mevcut = PetShopProduct.builder()
                .id(5L).petShop(petShop).name("Eski").description("Eski açıklama")
                .price(new BigDecimal("10.00")).build();
        when(petShopRepository.findByUser_Uid(7L)).thenReturn(Optional.of(petShop));
        when(petShopProductRepository.findByPetShop_IdAndId(3L, 5L)).thenReturn(Optional.of(mevcut));
        when(petShopProductReviewRepository.findAverageRating(5L)).thenReturn(null);
        when(petShopProductReviewRepository.countByProduct_Id(5L)).thenReturn(0L);

        PetShopProductResponse yanit = service.updateMine(SAHIP_EPOSTA, 5L,
                new PetShopProductUpsertRequest("Yeni İsim", "Yeni Açıklama", new BigDecimal("20.00")), null);

        assertThat(mevcut.getName()).isEqualTo("Yeni İsim");
        assertThat(mevcut.getPrice()).isEqualByComparingTo("20.00");
        assertThat(yanit.name()).isEqualTo("Yeni İsim");
    }

    @Test
    void kendiUrununuSilmeBasarili() {
        PetShopProduct mevcut = PetShopProduct.builder()
                .id(5L).petShop(petShop).name("Eski").price(new BigDecimal("10.00")).build();
        when(petShopRepository.findByUser_Uid(7L)).thenReturn(Optional.of(petShop));
        when(petShopProductRepository.findByPetShop_IdAndId(3L, 5L)).thenReturn(Optional.of(mevcut));

        service.deleteMine(SAHIP_EPOSTA, 5L);

        ArgumentCaptor<PetShopProduct> silinen = ArgumentCaptor.forClass(PetShopProduct.class);
        verify(petShopProductRepository).delete(silinen.capture());
        assertThat(silinen.getValue().getId()).isEqualTo(5L);
    }

    @Test
    void toResponseOrtalamaPuanVeSayiyiIcerirYorumYokkenNullDoner() {
        PetShopProduct mevcut = PetShopProduct.builder()
                .id(5L).petShop(petShop).name("Mama").price(new BigDecimal("10.00")).build();
        when(petShopProductRepository.findById(5L)).thenReturn(Optional.of(mevcut));
        when(petShopProductReviewRepository.findAverageRating(5L)).thenReturn(null);
        when(petShopProductReviewRepository.countByProduct_Id(5L)).thenReturn(0L);

        PetShopProductResponse yanit = service.getPublicById(5L);

        assertThat(yanit.averageRating()).isNull();
        assertThat(yanit.reviewCount()).isZero();
    }

    @Test
    void toResponseOrtalamaPuanVeSayiyiIcerirYorumVarken() {
        PetShopProduct mevcut = PetShopProduct.builder()
                .id(5L).petShop(petShop).name("Mama").price(new BigDecimal("10.00")).build();
        when(petShopProductRepository.findById(5L)).thenReturn(Optional.of(mevcut));
        when(petShopProductReviewRepository.findAverageRating(5L)).thenReturn(4.5);
        when(petShopProductReviewRepository.countByProduct_Id(5L)).thenReturn(2L);

        PetShopProductResponse yanit = service.getPublicById(5L);

        assertThat(yanit.averageRating()).isEqualTo(4.5);
        assertThat(yanit.reviewCount()).isEqualTo(2);
    }

    @Test
    void listMineKendiDukkaninUrunleriniDoner() {
        Pageable pageable = PageRequest.of(0, 20);
        PetShopProduct urun = PetShopProduct.builder()
                .id(5L).petShop(petShop).name("Mama").price(new BigDecimal("10.00")).build();
        when(petShopRepository.findByUser_Uid(7L)).thenReturn(Optional.of(petShop));
        when(petShopProductRepository.findByPetShop_IdOrderByCreatedAtDesc(3L, pageable))
                .thenReturn(new PageImpl<>(List.of(urun)));
        when(petShopProductReviewRepository.findAverageRating(5L)).thenReturn(null);
        when(petShopProductReviewRepository.countByProduct_Id(5L)).thenReturn(0L);

        var sayfa = service.listMine(SAHIP_EPOSTA, pageable);

        assertThat(sayfa.getContent()).hasSize(1);
        assertThat(sayfa.getContent().get(0).name()).isEqualTo("Mama");
    }

    @Test
    void getPublicByIdBulunamayanUrunKaynakYokHatasiVerir() {
        when(petShopProductRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPublicById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
