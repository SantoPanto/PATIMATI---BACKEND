package com.works.patimati.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.works.patimati.dto.poi.PoiSyncResult;
import com.works.patimati.entity.PointOfInterest;
import com.works.patimati.entity.enums.PoiSource;
import com.works.patimati.entity.enums.PoiType;
import com.works.patimati.repository.PointOfInterestRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Veteriner/petshop/barınak konumlarını OpenStreetMap Overpass API'sinden
 * çeker ve {@code points_of_interest} tablosuna upsert eder (V20).
 *
 * <p><b>Neden Overpass:</b> Google Places ücretli ve key/faturalandırma
 * ister; harita zaten OSM tile kullanıyor, aynı veri kaynağı ücretsiz ve
 * anahtarsız. Bedeli: veri topluluk kaynaklı, bazı bölgelerde eksik olabilir
 * — bu yüzden sonuç kaydı (created/updated/skipped) loglanır ve admin
 * ucundan izlenebilir.</p>
 *
 * <p><b>Sözleşme:</b> {@link ReverseGeocodingService} ile aynı ilke — dış
 * servis çökerse istisna fırlatmaz, {@link PoiSyncResult} içinde 0 sonuçla
 * döner ve loglar; zamanlayıcı (PoiSyncScheduler) veya admin ucu bu yüzden
 * asla 500 almaz.</p>
 */
@Service
public class PoiSyncService {

    private static final Logger log = LoggerFactory.getLogger(PoiSyncService.class);
    private static final String USER_AGENT = "patimati-backend/1.0 (+https://patimati.me)";
    private static final int WGS_84_SRID = 4326;

