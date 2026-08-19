package com.works.patimati.service;

import com.works.patimati.ai.AiCandidateRow;
import com.works.patimati.entity.Ad;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.storage.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class AiMatchServiceTest {

    @Mock
    private AdRepository adRepository;

    @Mock
    private ImageStorageService imageStorageService;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private RestTemplate restTemplate;

    private AiMatchService aiMatchService;

    @BeforeEach
    void setUp() {
        when(restTemplateBuilder.setConnectTimeout(any(Duration.class))).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.setReadTimeout(any(Duration.class))).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);

        aiMatchService = new AiMatchService(adRepository, imageStorageService, restTemplateBuilder);
        ReflectionTestUtils.setField(aiMatchService, "aiServiceUrl", "http://localhost:8000");
    }


    
    @Test
    void matchImages_EmptyCandidates_ReturnsEmptyList() throws Exception {
        // Arrange
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "dummy".getBytes());
        List<MultipartFile> images = List.of(file);
        
        Map<String, Object> analyzeBody = new HashMap<>();
        analyzeBody.put("species", "cat");
        when(restTemplate.postForEntity(eq("http://localhost:8000/analyze"), any(), any(Class.class)))
                .thenReturn(new ResponseEntity<>(analyzeBody, HttpStatus.OK));

        when(adRepository.findAiCandidatesWithoutLocation(anyString(), any(Instant.class)))
                .thenReturn(List.of());

        // Act
        List<Map<String, Object>> results = aiMatchService.matchImages(images, "FOUND");

        // Assert
        assertTrue(results.isEmpty());
        verify(restTemplate, never()).postForEntity(eq("http://localhost:8000/match"), any(), any(Class.class));
    }
}
