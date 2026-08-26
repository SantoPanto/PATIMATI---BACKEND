package com.works.patimati.instagram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI servisinden bir ilan için Instagram gönderi metni (caption) ister --
 * {@code AiMatchService}'in AYNI AI servisine yaptığı çağrılarla (bkz.
 * {@code aiBasliklari}) aynı ilkeyi izler, ama BİLEREK ayrı bir sınıf:
 * {@code AiMatchService} zaten {@code AdService}'e bağımlı, {@code
 * InstagramPublishService} de {@code AdService}'in ÇAĞIRDIĞI bir servis --
 * caption isteği oraya eklenseydi AdService → InstagramPublishService →
 * AiMatchService → AdService devresel bağımlılığı (circular dependency)
 * oluşurdu. Bu sınıf hiçbir Ad/AdService bilgisi taşımaz, yalnızca düz
 * metin alanları alır.
 *
 * <p><b>Hiçbir zaman istisna fırlatmaz</b> -- sağlayıcı yapılandırılmamışsa
 * ya da çağrı başarısız olursa {@code null} döner, çağıran
 * ({@code InstagramPublishService}) kendi şablon caption'ına düşer.
 */
@Component
@Slf4j
public class InstagramCaptionAiClient {

    private static final String ANAHTAR_BASLIGI = "X-Api-Key";

    private final RestTemplate restTemplate;

    @Value("${ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    @Value("${ai.service.api-key:}")
    private String aiApiKey;

    public InstagramCaptionAiClient(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(30))
                .build();
    }

    public String generateCaption(String ilanTuru, String tur, String irk,
                                  String renkler, String ilIlce, String aciklama,
                                  List<String> ayirtEdici) {
        Map<String, Object> body = new HashMap<>();
        body.put("ilan_turu", ilanTuru);
        body.put("tur", tur);
        body.put("irk", irk);
        body.put("renkler", renkler);
        body.put("il_ilce", ilIlce);
        body.put("aciklama", aciklama);
        body.put("ayirt_edici", ayirtEdici != null ? ayirtEdici : List.of());

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (aiApiKey != null && !aiApiKey.isBlank()) {
                headers.set(ANAHTAR_BASLIGI, aiApiKey);
            }

            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
                    aiServiceUrl + "/generate_instagram_caption",
                    new HttpEntity<>(body, headers),
                    (Class<Map<String, Object>>) (Class<?>) Map.class
            );
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return null;
            }
            Object caption = response.getBody().get("caption");
            return caption instanceof String && !((String) caption).isBlank() ? (String) caption : null;
        } catch (RuntimeException e) {
            log.warn("Instagram caption üretimi başarısız, şablona düşülecek: {}", e.toString());
            return null;
        }
    }
}
