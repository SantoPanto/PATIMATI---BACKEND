package com.works.patimati.instagram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Instagram Graph API'nin (Content Publishing) ham HTTP istemcisi.
 *
 * <p><b>Kimlik doğrulama:</b> PatiMati'nin kendi Instagram Business/Creator
 * hesabına, tek bir uzun ömürlü Page Access Token ile paylaşım yapılır
 * (kullanıcı-bazlı OAuth akışı YOK -- bu özellik başka kullanıcıların
 * hesaplarına bağlanmıyor, yalnızca kendi hesabımıza; Meta Business
 * Verification/App Review bu yüzden gerekmiyor, hesap Meta Developer App'e
 * "Tester" rolüyle eklenip Development Mode'da kullanılıyor).
 *
 * <p><b>Akış</b> (bkz. {@link #publish}): (1) medya container'ı oluştur --
 * tek fotoğrafsa doğrudan, birden fazlaysa önce her biri için bir
 * carousel-item container'ı, sonra hepsini saran bir CAROUSEL container'ı;
 * (2) container {@code FINISHED} olana kadar kısa aralıklarla durumunu
 * sorgula; (3) container'ı yayınla. Bu tek, admin-tetiklemeli senkron bir
 * çağrı olduğu için basit bir bekleme döngüsü yeterli -- {@code @Scheduled}
 * bir arka plan poller'ına gerek yok.
 */
@Component
@Slf4j
public class InstagramGraphClient {

    private static final int MAX_POLL_ATTEMPTS = 15;
    private static final long POLL_INTERVAL_MS = 2000;

    private final RestTemplate restTemplate;

    @Value("${instagram.graph.api-base:https://graph.facebook.com/v21.0}")
    private String apiBase;

    @Value("${instagram.graph.access-token:}")
    private String accessToken;

    @Value("${instagram.graph.business-account-id:}")
    private String businessAccountId;

    public InstagramGraphClient(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(30))
                .build();
    }

    /** false ise {@link #publish} hiç denenmemeli -- token/hesap kimliği eksik. */
    public boolean isConfigured() {
        return accessToken != null && !accessToken.isBlank()
                && businessAccountId != null && !businessAccountId.isBlank();
    }

    /**
     * Bir ya da daha fazla (Instagram carousel sınırı: en fazla 10) fotoğrafı
     * tek bir gönderi olarak yayınlar.
     */
    public InstagramGraphResult publish(List<String> photoUrls, String caption) {
        if (!isConfigured()) {
            return InstagramGraphResult.failure("Instagram entegrasyonu yapılandırılmamış (access-token/business-account-id eksik).");
        }
        if (photoUrls == null || photoUrls.isEmpty()) {
            return InstagramGraphResult.failure("İlanın paylaşılacak fotoğrafı yok.");
        }

        try {
            String containerId = photoUrls.size() == 1
                    ? createImageContainer(photoUrls.get(0), caption)
                    : createCarouselContainer(photoUrls, caption);

            if (!pollUntilFinished(containerId)) {
                return InstagramGraphResult.failure("Instagram medyayı işlerken zaman aşımına uğradı.");
            }

            String mediaId = publishContainer(containerId);
            String permalink = fetchPermalinkQuietly(mediaId);
            return InstagramGraphResult.success(mediaId, permalink);
        } catch (RestClientException e) {
            String mesaj = extractErrorMessage(e);
            log.warn("Instagram Graph API çağrısı başarısız: {}", mesaj);
            return InstagramGraphResult.failure(mesaj);
        }
    }

    private String createImageContainer(String imageUrl, String caption) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image_url", imageUrl);
        body.add("caption", caption);
        return post(businessAccountId + "/media", body);
    }

    private String createCarouselContainer(List<String> photoUrls, String caption) {
        List<String> itemContainerIds = new ArrayList<>();
        for (String url : photoUrls) {
            MultiValueMap<String, Object> itemBody = new LinkedMultiValueMap<>();
            itemBody.add("image_url", url);
            itemBody.add("is_carousel_item", "true");
            itemContainerIds.add(post(businessAccountId + "/media", itemBody));
        }

        MultiValueMap<String, Object> parentBody = new LinkedMultiValueMap<>();
        parentBody.add("media_type", "CAROUSEL");
        parentBody.add("caption", caption);
        parentBody.add("children", String.join(",", itemContainerIds));
        return post(businessAccountId + "/media", parentBody);
    }

    /** @return true ise container {@code FINISHED}, false ise zaman aşımı/hata. */
    private boolean pollUntilFinished(String containerId) {
        for (int attempt = 1; attempt <= MAX_POLL_ATTEMPTS; attempt++) {
            String url = UriComponentsBuilder.fromHttpUrl(apiBase + "/" + containerId)
                    .queryParam("fields", "status_code")
                    .queryParam("access_token", accessToken)
                    .toUriString();

            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.getForEntity(url, (Class<Map<String, Object>>) (Class<?>) Map.class);

            String status = response.getBody() != null
                    ? String.valueOf(response.getBody().get("status_code"))
                    : null;

            if ("FINISHED".equals(status)) {
                return true;
            }
            if ("ERROR".equals(status) || "EXPIRED".equals(status)) {
                return false;
            }

            sleepQuietly(POLL_INTERVAL_MS);
        }
        return false;
    }

    private String publishContainer(String containerId) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("creation_id", containerId);
        Map<String, Object> response = postForBody(businessAccountId + "/media_publish", body);
        return String.valueOf(response.get("id"));
    }

    /** Kalıcı gönderi linki -- salt bilgi amaçlı, alınamazsa yayın YİNE DE başarılı sayılır. */
    private String fetchPermalinkQuietly(String mediaId) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(apiBase + "/" + mediaId)
                    .queryParam("fields", "permalink")
                    .queryParam("access_token", accessToken)
                    .toUriString();
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.getForEntity(url, (Class<Map<String, Object>>) (Class<?>) Map.class);
            Object permalink = response.getBody() != null ? response.getBody().get("permalink") : null;
            return permalink != null ? String.valueOf(permalink) : null;
        } catch (RestClientException e) {
            log.warn("Instagram permalink alınamadı (gönderi yine de yayınlandı): {}", e.toString());
            return null;
        }
    }

    private String post(String path, MultiValueMap<String, Object> body) {
        Map<String, Object> response = postForBody(path, body);
        return String.valueOf(response.get("id"));
    }

    private Map<String, Object> postForBody(String path, MultiValueMap<String, Object> body) {
        body.add("access_token", accessToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        @SuppressWarnings("unchecked")
        ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
                apiBase + "/" + path,
                new HttpEntity<>(body, headers),
                (Class<Map<String, Object>>) (Class<?>) Map.class
        );

        if (response.getBody() == null) {
            throw new RestClientException("Instagram Graph API boş yanıt döndürdü.");
        }
        return response.getBody();
    }

    /** Graph API hatalarını okunur bir mesaja çevirir -- {"error":{"message": "..."}} zarfı. */
    private String extractErrorMessage(RestClientException e) {
        if (e instanceof HttpStatusCodeException httpError) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> body = new com.fasterxml.jackson.databind.ObjectMapper()
                        .readValue(httpError.getResponseBodyAsString(), Map.class);
                Object error = body.get("error");
                if (error instanceof Map<?, ?> errorMap && errorMap.get("message") != null) {
                    return String.valueOf(errorMap.get("message"));
                }
            } catch (Exception parseError) {
                // Ayrıştırılamadı -- aşağıdaki genel mesaja düşülür.
            }
            return "Instagram API hatası (HTTP " + httpError.getStatusCode().value() + ")";
        }
        return "Instagram API'sine ulaşılamadı: " + e.getMessage();
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
