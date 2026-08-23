package com.works.patimati.controller;

import com.works.patimati.dto.match.MatchedAdResponseDTO;
import com.works.patimati.service.AiMatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiMatchControllerTest {

    @Mock
    private AiMatchService aiMatchService;

    @InjectMocks
    private AiMatchController aiMatchController;

    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        mockFile = new MockMultipartFile("images", "test.jpg", "image/jpeg", "dummy-image-content".getBytes());
    }

    @Test
    void matchAd_Success() throws Exception {
        // Arrange
        MatchedAdResponseDTO mockResult = MatchedAdResponseDTO.builder()
                .score(0.98)
                .build();
        List<MatchedAdResponseDTO> mockServiceResponse = List.of(mockResult);

        when(aiMatchService.matchImages(any(), eq("LOST"), any(), any())).thenReturn(mockServiceResponse);

        // Act
        ResponseEntity<?> response = aiMatchController.matchAd(List.of(mockFile), "LOST", null, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockServiceResponse, response.getBody());
    }

    @Test
    void matchAd_Exception_ReturnsInternalServerError() throws Exception {
        // Arrange
        when(aiMatchService.matchImages(any(), eq("LOST"), any(), any())).thenThrow(new RuntimeException("AI down"));

        // Act
        ResponseEntity<?> response = aiMatchController.matchAd(List.of(mockFile), "LOST", null, null);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("AI servisi ge\u00E7ici olarak hizmet veremiyor.", response.getBody());
    }
}
