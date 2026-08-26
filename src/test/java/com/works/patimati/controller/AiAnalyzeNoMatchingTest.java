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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * POST /api/ai/analyze endpoint'inin veritabanı eşleşme taraması (matching) yapmadığını,
 * yalnızca fotoğraf nitelik çıkarımı (attribute extraction) yaptığını doğrulayan izolasyon testi.
 */
class AiAnalyzeNoMatchingTest {

    private AiMatchService aiMatchService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        aiMatchService = mock(AiMatchService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AiAnalyzeController(aiMatchService)).build();
    }

    @Test
    @DisplayName("/api/ai/analyze yalnızca fotoğraf niteliklerini döner ve matchImages çağrısı yapmaz")
    void analyzeOnlyExtractsAttributesWithoutMatching() throws Exception {
        AiAnalyzeResponse mockResponse = new AiAnalyzeResponse(
                Species.DOG,
                0.98,
                "Golden Retriever",
                0.95,
                CoatPattern.SOLID,
                Set.of(PetColor.GOLDEN),
                true
        );

        when(aiMatchService.analyzeImageForFrontend(any())).thenReturn(mockResponse);

        MockMultipartFile file = new MockMultipartFile("file", "dog.jpg", "image/jpeg", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/ai/analyze").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.species").value("DOG"))
                .andExpect(jsonPath("$.speciesConfidence").value(0.98))
                .andExpect(jsonPath("$.breed").value("Golden Retriever"))
                .andExpect(jsonPath("$.breedConfidence").value(0.95))
                .andExpect(jsonPath("$.coatPattern").value("SOLID"))
                .andExpect(jsonPath("$.isPet").value(true));

        // verify: analyzeImageForFrontend çağrıldı, ama matchImages ASLA çağrılmadı
        verify(aiMatchService, times(1)).analyzeImageForFrontend(any());
        verify(aiMatchService, never()).matchImages(any(), any(), any(), any());
    }
}
