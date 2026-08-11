package com.works.patimati.ai;

import com.works.patimati.ai.dto.AiAnalysisResult;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.enums.AiStatus;
import com.works.patimati.repository.AdRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConverter;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * AI'ın "fotoğrafta gerçekten kedi/köpek var mı" cevabının ilana yazıldığını
 * doğrular.
 *
 * <p><b>Neden bu test var:</b> 2026-08-05 uçtan uca denemesinde bulundu —
 * AI bu bilgiyi doğru üretiyor ({@code is_pet=false} düz renk ve gürültü
 * görüntülerinde ölçüldü) ve mesajda gönderiyordu, ama Java tarafında
 * {@code AiAnalysisResult.Analysis.isPet()} <b>hiçbir yerde okunmuyordu.</b>
 * Sonuç: ekran görüntüsüyle açılan bir ilan normal ilan gibi {@code DONE} olup
 * aday havuzuna giriyor, kullanıcı da uyarılamıyordu. Hata veren bir şey yoktu;
 * bilgi sessizce çöpe gidiyordu.
 *
 * <p>Test iki ayrı tuzağı birden bekler:
 * <ol>
 *   <li>alan okunup ilana yazılıyor mu (asıl hata),</li>
 *   <li>alan mesajda <b>hiç gelmezse</b> {@code null} mı kalıyor — yoksa
 *       ilkel {@code boolean} yüzünden sessizce {@code false} olup "bu
 *       fotoğrafta hayvan yok" diye yanlış bir iddiaya mı dönüşüyor.</li>
 * </ol>
 *
 * <p>Broker ve veritabanı gerektirmez.
 */
class AiIsPetPersistedTest {

    private static final String CEVAP_HAYVAN_YOK = """
            {
              "schema_version": 1,
              "request_id": "b7c1f3a0-0000-4000-8000-000000000002",
              "ad_id": 42,
              "status": "ok",
              "model_version": "siglip2-animal/v2",
              "processed_at": "2026-08-07T18:00:00Z",
              "analysis": {
                "species": "unknown",
                "species_confidence": 0.31,
                "breed": null,
                "breed_confidence": 0.0,
                "labels": [],
                "is_pet": false,
                "embeddings": [[0.1, 0.2, 0.3]]
              },
              "matches": []
            }
            """;

    /** Aynı cevap, ama {@code is_pet} alanı HİÇ yok. */
    private static final String CEVAP_ALAN_YOK = """
            {
              "schema_version": 1,
              "request_id": "b7c1f3a0-0000-4000-8000-000000000003",
              "ad_id": 42,
              "status": "ok",
              "model_version": "siglip2-animal/v2",
              "processed_at": "2026-08-07T18:00:00Z",
              "analysis": {
                "species": "cat",
                "species_confidence": 0.97,
                "breed": null,
                "breed_confidence": 0.0,
                "labels": ["cat"],
                "embeddings": [[0.1, 0.2, 0.3]]
              },
              "matches": []
            }
            """;

    private final MessageConverter converter = new AiRabbitConfig().aiJsonMessageConverter();

    @Test
    @DisplayName("is_pet=false ilana yazılmalı — bilgi çöpe gitmemeli")
    void hayvanYokBilgisiIlanaYazilmali() {
        Ad ad = isle(CEVAP_HAYVAN_YOK);

        assertThat(ad.getAiIsPet())
                .withFailMessage("AI 'fotoğrafta hayvan yok' dedi ama ilana yazılmadı — "
                        + "AiAnalysisListener.applyAnalysis içinde setAiIsPet var mı?")
                .isFalse();

        // İlan yine de yayında ve analizi bitmiş sayılır: is_pet bir eleme
        // ölçütü değil, kullanıcıya gösterilecek bir bilgi (sözleşme §4).
        assertThat(ad.getAiStatus()).isEqualTo(AiStatus.DONE);
    }

    @Test
    @DisplayName("is_pet alanı hiç gelmezse null kalmalı — sessizce false olmamalı")
    void alanGelmezseBilinmiyorKalmali() {
        Ad ad = isle(CEVAP_ALAN_YOK);

        assertThat(ad.getAiIsPet())
                .withFailMessage("is_pet mesajda yokken 'false' yazıldı. Bu, 'AI söylemedi' "
                        + "ile 'AI hayvan görmedi'yi aynı şeye indirir ve arayüz "
                        + "ölçülmemiş bir iddiayı kullanıcıya gösterir. "
                        + "Analysis.isPet() ilkel boolean mı olmuş?")
                .isNull();
    }

    /** Cevabı gerçek dönüştürücüyle çözüp dinleyiciye verir, ilanı geri döner. */
    private Ad isle(String json) {
        MessageProperties props = new MessageProperties();
        props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        props.setInferredArgumentType(AiAnalysisResult.class);

        AiAnalysisResult sonuc = (AiAnalysisResult) converter.fromMessage(
                new Message(json.getBytes(StandardCharsets.UTF_8), props));

        Ad ad = Ad.builder()
                .id(42L)
                .photoUrls(List.of("s3://patimati/42/a.jpg"))
                .build();

        AdRepository repo = mock(AdRepository.class);
        when(repo.findById(42L)).thenReturn(Optional.of(ad));
        when(repo.save(any(Ad.class))).thenAnswer(cagri -> cagri.getArgument(0));

        new AiAnalysisListener(repo, mock(AiMatchNotifier.class)).onResult(sonuc);
        return ad;
    }
}
