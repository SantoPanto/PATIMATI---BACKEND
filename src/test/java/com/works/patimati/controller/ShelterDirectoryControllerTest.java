package com.works.patimati.controller;

import com.works.patimati.dto.ad.AdResponse;
import com.works.patimati.dto.shelter.ShelterPublicResponse;
import com.works.patimati.exception.GlobalExceptionHandler;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.service.AdoptionService;
import com.works.patimati.service.ShelterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@code PetShopDirectoryController} test deseniyle -- herkese açık dizin +
 * ilan listesi uçlarının güvenlik filtresinden bağımsız durum kodu/JSON
 * şekli (bkz. ayrıca {@code ShelterReviewGuvenlikRegresyonTest} gerçek
 * filtre zinciriyle).
 */
@ExtendWith(MockitoExtension.class)
class ShelterDirectoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ShelterService shelterService;

    @Mock
    private AdoptionService adoptionService;

    @InjectMocks
    private ShelterDirectoryController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private static ShelterPublicResponse ornekYanit() {
        return new ShelterPublicResponse(1L, "Pati Barınağı", "Adres", "Ankara", "Çankaya",
                "0312 000 00 00", "09:00-18:00", null, 39.92, 32.85, 4.5, 3);
    }

    @Test
    void barinakDetayiDoner() throws Exception {
        when(shelterService.getPublicById(1L)).thenReturn(ornekYanit());

        mockMvc.perform(get("/api/shelters/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pati Barınağı"))
                .andExpect(jsonPath("$.averageRating").value(4.5));
    }

    @Test
    void olmayanBarinakDetayi404Doner() throws Exception {
        when(shelterService.getPublicById(999L))
                .thenThrow(new ResourceNotFoundException("Barınak bulunamadı: 999"));

        mockMvc.perform(get("/api/shelters/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void dizinListelemeSehirFiltresiyleServisiCagirir() throws Exception {
        when(shelterService.listPublic(any(), eq("Ankara")))
                .thenReturn(new PageImpl<>(List.of(ornekYanit()), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/shelters").param("city", "Ankara"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Pati Barınağı"));

        verify(shelterService).listPublic(any(), eq("Ankara"));
    }

    @Test
    void ilanListesiOnceSahipUidiniCozerSonraAdoptionServisiniCagirir() throws Exception {
        AdResponse adYaniti = mock(AdResponse.class);
        when(shelterService.resolveOwnerUid(1L)).thenReturn(42L);
        when(adoptionService.getPublicAdoptionAdsByOwner(eq(42L), any()))
                .thenReturn(new PageImpl<>(List.of(adYaniti), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/shelters/1/adoptions"))
                .andExpect(status().isOk());

        verify(shelterService).resolveOwnerUid(1L);
        verify(adoptionService).getPublicAdoptionAdsByOwner(eq(42L), any());
    }

    @Test
    void olmayanBarinaginIlanListesi404Doner() throws Exception {
        when(shelterService.resolveOwnerUid(999L))
                .thenThrow(new ResourceNotFoundException("Barınak bulunamadı: 999"));

        mockMvc.perform(get("/api/shelters/999/adoptions"))
                .andExpect(status().isNotFound());
    }
}
