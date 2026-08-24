package com.works.patimati.service;

import com.works.patimati.ai.dto.AiCandidate;
import com.works.patimati.ai.AiCandidateRow;
import com.works.patimati.dto.match.MatchedAdResponseDTO;
import com.works.patimati.entity.Ad;
import com.works.patimati.repository.AdRepository;
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
import java.util.stream.Collectors;

@Service
@Slf4j
public class AiMatchService {

    private final AdRepository adRepository;
    private final AdService adService;
    private final RestTemplate restTemplate;

    public AiMatchService(AdRepository adRepository, AdService adService, org.springframework.boot.web.client.RestTemplateBuilder restTemplateBuilder) {
        this.adRepository = adRepository;
        this.adService = adService;
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

    public List<MatchedAdResponseDTO> matchImages(List<MultipartFile> images, String listingType) throws Exception {
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
        String oppositeAdType = resolveOppositeAdType(listingType);
        if (oppositeAdType == null) {
            // Geçerli bir tür (ADOPTION) ama karşıtı yok -- aranacak bir
            // eşleşme kavramı yok, hata değil.
            return List.of();
        }
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

        // findAllById ile tek sorguda çekilip Map'e konuyor -- öncesinde
        // her satır için ayrı bir findById çağrılıyordu (N+1).
        List<Long> candidateAdIds = rows.stream().map(AiCandidateRow::getAdId).toList();
        Map<Long, Ad> candidateAdsById = adRepository.findAllById(candidateAdIds).stream()
                .collect(Collectors.toMap(Ad::getId, ad -> ad));

        List<AiCandidate> candidates = new ArrayList<>();
        for (AiCandidateRow row : rows) {
            Ad ad = candidateAdsById.get(row.getAdId());
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
                    ad.getAiModelVersion(),
                    null
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
                log.info("Yapay zeka eşleştirmede {} adet adayı (tür vb. uyuşmazlığından) sessizce eledi.", skipped);
            }
            
            if (matches == null) return List.of();

            // ad_id null gelebilir (bugün bu uç yalnızca native aday
            // gönderdiği için teorik, ama savunma amaçlı) -- longValue()'dan
            // ÖNCE kontrol edilmezse NPE fırlatırdı. findAllById ile tek
            // sorguda toplu çekiliyor -- öncesinde her eşleşme için ayrı bir
            // findById çağrılıyordu (N+1).
            List<Long> matchedAdIds = new ArrayList<>();
            for (Map<String, Object> match : matches) {
                Integer adId = (Integer) match.get("ad_id");
                if (adId != null) {
                    matchedAdIds.add(adId.longValue());
                }
            }
            Map<Long, Ad> matchedAdsById = adRepository.findAllById(matchedAdIds).stream()
                    .collect(Collectors.toMap(Ad::getId, ad -> ad));

            // Map the matched Ad info along with score into MatchedAdResponseDTO
            List<MatchedAdResponseDTO> result = new ArrayList<>();
            for (Map<String, Object> match : matches) {
                Integer adId = (Integer) match.get("ad_id");
                if (adId == null) {
                    continue;
                }
                Object rawScore = match.get("score");
                Double score = rawScore instanceof Number ? ((Number) rawScore).doubleValue() : null;

                Ad ad = matchedAdsById.get(adId.longValue());
                if (ad != null) {
                    MatchedAdResponseDTO dto = MatchedAdResponseDTO.builder()
                            .score(score)
                            .ad(adService.toResponseWithTemporaryPhotoUrls(ad))
                            .build();
                    result.add(dto);
                }
            }
            return result;
        }

        return List.of();
    }

    /**
     * {@code listingType}'ı {@link Ad.AdType}'a doğrular ve karşıt türünü
     * döner (LOST&harr;FOUND). ADOPTION geçerli bir tür ama karşıtı yoktur --
     * {@code null} döner, çağıran bunu boş sonuç olarak ele alır.
     *
     * <p>Öncesinde yalnızca {@code "LOST".equalsIgnoreCase(listingType)}
     * kontrol ediliyordu: "LOST" dışında HER ŞEY (yazım hatası dahil)
     * sessizce "FOUND"un karşıtı sayılıyor, yanlış adaylarla eşleştiriliyordu.
     * Artık geçersiz bir değer {@link IllegalArgumentException} fırlatır --
     * {@code GlobalExceptionHandler} bunu zaten 400'e çeviriyor.
     */
    private String resolveOppositeAdType(String listingType) {
        Ad.AdType type;
        try {
            type = Ad.AdType.valueOf(listingType.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException(
                    "Geçersiz listingType: '" + listingType + "'. Geçerli değerler: "
                            + Arrays.toString(Ad.AdType.values()));
        }
        return switch (type) {
            case LOST -> Ad.AdType.FOUND.name();
            case FOUND -> Ad.AdType.LOST.name();
            case ADOPTION -> null;
        };
    }

    private String declaredSpecies(Ad ad) {
        if (ad.getSpecies() == null) return null;
        return switch (ad.getSpecies()) {
            case CAT -> "cat";
            case DOG -> "dog";
            default -> null;
        };
    }
}
