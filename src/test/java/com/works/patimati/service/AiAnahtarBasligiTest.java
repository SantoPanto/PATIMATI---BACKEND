package com.works.patimati.service;

import com.works.patimati.ai.AiCandidateRow;
import com.works.patimati.entity.Ad;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.storage.ImageStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Backend, AI'ya giderken paylaşılan anahtarı taşıyor mu (A1).
 *
 * <p><b>Neden gerçek HTTP katmanı:</b> iddia <i>telde ne gittiği</i> hakkında.
 * {@code aiBasliklari} metodunu doğrudan çağırıp dönen {@code HttpHeaders}'a
 * bakmak, başlığın isteğe gerçekten bağlandığını kanıtlamaz — metot doğru
 * çalışıp sonucu kullanılmasa da test yeşil yanardı.
 *
 * <p><b>İkinci hâl en az birincisi kadar önemli:</b> anahtar yapılandırılmamışken
 * başlık <b>hiç eklenmemeli</b>. Boş bir {@code X-Api-Key} göndermek, AI
 * tarafında "yanlış anahtar" ile aynı sonucu verir (401) ve bugünkü kurulumu
 * tek hamlede kırardı.
 */
class AiAnahtarBasligiTest {

    private static final String AI_ADRESI = "http://ai.test";
    private static final String ANAHTAR = "cok-gizli-anahtar";

    @Test
    @DisplayName("Anahtar yapılandırılmışsa X-Api-Key başlığı AI'ya gider")
    void anahtarVarsaBaslikGonderilir() throws IOException {
        MockServerRestTemplateCustomizer duzenek = new MockServerRestTemplateCustomizer();
        AiMatchService servis = servis(duzenek, ANAHTAR);
        MockRestServiceServer sunucu = duzenek.getServer();

        sunucu.expect(requestTo(AI_ADRESI + "/analyze"))
                .andExpect(header("X-Api-Key", ANAHTAR))
                .andRespond(withSuccess("{\"species\":\"cat\"}", MediaType.APPLICATION_JSON));

        servis.analyzeImage(fotograf());

        // verify(): beklenen isteğin GERÇEKTEN yapıldığını da kanıtlar.
        // Yapılmasaydı yukarıdaki beklenti hiç sınanmaz, test yine geçerdi.
        sunucu.verify();
    }

    @Test
    @DisplayName("Anahtar boşsa başlık HİÇ eklenmez — boş anahtar 401 demektir")
    void anahtarYoksaBaslikEklenmez() throws IOException {
        MockServerRestTemplateCustomizer duzenek = new MockServerRestTemplateCustomizer();
        AiMatchService servis = servis(duzenek, "");
        MockRestServiceServer sunucu = duzenek.getServer();

        sunucu.expect(requestTo(AI_ADRESI + "/analyze"))
                .andExpect(headerDoesNotExist("X-Api-Key"))
                .andRespond(withSuccess("{\"species\":\"cat\"}", MediaType.APPLICATION_JSON));

        servis.analyzeImage(fotograf());

        sunucu.verify();
    }

    @Test
    @DisplayName("/match çağrısı da anahtarı taşır — arama yolu unutulmamalı")
    void matchCagrisiDaAnahtariTasir() throws Exception {
        MockServerRestTemplateCustomizer duzenek = new MockServerRestTemplateCustomizer();
        AdRepository adRepository = mock(AdRepository.class);
        AiMatchService servis = servis(duzenek, ANAHTAR, adRepository);
        ReflectionTestUtils.setField(servis, "maxCandidates", 100);
        adaylariKur(adRepository);

        MockRestServiceServer sunucu = duzenek.getServer();
        sunucu.expect(requestTo(AI_ADRESI + "/analyze"))
                .andRespond(withSuccess("{\"embedding\":[0.1,0.2],\"labels\":[\"cat\"],\"species\":\"cat\"}",
                        MediaType.APPLICATION_JSON));
        sunucu.expect(requestTo(AI_ADRESI + "/match"))
                .andExpect(header("X-Api-Key", ANAHTAR))
                .andRespond(withSuccess("{\"matches\":[]}", MediaType.APPLICATION_JSON));

        servis.matchImages(List.of(fotograf()), "LOST");

        // İkinci beklenti hiç karşılanmasaydı (akış /match'e varmadan dönseydi)
        // yukarıdaki başlık iddiası sınanmamış olurdu; verify() onu yakalar.
        sunucu.verify();
    }

    /** {@code /match}'e varmak için gereken en küçük aday havuzu. */
    private void adaylariKur(AdRepository adRepository) {
        AiCandidateRow satir = mock(AiCandidateRow.class);
        when(satir.getAdId()).thenReturn(7L);
        when(adRepository.findAiCandidatesWithoutLocation(any(), any()))
                .thenReturn(List.of(satir));

        Ad aday = Ad.builder()
                .id(7L)
                .aiEmbeddings(List.of(new Ad.AiPhotoVector("s3://patimati/7/a.jpg", new float[]{0.1f, 0.2f})))
                .aiLabels(List.of("cat"))
                .aiModelVersion("siglip2-animal/v2")
                .build();
        when(adRepository.findById(7L)).thenReturn(Optional.of(aday));
        // Aday toplama artik findAllById ile toplu cekiyor (N+1 duzeltmesi) --
        // yalniz findById mock'lamak candidateAdsById'i bos birakir, akis
        // /match'e hic varmadan bos donerdi.
        when(adRepository.findAllById(any())).thenReturn(List.of(aday));
    }

    private AiMatchService servis(MockServerRestTemplateCustomizer duzenek, String anahtar) {
        return servis(duzenek, anahtar, mock(AdRepository.class));
    }

    private AiMatchService servis(MockServerRestTemplateCustomizer duzenek, String anahtar,
                                  AdRepository adRepository) {
        AiMatchService servis = new AiMatchService(
                adRepository,
                mock(AdService.class),
                new RestTemplateBuilder(duzenek));

        // @Value alanları bağlam olmadan doldurulmaz; ölçülen şey bu iki alanın
        // isteğe nasıl yansıdığı olduğu için burada elle veriliyor.
        ReflectionTestUtils.setField(servis, "aiServiceUrl", AI_ADRESI);
        ReflectionTestUtils.setField(servis, "aiApiKey", anahtar);
        return servis;
    }

    private MockMultipartFile fotograf() {
        return new MockMultipartFile("file", "kedi.jpg", "image/jpeg", new byte[]{1, 2, 3});
    }
}
