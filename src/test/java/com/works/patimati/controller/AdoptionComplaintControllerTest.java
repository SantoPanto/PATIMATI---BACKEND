package com.works.patimati.controller;

import com.works.patimati.dto.complaint.ComplaintResponse;
import com.works.patimati.entity.enums.ComplaintReason;
import com.works.patimati.entity.enums.ComplaintStatus;
import com.works.patimati.exception.GlobalExceptionHandler;
import com.works.patimati.service.AdoptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdoptionComplaintControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdoptionService adoptionService;

    @InjectMocks
    private AdoptionComplaintController adoptionComplaintController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(adoptionComplaintController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void resolveAdoptionComplaint_ShouldReturnOk_WhenResolvedSuccessfully() throws Exception {
        ComplaintResponse mockResponse = new ComplaintResponse(
                10L,
                1L,
                "reporter@example.com",
                20L,
                2L,
                ComplaintReason.SAHTE_ILAN,
                "Şikayet detay açıklaması",
                ComplaintStatus.COZULDU,
                Instant.now()
        );

        when(adoptionService.resolveAdoptionComplaint(10L)).thenReturn(mockResponse);

        mockMvc.perform(patch("/api/v1/adoption-complaints/10/resolve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("COZULDU"));

        verify(adoptionService, times(1)).resolveAdoptionComplaint(10L);
    }
}
