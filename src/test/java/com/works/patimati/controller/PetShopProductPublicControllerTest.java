package com.works.patimati.controller;

import com.works.patimati.dto.petshop.PetShopProductReviewResponse;
import com.works.patimati.dto.petshop.PetShopProductResponse;
import com.works.patimati.exception.GlobalExceptionHandler;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.service.PetShopProductReviewService;
import com.works.patimati.service.PetShopProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@code VetClinicReviewControllerTest} deseniyle -- durum kodu/JSON şekli,
 * güvenlik filtresinden bağımsız (bkz. ayrıca
 * {@code PetShopProductReviewGuvenlikRegresyonTest} gerçek filtre zinciriyle).
 */
@ExtendWith(MockitoExtension.class)
class PetShopProductPublicControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PetShopProductService petShopProductService;

    @Mock
    private PetShopProductReviewService petShopProductReviewService;

    @InjectMocks
    private PetShopProductPublicController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private static PetShopProductReviewResponse ornekYanit() {
        return new PetShopProductReviewResponse(1L, 9L, "Ali Yılmaz", 5, "Çok iyi",
                true, OffsetDateTime.now(), OffsetDateTime.now());
    }

    private static PetShopProductResponse ornekUrun() {
        return new PetShopProductResponse(1L, 3L, "Mama", "Açıklama", new BigDecimal("99.90"),
                null, 4.5, 2, OffsetDateTime.now(), OffsetDateTime.now());
    }

    @Test
    void urunDetayiDoner() throws Exception {
        when(petShopProductService.getPublicById(1L)).thenReturn(ornekUrun());

        mockMvc.perform(get("/api/petshop-products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mama"))
                .andExpect(jsonPath("$.price").value(99.90));
    }

    @Test
    void olmayanUrunDetayi404Doner() throws Exception {
        when(petShopProductService.getPublicById(999L))
                .thenThrow(new ResourceNotFoundException("Ürün bulunamadı: 999"));

        mockMvc.perform(get("/api/petshop-products/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void anonimListelemeGorunteleyiciNullOlarakGecer() throws Exception {
        when(petShopProductReviewService.listForProduct(eq(1L), any(), isNull()))
                .thenReturn(new PageImpl<>(List.of(ornekYanit()), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/petshop-products/1/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].rating").value(5));

        verify(petShopProductReviewService).listForProduct(eq(1L), any(), isNull());
    }

    @Test
    void kimlikliListelemeGoruntuleyiciCozulur() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("musteri@ornek.com");
        when(auth.isAuthenticated()).thenReturn(true);
        when(petShopProductReviewService.resolveViewerUid("musteri@ornek.com")).thenReturn(9L);
        when(petShopProductReviewService.listForProduct(eq(1L), any(), eq(9L)))
                .thenReturn(new PageImpl<>(List.of(ornekYanit()), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/petshop-products/1/reviews").principal(auth))
                .andExpect(status().isOk());

        verify(petShopProductReviewService).listForProduct(eq(1L), any(), eq(9L));
    }

    @Test
    void getMineYorumYoksa404Doner() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("musteri@ornek.com");
        when(petShopProductReviewService.getMine("musteri@ornek.com", 1L))
                .thenThrow(new ResourceNotFoundException("Bu ürüne yaptığınız bir değerlendirme yok"));

        mockMvc.perform(get("/api/petshop-products/1/reviews/me").principal(auth))
                .andExpect(status().isNotFound());
    }

    @Test
    void putIleYorumOlusturulur() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("musteri@ornek.com");
        when(petShopProductReviewService.upsertMine(eq("musteri@ornek.com"), eq(1L), any()))
                .thenReturn(ornekYanit());

        mockMvc.perform(put("/api/petshop-products/1/reviews/me")
                        .principal(auth)
                        .contentType("application/json")
                        .content("{\"rating\":5,\"comment\":\"Çok iyi\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.canEdit").value(true));
    }

    @Test
    void deleteIleYorumSilinir204Doner() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("musteri@ornek.com");

        mockMvc.perform(delete("/api/petshop-products/1/reviews/me").principal(auth))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(petShopProductReviewService).deleteMine("musteri@ornek.com", 1L);
    }
}
