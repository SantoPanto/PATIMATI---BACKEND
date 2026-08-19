package com.works.patimati.service;

import com.works.patimati.ai.dto.AiCandidate;
import com.works.patimati.ai.AiCandidateRow;
import com.works.patimati.entity.Ad;
import com.works.patimati.repository.AdRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Slf4j
public class AiMatchService {

    private final AdRepository adRepository;
    private final com.works.patimati.storage.ImageStorageService imageStorageService;
    private final RestTemplate restTemplate;

    public AiMatchService(AdRepository adRepository, com.works.patimati.storage.ImageStorageService imageStorageService, org.springframework.boot.web.client.RestTemplateBuilder restTemplateBuilder) {
        this.adRepository = adRepository;
        this.imageStorageService = imageStorageService;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(java.time.Duration.ofSeconds(10))
                .setReadTimeout(java.time.Duration.ofSeconds(30))
                .build();
    }

    /** AI'nın uçlarını koruyan paylaşılan sırrın taşındığı başlık (sözleşme §10). */
    private static final String ANAHTAR_BASLIGI = "X-Api-Key";

    @Value("${ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    /**
     * AI ile paylaşılan sır. Boşsa başlık <b>hiç eklenmez</b> — AI tarafında da
     * anahtar yapılandırılmamışsa bu doğru davranıştır ve bugünkü kurulum aynen
     * çalışır. İki taraftan yalnız birine anahtar verilirse çağrılar 401 alır;
     * ikisi birlikte yapılandırılmalıdır (bkz. AI deposunda {@code AI_API_KEY}).
     */
    @Value("${ai.service.api-key:}")
    private String aiApiKey;

    @Value("${ai.matching.window-days:90}")
    private int windowDays;

    @Value("${ai.matching.max-candidates:100}")
    private int maxCandidates;

    /**
     * Tek bir görseli AI'ya analiz ettirir ve cevabı <b>olduğu gibi</b> döner.
     *
     * <p>İki çağıranı var ve <b>ikisi de aynı kapıdan geçsin diye ortak:</b>
     * ilan oluşturma ekranının analiz isteği ({@code AiAnalyzeController}) ve
     * eşleştirme akışının her fotoğraf için yaptığı analiz. Kod iki yere
     * kopyalansaydı, AI'ya kimlik eklenirken biri unutulur ve sessizce 401
     * almaya başlardı.
     *
     * <p>Cevap süzülmeden aktarılır. AI'nın {@code /analyze} cevabı
     * ({@code AnalyzeResponse}) zaten dışarıya gösterilmek üzere tanımlanmış
     * temiz bir sözleşmedir; alanları burada tekrar saymak, AI'ya eklenen her
     * yeni alanın sessizce düşmesi demek olurdu.
     *
     * @return AI'nın cevabı; AI 2xx dışında bir şey döndürürse {@code null}
     *         <i>(pratikte {@code RestTemplate} 4xx/5xx'te zaten istisna atar —
     *         bu kontrol, hata yönetimi ileride değişirse diye duruyor)</i>
     */
    public Map<String, Object> analyzeImage(MultipartFile file) throws IOException {
        HttpHeaders headers = aiBasliklari(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg";
            }
        });

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        @SuppressWarnings("unchecked")
        ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
                aiServiceUrl + "/analyze",
                requestEntity,
                (Class<Map<String, Object>>) (Class<?>) Map.class
        );

        return response.getStatusCode().is2xxSuccessful() ? response.getBody() : null;
    }

    /**
     * AI'ya gidecek isteğin başlıkları: içerik tipi + varsa paylaşılan anahtar.
     *
     * <p>Anahtar <b>yapılandırılmamışsa başlık hiç eklenmez.</b> Boş bir
     * {@code X-Api-Key} göndermek, AI tarafında "yanlış anahtar" ile aynı
     * sonucu verir ve "anahtar kullanmıyoruz" ile "anahtarı yanlış yazdık"
     * durumlarını ayırt edilemez hâle getirirdi.
     */
    private HttpHeaders aiBasliklari(MediaType icerikTipi) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(icerikTipi);
        if (aiApiKey != null && !aiApiKey.isBlank()) {
            headers.set(ANAHTAR_BASLIGI, aiApiKey);
        }
        return headers;
    }

    public List<Map<String, Object>> matchImages(List<MultipartFile> images, String listingType) throws Exception {
        List<List<Float>> allEmbeddings = new ArrayList<>();
        Set<String> allLabels = new HashSet<>();
        String majoritySpecies = "unknown";
        Map<String, Integer> speciesCount = new HashMap<>();

        // 1. Analyze each image
        for (MultipartFile file : images) {
            Map<String, Object> res = analyzeImage(file);

            if (res != null) {
                @SuppressWarnings("unchecked")
                List<Float> embedding = (List<Float>) res.get("embedding");
                if (embedding != null) {
                    allEmbeddings.add(embedding);
                }
                
                @SuppressWarnings("unchecked")
                List<String> labels = (List<String>) res.get("labels");
                if (labels != null) {
                    allLabels.addAll(labels);
                }
                
                String species = (String) res.get("species");
                if (species != null) {
                    speciesCount.put(species, speciesCount.getOrDefault(species, 0) + 1);
                }
            }
        }

        if (allEmbeddings.isEmpty()) {
            return List.of();
        }

        if (!speciesCount.isEmpty()) {
            majoritySpecies = Collections.max(speciesCount.entrySet(), Map.Entry.comparingByValue()).getKey();
        }

        // 2. Fetch candidates
        String oppositeAdType = "LOST".equalsIgnoreCase(listingType) ? "FOUND" : "LOST";
        List<AiCandidateRow> rows = adRepository.findAiCandidatesWithoutLocation(
                oppositeAdType,
                Instant.now().minus(windowDays, ChronoUnit.DAYS)
        );

        if (rows.isEmpty()) {
            return List.of();
        }

        if (rows.size() > maxCandidates) {
            rows = rows.subList(0, maxCandidates);
        }

        List<AiCandidate> candidates = new ArrayList<>();
        for (AiCandidateRow row : rows) {
            Ad ad = adRepository.findById(row.getAdId()).orElse(null);
            if (ad == null || ad.getAiEmbeddings() == null || ad.getAiEmbeddings().isEmpty()) {
                continue;
            }
            
            List<float[]> candidateEmbeddings = new ArrayList<>();
            for (Ad.AiPhotoVector vector : ad.getAiEmbeddings()) {
                candidateEmbeddings.add(vector.embedding());
            }

            AiCandidate candidate = new AiCandidate(
                    ad.getId(),
                    candidateEmbeddings,
                    ad.getAiLabels() != null ? ad.getAiLabels() : List.of(),
                    declaredSpecies(ad) != null ? declaredSpecies(ad) : "unknown",
                    0.0,
                    ad.getAiModelVersion()
            );
            candidates.add(candidate);
        }

        if (candidates.isEmpty()) {
            return List.of();
        }

        // 3. Call Match API
        Map<String, Object> matchRequest = new HashMap<>();
        matchRequest.put("embeddings", allEmbeddings);
        matchRequest.put("labels", new ArrayList<>(allLabels));
        matchRequest.put("species", majoritySpecies);
        matchRequest.put("candidates", candidates);

        @SuppressWarnings("unchecked")
        ResponseEntity<Map<String, Object>> matchResponse = restTemplate.postForEntity(
                aiServiceUrl + "/match",
                new HttpEntity<>(matchRequest, aiBasliklari(MediaType.APPLICATION_JSON)),
                (Class<Map<String, Object>>) (Class<?>) Map.class
        );

        if (matchResponse.getStatusCode().is2xxSuccessful() && matchResponse.getBody() != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> matches = (List<Map<String, Object>>) matchResponse.getBody().get("matches");
            
            Object skipped = matchResponse.getBody().get("skipped_candidates");
            if (skipped != null && skipped instanceof Integer && (Integer) skipped > 0) {
                log.info("Yapay zeka e\u00E7le\u00E7tirmede {} adet aday\u0131 (t\u00FCr vb. uyu\u00E7mazl\u0131\u011F\u0131ndan) sessizce eledi.", skipped);
            }
            
            if (matches == null) return List.of();
            
            // Map the matched Ad info along with score
            List<Map<String, Object>> result = new ArrayList<>();
            for (Map<String, Object> match : matches) {
                Integer adId = (Integer) match.get("ad_id");
                Ad ad = adRepository.findById(adId.longValue()).orElse(null);
                if (ad != null) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("score", match.get("score"));
                    item.put("ad", mapToDTO(ad));
                    result.add(item);
                }
            }
            return result;
        }

        return List.of();
    }

    private String declaredSpecies(Ad ad) {
        if (ad.getSpecies() == null) return null;
        return switch (ad.getSpecies()) {
            case CAT -> "cat";
            case DOG -> "dog";
            default -> null;
        };
    }
    
    private Map<String, Object> mapToDTO(Ad ad) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", ad.getId());
        dto.put("title", ad.getTitle());
        dto.put("description", ad.getDescription());
        
        List<String> mappedUrls = new ArrayList<>();
        if (ad.getPhotoUrls() != null) {
            for (String url : ad.getPhotoUrls()) {
                if (url != null && url.startsWith("s3://")) {
                    mappedUrls.add(imageStorageService.createTemporaryReadUrl(url));
                } else {
                    mappedUrls.add(url);
                }
            }
        }
        dto.put("photoUrls", mappedUrls);
        
        dto.put("createdAt", ad.getCreatedAt());
        String ownerName = ad.getUser() != null ? ad.getUser().getFirstName() + " " + ad.getUser().getLastName() : null;
        dto.put("ownerDisplayName", ownerName);
        return dto;
    }
}
