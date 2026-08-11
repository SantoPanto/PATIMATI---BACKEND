package com.works.patimati.ai;

import com.works.patimati.ai.dto.AiAnalysisResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConverter;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AI kuyruğundan gelen cevabın gerçekten çözülebildiğini doğrular.
 *
 * <p><b>Neden bu test var:</b> 2026-08-06'da AI analizleri başarıyla bitiyor,
 * sonuç kuyruğa yazılıyor, ama ilana hiç işlenmiyordu — ilanlar sonsuza kadar
 * {@code PENDING} kalıyordu. Dışarıdan bakınca "AI çalışmıyor" görünüyordu.
 *
 * <p>Sebep, elle kurulan {@code ObjectMapper}'ın {@code java.time} tipini
 * tanımamasıydı:
 *
 * <pre>
 * Java 8 date/time type `java.time.Instant` not supported by default
 * (through reference chain: AiAnalysisResult["processed_at"])
 * </pre>
 *
 * <p>Daha önce görülmemesinin sebebi: proje Jackson 3 kullanırken
 * {@code java.time} desteği çekirdekte geliyordu. Jackson 2'ye dönülünce ayrı
 * modül gerekti ve dönüştürücü sessizce kırıldı. Bu test, aynı sessiz kırılmayı
 * bir daha yaşamamak için sözleşmedeki gerçek cevabı baştan sona çözer.
 *
 * <p>Broker gerektirmez.
 */
class AiJsonMessageConverterTest {

    /** Sözleşme §6'daki cevabın birebir biçimi (snake_case + ISO-8601 zaman). */
    private static final String AI_CEVABI = """
            {
              "schema_version": 1,
              "request_id": "b7c1f3a0-0000-4000-8000-000000000001",
              "ad_id": 42,
              "status": "ok",
              "model_version": "siglip2-animal/v2",
              "processed_at": "2026-08-06T17:05:52Z",
              "analysis": {
                "species": "cat",
                "species_confidence": 0.97,
                "breed": null,
                "breed_confidence": 0.34,
                "labels": ["cat", "tabby", "gray"],
                "is_pet": true,
                "embeddings": [[0.1, 0.2, 0.3]]
              },
              "matches": []
            }
            """;

    private final MessageConverter converter = new AiRabbitConfig().aiJsonMessageConverter();

    @Test
    @DisplayName("AI cevabındaki processed_at zaman damgası çözülebilmeli")
    void aiCevabiCozulebilmeli() {
        MessageProperties props = new MessageProperties();
        props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        props.setInferredArgumentType(AiAnalysisResult.class);

        Message mesaj = new Message(AI_CEVABI.getBytes(StandardCharsets.UTF_8), props);

        Object cozulen = converter.fromMessage(mesaj);

        assertThat(cozulen)
                .withFailMessage("AI cevabı AiAnalysisResult'a çözülemedi: %s", cozulen)
                .isInstanceOf(AiAnalysisResult.class);

        AiAnalysisResult sonuc = (AiAnalysisResult) cozulen;

        assertThat(sonuc.adId()).isEqualTo(42L);
        assertThat(sonuc.status()).isEqualTo("ok");
        assertThat(sonuc.modelVersion()).isEqualTo("siglip2-animal/v2");
        assertThat(sonuc.processedAt())
                .withFailMessage("processed_at çözülemedi — java.time modülü kayıtlı mı?")
                .isEqualTo(Instant.parse("2026-08-06T17:05:52Z"));
        assertThat(sonuc.analysis().species()).isEqualTo("cat");
        assertThat(sonuc.analysis().labels()).containsExactly("cat", "tabby", "gray");
    }
}
