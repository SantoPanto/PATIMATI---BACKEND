package com.works.patimati.service;

import com.works.patimati.ai.AiAnalysisPublisher;
import com.works.patimati.entity.Ad;
import com.works.patimati.mapper.AdMapper;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.storage.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * İl/ilçe doldurma kuralları (V19): form beyanı ÖNCELİKLİDİR — beyan
 * varken dış servise hiç gidilmez (gereksiz istek + Nominatim hız
 * politikası). Beyan yoksa koordinattan çözülür; çözüm de yoksa alanlar
 * boş kalır ve ilan akışı etkilenmez.
 */
class KonumBilgisiDoldurmaTest {

    private final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), 4326);

    private ReverseGeocodingService reverseGeocodingService;
    private AdService adService;

    @BeforeEach
    void hazirla() {
        reverseGeocodingService = mock(ReverseGeocodingService.class);
        adService = new AdService(
                mock(AdRepository.class),
                mock(UserRepository.class),
                mock(AdMapper.class),
                mock(ImageStorageService.class),
                mock(AiAnalysisPublisher.class),
                mock(RewardService.class),
                mock(NotificationService.class),
                reverseGeocodingService,
                mock(com.works.patimati.notification.NearbyAlertNotifier.class)
        );
    }

    private Ad konumluAd() {
        Ad ad = new Ad();
        ad.setLocation(geometryFactory.createPoint(new Coordinate(32.8597, 39.9334)));
        return ad;
    }

    @Test
    void beyanVarkenDisServiseHicGidilmez() {
        Ad ad = konumluAd();

        adService.konumBilgisiniDoldur(ad, "  Bursa  ", "Nilüfer");

        assertThat(ad.getCity()).isEqualTo("Bursa");
        assertThat(ad.getDistrict()).isEqualTo("Nilüfer");
        verifyNoInteractions(reverseGeocodingService);
    }

    @Test
    void beyanYoksaKoordinattanCozulur() {
        when(reverseGeocodingService.cozumle(any(), any())).thenReturn(
                Optional.of(new ReverseGeocodingService.IlIlce("Ankara", "Çankaya"))
        );
        Ad ad = konumluAd();

        adService.konumBilgisiniDoldur(ad, null, null);

        assertThat(ad.getCity()).isEqualTo("Ankara");
        assertThat(ad.getDistrict()).isEqualTo("Çankaya");
    }

    @Test
    void yalnizIlceBeyanliysaIlGeokoddanIlceBeyandanGelir() {
        when(reverseGeocodingService.cozumle(any(), any())).thenReturn(
                Optional.of(new ReverseGeocodingService.IlIlce("Ankara", "Çankaya"))
        );
        Ad ad = konumluAd();

        adService.konumBilgisiniDoldur(ad, " ", "Keçiören");

        assertThat(ad.getCity()).isEqualTo("Ankara");
        assertThat(ad.getDistrict()).isEqualTo("Keçiören");
    }

    @Test
    void cozumYoksaAlanlarBosKalirIstisnaCikmaz() {
        when(reverseGeocodingService.cozumle(any(), any()))
                .thenReturn(Optional.empty());
        Ad ad = konumluAd();

        adService.konumBilgisiniDoldur(ad, null, null);

        assertThat(ad.getCity()).isNull();
        assertThat(ad.getDistrict()).isNull();
    }

    @Test
    void konumsuzIlandaDisServiseGidilmez() {
        Ad ad = new Ad();

        adService.konumBilgisiniDoldur(ad, null, null);

        assertThat(ad.getCity()).isNull();
        verifyNoInteractions(reverseGeocodingService);
    }
}
