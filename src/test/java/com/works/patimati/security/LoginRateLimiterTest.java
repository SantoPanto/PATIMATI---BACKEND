package com.works.patimati.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRateLimiterTest {

    private LoginRateLimiter limiter;

    @BeforeEach
    void setUp() {
        limiter = new LoginRateLimiter();
        ReflectionTestUtils.setField(limiter, "maxAttempts", 5);
        ReflectionTestUtils.setField(limiter, "windowMinutes", 15L);
    }

    @Test
    void esikAltindaEngellenmez() {
        for (int i = 0; i < 4; i++) {
            limiter.recordFailure("1.2.3.4");
        }
        assertThat(limiter.isBlocked("1.2.3.4")).isFalse();
    }

    @Test
    void esigeUlasincaEngellenir() {
        for (int i = 0; i < 5; i++) {
            limiter.recordFailure("1.2.3.4");
        }
        assertThat(limiter.isBlocked("1.2.3.4")).isTrue();
    }

    @Test
    void basariliGirisSayaciSifirlar() {
        for (int i = 0; i < 5; i++) {
            limiter.recordFailure("1.2.3.4");
        }
        assertThat(limiter.isBlocked("1.2.3.4")).isTrue();

        limiter.recordSuccess("1.2.3.4");

        assertThat(limiter.isBlocked("1.2.3.4")).isFalse();
    }

    @Test
    void farkliIpBirbirindenBagimsizdir() {
        for (int i = 0; i < 5; i++) {
            limiter.recordFailure("1.2.3.4");
        }
        assertThat(limiter.isBlocked("1.2.3.4")).isTrue();
        assertThat(limiter.isBlocked("5.6.7.8")).isFalse();

        limiter.recordFailure("5.6.7.8");
        assertThat(limiter.isBlocked("5.6.7.8")).isFalse();
    }

    @Test
    void hicDenemeYapilmamisIpEngellenmez() {
        assertThat(limiter.isBlocked("9.9.9.9")).isFalse();
    }
}
