package com.works.patimati.service;

import com.works.patimati.dto.alert.AlertSubscriptionResponse;
import com.works.patimati.dto.alert.AlertSubscriptionUpsertRequest;
import com.works.patimati.entity.AlertSubscription;
import com.works.patimati.entity.User;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.AlertSubscriptionRepository;
import com.works.patimati.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;

/**
 * Kullanıcının konum tabanlı uyarı aboneliğini okur ve yazar
 * (kullanıcı başına tek satır; bkz. V21 + {@link AlertSubscription}).
 */
@Service
@RequiredArgsConstructor
public class AlertSubscriptionService {

    private static final double METRE_KATSAYISI = 1000.0;

    private final AlertSubscriptionRepository alertSubscriptionRepository;
    private final UserRepository userRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Transactional(readOnly = true)
    public AlertSubscriptionResponse getMine(String userEmail) {
        User user = findUserByEmail(userEmail);
        AlertSubscription abonelik = alertSubscriptionRepository.findByUser_Uid(user.getUid())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Uyarı aboneliği bulunamadı"));
        return toResponse(abonelik);
    }

    @Transactional
    public AlertSubscriptionResponse upsertMine(String userEmail, AlertSubscriptionUpsertRequest request) {
        User user = findUserByEmail(userEmail);

        AlertSubscription abonelik = alertSubscriptionRepository.findByUser_Uid(user.getUid())
                .orElseGet(() -> AlertSubscription.builder().user(user).build());

        abonelik.setLocation(toPoint(request.latitude(), request.longitude()));
        abonelik.setRadiusMeters(request.radiusKm().doubleValue() * METRE_KATSAYISI);
        abonelik.setEnabled(Boolean.TRUE.equals(request.enabled()));
        abonelik.setUpdatedAt(OffsetDateTime.now());

        return toResponse(alertSubscriptionRepository.save(abonelik));
    }

    private Point toPoint(BigDecimal latitude, BigDecimal longitude) {
        // JTS koordinat sırası (X, Y) = (boylam, enlem) — AdMapper.toPoint
        // ile aynı; ters yazılırsa nokta okyanusa düşer ve hiç uyarı gitmez.
        return geometryFactory.createPoint(
                new Coordinate(longitude.doubleValue(), latitude.doubleValue()));
    }

    private AlertSubscriptionResponse toResponse(AlertSubscription abonelik) {
        Point konum = abonelik.getLocation();
        return new AlertSubscriptionResponse(
                BigDecimal.valueOf(konum.getY()),
                BigDecimal.valueOf(konum.getX()),
                // stripTrailingZeros KULLANMA: 10.0'ı 1E+1'e çevirir ve
                // Jackson JSON'a bilimsel gösterimle yazabilir.
                BigDecimal.valueOf(abonelik.getRadiusMeters() / METRE_KATSAYISI)
                        .setScale(1, RoundingMode.HALF_UP),
                abonelik.isEnabled()
        );
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Kullanıcı bulunamadı: " + email));
    }
}
