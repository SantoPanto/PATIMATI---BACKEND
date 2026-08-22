package com.works.patimati.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * Koordinattan il/ilçe çözer (Nominatim reverse, V19).
 *
 * <p><b>Sözleşme:</b> bu servis HİÇBİR ZAMAN istisna fırlatmaz — ilan
 * oluşturma akışının içinden çağrılıyor ve dış servisin çökmesi ilan
 * kaydını asla engellememeli. Her hata {@code Optional.empty()} + log.
 *
 * <p><b>Nominatim kullanım politikası:</b> tanıtıcı User-Agent zorunlu,
 * istek hızı en fazla 1/sn. Kayıt yolunda ilan başına zaten en fazla bir
 * istek çıkar; toplu backfill ise aramalar arasında bekler
 * (bkz. AdminServiceImpl#backfillAdLocations).
 */
@Service
public class ReverseGeocodingService {

    private static final Logger log = LoggerFactory.getLogger(ReverseGeocodingService.class);
    private static final String USER_AGENT = "patimati-backend/1.0 (+https://patimati.me)";

    /** İl + ilçe; ikisi de boş olamaz (o durumda Optional.empty döner). */
    public record IlIlce(String il, String ilce) {
    }

    private final RestTemplate restTemplate;
    private final String url;
    private final boolean enabled;

    // İki constructor var (alttaki testler için) — Spring'in hangisiyle
    // kuracağı bu işaret olmadan belirsiz kalıyor ve context çöküyordu.
    @Autowired
    public ReverseGeocodingService(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${app.geocoding.url}") String url,
            @Value("${app.geocoding.enabled}") boolean enabled,
            @Value("${app.geocoding.timeout-ms}") long timeoutMs
    ) {
        this(
                restTemplateBuilder
                        .connectTimeout(Duration.ofMillis(timeoutMs))
                        .readTimeout(Duration.ofMillis(timeoutMs))
                        .build(),
                url,
                enabled
        );
    }

    /** Test için: hazır RestTemplate ile kurulum. */
    ReverseGeocodingService(RestTemplate restTemplate, String url, boolean enabled) {
        this.restTemplate = restTemplate;
        this.url = url;
        this.enabled = enabled;
    }

    @SuppressWarnings("unchecked")
    public Optional<IlIlce> cozumle(Double latitude, Double longitude) {
        if (!enabled || latitude == null || longitude == null) {
            return Optional.empty();
        }

        try {
            String istekUrl = UriComponentsBuilder.fromUriString(url)
                    .queryParam("format", "jsonv2")
                    .queryParam("lat", latitude)
                    .queryParam("lon", longitude)
                    // zoom=10: ilçe düzeyi — bina/sokak ayrıntısı gereksiz.
                    .queryParam("zoom", 10)
                    .queryParam("accept-language", "tr")
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.USER_AGENT, USER_AGENT);

            ResponseEntity<Map> cevap = restTemplate.exchange(
                    istekUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );

            Object addressObj = cevap.getBody() == null
                    ? null
                    : cevap.getBody().get("address");
            if (!(addressObj instanceof Map)) {
                return Optional.empty();
            }
            Map<String, Object> address = (Map<String, Object>) addressObj;

            // Türkiye'de Nominatim büyükşehirlerde ili "city" ("Ankara"),
            // diğer illerde "province" olarak döner; ilçe çoğunlukla
            // "county" ("Çankaya"), bazen "town"/"district".
            String il = ilkDolu(address, "city", "province", "state");
            String ilce = ilkDolu(address, "county", "town", "district", "suburb");

            if (il == null && ilce == null) {
                return Optional.empty();
            }
            return Optional.of(new IlIlce(il, ilce));
        } catch (RuntimeException exception) {
            log.warn("Ters geokodlama başarısız (lat={} lon={}): {}",
                    latitude, longitude, exception.getMessage());
            return Optional.empty();
        }
    }

    private static String ilkDolu(Map<String, Object> address, String... anahtarlar) {
        for (String anahtar : anahtarlar) {
            Object deger = address.get(anahtar);
            if (deger instanceof String s && !s.isBlank()) {
                return s.trim();
            }
        }
        return null;
    }
}
