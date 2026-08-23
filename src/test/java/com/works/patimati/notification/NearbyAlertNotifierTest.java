package com.works.patimati.notification;

import com.works.patimati.entity.Ad;
import com.works.patimati.entity.AlertSubscription;
import com.works.patimati.entity.User;
import com.works.patimati.repository.AlertSubscriptionRepository;
import com.works.patimati.repository.NotificationRepository;
import com.works.patimati.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class NearbyAlertNotifierTest {

    private static final int GUNLUK_TAVAN = 2;

    private final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), 4326);

    private AlertSubscriptionRepository alertSubscriptionRepository;
    private NotificationRepository notificationRepository;
    private NotificationService notificationService;
    private NearbyAlertNotifier notifier;

    @BeforeEach
    void hazirla() {
        alertSubscriptionRepository = mock(AlertSubscriptionRepository.class);
        notificationRepository = mock(NotificationRepository.class);
        notificationService = mock(NotificationService.class);
        notifier = new NearbyAlertNotifier(
                alertSubscriptionRepository,
                notificationRepository,
                notificationService,
                GUNLUK_TAVAN
        );
    }

    private Point nokta() {
        // (X, Y) = (boylam, enlem)
        return geometryFactory.createPoint(new Coordinate(32.8597, 39.9334));
    }

    private Ad kayipIlan(Long sahipUid) {
        return Ad.builder()
                .id(7L)
                .title("Boncuk kayboldu")
                .adType(Ad.AdType.LOST)
                .location(nokta())
                .user(User.builder().uid(sahipUid).build())
                .build();
    }

    private AlertSubscription abonelik(Long uid) {
        return AlertSubscription.builder()
                .id(uid)
                .user(User.builder().uid(uid).build())
                .location(nokta())
                .radiusMeters(10_000.0)
                .enabled(true)
                .build();
    }

    @Test
    void kayipIlandaYaricapIcindekiAbonelereTipVeTekillestirmeAnahtariylaGider() {
        when(alertSubscriptionRepository.findEnabledWithinOwnRadius(any(), eq(42L)))
                .thenReturn(List.of(abonelik(1L)));
        when(notificationRepository.countByUser_UidAndTypeAndCreatedAtAfter(
                eq(1L), eq("NEARBY_AD"), any())).thenReturn(0L);

        notifier.yeniIlaniBildir(kayipIlan(42L));

        verify(notificationService).createAndSend(
                any(User.class),
                eq("Çevrende kayıp ilanı"),
                eq("Yakınında yeni bir kayıp ilanı: Boncuk kayboldu"),
                eq("NEARBY_AD"),
                eq(Map.of("type", "NEARBY_AD", "adId", "7")),
                eq("NEARBY_AD:7:1")
        );
    }

    @Test
    void kayipDisiIlanHicbirSorguVeBildirimTetiklemez() {
        Ad bulunduIlani = Ad.builder()
                .id(8L)
                .adType(Ad.AdType.FOUND)
                .location(nokta())
                .user(User.builder().uid(42L).build())
                .build();

        notifier.yeniIlaniBildir(bulunduIlani);

        verifyNoInteractions(alertSubscriptionRepository, notificationService);
    }

    @Test
    void konumsuzIlanSessizceAtlanir() {
        Ad konumsuz = Ad.builder()
                .id(9L)
                .adType(Ad.AdType.LOST)
                .user(User.builder().uid(42L).build())
                .build();

        notifier.yeniIlaniBildir(konumsuz);

        verifyNoInteractions(alertSubscriptionRepository, notificationService);
    }

    @Test
    void gunlukTavaniDolduranAboneAtlanirDigerleriAlir() {
        when(alertSubscriptionRepository.findEnabledWithinOwnRadius(any(), eq(42L)))
                .thenReturn(List.of(abonelik(1L), abonelik(2L)));
        // 1 numaralı abone tavanın altında, 2 numaralı tavana ulaşmış.
        when(notificationRepository.countByUser_UidAndTypeAndCreatedAtAfter(
                eq(1L), eq("NEARBY_AD"), any())).thenReturn(GUNLUK_TAVAN - 1L);
        when(notificationRepository.countByUser_UidAndTypeAndCreatedAtAfter(
                eq(2L), eq("NEARBY_AD"), any())).thenReturn((long) GUNLUK_TAVAN);

        notifier.yeniIlaniBildir(kayipIlan(42L));

        verify(notificationService).createAndSend(
                any(User.class), anyString(), anyString(), anyString(), any(), eq("NEARBY_AD:7:1"));
        verify(notificationService, never()).createAndSend(
                any(User.class), anyString(), anyString(), anyString(), any(), eq("NEARBY_AD:7:2"));
    }

    @Test
    void aboneSorgusuPatlarsaIlanAkisinaIstisnaSizmaz() {
        when(alertSubscriptionRepository.findEnabledWithinOwnRadius(any(), anyLong()))
                .thenThrow(new RuntimeException("veritabanı yok"));

        assertThatCode(() -> notifier.yeniIlaniBildir(kayipIlan(42L)))
                .doesNotThrowAnyException();

        verifyNoInteractions(notificationService);
    }
}
