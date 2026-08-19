package com.works.patimati.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/ads/1");
        when(request.getMethod()).thenReturn("GET");
    }

    @Test
    void mevcutSpesifikHandlerDavranisiDegismiyor() {
        // Katalog listesindeki spesifik handler'lardan biri: yeni catch-all
        // eklenmeden önceki davranışıyla aynı kalmalı (400, kendi mesajıyla).
        ResponseEntity<ProblemDetail> response =
                handler.handleIllegalArgument(new IllegalArgumentException("geçersiz sayfa boyutu"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getDetail()).isEqualTo("geçersiz sayfa boyutu");
    }

    @Test
    void beklenmeyenHataIcinCatchAll500VeProblemDetailDoner() {
        // Daha önce hiçbir handler'a uymayan bir istisna (ör. gerçek bir NPE)
        // yakalanmıyordu -- artık tutarlı bir ProblemDetail ile 500 döner.
        ResponseEntity<ProblemDetail> response =
                handler.handleUnexpected(new NullPointerException("beklenmedik null"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getInstance()).hasToString("/api/ads/1");
    }

    @Test
    void catchAllHamExceptionMesajiniIstemciyeSizdirmiyor() {
        // İç detay (ör. bir SQL hata metni) client'a çıplak dönmemeli --
        // yalnızca sabit, genel bir mesaj.
        ResponseEntity<ProblemDetail> response =
                handler.handleUnexpected(new RuntimeException("Duplicate entry 'x' for key PRIMARY"), request);

        assertThat(response.getBody().getDetail())
                .doesNotContain("Duplicate entry")
                .doesNotContain("PRIMARY");
    }
}
