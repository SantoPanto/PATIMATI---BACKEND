package com.works.patimati.service;

import com.works.patimati.dto.poi.PoiResponse;
import com.works.patimati.entity.PointOfInterest;
import com.works.patimati.entity.enums.PoiType;
import com.works.patimati.repository.PointOfInterestRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PoiService {

    private static final int WGS_84_SRID = 4326;

    private final PointOfInterestRepository poiRepository;
    private final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), WGS_84_SRID);

    /**
     * Harita için yarıçap içindeki veteriner/petshop/barınakları getirir.
     * {@code types} boş/null ise tümü döner; doluysa yalnızca istenen
     * türler süzülür.
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

        return poiRepository.findNearby(origin, radiusInMeters)
                .stream()
                .filter(poi -> types == null || types.isEmpty() || types.contains(poi.getType()))
                .map(PoiService::toResponse)
                .toList();
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
                poi.getOpeningHours()
        );
    }
}
