package com.works.patimati.service;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class SightingRateLimiterTest {

    /** Testin elle ilerlettiği saat — gerçek zaman geçirmeden pencere kanıtı. */
    private static final class OynarSaat extends Clock {
        private Instant an = Instant.parse("2026-08-22T12:00:00Z");

        void ilerle(Duration sure) {
            an = an.plus(sure);
        }

        @Override
        public Instant instant() {
            return an;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }

    @Test
    void tavanaKadarIzinVerirTavandaReddeder() {
        OynarSaat saat = new OynarSaat();
        SightingRateLimiter limiter = new SightingRateLimiter(3, saat);

        assertThat(limiter.izinVer("203.0.113.7")).isTrue();
        assertThat(limiter.izinVer("203.0.113.7")).isTrue();
        assertThat(limiter.izinVer("203.0.113.7")).isTrue();
        assertThat(limiter.izinVer("203.0.113.7")).isFalse();

        // Başka IP kendi kovasından sayılır — çapraz kirlenme yok.
        assertThat(limiter.izinVer("198.51.100.9")).isTrue();
    }

    @Test
    void pencereGecinceAyniIpYenidenIzinAlir() {
        OynarSaat saat = new OynarSaat();
        SightingRateLimiter limiter = new SightingRateLimiter(2, saat);

        assertThat(limiter.izinVer("203.0.113.7")).isTrue();
        assertThat(limiter.izinVer("203.0.113.7")).isTrue();
        assertThat(limiter.izinVer("203.0.113.7")).isFalse();

        saat.ilerle(Duration.ofMinutes(61));

        assertThat(limiter.izinVer("203.0.113.7")).isTrue();
    }

    @Test
    void bosIpBilinmiyorKovasindaSinirlanir() {
        OynarSaat saat = new OynarSaat();
        SightingRateLimiter limiter = new SightingRateLimiter(1, saat);

        assertThat(limiter.izinVer(null)).isTrue();
        // null ve boş dizgi AYNI kovaya düşer: anahtar üretilemeyen istekler
        // birbirinden bağımsız sayılıp sınırsız kalmasın.
        assertThat(limiter.izinVer("")).isFalse();
    }
}
