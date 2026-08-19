package com.works.patimati.service;

import com.works.patimati.ai.AiCandidateRow;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.enums.Species;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.storage.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * matchImages'in dört nokta-atışı düzeltmesini (N+1 x2, null ad_id, geçersiz
 * listingType) sınar.
 *
 * <p>AI servisi gerçekten çağrılmıyor. {@code AiMatchService} kendi
 * {@code RestTemplate}'ini constructor içinde {@code RestTemplateBuilder}'dan
 * kurduğu için (bkz. AiMatchService), dışarıdan bir mock RestTemplate/
 * MockRestServiceServer bağlamanın temiz bir yolu yok -- bunun yerine
 * builder'a bir {@link org.springframework.http.client.ClientHttpRequestInterceptor}
 * eklenip AI çağrıları burada yakalanıyor. Bu, mevcut dört noktasal
 * düzeltmenin kapsamı dışında AiMatchService'in HTTP kurulumunu YENİDEN
 * YAZMADAN test etmenin tek yolu.
 */
class AiMatchServiceTest {

    private AdRepository adRepository;
    private ImageStorageService imageStorageService;
    private List<String> cagrilanYollar;
    private AiMatchService service;

    private static final String ANALYZE_YANITI =
            "{\"embedding\":[0.1,0.2,0.3],\"labels\":[\"cat\"],\"species\":\"cat\"}";

    @BeforeEach
    void setUp() {
        adRepository = mock(AdRepository.class);
        imageStorageService = mock(ImageStorageService.class);
        cagrilanYollar = new ArrayList<>();
    }

    /** matchJson null ise /match hiç beklenmiyor demektir -- çağrılırsa 500 döner, test bunu yakalar. */
    private void servisiKur(String matchJson) {
        RestTemplateBuilder builder = new RestTemplateBuilder()
                .additionalInterceptors((request, body, execution) -> {
                    String yol = request.getURI().getPath();
                    cagrilanYollar.add(yol);
                    String govde;
                    if (yol.endsWith("/analyze")) {
                        govde = ANALYZE_YANITI;
                    } else if (yol.endsWith("/match") && matchJson != null) {
                        govde = matchJson;
                    } else {
                        return new MockClientHttpResponse(
                                ("beklenmeyen istek: " + yol).getBytes(StandardCharsets.UTF_8),
                                HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                    MockClientHttpResponse yanit = new MockClientHttpResponse(
                            govde.getBytes(StandardCharsets.UTF_8), HttpStatus.OK);
                    yanit.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    return yanit;
                });

        service = new AiMatchService(adRepository, imageStorageService, builder);
        // @Value alanları yalnızca Spring context'inde enjekte edilir --
        // burada context yok, elle veriliyor (standart ReflectionTestUtils deseni).
        ReflectionTestUtils.setField(service, "aiServiceUrl", "http://ai.test");
        ReflectionTestUtils.setField(service, "windowDays", 90);
        ReflectionTestUtils.setField(service, "maxCandidates", 100);
    }

    private List<MultipartFile> tekFotograf() {
        return List.of(new MockMultipartFile("images", "kedi.jpg", "image/jpeg", "sahte-bayt".getBytes()));
    }

    // --- 4) geçersiz listingType -----------------------------------------

    @Test
    void gecersizListingType_illegalArgumentException_firlatir_matchCagrilmaz() {
        servisiKur(null);

        assertThatThrownBy(() -> service.matchImages(tekFotograf(), "GARBAGE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("GARBAGE");

        assertThat(cagrilanYollar).contains("/analyze").doesNotContain("/match");
    }

    @Test
    void adoptionListingType_gecerliAmaKarsitiYok_bosListeDoner_matchCagrilmaz() throws Exception {
        servisiKur(null);

        List<Map<String, Object>> sonuc = service.matchImages(tekFotograf(), "ADOPTION");

        assertThat(sonuc).isEmpty();
        assertThat(cagrilanYollar).contains("/analyze").doesNotContain("/match");
    }

    @Test
    void listingTypeKucukHarfVeBosluklaGelirse_yineDeDogruCalisir() throws Exception {
        adayVeEslesmeKur("{\"matches\":[{\"ad_id\":5,\"score\":0.91}],\"skipped_candidates\":0}");

        List<Map<String, Object>> sonuc = service.matchImages(tekFotograf(), "  lost  ");

        assertThat(sonuc).hasSize(1);
    }

    // --- 1) & 2) N+1 -> findAllById, 2) null ad_id -----------------------

    private Ad ornekIlan(long id) {
        return Ad.builder()
                .id(id)
                .title("Kayıp kedi")
                .description("Bursa'da kayboldu")
                .species(Species.CAT)
                .photoUrls(List.of("https://example.test/foto.jpg"))
                .aiEmbeddings(List.of(new Ad.AiPhotoVector("https://example.test/foto.jpg", new float[]{0.1f, 0.2f})))
                .aiLabels(List.of("cat"))
                .aiModelVersion("clip-vit-base-patch32/v1")
                .build();
    }

    private void adayVeEslesmeKur(String matchJson) {
        servisiKur(matchJson);
        AiCandidateRow satir = mock(AiCandidateRow.class);
        when(satir.getAdId()).thenReturn(5L);
        when(adRepository.findAiCandidatesWithoutLocation(any(), any())).thenReturn(List.of(satir));
        when(adRepository.findAllById(any())).thenReturn(List.of(ornekIlan(5L)));
    }

    @Test
    void adayToplamaVeYanitZenginlestirme_findAllByIdKullanir_findByIdHicCagrilmaz() throws Exception {
        adayVeEslesmeKur("{\"matches\":[{\"ad_id\":5,\"score\":0.91}],\"skipped_candidates\":0}");

        List<Map<String, Object>> sonuc = service.matchImages(tekFotograf(), "LOST");

        assertThat(sonuc).hasSize(1);
        assertThat(sonuc.get(0).get("score")).isEqualTo(0.91);
        @SuppressWarnings("unchecked")
        Map<String, Object> ad = (Map<String, Object>) sonuc.get(0).get("ad");
        assertThat(ad.get("id")).isEqualTo(5L);
        assertThat(ad.get("title")).isEqualTo("Kayıp kedi");

        verify(adRepository, never()).findById(any());
    }

    @Test
    void eslesmedeAdIdNullGelirse_atlanir_npeFirlatmaz() throws Exception {
        // İkinci eşleşmenin ad_id'si null -- bugün teorik (bu uç yalnızca
        // native aday gönderiyor) ama savunma amaçlı: NPE fırlatmadan
        // sessizce atlanmalı, geçerli olan (id=5) yine dönmeli.
        adayVeEslesmeKur("{\"matches\":[{\"ad_id\":5,\"score\":0.91},{\"ad_id\":null,\"score\":0.5}],"
                + "\"skipped_candidates\":0}");

        List<Map<String, Object>> sonuc = service.matchImages(tekFotograf(), "LOST");

        assertThat(sonuc).hasSize(1);
        assertThat(((Map<?, ?>) sonuc.get(0).get("ad")).get("id")).isEqualTo(5L);
    }
}
