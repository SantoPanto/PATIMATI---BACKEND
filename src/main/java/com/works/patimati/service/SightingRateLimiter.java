package com.works.patimati.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Görülme bildirimi için IP başına kayan pencereli hız sınırı.
 *
 * <p>Neden var: POST /api/public/ads/{id}/sightings, bu deponun
 * {@code /api/public/**} altındaki İLK yazma ucu — girişsiz erişim ürün
 * kararı (afiş QR'ı okutan kişi üye değildir), bedeli de budur. Depoda
 * başka hiçbir hız sınırlama altyapısı yok (bucket4j/filter/interceptor
 * arandı, sıfır), o yüzden en küçük yeterli araç: bellek içi sayaç.
 * Tek örnekli dağıtımda doğru; ikinci örnek gelirse paylaşımlı depoya
 * (ör. veritabanı/Redis) taşınmalı.
 *
 * <p>{@link Clock} enjeksiyonu testler gerçek zaman geçirmeden pencereyi
 * kanıtlayabilsin diye (metin_analiz.py'deki sleep enjeksiyonuyla aynı ilke).
 */
@Component
public class SightingRateLimiter {

    private static final Duration PENCERE = Duration.ofHours(1);

    private final int saatlikTavan;
    private final Clock clock;
    private final Map<String, Deque<Instant>> istekler = new ConcurrentHashMap<>();

    // İki kurucu var (testler Clock enjekte eder); Spring'in hangisini
    // kullanacağı @Autowired ile açıkça işaretli — işaretsiz bırakmak
    // "No default constructor found" ile TÜM bağlam testlerini düşürdü.
    @Autowired
    public SightingRateLimiter(
            @Value("${app.sighting.hourly-cap-per-ip:5}") int saatlikTavan
    ) {
        this(saatlikTavan, Clock.systemUTC());
    }

    SightingRateLimiter(int saatlikTavan, Clock clock) {
        this.saatlikTavan = saatlikTavan;
        this.clock = clock;
    }

    /** true = izin verildi (ve sayıldı); false = tavan dolu, istek reddedilmeli. */
    public boolean izinVer(String ip) {
        String anahtar = (ip == null || ip.isBlank()) ? "bilinmiyor" : ip;
        Instant simdi = clock.instant();
        Instant pencereBasi = simdi.minus(PENCERE);

        Deque<Instant> gecmis = istekler.computeIfAbsent(anahtar, k -> new ArrayDeque<>());
        synchronized (gecmis) {
            while (!gecmis.isEmpty() && gecmis.peekFirst().isBefore(pencereBasi)) {
                gecmis.pollFirst();
            }
            if (gecmis.size() >= saatlikTavan) {
                return false;
            }
            gecmis.addLast(simdi);
            return true;
        }
    }
}
