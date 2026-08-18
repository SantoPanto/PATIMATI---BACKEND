package com.works.patimati.controller;

import com.works.patimati.exception.GlobalExceptionHandler;
import com.works.patimati.dto.ad.AdCountersResponse;
import com.works.patimati.service.AdService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class PublicAdControllerTest {

    private AdService adService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        /*
         * Controller testi gerçek veritabanı ve PostGIS bağlantısı kullanmaz.
         * AdService bunun yerine Mockito ile taklit edilir.
         */
        adService = mock(AdService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new PublicAdController(adService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldListNearbyAdsWithoutAuthentication() throws Exception {
        /*
         * Servis boş liste döndürecek şekilde hazırlanır.
         * Bu testte JWT veya kullanıcı bilgisi kullanılmaz.
         */
        when(adService.findPublicNearbyAds(
                40.195,
                29.060,
                5000.0
        )).thenReturn(List.of());

        /*
         * İstek üzerinde principal veya Authorization başlığı bulunmuyor.
         * Public endpoint'in kullanıcı kimliği istemediği kontrol ediliyor.
         */
        mockMvc.perform(
                        get("/api/public/ads/nearby")
                                .param("latitude", "40.195")
                                .param("longitude", "29.060")
                )
                .andExpect(status().isOk());

        /*
         * Radius gönderilmediği için controller'ın varsayılan
         * 5000 metre değerini kullandığı doğrulanır.
         */
        verify(adService).findPublicNearbyAds(
                40.195,
                29.060,
                5000.0
        );
    }

    @Test
    void shouldReturnAdCountersWithoutAuthentication() throws Exception {
        when(adService.getAdCounters()).thenReturn(new AdCountersResponse(12L, 7L));

        mockMvc.perform(get("/api/public/ads/counters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeAds").value(12))
                .andExpect(jsonPath("$.happyEndings").value(7));

        verify(adService).getAdCounters();
    }
}
