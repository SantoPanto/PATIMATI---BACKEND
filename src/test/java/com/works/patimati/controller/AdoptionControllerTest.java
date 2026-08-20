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
import com.works.patimati.service.AdoptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdoptionControllerTest {

    private AdoptionService adoptionService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        adoptionService = mock(AdoptionService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AdoptionController(adoptionService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldCreateAdoptionAdSuccessfullyWhenRequestIsValid() throws Exception {
        AdResponse response = sampleAdResponse();

        String requestJson = """
                {
                  "title": "Sahiplendirilecek Sevimli Kedi",
                  "species": "CAT",
                  "date": "2026-08-20",
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
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}
        );

        when(adoptionService.createAdoptionAd(
                eq("owner@patimati.com"),
                any(),
                anyList()
        )).thenReturn(response);

        mockMvc.perform(
                        multipart("/api/adoptions")
                                .part(adPart)
                                .file(image)
                                .principal(authentication())
                )
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/public/adoptions/10"))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("Sahiplendirilecek Sevimli Kedi"))
                .andExpect(jsonPath("$.adType").value("ADOPTION"));
    }

    @Test
    void shouldReturn400BadRequestWithInvalidParamsWhenTitleAndSpeciesAreMissing() throws Exception {
        String requestJson = """
                {
                  "title": "",
                  "date": "2026-08-20",
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
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}
        );

        mockMvc.perform(
                        multipart("/api/adoptions")
                                .part(adPart)
                                .file(image)
                                .principal(authentication())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Doğrulama hatası"))
                .andExpect(jsonPath("$.invalid_params.title").value("İlan başlığı boş bırakılamaz"))
                .andExpect(jsonPath("$.invalid_params.species").value("Tür zorunludur"))
                .andExpect(jsonPath("$.validationErrors.title").exists())
                .andExpect(jsonPath("$.validationErrors.species").exists());

        verifyNoInteractions(adoptionService);
    }

    @Test
    void shouldReturn400BadRequestWhenLocationIsMissing() throws Exception {
        String requestJson = """
                {
                  "title": "Sahiplendirilecek Sevimli Kedi",
                  "species": "CAT",
                  "date": "2026-08-20"
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
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}
        );

        mockMvc.perform(
                        multipart("/api/adoptions")
                                .part(adPart)
                                .file(image)
                                .principal(authentication())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Doğrulama hatası"))
                .andExpect(jsonPath("$.invalid_params.latitude").value("Enlem (Latitude) zorunludur"))
                .andExpect(jsonPath("$.invalid_params.longitude").value("Boylam (Longitude) zorunludur"));

        verifyNoInteractions(adoptionService);
    }

    @Test
    void shouldReturn400BadRequestWhenDateIsMissing() throws Exception {
        String requestJson = """
                {
                  "title": "Sahiplendirilecek Sevimli Kedi",
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
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}
        );

        mockMvc.perform(
                        multipart("/api/adoptions")
                                .part(adPart)
                                .file(image)
                                .principal(authentication())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Doğrulama hatası"))
                .andExpect(jsonPath("$.invalid_params.date").value("Tarih alanı boş bırakılamaz"))
                .andExpect(jsonPath("$.validationErrors.date").exists());

        verifyNoInteractions(adoptionService);
    }

    @Test
    void shouldReturn400BadRequestWhenDateIsInFuture() throws Exception {
        String requestJson = """
                {
                  "title": "Sahiplendirilecek Sevimli Kedi",
                  "species": "CAT",
                  "date": "2099-12-31",
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
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}
        );

        when(adoptionService.createAdoptionAd(anyString(), any(), anyList()))
                .thenThrow(new IllegalArgumentException("Tarih gelecekte bir tarih olamaz"));

        mockMvc.perform(
                        multipart("/api/adoptions")
                                .part(adPart)
                                .file(image)
                                .principal(authentication())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Geçersiz parametre"))
                .andExpect(jsonPath("$.detail").value("Tarih gelecekte bir tarih olamaz"));
    }

    @Test
    void shouldReturn400BadRequestWhenDateHasInvalidFormat() throws Exception {
        String requestJson = """
                {
                  "title": "Sahiplendirilecek Sevimli Kedi",
                  "species": "CAT",
                  "date": "2026/08/20",
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
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}
        );

        mockMvc.perform(
                        multipart("/api/adoptions")
                                .part(adPart)
                                .file(image)
                                .principal(authentication())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Doğrulama hatası"))
                .andExpect(jsonPath("$.invalid_params.date").value("Tarih formatı yyyy-MM-dd olmalıdır"));

        verifyNoInteractions(adoptionService);
    }

    @Test
    void shouldReturn400BadRequestWhenDateIsEmptyString() throws Exception {
        String requestJson = """
                {
                  "title": "Sahiplendirilecek Sevimli Kedi",
                  "species": "CAT",
                  "date": "",
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
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}
        );

        mockMvc.perform(
                        multipart("/api/adoptions")
                                .part(adPart)
                                .file(image)
                                .principal(authentication())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.invalid_params.date").value("Tarih alanı boş bırakılamaz"));

        verifyNoInteractions(adoptionService);
    }

    @Test
    void shouldReturn400BadRequestWhenImagesPartIsMissing() throws Exception {
        String requestJson = """
                {
                  "title": "Sahiplendirilecek Sevimli Kedi",
                  "species": "CAT",
                  "date": "2026-08-20",
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

        mockMvc.perform(
                        multipart("/api/adoptions")
                                .part(adPart)
                                .principal(authentication())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Geçersiz istek"));

        verifyNoInteractions(adoptionService);
    }

    private AdResponse sampleAdResponse() {
        return new AdResponse(
                10L,
                "Sahiplendirilecek Sevimli Kedi",
                "Ev ortamına alışkın sevimli kedi",
                Ad.AdType.ADOPTION,
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
                null,
                null,
                List.of("https://example.com/cat.jpg"),
                40.195,
                29.060,
                1L,
                "Owner",
                true,
                false,
                Instant.parse("2026-08-20T10:00:00Z"),
                Instant.parse("2026-08-20T10:00:00Z"),
                AiStatus.NOT_APPLICABLE,
                false,
                false,
                false,
                false
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
