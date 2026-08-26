package com.works.patimati.controller;

import com.works.patimati.dto.shelter.ShelterResponse;
import com.works.patimati.exception.GlobalExceptionHandler;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.service.ShelterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockPart;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@code VetClinicController}/{@code PetShopController} test deseniyle --
 * durum kodu/JSON şekli, güvenlik filtresinden bağımsız (bkz. ayrıca
 * {@code ShelterReviewGuvenlikRegresyonTest} gerçek filtre zinciriyle).
 */
@ExtendWith(MockitoExtension.class)
class ShelterControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ShelterService shelterService;

    @InjectMocks
    private ShelterController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private static ShelterResponse ornekYanit() {
        return new ShelterResponse(1L, "Pati Barınağı", "Adres", "Ankara", "Çankaya",
                "0312 000 00 00", "09:00-18:00", null, 39.92, 32.85, 4.5, 3);
    }

    @Test
    void kartYokkenGetMine404Doner() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("barinak@ornek.com");
        when(shelterService.getMine("barinak@ornek.com"))
                .thenThrow(new ResourceNotFoundException("Barınak kartı henüz oluşturulmamış"));

        mockMvc.perform(get("/api/shelter/card").principal(auth))
                .andExpect(status().isNotFound());
    }

    @Test
    void kartVarkenGetMineDoner() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("barinak@ornek.com");
        when(shelterService.getMine("barinak@ornek.com")).thenReturn(ornekYanit());

        mockMvc.perform(get("/api/shelter/card").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pati Barınağı"))
                .andExpect(jsonPath("$.averageRating").value(4.5))
                .andExpect(jsonPath("$.reviewCount").value(3));
    }

    @Test
    void multipartPutIleKartFotografsizGuncellenir() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("barinak@ornek.com");
        when(shelterService.upsertMine(eq("barinak@ornek.com"), any(), eq(null))).thenReturn(ornekYanit());

        MockPart data = new MockPart("data", "data",
                """
                {"name":"Pati Barınağı","address":"Adres","city":"Ankara","district":"Çankaya","phone":"0312 000 00 00","workingHours":"09:00-18:00","latitude":null,"longitude":null}
                """.getBytes(StandardCharsets.UTF_8));
        data.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/shelter/card").part(data).principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pati Barınağı"));

        verify(shelterService).upsertMine(eq("barinak@ornek.com"), any(), eq(null));
    }
}
