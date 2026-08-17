package com.works.patimati.controller;

import com.works.patimati.service.AiMatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/ai-match")
@RequiredArgsConstructor
public class AiMatchController {

    private final AiMatchService aiMatchService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> matchAd(
            @RequestParam("images") List<MultipartFile> images,
            @RequestParam("listingType") String listingType) {
        try {
            var results = aiMatchService.matchImages(images, listingType);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("AI servisi ge\u00E7ici olarak hizmet veremiyor.");
        }
    }
}
