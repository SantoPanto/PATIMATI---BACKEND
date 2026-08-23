package com.works.patimati.ai;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.works.patimati.ai.dto.AiMatchRequest;
import com.works.patimati.ai.dto.AiMatchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * AŞAMA 2'nin tek yeni bağımlılığı: AI servisinin ZATEN VAR OLAN
 * {@code POST /match} ucuna senkron HTTP çağrısı (Faz 2 revize blueprint
 * §1/§5). Java ↔ Python arasındaki tek HTTP köprüsü budur — geri kalan her
 * şey RabbitMQ üzerinden yürümeye devam eder.
 *
 * <p>Aynı güven sınırı RabbitMQ bağlantısıyla aynıdır: AI servisi iç ağda
 * kalmalıdır (AI deposu {@code docs/entegrasyon-sozlesmesi.md} §10).
 *
 * <p>Herhangi bir hata (zaman aşımı, bağlantı reddi, 5xx) burada tutulmaz —
 * çağıran taraf ({@code ExternalMatchingService}) bunu yakalayıp kaydı
 * {@code ANALYZED} durumunda bırakmakla yükümlüdür; Aşama 1'in sonucu asla
 * kaybolmaz.
 */
@Component
public class AiMatchClient {

    private static final Logger log = LoggerFactory.getLogger(AiMatchClient.class);

    private final RestClient restClient;

    public AiMatchClient(
            @Value("${ai.service.base-url:http://localhost:8000}") String baseUrl,
            @Value("${ai.service.match-timeout:5s}") Duration timeout) {

        ObjectMapper matchMapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        matchMapper.findAndRegisterModules();

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(matchMapper);
        converter.setSupportedMediaTypes(java.util.List.of(MediaType.APPLICATION_JSON));

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        int timeoutMillis = (int) timeout.toMillis();
        requestFactory.setConnectTimeout(timeoutMillis);
        requestFactory.setReadTimeout(timeoutMillis);

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .messageConverters(converters -> {
                    converters.clear();
                    converters.add(converter);
                })
                .build();
    }

    /** @throws RuntimeException herhangi bir ağ/HTTP hatasında — bilinçli olarak yutulmaz, çağıran karar versin. */
    public AiMatchResponse match(AiMatchRequest request) {
        return restClient.post()
                .uri("/match")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(AiMatchResponse.class);
    }
}
