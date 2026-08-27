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
import com.works.patimati.storage.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@code PoiService.findNearby} artık yalnızca {@code points_of_interest}
 * (OSM/MANUAL) DEĞİL, platforma kayıtlı VetClinic/PetShop/Shelter
 * hesaplarının konumlarını da birleştirip döner (source=PLATFORM, refId =
 * gerçek hesap kimliği) -- daha önce kayıtlı hesapların konumu ana haritada
 * HİÇ görünmüyordu, bu regresyonu kapatır.
 */
class PoiServiceTest {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    private PointOfInterestRepository poiRepository;
    private VetClinicRepository vetClinicRepository;
    private PetShopRepository petShopRepository;
    private ShelterRepository shelterRepository;
    private ImageStorageService imageStorageService;
    private PoiService service;

    @BeforeEach
    void hazirla() {
        poiRepository = mock(PointOfInterestRepository.class);
        vetClinicRepository = mock(VetClinicRepository.class);
        petShopRepository = mock(PetShopRepository.class);
        shelterRepository = mock(ShelterRepository.class);
        imageStorageService = mock(ImageStorageService.class);
        service = new PoiService(poiRepository, vetClinicRepository, petShopRepository, shelterRepository, imageStorageService);

        when(poiRepository.findNearby(any(), anyDouble())).thenReturn(List.of());
        when(vetClinicRepository.findNearby(any(), anyDouble())).thenReturn(List.of());
        when(petShopRepository.findNearby(any(), anyDouble())).thenReturn(List.of());
        when(shelterRepository.findNearby(any(), anyDouble())).thenReturn(List.of());
    }

    private static Point nokta(double lat, double lng) {
        return GEOMETRY_FACTORY.createPoint(new Coordinate(lng, lat));
    }

    @Test
    void kayitliVeterinerKlinigiHaritayaPlatformKaynagiyleKatilir() {
        VetClinic klinik = VetClinic.builder()
                .id(5L).name("Pati Veteriner").address("Adres").phone("0555").workingHours("09-18")
                .location(nokta(40.1, 29.5)).build();
        when(vetClinicRepository.findNearby(any(), anyDouble())).thenReturn(List.of(klinik));

        List<PoiResponse> sonuc = service.findNearby(40.1, 29.5, 5000, null);

        assertThat(sonuc).hasSize(1);
        PoiResponse poi = sonuc.get(0);
        assertThat(poi.type()).isEqualTo(PoiType.VETERINARY);
        assertThat(poi.source()).isEqualTo(PoiSource.PLATFORM);
        assertThat(poi.refId()).isEqualTo(5L);
        assertThat(poi.id()).isNotEqualTo(5L); // çakışma önleme için negatiflenir
        assertThat(poi.latitude()).isEqualTo(40.1);
        assertThat(poi.longitude()).isEqualTo(29.5);
    }

    @Test
    void kayitliPetshopVeBarinakDaBirlesirVeOsmSonuclariylaAyniListedeDoner() {
        PetShop petshop = PetShop.builder().id(7L).name("Pati Petshop").address("Adres").phone("0555")
                .location(nokta(40.2, 29.6)).build();
        Shelter barinak = Shelter.builder().id(9L).name("Umut Barınağı").address("Adres").phone("0555")
                .location(nokta(40.3, 29.7)).build();
        PointOfInterest osmNoktasi = PointOfInterest.builder()
                .id(1L).type(PoiType.VETERINARY).source(PoiSource.OSM).name("OSM Veteriner")
                .location(nokta(40.15, 29.55)).build();

        when(petShopRepository.findNearby(any(), anyDouble())).thenReturn(List.of(petshop));
        when(shelterRepository.findNearby(any(), anyDouble())).thenReturn(List.of(barinak));
        when(poiRepository.findNearby(any(), anyDouble())).thenReturn(List.of(osmNoktasi));

        List<PoiResponse> sonuc = service.findNearby(40.2, 29.6, 20000, null);

        assertThat(sonuc).hasSize(3);
        assertThat(sonuc).anySatisfy(poi -> {
            assertThat(poi.type()).isEqualTo(PoiType.PET_SHOP);
            assertThat(poi.refId()).isEqualTo(7L);
        });
        assertThat(sonuc).anySatisfy(poi -> {
            assertThat(poi.type()).isEqualTo(PoiType.SHELTER);
            assertThat(poi.refId()).isEqualTo(9L);
        });
        assertThat(sonuc).anySatisfy(poi -> {
            assertThat(poi.source()).isEqualTo(PoiSource.OSM);
            assertThat(poi.refId()).isNull();
        });
    }

    @Test
    void turFiltresiIstenmeyenRepolariHicSorgulamaz() {
        service.findNearby(40.1, 29.5, 5000, Set.of(PoiType.VETERINARY));

        verify(vetClinicRepository).findNearby(any(), anyDouble());
        verify(petShopRepository, never()).findNearby(any(), anyDouble());
        verify(shelterRepository, never()).findNearby(any(), anyDouble());
    }
}
