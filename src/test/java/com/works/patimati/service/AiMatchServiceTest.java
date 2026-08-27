package com.works.patimati.service;

import com.works.patimati.ai.AiCandidateRow;
import com.works.patimati.ai.dto.AiCandidate;
import com.works.patimati.dto.match.MatchedAdResponseDTO;
import com.works.patimati.entity.Ad;
import com.works.patimati.repository.AdRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Point;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

    private AiMatchService aiMatchService;

    /** satir()'ın oluşturduğu ilanlar -- findAllById buradan besleniyor (N+1 fix). */
    private final List<Ad> stubEdilenIlanlar = new ArrayList<>();

    @BeforeEach
    void setUp() {
        when(restTemplateBuilder.connectTimeout(any(Duration.class))).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.readTimeout(any(Duration.class))).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);

        aiMatchService = new AiMatchService(adRepository, adService, restTemplateBuilder, new com.works.patimati.mapper.AiAnalyzeMapper());
        ReflectionTestUtils.setField(aiMatchService, "aiServiceUrl", "http://localhost:8000");
        // @Value alanları düz kurulumda 0 kalır; 0'lık maxCandidates her listeyi
        // boşaltır ve test yanlış sebepten geçer/kalır — üretim varsayılanları.
        ReflectionTestUtils.setField(aiMatchService, "windowDays", 90);
        ReflectionTestUtils.setField(aiMatchService, "maxCandidates", 100);
        ReflectionTestUtils.setField(aiMatchService, "radiusKm", 25.0);
    }

    private void analyzeCevabi(Map<String, Object> body) {
        when(restTemplate.postForEntity(eq("http://localhost:8000/analyze"), any(), any(Class.class)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));
    }

    private void matchCevabi(List<Map<String, Object>> matches) {
        Map<String, Object> body = new HashMap<>();
        body.put("matches", matches);
        when(restTemplate.postForEntity(eq("http://localhost:8000/match"), any(), any(Class.class)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));
    }

    private static Map<String, Object> tamAnaliz() {
        Map<String, Object> analyzeBody = new HashMap<>();
        analyzeBody.put("species", "cat");
        analyzeBody.put("embedding", List.of(0.1f, 0.2f));
        analyzeBody.put("labels", List.of("hard:species_cat"));
        return analyzeBody;
    }

    /**
     * Aday satırı üretir VE {@code findAllById}'ı bu ana kadar üretilen tüm
     * ilanları dönecek şekilde günceller. Üretim kodu artık her aday için ayrı
     * {@code findById} DEĞİL, tek seferde {@code findAllById} çağırıyor (N+1
     * fix) -- eski tekil {@code findById} stub'u bu akışta hiç görülmez.
     */
    private AiCandidateRow satir(long adId, Double mesafeKm) {
        AiCandidateRow row = mock(AiCandidateRow.class);
        when(row.getAdId()).thenReturn(adId);
        when(row.getDistanceKm()).thenReturn(mesafeKm);

        Ad ad = Ad.builder().id(adId).build();
        ad.setAiEmbeddings(List.of(new Ad.AiPhotoVector("s3://k/" + adId + ".jpg", new float[]{0.5f})));
        stubEdilenIlanlar.add(ad);
        when(adRepository.findAllById(any())).thenReturn(new ArrayList<>(stubEdilenIlanlar));
        return row;
    }

    private static Map<String, Object> aiEslesme(int adId, double skor, Object matchAlani) {
        Map<String, Object> m = new HashMap<>();
        m.put("ad_id", adId);
        m.put("score", skor);
        if (matchAlani != null) {
            m.put("match", matchAlani);
        }
        return m;
    }

    @Test
    void matchImages_EmptyCandidates_ReturnsEmptyList() throws Exception {
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "dummy".getBytes());

        Map<String, Object> analyzeBody = new HashMap<>();
        analyzeBody.put("species", "cat");
        analyzeCevabi(analyzeBody);

        when(adRepository.findAiCandidatesWithoutLocation(anyString(), any(Instant.class)))
                .thenReturn(List.of());

        List<MatchedAdResponseDTO> results =
                aiMatchService.matchImages(List.of(file), "FOUND", null, null);

        assertTrue(results.isEmpty());
        verify(restTemplate, never()).postForEntity(eq("http://localhost:8000/match"), any(), any(Class.class));
    }

    // --- geçersiz / sınır listingType değerleri --------------------------

    @Test
    void gecersizListingType_illegalArgumentException_firlatir_matchCagrilmaz() throws Exception {
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "dummy".getBytes());
        analyzeCevabi(tamAnaliz());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> aiMatchService.matchImages(List.of(file), "GARBAGE", null, null));
        assertTrue(ex.getMessage().contains("GARBAGE"));

        verify(restTemplate, never()).postForEntity(eq("http://localhost:8000/match"), any(), any(Class.class));
    }

    @Test
    void adoptionListingType_gecerliAmaKarsitiYok_bosListeDoner_matchCagrilmaz() throws Exception {
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "dummy".getBytes());
        analyzeCevabi(tamAnaliz());

        List<MatchedAdResponseDTO> results =
                aiMatchService.matchImages(List.of(file), "ADOPTION", null, null);

        assertTrue(results.isEmpty());
        verify(restTemplate, never()).postForEntity(eq("http://localhost:8000/match"), any(), any(Class.class));
    }

    @Test
    void helpListingType_gecerliAmaKarsitiYok_bosListeDoner_matchCagrilmaz() throws Exception {
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "dummy".getBytes());
        analyzeCevabi(tamAnaliz());

        List<MatchedAdResponseDTO> results =
                aiMatchService.matchImages(List.of(file), "HELP", null, null);

        assertTrue(results.isEmpty());
        verify(restTemplate, never()).postForEntity(eq("http://localhost:8000/match"), any(), any(Class.class));
    }

    @Test
    void listingTypeKucukHarfVeBosluklaGelirse_yineDeDogruCalisir() throws Exception {
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "dummy".getBytes());
        analyzeCevabi(tamAnaliz());

        List<AiCandidateRow> satirlar = List.of(satir(5L, 0.0));
        when(adRepository.findAiCandidatesWithoutLocation(anyString(), any(Instant.class)))
                .thenReturn(satirlar);
        matchCevabi(List.of(aiEslesme(5, 0.91, Boolean.TRUE)));

        List<MatchedAdResponseDTO> results =
                aiMatchService.matchImages(List.of(file), "  lost  ", null, null);

        assertEquals(1, results.size());
    }

    @Test
    void eslesmedeAdIdNullGelirse_atlanir_npeFirlatmaz() throws Exception {
        // İkinci eşleşmenin ad_id'si null -- bugün teorik (bu uç yalnızca
        // native aday gönderiyor) ama savunma amaçlı: NPE fırlatmadan
        // sessizce atlanmalı, geçerli olan (id=5) yine dönmeli.
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "dummy".getBytes());
        analyzeCevabi(tamAnaliz());

        List<AiCandidateRow> satirlar = List.of(satir(5L, 0.0));
        when(adRepository.findAiCandidatesWithoutLocation(anyString(), any(Instant.class)))
                .thenReturn(satirlar);

        Map<String, Object> nullAdIdEslesme = new HashMap<>();
        nullAdIdEslesme.put("ad_id", null);
        nullAdIdEslesme.put("score", 0.5);
        nullAdIdEslesme.put("match", Boolean.TRUE);
        matchCevabi(List.of(aiEslesme(5, 0.91, Boolean.TRUE), nullAdIdEslesme));

        List<MatchedAdResponseDTO> results =
                aiMatchService.matchImages(List.of(file), "FOUND", null, null);

        assertEquals(1, results.size());
    }

    @Test
    void adayToplamaVeYanitZenginlestirme_findAllByIdKullanir_findByIdHicCagrilmaz() throws Exception {
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "dummy".getBytes());
        analyzeCevabi(tamAnaliz());

        List<AiCandidateRow> satirlar = List.of(satir(5L, 0.0));
        when(adRepository.findAiCandidatesWithoutLocation(anyString(), any(Instant.class)))
                .thenReturn(satirlar);
        matchCevabi(List.of(aiEslesme(5, 0.91, Boolean.TRUE)));

        List<MatchedAdResponseDTO> results =
                aiMatchService.matchImages(List.of(file), "LOST", null, null);

        assertEquals(1, results.size());
        verify(adRepository, never()).findById(anyLong());
    }

    // --- B8: eşik ve konum davranışı --------------------------------------

    @Test
    @DisplayName("B8: eşik altı adaylar cevaptan elenir — pencere yalnız eşiği geçenleri görür")
    void matchImages_EsikAltiAdaylarElenir() throws Exception {
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "dummy".getBytes());
        analyzeCevabi(tamAnaliz());

        // satir() kendi stub'larını kurduğu için DIŞ when()'den ÖNCE çağrılmalı;
        // thenReturn argümanı içinde çağrılırsa Mockito yarım stub'a düşer.
        List<AiCandidateRow> satirlar = List.of(satir(1L, 0.0), satir(2L, 0.0), satir(3L, 0.0));
        when(adRepository.findAiCandidatesWithoutLocation(anyString(), any(Instant.class)))
                .thenReturn(satirlar);

        // AI üçünü de skorlar; yalnız 1 eşiği geçer. 3'ün "match" alanı hiç yok
        // (bozuk/eski cevap) — o da GÖSTERİLMEZ.
        matchCevabi(List.of(
                aiEslesme(1, 0.91, Boolean.TRUE),
                aiEslesme(2, 0.55, Boolean.FALSE),
                aiEslesme(3, 0.99, null)));

        List<MatchedAdResponseDTO> results =
                aiMatchService.matchImages(List.of(file), "FOUND", null, null);

        assertEquals(1, results.size());
        assertEquals(0.91, results.get(0).getScore(), 1e-9);
    }

    @Test
    @DisplayName("B8: konum verilince yarıçaplı süzgeç kullanılır ve gerçek mesafe AI'ya gider")
    void matchImages_KonumluCagriYaricapliSuzgeciKullanir() throws Exception {
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "dummy".getBytes());
        analyzeCevabi(tamAnaliz());

        List<AiCandidateRow> satirlar = List.of(satir(7L, 19.6));
        when(adRepository.findAiCandidates(eq(-1L), anyString(), any(Point.class), eq(25000.0), any(Instant.class)))
                .thenReturn(satirlar);
        matchCevabi(List.of(aiEslesme(7, 0.85, Boolean.TRUE)));

        List<MatchedAdResponseDTO> results =
                aiMatchService.matchImages(List.of(file), "LOST", 40.1955, 29.0611);

        assertEquals(1, results.size());
        verify(adRepository, never()).findAiCandidatesWithoutLocation(anyString(), any(Instant.class));

        // AI'ya giden istekte adayın mesafesi 0 DEĞİL, PostGIS'in değeri olmalı —
        // konum cezasının pop-up'ta da işlemesinin tek koşulu bu.
        ArgumentCaptor<HttpEntity<Map<String, Object>>> istek = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(eq("http://localhost:8000/match"), istek.capture(), any(Class.class));
        @SuppressWarnings("unchecked")
        List<AiCandidate> adaylar = (List<AiCandidate>) istek.getValue().getBody().get("candidates");
        assertEquals(1, adaylar.size());
        assertEquals(19.6, adaylar.get(0).distanceKm(), 1e-9);
    }

    @Test
    @DisplayName("Konum verilmeyince eski yol korunur (en yeni adaylar, mesafe 0)")
    void matchImages_KonumsuzCagriEskiYoluKorur() throws Exception {
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "dummy".getBytes());
        analyzeCevabi(tamAnaliz());

        List<AiCandidateRow> satirlar = List.of(satir(5L, 0.0));
        when(adRepository.findAiCandidatesWithoutLocation(anyString(), any(Instant.class)))
                .thenReturn(satirlar);
        matchCevabi(List.of(aiEslesme(5, 0.88, Boolean.TRUE)));

        List<MatchedAdResponseDTO> results =
                aiMatchService.matchImages(List.of(file), "FOUND", null, null);

        assertEquals(1, results.size());
        verify(adRepository, never()).findAiCandidates(anyLong(), anyString(), any(), anyDouble(), any());
    }

    @Test
    void esikGecti_YalnizAcikTrueKabulEder() {
        assertTrue(AiMatchService.esikGecti(Map.of("match", Boolean.TRUE)));
        assertFalse(AiMatchService.esikGecti(Map.of("match", Boolean.FALSE)));
        assertFalse(AiMatchService.esikGecti(Map.of("score", 0.99)));
        assertFalse(AiMatchService.esikGecti(Map.of("match", "true")));
        assertFalse(AiMatchService.esikGecti(null));
    }
    @Test
    @DisplayName("Oto-doldur klasik /analyze ucunda kalır — rapor (/analyze_pet) şeması mapper'la uyumsuz ve Gemini'ye bağımlı")
    void otoDoldurKlasikAnalyzeUcundaKalir() throws Exception {
        Map<String, Object> klasikCevap = new HashMap<>();
        klasikCevap.put("species", "dog");
        klasikCevap.put("species_confidence", 0.93);
        klasikCevap.put("breed", "golden retriever");
        klasikCevap.put("breed_top", "golden retriever");
        klasikCevap.put("breed_confidence", 0.82);
        klasikCevap.put("pattern", "solid");
        klasikCevap.put("is_pet", true);
        analyzeCevabi(klasikCevap);

        com.works.patimati.dto.ai.AiAnalyzeResponse sonuc = aiMatchService.analyzeImageForFrontend(
                new MockMultipartFile("file", "golden.jpg", "image/jpeg", new byte[]{1, 2, 3}));

        assertNotNull(sonuc);
        assertEquals(com.works.patimati.entity.enums.Species.DOG, sonuc.species());
        assertEquals("golden retriever", sonuc.breedTop());
        assertEquals(com.works.patimati.entity.enums.CoatPattern.SOLID, sonuc.coatPattern());
        assertEquals(Boolean.TRUE, sonuc.isPet());
        verify(restTemplate, never()).postForEntity(contains("/analyze_pet"), any(), any(Class.class));
    }
}
