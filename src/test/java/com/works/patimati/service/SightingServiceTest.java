package com.works.patimati.service;

import com.works.patimati.dto.sighting.SightingCreateRequest;
import com.works.patimati.dto.sighting.SightingResponse;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.AdSighting;
import com.works.patimati.entity.User;
import com.works.patimati.exception.BusinessException;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.AdSightingRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.storage.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SightingServiceTest {

    private static final String SAHIP_EPOSTA = "sahip@ornek.com";

    private AdSightingRepository adSightingRepository;
    private AdRepository adRepository;
    private UserRepository userRepository;
    private ImageStorageService imageStorageService;
    private NotificationService notificationService;
    private SightingRateLimiter rateLimiter;
    private SightingService service;

    private User sahip;
    private Ad kayipIlan;

    @BeforeEach
    void hazirla() {
        adSightingRepository = mock(AdSightingRepository.class);
        adRepository = mock(AdRepository.class);
        userRepository = mock(UserRepository.class);
        imageStorageService = mock(ImageStorageService.class);
        notificationService = mock(NotificationService.class);
        rateLimiter = mock(SightingRateLimiter.class);
        service = new SightingService(
                adSightingRepository,
                adRepository,
                userRepository,
                imageStorageService,
                notificationService,
                rateLimiter
        );

        sahip = User.builder().uid(42L).email(SAHIP_EPOSTA).build();
        kayipIlan = Ad.builder()
                .id(7L)
                .title("Boncuk kayboldu")
                .adType(Ad.AdType.LOST)
                .user(sahip)
                .build();

        when(rateLimiter.izinVer(anyString())).thenReturn(true);
        when(adRepository.findByIdAndActiveTrueAndSuspendedFalse(7L))
                .thenReturn(Optional.of(kayipIlan));
        // saveAndFlush verilen nesneye kimlik atayıp aynen döndürsün.
        when(adSightingRepository.saveAndFlush(any(AdSighting.class)))
                .thenAnswer(cagri -> {
                    AdSighting s = cagri.getArgument(0);
                    s.setId(99L);
                    return s;
                });
    }

    private SightingCreateRequest istek() {
        return new SightingCreateRequest(
                new BigDecimal("40.1928"),
                new BigDecimal("29.0610"),
                "Parkta gördüm, tasması yoktu.",
                "0555 111 22 33");
    }

    @Test
    void girissizBirakmaKaydedilirVeSahibineTipVeTekillestirmeyleBildirilir() {
        SightingResponse yanit = service.createSighting(
                7L, istek(), null, null, "203.0.113.7");

        assertThat(yanit.id()).isEqualTo(99L);
        assertThat(yanit.latitude()).isEqualByComparingTo("40.1928");
        assertThat(yanit.longitude()).isEqualByComparingTo("29.0610");
        assertThat(yanit.photoUrl()).isNull();

        verify(adSightingRepository).saveAndFlush(any(AdSighting.class));
        verify(notificationService).createAndSend(
                eq(sahip),
                eq("İlanın için görülme bildirimi"),
                anyString(),
                eq("SIGHTING"),
                eq(Map.of(
                        "type", "SIGHTING",
                        "adId", "7",
                        "sightingId", "99")),
                eq("SIGHTING:99")
        );
        verifyNoInteractions(imageStorageService);
    }

    @Test
    void girisliBirakanGorulmeyeBaglanir() {
        User bildiren = User.builder().uid(5L).email("gorup@ornek.com").build();
        when(userRepository.findByEmail("gorup@ornek.com"))
                .thenReturn(Optional.of(bildiren));

        service.createSighting(7L, istek(), null, "gorup@ornek.com", "203.0.113.7");

        verify(adSightingRepository).saveAndFlush(
                org.mockito.ArgumentMatchers.argThat(s -> s.getReporter() == bildiren));
    }

    @Test
    void kayipDisiIlanaBirakmaReddedilir() {
        Ad bulundu = Ad.builder().id(8L).adType(Ad.AdType.FOUND).user(sahip).build();
        when(adRepository.findByIdAndActiveTrueAndSuspendedFalse(8L))
                .thenReturn(Optional.of(bulundu));

        assertThatThrownBy(() ->
                service.createSighting(8L, istek(), null, null, "203.0.113.7"))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(adSightingRepository, notificationService);
    }

    @Test
    void pasifVeyaOlmayanIlanYokSayilir() {
        when(adRepository.findByIdAndActiveTrueAndSuspendedFalse(70L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.createSighting(70L, istek(), null, null, "203.0.113.7"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void hizSiniriDolunca429VeHicbirIsYapilmaz() {
        when(rateLimiter.izinVer("203.0.113.7")).thenReturn(false);

        assertThatThrownBy(() ->
                service.createSighting(7L, istek(), null, null, "203.0.113.7"))
                .isInstanceOfSatisfying(ResponseStatusException.class, hata ->
                        assertThat(hata.getStatusCode())
                                .isEqualTo(HttpStatus.TOO_MANY_REQUESTS));

        verifyNoInteractions(adRepository, adSightingRepository, notificationService);
    }

    @Test
    void dbKaydiDusersYuklenmisFotoGeriSilinir() {
        MockMultipartFile foto = new MockMultipartFile(
                "photo", "gorulme.jpg", "image/jpeg", new byte[]{1, 2, 3});
        when(imageStorageService.uploadImages(any()))
                .thenReturn(List.of("s3://kova/gorulme.jpg"));
        when(adSightingRepository.saveAndFlush(any(AdSighting.class)))
                .thenThrow(new RuntimeException("veritabanı düştü"));

        assertThatThrownBy(() ->
                service.createSighting(7L, istek(), foto, null, "203.0.113.7"))
                .isInstanceOf(RuntimeException.class);

        verify(imageStorageService).deleteImages(List.of("s3://kova/gorulme.jpg"));
        verifyNoInteractions(notificationService);
    }

    @Test
    void bildirimHatasiKaydiDusurmez() {
        when(notificationService.createAndSend(any(), anyString(), anyString(),
                anyString(), any(), anyString()))
                .thenThrow(new RuntimeException("push servisi yok"));

        assertThatCode(() ->
                service.createSighting(7L, istek(), null, null, "203.0.113.7"))
                .doesNotThrowAnyException();
    }

    @Test
    void gorulmeleriYalnizIlanSahibiListeler() {
        when(userRepository.findByEmail(SAHIP_EPOSTA)).thenReturn(Optional.of(sahip));
        when(adRepository.findByIdAndUser_Uid(7L, 42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listSightings(7L, SAHIP_EPOSTA))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(adSightingRepository);
    }

    @Test
    void listedeFotoReferansiGeciciUrlyeCevrilir() {
        when(userRepository.findByEmail(SAHIP_EPOSTA)).thenReturn(Optional.of(sahip));
        when(adRepository.findByIdAndUser_Uid(7L, 42L)).thenReturn(Optional.of(kayipIlan));

        AdSighting kayit = AdSighting.builder()
                .id(99L)
                .ad(kayipIlan)
                .reporterContact("0555 111 22 33")
                .photoUrl("s3://kova/gorulme.jpg")
                .location(new org.locationtech.jts.geom.GeometryFactory(
                        new org.locationtech.jts.geom.PrecisionModel(), 4326)
                        .createPoint(new org.locationtech.jts.geom.Coordinate(29.0, 41.0)))
                .build();
        when(adSightingRepository.findByAd_IdOrderByCreatedAtDescIdDesc(7L))
                .thenReturn(List.of(kayit));
        when(imageStorageService.createTemporaryReadUrl("s3://kova/gorulme.jpg"))
                .thenReturn("https://gecici/gorulme.jpg?imza");

        List<SightingResponse> liste = service.listSightings(7L, SAHIP_EPOSTA);

        assertThat(liste).hasSize(1);
        assertThat(liste.get(0).photoUrl()).isEqualTo("https://gecici/gorulme.jpg?imza");
        assertThat(liste.get(0).latitude()).isEqualByComparingTo("41.0");
        assertThat(liste.get(0).longitude()).isEqualByComparingTo("29.0");
    }
}
