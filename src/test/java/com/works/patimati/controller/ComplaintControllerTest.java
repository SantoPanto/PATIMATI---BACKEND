package com.works.patimati.controller;

import com.works.patimati.dto.complaint.AdComplaintRequestDTO;
import com.works.patimati.dto.complaint.ComplaintResponse;
import com.works.patimati.dto.complaint.UserComplaintRequestDTO;
import com.works.patimati.entity.enums.ComplaintReason;
import com.works.patimati.entity.enums.ComplaintStatus;
import com.works.patimati.exception.GlobalExceptionHandler;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.service.AdComplaintService;
import com.works.patimati.service.UserComplaintService;
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

    private UserComplaintService userComplaintService;
    private AdComplaintService adComplaintService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userComplaintService = mock(UserComplaintService.class);
        adComplaintService = mock(AdComplaintService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new ComplaintController(
                        userComplaintService, adComplaintService,
                        mock(com.works.patimati.service.MyComplaintsService.class)))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/complaints/ad - Case-insensitive enum ile ilan şikayeti başarıyla oluşturulmalı")
    void shouldCreateAdComplaintSuccessfully() throws Exception {
        ComplaintResponse mockResponse = new ComplaintResponse(
                1L,
                100L,
                "reporter@patimati.com",
                10L,
                200L,
                ComplaintReason.SAHTE_ILAN,
                "Şüpheli sahte ilan açıklaması",
                ComplaintStatus.BEKLEMEDE,
                Instant.now()
        );

        when(adComplaintService.createAdComplaint(eq("reporter@patimati.com"), any(AdComplaintRequestDTO.class)))
                .thenReturn(mockResponse);

        String jsonPayload = """
                {
                  "reportedAdId": 10,
                  "reason": "sahte_ilan",
                  "description": "Şüpheli sahte ilan açıklaması"
                }
                """;

        mockMvc.perform(
                        post("/api/complaints/ad")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonPayload)
                                .principal(authentication())
                )
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/complaints/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.reportedAdId").value(10))
                .andExpect(jsonPath("$.reportedUserId").value(200))
                .andExpect(jsonPath("$.reason").value("SAHTE_ILAN"))
                .andExpect(jsonPath("$.status").value("BEKLEMEDE"));
    }

    @Test
    @DisplayName("POST /api/complaints/user - Kullanıcı profil şikayeti başarıyla oluşturulmalı")
    void shouldCreateUserComplaintSuccessfully() throws Exception {
        ComplaintResponse mockResponse = new ComplaintResponse(
                2L,
                100L,
                "reporter@patimati.com",
                null,
                300L,
                ComplaintReason.DOLANDIRICILIK,
                "Şüpheli kullanıcı faaliyeti",
                ComplaintStatus.BEKLEMEDE,
                Instant.now()
        );

        when(userComplaintService.createUserComplaint(eq("reporter@patimati.com"), any(UserComplaintRequestDTO.class)))
                .thenReturn(mockResponse);

        String jsonPayload = """
                {
                  "reportedUserId": 300,
                  "reason": "DOLANDIRICILIK",
                  "description": "Şüpheli kullanıcı faaliyeti"
                }
                """;

        mockMvc.perform(
                        post("/api/complaints/user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonPayload)
                                .principal(authentication())
                )
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/complaints/2"))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.reportedAdId").doesNotExist())
                .andExpect(jsonPath("$.reportedUserId").value(300))
                .andExpect(jsonPath("$.reason").value("DOLANDIRICILIK"))
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
                        post("/api/complaints/ad")
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
                        post("/api/complaints/ad")
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
    @DisplayName("Şikayet edilen ilan veritabanında bulunamadığında 404 Not Found dönmeli")
    void shouldReturn404WhenReportedAdNotFound() throws Exception {
        when(adComplaintService.createAdComplaint(eq("reporter@patimati.com"), any(AdComplaintRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Şikayet edilen ilan bulunamadı ID: 999"));

        String jsonPayload = """
                {
                  "reportedAdId": 999,
                  "reason": "SAHTE_ILAN",
                  "description": "Açıklama"
                }
                """;

        mockMvc.perform(
                        post("/api/complaints/ad")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonPayload)
                                .principal(authentication())
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Kaynak bulunamadı"))
                .andExpect(jsonPath("$.detail").value("Şikayet edilen ilan bulunamadı ID: 999"));
    }

    @Test
    @DisplayName("Şikayet edilen kullanıcı veritabanında bulunamadığında 404 Not Found dönmeli")
    void shouldReturn404WhenReportedUserNotFound() throws Exception {
        when(userComplaintService.createUserComplaint(eq("reporter@patimati.com"), any(UserComplaintRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Şikayet edilen kullanıcı bulunamadı ID: 888"));

        String jsonPayload = """
                {
                  "reportedUserId": 888,
                  "reason": "DOLANDIRICILIK",
                  "description": "Açıklama"
                }
                """;

        mockMvc.perform(
                        post("/api/complaints/user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonPayload)
                                .principal(authentication())
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Kaynak bulunamadı"))
                .andExpect(jsonPath("$.detail").value("Şikayet edilen kullanıcı bulunamadı ID: 888"));
    }

    @Test
    @DisplayName("Mükerrer şikayette IllegalStateException 409 Conflict dönmeli")
    void shouldReturnConflictWhenDuplicateComplaintExists() throws Exception {
        when(adComplaintService.createAdComplaint(eq("reporter@patimati.com"), any(AdComplaintRequestDTO.class)))
                .thenThrow(new IllegalStateException("Bu ilan için halihazırda incelenmekte olan bir şikayetiniz bulunmaktadır."));

        String jsonPayload = """
                {
                  "reportedAdId": 10,
                  "reason": "SAHTE_ILAN",
                  "description": "Mükerrer şikayet açıklaması"
                }
                """;

        mockMvc.perform(
                        post("/api/complaints/ad")
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
