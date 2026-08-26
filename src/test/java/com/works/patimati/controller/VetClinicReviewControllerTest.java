package com.works.patimati.controller;

import com.works.patimati.dto.vet.VetClinicReviewResponse;
import com.works.patimati.exception.GlobalExceptionHandler;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.service.VetClinicReviewService;
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
 * {@code AdoptionComplaintControllerTest}'in {@code standaloneSetup} deseniyle
 * -- durum kodu/JSON şekli, güvenlik filtresinden bağımsız (bkz. ayrıca
 * {@code VetClinicReviewGuvenlikRegresyonTest} gerçek filtre zinciriyle).
 */
@ExtendWith(MockitoExtension.class)
class VetClinicReviewControllerTest {

    private MockMvc mockMvc;

    @Mock
    private VetClinicReviewService vetClinicReviewService;

    @InjectMocks
    private VetClinicReviewController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private static VetClinicReviewResponse ornekYanit() {
        return new VetClinicReviewResponse(1L, 9L, "Ali Yılmaz", 5, "Çok iyi",
                true, OffsetDateTime.now(), OffsetDateTime.now());
    }

    @Test
    void anonimListelemeGorunteleyiciNullOlarakGecer() throws Exception {
        when(vetClinicReviewService.listForClinic(eq(1L), any(), isNull()))
                .thenReturn(new PageImpl<>(List.of(ornekYanit()), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/vet-clinics/1/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].rating").value(5));

        verify(vetClinicReviewService).listForClinic(eq(1L), any(), isNull());
    }

    @Test
    void kimlikliListelemeGoruntuleyiciCozulur() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("musteri@ornek.com");
        when(auth.isAuthenticated()).thenReturn(true);
        when(vetClinicReviewService.resolveViewerUid("musteri@ornek.com")).thenReturn(9L);
        when(vetClinicReviewService.listForClinic(eq(1L), any(), eq(9L)))
                .thenReturn(new PageImpl<>(List.of(ornekYanit()), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/vet-clinics/1/reviews").principal(auth))
                .andExpect(status().isOk());

        verify(vetClinicReviewService).listForClinic(eq(1L), any(), eq(9L));
    }

    @Test
    void getMineYorumYoksa404Doner() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("musteri@ornek.com");
        when(vetClinicReviewService.getMine("musteri@ornek.com", 1L))
                .thenThrow(new ResourceNotFoundException("Bu kliniğe yaptığınız bir değerlendirme yok"));

        mockMvc.perform(get("/api/vet-clinics/1/reviews/me").principal(auth))
                .andExpect(status().isNotFound());
    }

    @Test
    void putIleYorumOlusturulur() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("musteri@ornek.com");
        when(vetClinicReviewService.upsertMine(eq("musteri@ornek.com"), eq(1L), any()))
                .thenReturn(ornekYanit());

        mockMvc.perform(put("/api/vet-clinics/1/reviews/me")
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

        mockMvc.perform(delete("/api/vet-clinics/1/reviews/me").principal(auth))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(vetClinicReviewService).deleteMine("musteri@ornek.com", 1L);
    }
}
