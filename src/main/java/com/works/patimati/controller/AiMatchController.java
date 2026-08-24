package com.works.patimati.controller;

import com.works.patimati.dto.match.MatchedAdResponseDTO;
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
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/ai-match")
@RequiredArgsConstructor
public class AiMatchController {

    private static final Logger log = LoggerFactory.getLogger(AiMatchController.class);

    private final AiMatchService aiMatchService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> matchAd(
            @RequestParam("images") List<MultipartFile> images,
            @RequestParam("listingType") String listingType) {
        try {
            List<MatchedAdResponseDTO> results = aiMatchService.matchImages(images, listingType);
            return ResponseEntity.ok(results);
        } catch (IllegalArgumentException e) {
            // Ge\u00E7ersiz listingType -- GlobalExceptionHandler bunu zaten
            // 400'e \u00E7eviriyor (handleIllegalArgument), burada YAKALANMAMALI.
            throw e;
        } catch (RestClientException e) {
            // AI servisine ula\u015F\u0131lamad\u0131/zaman a\u015F\u0131m\u0131/5xx -- beklenen bir d\u0131\u015F
            // servis ar\u0131zas\u0131, programlama hatas\u0131 de\u011Fil. Tam stack trace
            // burada g\u00FCr\u00FClt\u00FCden ba\u015Fka bir \u015Fey katmaz.
            log.warn("AI e\u015Fle\u015Ftirme servisine ula\u015F\u0131lamad\u0131: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("AI servisi ge\u00E7ici olarak hizmet veremiyor.");
        } catch (Exception e) {
            // Beklenmeyen -- NPE veya ba\u015Fka bir programlama hatas\u0131 olabilir,
            // tan\u0131 i\u00E7in tam stack trace loglanmal\u0131.
            log.error("AI e\u015Fle\u015Ftirme s\u0131ras\u0131nda beklenmeyen hata", e);
            return ResponseEntity.internalServerError().body("AI servisi ge\u00E7ici olarak hizmet veremiyor.");
        }
    }
}

