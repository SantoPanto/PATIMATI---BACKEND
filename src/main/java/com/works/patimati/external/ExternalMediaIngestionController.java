package com.works.patimati.external;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Collector → Backend medya transferi (Faz 2 revize blueprint §4).
 *
 * <p><b>Bu bir kullanıcı ucu DEĞİLDİR.</b> {@code InternalServiceAuthFilter}
 * tarafından paylaşılan-sır ile korunur, normal kullanıcı JWT'si kabul
 * etmez/gerektirmez.
 */
@RestController
@RequestMapping("/internal/ingestion")
@RequiredArgsConstructor
public class ExternalMediaIngestionController {

    private final ExternalMediaIngestionService mediaIngestionService;

    @PostMapping(value = "/media", consumes = "multipart/form-data")
    public ResponseEntity<ExternalMediaIngestionService.Result> uploadMedia(
            @RequestParam("source") String source,
            @RequestParam("source_post_id") String sourcePostId,
            @RequestParam("ordinal") int ordinal,
            @RequestParam(value = "content_sha256", required = false) String contentSha256,
            @RequestParam("file") MultipartFile file) {

        ExternalMediaIngestionService.Result result =
                mediaIngestionService.attach(source, sourcePostId, ordinal, contentSha256, file);
        return ResponseEntity.ok(result);
    }
}
