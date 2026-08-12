package com.works.patimati.controller;

import com.works.patimati.dto.ad.AdResponse;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.enums.AgeGroup;
import com.works.patimati.entity.enums.AiStatus;
import com.works.patimati.entity.enums.CoatPattern;
import com.works.patimati.entity.enums.EyeColor;
import com.works.patimati.entity.enums.PetGender;
import com.works.patimati.entity.enums.PresenceStatus;
import com.works.patimati.entity.enums.Species;
import com.works.patimati.exception.GlobalExceptionHandler;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.service.AdService;
import com.works.patimati.service.PdfPosterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdControllerTest {

    private AdService adService;
    private UserRepository userRepository;
    private PdfPosterService pdfPosterService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        adService = mock(AdService.class);
        userRepository = mock(UserRepository.class);
        pdfPosterService = mock(PdfPosterService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AdController(adService, userRepository, pdfPosterService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldCreateAdFromDtoAndAuthenticatedUser() throws Exception {
        AdResponse response = response();

        String requestJson = """
                {
                  "title": "Kayıp tekir kedi",
                  "adType": "LOST",
                  "species": "CAT",
                  "lostDate": "2026-07-20",
                  "latitude": 40.195,
                  "longitude": 29.060
                }
                """;

        MockPart adPart = new MockPart(
                "ad",
                "ad",
                requestJson.getBytes(StandardCharsets.UTF_8)
        );
        adPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        MockMultipartFile image = new MockMultipartFile(
                "images",
                "cat.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[]{
                        (byte) 0xFF,
                        (byte) 0xD8,
                        (byte) 0xFF
                }
        );

        when(adService.createAd(
                eq("owner@patimati.com"),
                any(),
                anyList()
        )).thenReturn(response);

        mockMvc.perform(
                        multipart("/api/ads")
                                .part(adPart)
                                .file(image)
                                .principal(authentication())
                )
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "/api/ads/7"
                ))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.species").value("CAT"));
    }

    @Test
    void shouldRejectInvalidCreateRequestBeforeCallingService()
            throws Exception {
        String requestJson = """
                {
                  "title": "",
                  "adType": "LOST",
                  "species": "CAT",
                  "latitude": 40.195,
                  "longitude": 29.060
                }
                """;

        MockPart adPart = new MockPart(
                "ad",
                "ad",
                requestJson.getBytes(StandardCharsets.UTF_8)
        );
        adPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        MockMultipartFile image = new MockMultipartFile(
                "images",
                "cat.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[]{
                        (byte) 0xFF,
                        (byte) 0xD8,
                        (byte) 0xFF
                }
        );

        mockMvc.perform(
                        multipart("/api/ads")
                                .part(adPart)
                                .file(image)
                                .principal(authentication())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title")
                        .value("Doğrulama hatası"))
                .andExpect(jsonPath(
                        "$.validationErrors.title"
                ).exists());
    }

    @Test
    void shouldListActiveAdsWithPagination() throws Exception {
        AdResponse response = response();

        when(adService.getActiveAds(eq(Ad.AdType.LOST), any()))
                .thenReturn(new PageImpl<>(
                        List.of(response),
                        PageRequest.of(
                                0,
                                20,
                                Sort.by(
                                        Sort.Direction.DESC,
                                        "createdAt"
                                )
                        ),
                        1
                ));

        mockMvc.perform(
                        get("/api/ads")
                                .param("adType", "LOST")
                                .param("page", "0")
                                .param("size", "20")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(7))
                .andExpect(jsonPath("$.content[0].adType")
                        .value("LOST"));
    }

    @Test
    void shouldSoftDeleteAdForAuthenticatedOwner() throws Exception {
        mockMvc.perform(
                        delete("/api/ads/7")
                                .principal(authentication())
                )
                .andExpect(status().isNoContent());

        verify(adService).deactivateAd(
                "owner@patimati.com",
                7L
        );
    }

    private AdResponse response() {
        return new AdResponse(
                7L,
                "Kayıp tekir kedi",
                null,
                Ad.AdType.LOST,
                Species.CAT,
                "MIXED_OR_UNKNOWN",
                Set.of(),
                PetGender.UNKNOWN,
                AgeGroup.UNKNOWN,
                CoatPattern.UNKNOWN,
                PresenceStatus.UNKNOWN,
                null,
                null,
                EyeColor.UNKNOWN,
                PresenceStatus.UNKNOWN,
                PresenceStatus.UNKNOWN,
                false,
                LocalDate.of(2026, 7, 20),
                null,
                List.of("https://example.com/cat.jpg"),
                40.195,
                29.060,
                42L,
                "Test User",
                true,
                Instant.parse("2026-07-27T12:00:00Z"),
                Instant.parse("2026-07-27T12:00:00Z"),
                AiStatus.DONE,
                true
        );
    }

    private UsernamePasswordAuthenticationToken authentication() {
        return new UsernamePasswordAuthenticationToken(
                "owner@patimati.com",
                null,
                List.of()
        );
    }
}