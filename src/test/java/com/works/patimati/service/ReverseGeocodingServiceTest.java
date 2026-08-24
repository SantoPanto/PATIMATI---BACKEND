package com.works.patimati.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Ters geokodlama sözleşmesi (V19).
 *
 * <p><b>Sözleşmenin kalbi:</b> bu servis ilan OLUŞTURMA akışının içinden
 * çağrılır; hangi hata olursa olsun istisna SIZDIRMAZ — sızdırsaydı dış
 * servisin çökmesi ilan kaydını engellerdi. Ayrıştırma vakaları ise
 * Nominatim'in Türkiye'de iki farklı cevap biçimini kapsar: büyükşehirde
 * il "city", diğer illerde "province" anahtarıyla gelir.
 */
class ReverseGeocodingServiceTest {

    private static final String URL = "https://nominatim.example/reverse";

    private final RestTemplate restTemplate = mock(RestTemplate.class);

    private ReverseGeocodingService servis(boolean enabled) {
        return new ReverseGeocodingService(restTemplate, URL, enabled);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void cevapVer(Map<String, Object> govde) {
        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)
        )).thenReturn((ResponseEntity) ResponseEntity.ok(govde));
    }

    @Test
    void buyukSehirCevabindaIlCityIlceCountyAnahtarindanGelir() {
        cevapVer(Map.of("address", Map.of(
                "city", "Ankara",
                "county", "Çankaya",
                "country", "Türkiye"
        )));

        Optional<ReverseGeocodingService.IlIlce> sonuc =
                servis(true).cozumle(39.9334, 32.8597);

        assertThat(sonuc).isPresent();
        assertThat(sonuc.get().il()).isEqualTo("Ankara");
        assertThat(sonuc.get().ilce()).isEqualTo("Çankaya");
    }

    @Test
    void kucukIlCevabindaIlProvinceAnahtarindanGelir() {
        cevapVer(Map.of("address", Map.of(
                "province", "Bolu",
                "town", "Gerede"
        )));

        Optional<ReverseGeocodingService.IlIlce> sonuc =
                servis(true).cozumle(40.8, 32.2);

        assertThat(sonuc).isPresent();
        assertThat(sonuc.get().il()).isEqualTo("Bolu");
        assertThat(sonuc.get().ilce()).isEqualTo("Gerede");
    }

    @Test
    void servisHatasiIstisnaSIZDIRMAZBosDoner() {
        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)
        )).thenThrow(new ResourceAccessException("bağlantı zaman aşımı"));

        Optional<ReverseGeocodingService.IlIlce> sonuc =
                servis(true).cozumle(39.9, 32.8);

        assertThat(sonuc).isEmpty();
    }

    @Test
    void adresBlokuYoksaBosDoner() {
        cevapVer(Map.of("error", "Unable to geocode"));

        assertThat(servis(true).cozumle(0.0, 0.0)).isEmpty();
    }

    @Test
    void kapaliykenDisServiseHicGidilmez() {
        Optional<ReverseGeocodingService.IlIlce> sonuc =
                servis(false).cozumle(39.9, 32.8);

        assertThat(sonuc).isEmpty();
        verifyNoInteractions(restTemplate);
    }
}
