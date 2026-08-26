package com.works.patimati.service;

import com.works.patimati.dto.poi.PoiResponse;
import com.works.patimati.entity.PetShop;
import com.works.patimati.entity.PointOfInterest;
import com.works.patimati.entity.Shelter;
import com.works.patimati.entity.VetClinic;
import com.works.patimati.entity.enums.PoiSource;
import com.works.patimati.entity.enums.PoiType;
import com.works.patimati.repository.PetShopRepository;
import com.works.patimati.repository.PointOfInterestRepository;
import com.works.patimati.repository.ShelterRepository;
import com.works.patimati.repository.VetClinicRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PoiService {

    private static final int WGS_84_SRID = 4326;

    private final PointOfInterestRepository poiRepository;
    private final VetClinicRepository vetClinicRepository;
    private final PetShopRepository petShopRepository;
    private final ShelterRepository shelterRepository;
    private final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), WGS_84_SRID);

    /**
     * Harita için yarıçap içindeki veteriner/petshop/barınakları getirir --
     * hem {@code points_of_interest} (OSM senkronu + admin elle giriş) HEM
     * DE platforma kayıtlı hesapların (VetClinic/PetShop/Shelter, konum
     * ayarlamış olanlar) birleşimi. Kayıtlı hesaplar {@code source=PLATFORM}
     * ve gerçek hesap kimliğini taşıyan {@code refId} ile döner -- ön yüz bu
     * sayede "Hizmete Git" bağlantısını kurabilir; OSM/MANUAL noktalarda
     * (gerçek bir hesap/detay sayfası olmadığı için) {@code refId} null'dur.
     *
     * <p>{@code types} boş/null ise tümü döner; doluysa yalnızca istenen
     * türler süzülür (ve o türe karşılık gelmeyen repo hiç sorgulanmaz).
     */
    @Transactional(readOnly = true)
    public List<PoiResponse> findNearby(
            double latitude,
            double longitude,
            double radiusInMeters,
            Set<PoiType> types
    ) {
        // JTS Coordinate sırası longitude (X), latitude (Y) şeklindedir.
        Point origin = geometryFactory.createPoint(new Coordinate(longitude, latitude));

        List<PoiResponse> results = new ArrayList<>();

        poiRepository.findNearby(origin, radiusInMeters).stream()
                .filter(poi -> wants(types, poi.getType()))
                .map(PoiService::toResponse)
                .forEach(results::add);

        if (wants(types, PoiType.VETERINARY)) {
            vetClinicRepository.findNearby(origin, radiusInMeters).stream()
                    .map(PoiService::toResponse)
                    .forEach(results::add);
        }

        if (wants(types, PoiType.PET_SHOP)) {
            petShopRepository.findNearby(origin, radiusInMeters).stream()
                    .map(PoiService::toResponse)
                    .forEach(results::add);
        }

        if (wants(types, PoiType.SHELTER)) {
            shelterRepository.findNearby(origin, radiusInMeters).stream()
                    .map(PoiService::toResponse)
                    .forEach(results::add);
        }

        return results;
    }

    private static boolean wants(Set<PoiType> types, PoiType type) {
        return types == null || types.isEmpty() || types.contains(type);
    }

    private static PoiResponse toResponse(PointOfInterest poi) {
        Point location = poi.getLocation();

        return new PoiResponse(
                poi.getId(),
                poi.getType(),
                poi.getName(),
                location == null ? null : location.getY(),
                location == null ? null : location.getX(),
                poi.getAddress(),
                poi.getPhone(),
                poi.getOpeningHours(),
                poi.getSource(),
                null
        );
    }

    // Platform hesaplarının ID'leri points_of_interest ile AYNI sayı
    // uzayını paylaşıyor (her biri kendi tablosunda 1'den başlayan
    // BIGSERIAL) -- aynı listede iki farklı kaynaktan aynı `id` gelip React
    // key çakışmasına yol açmaması için PLATFORM sonuçlarının `id`si
    // negatiflenir. Gerçek hesap kimliği zaten `refId`de taşınıyor, `id`
    // yalnızca listede/haritada benzersiz bir anahtar.

    private static PoiResponse toResponse(VetClinic clinic) {
        Point location = clinic.getLocation();

        return new PoiResponse(
                -clinic.getId(),
                PoiType.VETERINARY,
                clinic.getName(),
                location == null ? null : location.getY(),
                location == null ? null : location.getX(),
                clinic.getAddress(),
                clinic.getPhone(),
                clinic.getWorkingHours(),
                PoiSource.PLATFORM,
                clinic.getId()
        );
    }

    private static PoiResponse toResponse(PetShop petShop) {
        Point location = petShop.getLocation();

        return new PoiResponse(
                -petShop.getId(),
                PoiType.PET_SHOP,
                petShop.getName(),
                location == null ? null : location.getY(),
                location == null ? null : location.getX(),
                petShop.getAddress(),
                petShop.getPhone(),
                petShop.getWorkingHours(),
                PoiSource.PLATFORM,
                petShop.getId()
        );
    }

    private static PoiResponse toResponse(Shelter shelter) {
        Point location = shelter.getLocation();

        return new PoiResponse(
                -shelter.getId(),
                PoiType.SHELTER,
                shelter.getName(),
                location == null ? null : location.getY(),
                location == null ? null : location.getX(),
                shelter.getAddress(),
                shelter.getPhone(),
                shelter.getWorkingHours(),
                PoiSource.PLATFORM,
                shelter.getId()
        );
    }
}
