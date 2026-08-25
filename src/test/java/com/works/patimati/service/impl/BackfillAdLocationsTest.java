package com.works.patimati.service.impl;

import com.works.patimati.entity.Ad;
import com.works.patimati.repository.AdComplaintRepository;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.AdoptionComplaintRepository;
import com.works.patimati.repository.PotentialMatchRepository;
import com.works.patimati.repository.UserComplaintRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.repository.external.ExternalPetRecordRepository;
import com.works.patimati.repository.external.ExternalSourceMediaRepository;
import com.works.patimati.repository.external.ExternalSourcePostRepository;
import com.works.patimati.service.AdService;
import com.works.patimati.service.ReverseGeocodingService;
import com.works.patimati.storage.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * V19 backfill'i: yalnız çözülebilen ilan kaydedilir; çözülemeyen atlanır
 * ve SAYILIR (sessiz kayıp yok — [kapsam paydasını ölç] kuralının uç hali).
 * Adayları süzme işi repository sorgusunun kendisinde
 * (findByCityIsNullAndLocationIsNotNull) — burada o sorgunun çağrıldığı
 * ve sonucunun işlendiği ölçülüyor.
 */
class BackfillAdLocationsTest {

    private final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), 4326);

    private AdRepository adRepository;
    private ReverseGeocodingService reverseGeocodingService;
    private AdminServiceImpl adminService;

    @BeforeEach
    void hazirla() {
        adRepository = mock(AdRepository.class);
        reverseGeocodingService = mock(ReverseGeocodingService.class);
        adminService = new AdminServiceImpl(
                mock(UserRepository.class),
                adRepository,
                mock(AdComplaintRepository.class),
                mock(UserComplaintRepository.class),
                mock(AdoptionComplaintRepository.class),
                mock(AdService.class),
                mock(ExternalSourcePostRepository.class),
                mock(ExternalPetRecordRepository.class),
                mock(ExternalSourceMediaRepository.class),
                mock(PotentialMatchRepository.class),
                mock(ImageStorageService.class),
                reverseGeocodingService
        );
    }

    private Ad konumluAd(long id, double lat, double lon) {
        Ad ad = new Ad();
        ad.setId(id);
        ad.setLocation(geometryFactory.createPoint(new Coordinate(lon, lat)));
        return ad;
    }

    @Test
    void cozulenKaydedilirCozulemeyenSayilir() {
        Ad cozulen = konumluAd(1L, 39.9334, 32.8597);
        Ad cozulemeyen = konumluAd(2L, 0.0, 0.0);
        when(adRepository.findByCityIsNullAndLocationIsNotNull())
                .thenReturn(List.of(cozulen, cozulemeyen));

        when(reverseGeocodingService.cozumle(eq(39.9334), eq(32.8597))).thenReturn(
                Optional.of(new ReverseGeocodingService.IlIlce("Ankara", "Çankaya"))
        );
        when(reverseGeocodingService.cozumle(eq(0.0), eq(0.0)))
                .thenReturn(Optional.empty());

        Map<String, Integer> sonuc = adminService.backfillAdLocations();

        assertThat(sonuc).containsEntry("toplam", 2)
                .containsEntry("dolan", 1)
                .containsEntry("cozulemeyen", 1);
        assertThat(cozulen.getCity()).isEqualTo("Ankara");
        assertThat(cozulen.getDistrict()).isEqualTo("Çankaya");
        verify(adRepository).save(cozulen);
        verify(adRepository, never()).save(cozulemeyen);
    }

    @Test
    void adayYoksaHicIstekAtilmaz() {
        when(adRepository.findByCityIsNullAndLocationIsNotNull())
                .thenReturn(List.of());

        Map<String, Integer> sonuc = adminService.backfillAdLocations();

        assertThat(sonuc).containsEntry("toplam", 0);
        verify(adRepository, never()).save(any(Ad.class));
    }
}
