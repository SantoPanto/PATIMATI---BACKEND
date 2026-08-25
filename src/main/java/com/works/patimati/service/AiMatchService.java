package com.works.patimati.service;

import com.works.patimati.ai.dto.AiCandidate;
import com.works.patimati.ai.AiCandidateRow;
import com.works.patimati.dto.match.MatchedAdResponseDTO;
import com.works.patimati.entity.Ad;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.dto.ai.AiAnalyzeResponse;
import com.works.patimati.mapper.AiAnalyzeMapper;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
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
    private final RestTemplate petRaporuRestTemplate;
    private final AiAnalyzeMapper aiAnalyzeMapper;

    public AiMatchService(AdRepository adRepository, AdService adService,
                          org.springframework.boot.web.client.RestTemplateBuilder restTemplateBuilder,
                          AiAnalyzeMapper aiAnalyzeMapper) {
        this.adRepository = adRepository;
        this.adService = adService;
        this.aiAnalyzeMapper = aiAnalyzeMapper;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(java.time.Duration.ofSeconds(10))
                .setReadTimeout(java.time.Duration.ofSeconds(30))
                .build();
        // /analyze_pet (Gemini + AI tarafındaki retry/backoff dahil) tek bir
        // CLIP çıkarımından ÇOK daha uzun sürebilir -- AI tarafının kendi
        // kötü senaryosu (pet_raporu.py: 3 deneme * 25sn zaman aşımı + 1sn +
        // 2sn backoff) ~78 saniyeye kadar çıkabiliyor. Paylaşılan 30sn'lik
        // restTemplate'i BÜYÜTMEK yerine (bu, hızlı /analyze/match/compare
        // çağrılarının da yavaş başarısızlıklarda gereksiz uzun beklemesine
        // yol açardı) yalnızca bu uç için ayrı, daha uzun zaman aşımlı bir
        // RestTemplate kullanılıyor. Canlı testte doğrulandı: paylaşılan
        // 30sn'lik zaman aşımıyla "Read timed out" ile başarısız oluyordu.
        this.petRaporuRestTemplate = restTemplateBuilder
                .setConnectTimeout(java.time.Duration.ofSeconds(10))
                .setReadTimeout(java.time.Duration.ofSeconds(90))
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

    /** Aday yarıçapı — asenkron yolla ({@code AiAnalysisPublisher}) aynı anahtar ve varsayılan. */
    @Value("${ai.matching.radius-km:25}")
    private double radiusKm;

    /** WGS 84 (SRID 4326) — {@code ads.location} sütunuyla aynı referans sistemi. */
    private final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), 4326);

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
     * Tek bir görseli AI'nın "Ben Neyim?" ucuna (pet raporu) gönderir, cevabı
     * <b>olduğu gibi</b> döner -- {@link #analyzeImage} ile AYNI ilke (bkz.
     * oradaki javadoc): AI'nın {@code /analyze_pet} cevabı zaten dışarıya
     * gösterilmek üzere tanımlanmış bir sözleşme, burada yeniden modellenmez.
     *
     * @param kullaniciNotu opsiyonel, boşsa AI'ya hiç gönderilmez
     * @return AI'nın cevabı; AI 2xx dışında bir şey döndürürse {@code null}
     */
    public Map<String, Object> analyzePet(MultipartFile file, String kullaniciNotu) throws IOException {
        HttpHeaders headers = aiBasliklari(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg";
            }
        });
        if (kullaniciNotu != null && !kullaniciNotu.isBlank()) {
            body.add("kullanici_notu", kullaniciNotu);
        }

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        @SuppressWarnings("unchecked")
        ResponseEntity<Map<String, Object>> response = petRaporuRestTemplate.postForEntity(
                aiServiceUrl + "/analyze_pet",
                requestEntity,
                (Class<Map<String, Object>>) (Class<?>) Map.class
        );

        return response.getStatusCode().is2xxSuccessful() ? response.getBody() : null;
    }

    /**
     * İlan oluşturma ekranı için AI analiz sonucunu frontend sözleşmesine dönüştürerek döner.
     */
    public AiAnalyzeResponse analyzeImageForFrontend(MultipartFile file) throws IOException {
        Map<String, Object> raw = analyzeImage(file);
        if (raw == null) {
            return null;
        }
        return aiAnalyzeMapper.toResponse(raw);
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

    /**
     * Konum verilmişse aday süzgeci ve skorlama, asenkron boru hattıyla AYNI
     * kurala biner: {@code findAiCandidates} (25 km yarıçap + en yakın 100) ve
     * her adayın PostGIS mesafesi AI'ya gerçek değeriyle gider — konum cezası
     * pop-up'ta da işler. Konum yoksa eski davranış korunur (en yeni 100 aday,
     * mesafe 0): ilan formunda analiz düğmesi konum girilmeden de basılabiliyor
     * ve "mesafe bilinmiyor"u ceza gibi işletmek eşleşmeleri saklardı.
     */
    public List<MatchedAdResponseDTO> matchImages(List<MultipartFile> images, String listingType,
                                                  Double latitude, Double longitude) throws Exception {
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
        Instant since = Instant.now().minus(windowDays, ChronoUnit.DAYS);

        List<AiCandidateRow> rows;
        if (latitude != null && longitude != null) {
            Point origin = geometryFactory.createPoint(new Coordinate(longitude, latitude));
            // selfAdId=-1: ilan henüz OLUŞMADI, dışlanacak "kendisi" yok;
            // -1 hiçbir gerçek id ile çakışmaz.
            rows = adRepository.findAiCandidates(
                    -1L, oppositeAdType, origin, radiusKm * 1000.0, since);
        } else {
            rows = adRepository.findAiCandidatesWithoutLocation(oppositeAdType, since);
        }

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
                    // Konumlu yolda PostGIS'in gerçek mesafesi; konumsuz yolda
                    // sorgu 0.0 döndürüyor (herkese eşit — sıralamayı bozmaz).
                    row.getDistanceKm() != null ? row.getDistanceKm() : 0.0,
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
            int esikAltiElenen = 0;
            for (Map<String, Object> match : matches) {
                if (!esikGecti(match)) {
                    esikAltiElenen++;
                    continue;
                }
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
            if (esikAltiElenen > 0) {
                // "Pencere neden boş/kısa" sorusu cevapsız kalmasın.
                log.info("Eşleştirme cevabında {} aday eşik altında kaldığı için gösterilmedi.",
                        esikAltiElenen);
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

    /**
     * Eşik kararı AI'nındır: {@code /match} cevabındaki her satır
     * {@code match} alanını taşır (sözleşme; {@code compute_final_score}
     * skoru MATCH_THRESHOLD ile karşılaştırıp yazar). Burada sayıyı yeniden
     * eşikle kıyaslamıyoruz — eşik değeri AI ortamında değişirse (ör. 0.80 →
     * 0.75 ayarı) bu uç kod değişikliği olmadan yeni kurala uyar.
     *
     * <p>Alan yoksa ya da beklenmeyen tiptayse aday GÖSTERİLMEZ: "eşiği geçti"
     * bilgisini üretemeyen bir cevabla kullanıcıya %0'lık kart basmak, tam da
     * bu düzeltmenin kapattığı kusurdu (B8: pencere eşik altı ve tür dışı
     * kartları listeliyordu).
     */
    static boolean esikGecti(Map<String, Object> match) {
        return match != null && Boolean.TRUE.equals(match.get("match"));
    }
}
