package com.works.patimati.service;

import com.works.patimati.ai.AiCandidateRow;
import com.works.patimati.dto.ad.AdResponse;
import com.works.patimati.dto.match.MatchedAdResponseDTO;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.enums.Species;
import com.works.patimati.repository.AdRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * matchImages'in dört nokta-atışı düzeltmesini (N+1 x2, null ad_id, geçersiz
 * listingType) ve boş-aday davranışını sınar.
 *
 * <p>AI servisi gerçekten çağrılmıyor -- {@code RestTemplate} mock'lanıyor
 * (constructor'daki {@code RestTemplateBuilder} de mock'lanarak).
 */
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class AiMatchServiceTest {

    @Mock
    private AdRepository adRepository;

    @Mock
    private AdService adService;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private RestTemplate restTemplate;

    private AiMatchService service;

    private static final Map<String, Object> ANALYZE_YANITI = Map.of(
            "embedding", List.of(0.1, 0.2, 0.3),
            "labels", List.of("cat"),
            "species", "cat"
    );

    @BeforeEach
    void setUp() {
        when(restTemplateBuilder.setConnectTimeout(any(Duration.class))).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.setReadTimeout(any(Duration.class))).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);

        service = new AiMatchService(adRepository, adService, restTemplateBuilder);
        ReflectionTestUtils.setField(service, "aiServiceUrl", "http://localhost:8000");
        ReflectionTestUtils.setField(service, "windowDays", 90);
        ReflectionTestUtils.setField(service, "maxCandidates", 100);

        when(restTemplate.postForEntity(eq("http://localhost:8000/analyze"), any(), any(Class.class)))
                .thenReturn(new ResponseEntity<>(new HashMap<>(ANALYZE_YANITI), HttpStatus.OK));
    }

    private List<MultipartFile> tekFotograf() {
        return List.of(new MockMultipartFile("images", "kedi.jpg", "image/jpeg", "sahte-bayt".getBytes()));
    }

    @Test
    void matchImages_bosAdayHavuzu_bosListeDoner_matchCagrilmaz() throws Exception {
        when(adRepository.findAiCandidatesWithoutLocation(anyString(), any(Instant.class)))
                .thenReturn(List.of());

        List<MatchedAdResponseDTO> sonuc = service.matchImages(tekFotograf(), "FOUND");

        assertThat(sonuc).isEmpty();
        verify(restTemplate, never()).postForEntity(eq("http://localhost:8000/match"), any(), any(Class.class));
    }

    // --- 4) geçersiz listingType -----------------------------------------

    @Test
    void gecersizListingType_illegalArgumentException_firlatir_matchCagrilmaz() {
        assertThatThrownBy(() -> service.matchImages(tekFotograf(), "GARBAGE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("GARBAGE");

        verify(restTemplate, never()).postForEntity(eq("http://localhost:8000/match"), any(), any(Class.class));
    }

    @Test
    void adoptionListingType_gecerliAmaKarsitiYok_bosListeDoner_matchCagrilmaz() throws Exception {
        List<MatchedAdResponseDTO> sonuc = service.matchImages(tekFotograf(), "ADOPTION");

        assertThat(sonuc).isEmpty();
        verify(restTemplate, never()).postForEntity(eq("http://localhost:8000/match"), any(), any(Class.class));
    }

    @Test
    void listingTypeKucukHarfVeBosluklaGelirse_yineDeDogruCalisir() throws Exception {
        adayVeEslesmeKur(Map.of("matches", List.of(Map.of("ad_id", 5, "score", 0.91)), "skipped_candidates", 0));

        List<MatchedAdResponseDTO> sonuc = service.matchImages(tekFotograf(), "  lost  ");

        assertThat(sonuc).hasSize(1);
    }

    // --- 1) & 2) N+1 -> findAllById, 2) null ad_id ------------------------

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

    @SuppressWarnings("unchecked")
    private void adayVeEslesmeKur(Map<String, Object> matchYaniti) {
        AiCandidateRow satir = mock(AiCandidateRow.class);
        when(satir.getAdId()).thenReturn(5L);
        when(adRepository.findAiCandidatesWithoutLocation(any(), any())).thenReturn(List.of(satir));
        when(adRepository.findAllById(any())).thenReturn(List.of(ornekIlan(5L)));
        when(restTemplate.postForEntity(eq("http://localhost:8000/match"), any(), any(Class.class)))
                .thenReturn(new ResponseEntity<>(new HashMap<>(matchYaniti), HttpStatus.OK));

        AdResponse zenginlestirilmisIlan = mock(AdResponse.class);
        when(zenginlestirilmisIlan.id()).thenReturn(5L);
        when(zenginlestirilmisIlan.title()).thenReturn("Kayıp kedi");
        when(adService.toResponseWithTemporaryPhotoUrls(any(Ad.class))).thenReturn(zenginlestirilmisIlan);
    }

    @Test
    void adayToplamaVeYanitZenginlestirme_findAllByIdKullanir_findByIdHicCagrilmaz() throws Exception {
        adayVeEslesmeKur(Map.of("matches", List.of(Map.of("ad_id", 5, "score", 0.91)), "skipped_candidates", 0));

        List<MatchedAdResponseDTO> sonuc = service.matchImages(tekFotograf(), "LOST");

        assertThat(sonuc).hasSize(1);
        assertThat(sonuc.get(0).getScore()).isEqualTo(0.91);
        assertThat(sonuc.get(0).getAd().id()).isEqualTo(5L);
        assertThat(sonuc.get(0).getAd().title()).isEqualTo("Kayıp kedi");

        verify(adRepository, never()).findById(any());
    }

    @Test
    void eslesmedeAdIdNullGelirse_atlanir_npeFirlatmaz() throws Exception {
        // İkinci eşleşmenin ad_id'si null -- bugün teorik (bu uç yalnızca
        // native aday gönderiyor) ama savunma amaçlı: NPE fırlatmadan
        // sessizce atlanmalı, geçerli olan (id=5) yine dönmeli.
        Map<String, Object> ikinciEslesme = new HashMap<>();
        ikinciEslesme.put("ad_id", null);
        ikinciEslesme.put("score", 0.5);
        adayVeEslesmeKur(Map.of(
                "matches", List.of(Map.of("ad_id", 5, "score", 0.91), ikinciEslesme),
                "skipped_candidates", 0));

        List<MatchedAdResponseDTO> sonuc = service.matchImages(tekFotograf(), "LOST");

        assertThat(sonuc).hasSize(1);
        assertThat(sonuc.get(0).getAd().id()).isEqualTo(5L);
    }
}
