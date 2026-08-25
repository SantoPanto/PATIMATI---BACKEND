package com.works.patimati.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@code /api/auth/login} için IP başına bellek-içi deneme sayacı (rapor
 * bulgusu: brute-force koruması yoktu).
 *
 * <p><b>Bilinçli olarak IP bazlı, e-posta/hesap bazlı DEĞİL.</b> Hesap bazlı
 * kilitleme (ör. "bu e-posta 5 kez yanlış denendi, 15 dk kilitli") kendi
 * başına bir DoS aracı hâline gelir: saldırgan, kurbanın gerçek şifresini
 * bilmeden yalnızca e-postasını bilerek o hesabı sürekli kilitli tutabilir.
 * IP bazlı sayaç bu riski taşımaz -- farklı bir IP'den aynı hesaba giriş
 * her zaman normal çalışır.
 *
 * <p><b>Bellek-içi, kalıcı değil.</b> Uygulama yeniden başlarsa sayaçlar
 * sıfırlanır -- kabul edilebilir bir ödün: hedef "sonsuz otomatik deneme"yi
 * engellemek, adli bir kayıt tutmak değil. Kalıcı/dağıtık bir çözüm (Redis
 * vb.) yalnızca birden fazla instance arkasında paylaşımlı sayaç gerekirse
 * gerekli olur -- bu projede şu an öyle bir gereksinim yok.
 */
@Component
public class LoginRateLimiter {

    @Value("${auth.login.rate-limit.max-attempts:5}")
    private int maxAttempts;

    @Value("${auth.login.rate-limit.window-minutes:15}")
    private long windowMinutes;

    private final Map<String, Window> denemeler = new ConcurrentHashMap<>();

    private record Window(AtomicInteger sayac, Instant baslangic) {
    }

    /** true ise bu IP şu an engelli -- çağıran istekle hiç ilgilenmemeli. */
    public boolean isBlocked(String clientIp) {
        Window pencere = denemeler.get(clientIp);
        if (pencere == null) {
            return false;
        }
        if (pencereSuresiDoldu(pencere)) {
            denemeler.remove(clientIp, pencere);
            return false;
        }
        return pencere.sayac().get() >= maxAttempts;
    }

    /** Başarısız bir deneme (yanlış şifre, bilinmeyen e-posta, askıya alınmış hesap) sayaca eklenir. */
    public void recordFailure(String clientIp) {
        denemeler.compute(clientIp, (ip, mevcut) -> {
            if (mevcut == null || pencereSuresiDoldu(mevcut)) {
                return new Window(new AtomicInteger(1), Instant.now());
            }
            mevcut.sayac().incrementAndGet();
            return mevcut;
        });
    }

    /** Başarılı girişte bu IP'nin sayacı sıfırlanır -- meşru kullanıcı bir daha ket vurulmasın. */
    public void recordSuccess(String clientIp) {
        denemeler.remove(clientIp);
    }

    private boolean pencereSuresiDoldu(Window pencere) {
        return Duration.between(pencere.baslangic(), Instant.now()).toMinutes() >= windowMinutes;
    }

    /** Süresi dolmuş kayıtları temizler -- yoksa hiç dönmeyen/tekrar denemeyen IP'ler haritada sonsuza dek kalır. */
    @Scheduled(fixedDelayString = "${auth.login.rate-limit.cleanup-interval-minutes:30}", timeUnit = java.util.concurrent.TimeUnit.MINUTES)
    void suresiDolanlariTemizle() {
        denemeler.entrySet().removeIf(entry -> pencereSuresiDoldu(entry.getValue()));
    }
}
