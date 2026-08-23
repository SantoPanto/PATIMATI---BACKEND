package com.works.patimati.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InternalServiceAuthFilterTest {

    private static final String HEADER = "X-Internal-Api-Key";

    @AfterEach
    void temizle() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void dogruAnahtarlaGecerVeKimlikAtar() throws Exception {
        InternalServiceAuthFilter filter = new InternalServiceAuthFilter("gizli-anahtar-123");
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getRequestURI()).thenReturn("/internal/ingestion/media");
        when(request.getHeader(HEADER)).thenReturn("gizli-anahtar-123");

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    void yanlisAnahtarla401DonerVeZincireGecmez() throws Exception {
        InternalServiceAuthFilter filter = new InternalServiceAuthFilter("gizli-anahtar-123");
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getRequestURI()).thenReturn("/internal/ingestion/media");
        when(request.getHeader(HEADER)).thenReturn("yanlis-anahtar");

        filter.doFilterInternal(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void farkliUzunluktaAnahtarlaDaGuvenliCalisir() throws Exception {
        // MessageDigest.isEqual'a geçişin regresyon testi: uzunluk farkı
        // (kısa devre almadan) hâlâ doğru şekilde reddediyor mu.
        InternalServiceAuthFilter filter = new InternalServiceAuthFilter("uzun-gizli-anahtar-1234567890");
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getRequestURI()).thenReturn("/internal/ingestion/media");
        when(request.getHeader(HEADER)).thenReturn("k");

        filter.doFilterInternal(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void internalOlmayanYolaDokunmazVeZincireDevamEder() throws Exception {
        InternalServiceAuthFilter filter = new InternalServiceAuthFilter("gizli-anahtar-123");
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getRequestURI()).thenReturn("/api/ads");

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void bosBeklenenAnahtarlaHerZaman401() throws Exception {
        InternalServiceAuthFilter filter = new InternalServiceAuthFilter("");
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getRequestURI()).thenReturn("/internal/ingestion/media");
        when(request.getHeader(HEADER)).thenReturn("herhangi-bir-deger");

        filter.doFilterInternal(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(chain, never()).doFilter(request, response);
    }
}
