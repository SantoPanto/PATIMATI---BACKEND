package com.works.patimati.controller;

import com.works.patimati.dto.complaint.ComplaintRequest;
import com.works.patimati.dto.complaint.ComplaintResponse;
import com.works.patimati.entity.enums.ComplaintReason;
import com.works.patimati.entity.enums.ComplaintStatus;
import com.works.patimati.exception.GlobalExceptionHandler;
import com.works.patimati.service.ComplaintService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ComplaintControllerTest {

    private ComplaintService complaintService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        complaintService = mock(ComplaintService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new ComplaintController(complaintService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Case-insensitive enum (lowercase sahte_ilan) ile şikayet başarıyla oluşturulmalı")
    void shouldCreateComplaintSuccessfullyWithLowercaseEnum() throws Exception {
        ComplaintResponse mockResponse = new ComplaintResponse(
                1L,
                100L,
                "reporter@patimati.com",
                10L,
                null,
                ComplaintReason.SAHTE_ILAN,
                "Şüpheli sahte ilan açıklaması",
                ComplaintStatus.BEKLEMEDE,
                Instant.now()
        );

        when(complaintService.createComplaint(eq("reporter@patimati.com"), any(ComplaintRequest.class)))
                .thenReturn(mockResponse);

        String jsonPayload = """
                {
                  "reportedAdId": 10,
                  "reason": "sahte_ilan",
                  "description": "Şüpheli sahte ilan açıklaması"
                }
                """;

        mockMvc.perform(
                        post("/api/complaints")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonPayload)
                                .principal(authentication())
                )
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/complaints/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.reason").value("SAHTE_ILAN"))
                .andExpect(jsonPath("$.status").value("BEKLEMEDE"));
    }

    @Test
    @DisplayName("Geçersiz Enum değeri gönderildiğinde açıklayıcı JSON hatası dönmeli")
    void shouldReturnDetailedErrorWhenEnumIsInvalid() throws Exception {
        String jsonPayload = """
                {
                  "reportedAdId": 10,
                  "reason": "GECERSIZ_NEDEN_TEST",
                  "description": "Açıklama"
                }
                """;

        mockMvc.perform(
                        post("/api/complaints")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonPayload)
                                .principal(authentication())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Geçersiz JSON Verisi"))
                .andExpect(jsonPath("$.validationErrors.reason").exists());
    }

    @Test
    @DisplayName("Açıklama boş gönderildiğinde MethodArgumentNotValidException açıklayıcı mesaj dönmeli")
    void shouldReturnValidationErrorWhenDescriptionIsBlank() throws Exception {
        String jsonPayload = """
                {
                  "reportedAdId": 10,
                  "reason": "SAHTE_ILAN",
                  "description": ""
                }
                """;

        mockMvc.perform(
                        post("/api/complaints")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonPayload)
                                .principal(authentication())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Doğrulama hatası"))
                .andExpect(jsonPath("$.detail").value("Gönderilen istek verileri doğrulamadan geçemedi"))
                .andExpect(jsonPath("$.validationErrors.description").value("Şikayet açıklaması boş olamaz"));
    }

    @Test
    @DisplayName("Hedef (ilan veya kullanıcı) belirtilmediğinde IllegalArgumentException 400 Bad Request dönmeli")
    void shouldReturnBadRequestWhenNeitherAdNorUserIsProvided() throws Exception {
        when(complaintService.createComplaint(eq("reporter@patimati.com"), any(ComplaintRequest.class)))
                .thenThrow(new IllegalArgumentException("Şikayet etmek için bir ilan veya bir kullanıcı belirtilmelidir."));

        String jsonPayload = """
                {
                  "reason": "SAHTE_ILAN",
                  "description": "Geçerli bir açıklama"
                }
                """;

        mockMvc.perform(
                        post("/api/complaints")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonPayload)
                                .principal(authentication())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Geçersiz parametre"))
                .andExpect(jsonPath("$.detail").value("Şikayet etmek için bir ilan veya bir kullanıcı belirtilmelidir."));
    }

    @Test
    @DisplayName("Mükerrer şikayette IllegalStateException 409 Conflict dönmeli")
    void shouldReturnConflictWhenDuplicateComplaintExists() throws Exception {
        when(complaintService.createComplaint(eq("reporter@patimati.com"), any(ComplaintRequest.class)))
                .thenThrow(new IllegalStateException("Bu ilan için halihazırda incelenmekte olan bir şikayetiniz bulunmaktadır."));

        String jsonPayload = """
                {
                  "reportedAdId": 10,
                  "reason": "SAHTE_ILAN",
                  "description": "Mükerrer şikayet açıklaması"
                }
                """;

        mockMvc.perform(
                        post("/api/complaints")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonPayload)
                                .principal(authentication())
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("İşlem çakışması"))
                .andExpect(jsonPath("$.detail").value("Bu ilan için halihazırda incelenmekte olan bir şikayetiniz bulunmaktadır."));
    }

    private UsernamePasswordAuthenticationToken authentication() {
        return new UsernamePasswordAuthenticationToken(
                "reporter@patimati.com",
                null,
                List.of()
        );
    }
}