    private final RestTemplate restTemplate;
    private final PointOfInterestRepository poiRepository;
    private final String overpassUrl;
    private final boolean enabled;
    private final String bboxSouth;
    private final String bboxWest;
    private final String bboxNorth;
    private final String bboxEast;
    private final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), WGS_84_SRID);

    @Autowired
    public PoiSyncService(
            RestTemplateBuilder restTemplateBuilder,
            PointOfInterestRepository poiRepository,
            @Value("${app.poi.overpass.url}") String overpassUrl,
            @Value("${app.poi.overpass.enabled}") boolean enabled,
            @Value("${app.poi.overpass.timeout-ms}") long timeoutMs,
            @Value("${app.poi.overpass.bbox-south}") String bboxSouth,
            @Value("${app.poi.overpass.bbox-west}") String bboxWest,
            @Value("${app.poi.overpass.bbox-north}") String bboxNorth,
            @Value("${app.poi.overpass.bbox-east}") String bboxEast
    ) {
        this.restTemplate = restTemplateBuilder
                .connectTimeout(Duration.ofMillis(timeoutMs))
                .readTimeout(Duration.ofMillis(timeoutMs))
                .build();
        this.poiRepository = poiRepository;
        this.overpassUrl = overpassUrl;
        this.enabled = enabled;
        this.bboxSouth = bboxSouth;
        this.bboxWest = bboxWest;
        this.bboxNorth = bboxNorth;
        this.bboxEast = bboxEast;
    }

    @Transactional
    public PoiSyncResult syncFromOverpass() {
        if (!enabled) {
            log.info("POI senkronizasyonu kapalı (app.poi.overpass.enabled=false), atlanıyor.");
            return PoiSyncResult.disabled();
        }

        List<OverpassElement> elements;
        try {
            elements = fetchElements();
        } catch (RuntimeException exception) {
            log.warn("Overpass sorgusu başarısız, POI senkronizasyonu atlandı: {}", exception.getMessage());
            return PoiSyncResult.disabled();
        }

        int created = 0;
        int updated = 0;
        int skipped = 0;

        for (OverpassElement element : elements) {
            PoiType type = resolveType(element.tags());
            Double lat = element.lat() != null ? element.lat()
                    : (element.center() != null ? element.center().lat() : null);
            Double lon = element.lon() != null ? element.lon()
                    : (element.center() != null ? element.center().lon() : null);

            if (type == null || element.id() == null || lat == null || lon == null) {
                skipped++;
                continue;
            }

            PointOfInterest poi = poiRepository.findBySourceAndOsmId(PoiSource.OSM, element.id())
                    .orElseGet(PointOfInterest::new);
            boolean isNew = poi.getId() == null;

            poi.setSource(PoiSource.OSM);
            poi.setOsmId(element.id());
            poi.setType(type);
            poi.setName(resolveName(element.tags(), type));
            poi.setLocation(geometryFactory.createPoint(new Coordinate(lon, lat)));
            poi.setAddress(resolveAddress(element.tags()));
            poi.setPhone(firstNonBlank(element.tags(), "phone", "contact:phone"));
            poi.setOpeningHours(element.tags() == null ? null : element.tags().get("opening_hours"));

            poiRepository.save(poi);
            if (isNew) {
                created++;
            } else {
                updated++;
            }
        }

        PoiSyncResult result = new PoiSyncResult(true, created, updated, skipped);
        log.info("POI senkronizasyonu tamamlandı: {}", result);
        return result;
    }

    private List<OverpassElement> fetchElements() {
        String query = buildQuery();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set(HttpHeaders.USER_AGENT, USER_AGENT);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("data", query);

        ResponseEntity<OverpassResponse> response = restTemplate.postForEntity(
                overpassUrl,
                new HttpEntity<>(form, headers),
                OverpassResponse.class
        );

        OverpassResponse body = response.getBody();
        return body == null || body.elements() == null ? List.of() : body.elements();
    }

    /*
     * amenity=veterinary / shop=pet / amenity=animal_shelter — üç sorgu
     * tek istekte birleştirilir (Overpass sunucusuna daha az yük).
     * "out center" way/relation'lar için merkez koordinatı da döndürür.
     */
    private String buildQuery() {
        String bbox = String.join(",", bboxSouth, bboxWest, bboxNorth, bboxEast);
        return "[out:json][timeout:180];(" +
                "node[\"amenity\"=\"veterinary\"](" + bbox + ");" +
                "way[\"amenity\"=\"veterinary\"](" + bbox + ");" +
                "node[\"shop\"=\"pet\"](" + bbox + ");" +
                "way[\"shop\"=\"pet\"](" + bbox + ");" +
                "node[\"amenity\"=\"animal_shelter\"](" + bbox + ");" +
                "way[\"amenity\"=\"animal_shelter\"](" + bbox + ");" +
                ");out center;";
    }

    private static PoiType resolveType(Map<String, String> tags) {
        if (tags == null) {
            return null;
        }
        if ("veterinary".equals(tags.get("amenity"))) {
            return PoiType.VETERINARY;
        }
        if ("pet".equals(tags.get("shop"))) {
            return PoiType.PET_SHOP;
        }
        if ("animal_shelter".equals(tags.get("amenity"))) {
            return PoiType.SHELTER;
        }
        return null;
    }

    // OSM kaydının çoğunda "name" etiketi var ama hepsinde değil; boş
    // isimle harita/liste kartı anlamsız kalmasın diye türe göre varsayılan verilir.
    private static String resolveName(Map<String, String> tags, PoiType type) {
        String name = tags == null ? null : tags.get("name");
        if (name != null && !name.isBlank()) {
            return name.trim();
        }
        return switch (type) {
            case VETERINARY -> "Veteriner Kliniği";
            case PET_SHOP -> "Petshop";
            case SHELTER -> "Hayvan Barınağı";
        };
    }

    private static String resolveAddress(Map<String, String> tags) {
        if (tags == null) {
            return null;
        }
        String street = firstNonBlank(tags, "addr:street");
        String houseNumber = firstNonBlank(tags, "addr:housenumber");
        String district = firstNonBlank(tags, "addr:district", "addr:suburb");
        String city = firstNonBlank(tags, "addr:city", "addr:province");

        StringBuilder address = new StringBuilder();
        if (street != null) {
            address.append(street);
            if (houseNumber != null) {
                address.append(" ").append(houseNumber);
            }
        }
        if (district != null) {
            if (!address.isEmpty()) {
                address.append(", ");
            }
            address.append(district);
        }
        if (city != null) {
            if (!address.isEmpty()) {
                address.append(", ");
            }
            address.append(city);
        }
        return address.isEmpty() ? null : address.toString();
    }

    private static String firstNonBlank(Map<String, String> tags, String... keys) {
        if (tags == null) {
            return null;
        }
        for (String key : keys) {
            String value = tags.get(key);
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OverpassResponse(List<OverpassElement> elements) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OverpassElement(
            String type,
            Long id,
            Double lat,
            Double lon,
            OverpassCenter center,
            Map<String, String> tags
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OverpassCenter(Double lat, Double lon) {
    }
}
