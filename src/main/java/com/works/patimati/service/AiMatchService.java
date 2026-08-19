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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

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

    @Value("${ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    @Value("${ai.matching.window-days:90}")
    private int windowDays;

    @Value("${ai.matching.max-candidates:100}")
    private int maxCandidates;

    public List<Map<String, Object>> matchImages(List<MultipartFile> images, String listingType) throws Exception {
        List<List<Float>> allEmbeddings = new ArrayList<>();
        Set<String> allLabels = new HashSet<>();
        String majoritySpecies = "unknown";
        Map<String, Integer> speciesCount = new HashMap<>();

        // 1. Analyze each image
        for (MultipartFile file : images) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
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

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> res = response.getBody();
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
                matchRequest,
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

            // Map the matched Ad info along with score
            List<Map<String, Object>> result = new ArrayList<>();
            for (Map<String, Object> match : matches) {
                Integer adId = (Integer) match.get("ad_id");
                if (adId == null) {
                    continue;
                }
                Ad ad = matchedAdsById.get(adId.longValue());
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
