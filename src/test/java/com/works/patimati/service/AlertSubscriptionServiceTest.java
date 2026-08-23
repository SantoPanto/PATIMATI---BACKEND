package com.works.patimati.service;

import com.works.patimati.dto.alert.AlertSubscriptionResponse;
import com.works.patimati.dto.alert.AlertSubscriptionUpsertRequest;
import com.works.patimati.entity.AlertSubscription;
import com.works.patimati.entity.User;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.AlertSubscriptionRepository;
import com.works.patimati.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlertSubscriptionServiceTest {

    private static final String EPOSTA = "abone@ornek.com";

    private AlertSubscriptionRepository alertSubscriptionRepository;
    private UserRepository userRepository;
    private AlertSubscriptionService service;
    private User kullanici;

    @BeforeEach
    void hazirla() {
        alertSubscriptionRepository = mock(AlertSubscriptionRepository.class);
        userRepository = mock(UserRepository.class);
        service = new AlertSubscriptionService(alertSubscriptionRepository, userRepository);

        kullanici = User.builder().uid(5L).email(EPOSTA).build();
        when(userRepository.findByEmail(EPOSTA)).thenReturn(Optional.of(kullanici));
        // save(...) verilen nesneyi aynen döndürsün ki yanıt dönüşümü sınanabilsin.
        when(alertSubscriptionRepository.save(any(AlertSubscription.class)))
                .thenAnswer(cagri -> cagri.getArgument(0));
    }

    @Test
    void ilkKayitKoordinatDogruSirayla10KmYaricaplaOlusur() {
        when(alertSubscriptionRepository.findByUser_Uid(5L)).thenReturn(Optional.empty());

        AlertSubscriptionResponse yanit = service.upsertMine(EPOSTA,
                new AlertSubscriptionUpsertRequest(
                        new BigDecimal("39.9334"),   // enlem
                        new BigDecimal("32.8597"),   // boylam
                        new BigDecimal("10"),
                        true));

        ArgumentCaptor<AlertSubscription> kaydedilen =
                ArgumentCaptor.forClass(AlertSubscription.class);
        verify(alertSubscriptionRepository).save(kaydedilen.capture());

        // JTS (X, Y) = (boylam, enlem) — ters yazılırsa nokta okyanusa düşer.
        assertThat(kaydedilen.getValue().getLocation().getX()).isEqualTo(32.8597);
        assertThat(kaydedilen.getValue().getLocation().getY()).isEqualTo(39.9334);
        assertThat(kaydedilen.getValue().getRadiusMeters()).isEqualTo(10_000.0);
        assertThat(kaydedilen.getValue().isEnabled()).isTrue();
        assertThat(kaydedilen.getValue().getUser()).isSameAs(kullanici);
        assertThat(kaydedilen.getValue().getUpdatedAt()).isNotNull();

        assertThat(yanit.latitude()).isEqualByComparingTo("39.9334");
        assertThat(yanit.longitude()).isEqualByComparingTo("32.8597");
        assertThat(yanit.radiusKm()).isEqualByComparingTo("10");
        assertThat(yanit.enabled()).isTrue();
    }

    @Test
    void mevcutKayitYenidenOlusturulmadanGuncellenir() {
        GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 4326);
        AlertSubscription mevcut = AlertSubscription.builder()
                .id(11L)
                .user(kullanici)
                .location(gf.createPoint(new Coordinate(30.0, 40.0)))
                .radiusMeters(5_000.0)
                .enabled(true)
                .build();
        when(alertSubscriptionRepository.findByUser_Uid(5L)).thenReturn(Optional.of(mevcut));

        AlertSubscriptionResponse yanit = service.upsertMine(EPOSTA,
                new AlertSubscriptionUpsertRequest(
                        new BigDecimal("41.0"),
                        new BigDecimal("29.0"),
                        new BigDecimal("25"),
                        false));

        ArgumentCaptor<AlertSubscription> kaydedilen =
                ArgumentCaptor.forClass(AlertSubscription.class);
        verify(alertSubscriptionRepository).save(kaydedilen.capture());

        assertThat(kaydedilen.getValue()).isSameAs(mevcut);
        assertThat(kaydedilen.getValue().getRadiusMeters()).isEqualTo(25_000.0);
        assertThat(kaydedilen.getValue().isEnabled()).isFalse();
        assertThat(yanit.enabled()).isFalse();
        assertThat(yanit.radiusKm()).isEqualByComparingTo("25");
    }

    @Test
    void abonelikYokkenGetirmeKaynakYokHatasiVerir() {
        when(alertSubscriptionRepository.findByUser_Uid(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMine(EPOSTA))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getirmeMetreyiKmYeCevirir() {
        GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 4326);
        AlertSubscription mevcut = AlertSubscription.builder()
                .user(kullanici)
                .location(gf.createPoint(new Coordinate(29.0, 41.0)))
                .radiusMeters(25_000.0)
                .enabled(true)
                .build();
        when(alertSubscriptionRepository.findByUser_Uid(5L)).thenReturn(Optional.of(mevcut));

        AlertSubscriptionResponse yanit = service.getMine(EPOSTA);

        assertThat(yanit.radiusKm()).isEqualByComparingTo("25");
        assertThat(yanit.latitude()).isEqualByComparingTo("41.0");
        assertThat(yanit.longitude()).isEqualByComparingTo("29.0");
    }
}
