package com.works.patimati.controller;

import com.works.patimati.dto.ai.AiAnalyzeResponse;
import com.works.patimati.entity.enums.CoatPattern;
import com.works.patimati.entity.enums.PetColor;
import com.works.patimati.entity.enums.Species;
import com.works.patimati.service.AiMatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AiAnalyzeControllerTest {

    private AiMatchService aiMatchService;
    private MockMvc mockMvc;

    @BeforeEach
    void hazirla() {
        aiMatchService = mock(AiMatchService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AiAnalyzeController(aiMatchService)).build();
    }

    @Test
    @DisplayName("AI cevabı frontend DTO sözleşmesine uygun döndürülmeli")
    void cevapFrontendDtoFormatindaDoner() throws Exception {
        AiAnalyzeResponse response = new AiAnalyzeResponse(
                Species.CAT,
                0.9955,
                null,
                0.4843,
                CoatPattern.SOLID,
                Set.of(PetColor.CREAM, PetColor.GRAY),
                true
        );
        when(aiMatchService.analyzeImageForFrontend(any())).thenReturn(response);

        mockMvc.perform(multipart("/api/ai/analyze").file(fotograf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.species").value("CAT"))
                .andExpect(jsonPath("$.speciesConfidence").value(0.9955))
                .andExpect(jsonPath("$.breed").value((Object) null))
                .andExpect(jsonPath("$.breedConfidence").value(0.4843))
                .andExpect(jsonPath("$.coatPattern").value("SOLID"))
                .andExpect(jsonPath("$.colors[0]").exists())
                .andExpect(jsonPath("$.isPet").value(true));
    }

    @Test
    @DisplayName("Boş dosya 400 döner — AI'ya boşuna gidilmez")
    void bosDosyaReddedilir() throws Exception {
        mockMvc.perform(multipart("/api/ai/analyze")
                        .file(new MockMultipartFile("file", "bos.jpg", "image/jpeg", new byte[0])))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("AI patlarsa 502 döner ve iç ayrıntı İSTEMCİYE SIZMAZ")
    void icHataSizmaz() throws Exception {
        String icAyrinti = "jdbc:postgresql://10.0.0.7:5432/patimati_db kullanici=postgres";
        when(aiMatchService.analyzeImageForFrontend(any())).thenThrow(new RuntimeException(icAyrinti));

        MvcResult sonuc = mockMvc.perform(multipart("/api/ai/analyze").file(fotograf()))
                .andExpect(status().isBadGateway())
                .andReturn();

        String govde = sonuc.getResponse().getContentAsString();
        assertThat(govde)
                .doesNotContain("jdbc")
                .doesNotContain("10.0.0.7")
                .doesNotContain("postgres");
    }

    @Test
    @DisplayName("AI 2xx dışında cevap verirse 502 döner — boş 200 gösterilmez")
    void aiCevapVermezse502() throws Exception {
        when(aiMatchService.analyzeImageForFrontend(any())).thenReturn(null);

        mockMvc.perform(multipart("/api/ai/analyze").file(fotograf()))
                .andExpect(status().isBadGateway());
    }

    private MockMultipartFile fotograf() {
        return new MockMultipartFile("file", "kedi.jpg", "image/jpeg", new byte[]{1, 2, 3});
    }
}
