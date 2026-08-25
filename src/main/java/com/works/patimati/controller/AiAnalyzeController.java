package com.works.patimati.controller;

import com.works.patimati.dto.ai.AiAnalyzeResponse;
import com.works.patimati.service.AiMatchService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * İlan oluşturma ekranının fotoğraf analizi — <b>AI'ya backend üzerinden</b>.
 *
 * <p>AI servisinin ham çıktısını {@link AiAnalyzeResponse} DTO'suna haritalayarak
 * frontend'in ilan otomatik doldurma bileşenine iletir.
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiAnalyzeController {

    private static final Logger log = LoggerFactory.getLogger(AiAnalyzeController.class);

    private final AiMatchService aiMatchService;

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> analyze(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Analiz edilecek fotoğraf gerekli."));
        }

        try {
            AiAnalyzeResponse sonuc = aiMatchService.analyzeImageForFrontend(file);
            if (sonuc == null) {
                return ResponseEntity.status(502).body(Map.of("message", AI_ULASILAMIYOR));
            }
            return ResponseEntity.ok(sonuc);
        } catch (Exception e) {
            log.warn("AI fotoğraf analizi başarısız", e);
            return ResponseEntity.status(502).body(Map.of("message", AI_ULASILAMIYOR));
        }
    }

    private static final String AI_ULASILAMIYOR = "AI servisi geçici olarak hizmet veremiyor.";
}
