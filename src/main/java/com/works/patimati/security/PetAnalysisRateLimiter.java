package com.works.patimati.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * "Ben Neyim?" (pet raporu) özelliği için istek sayacı -- {@link LoginRateLimiter}
 * ile AYNI desen (bellek-içi, {@code @Scheduled} temizlik), ama iki farkla:
 *
 * <ol>
 *   <li>Anahtar giriş durumuna göre değişir: misafirse istemci IP'si, giriş
 *       yapmışsa kullanıcının e-postası (bkz. çağıran, {@code
 *       PublicPetAnalysisController} -- {@code AdController.getAdPoster}'daki
 *       "authentication != null && isAuthenticated() && !anonymousUser"
 *       deseniyle AYNI). Aynı ağdaki iki farklı kullanıcı birbirinin hakkını
 *       yemesin diye.</li>
 *   <li>Limit sabit değil, giriş durumuna göre İKİ farklı tavan var (misafir
 *       çok daha düşük -- her istek gerçek para maliyeti taşıyan bir LLM
 *       çağrısı tetikliyor).</li>
 * </ol>
 *
 * Başarılı/başarısız ayrımı YOK (LoginRateLimiter'daki recordFailure/
 * recordSuccess'in aksine): buradaki maliyet İSTEĞİN KENDİSİ (AI servisine
 * giden her çağrı), sonucun "gecerli" olup olmaması değil -- o yüzden tek bir
 * {@link #istekKaydet(String)} yeterli.
 */
@Component
public class PetAnalysisRateLimiter {

    @Value("${pet.analysis.rate-limit.guest-max-requests:3}")
    private int misafirMaxIstek;

    @Value("${pet.analysis.rate-limit.user-max-requests:10}")
    private int kullaniciMaxIstek;

    // Varsayılan 1440 dk = 24 saat ("günde" -- kullanıcı kararı).
    @Value("${pet.analysis.rate-limit.window-minutes:1440}")
    private long pencereSuresiDakika;

    private final Map<String, Window> denemeler = new ConcurrentHashMap<>();

    private record Window(AtomicInteger sayac, Instant baslangic) {
    }

    /**
     * true ise bu anahtar (IP ya da kullanıcı) şu an limit dolu -- çağıran
     * isteği AI servisine hiç göndermemeli.
     *
     * @param girisYapmis hangi tavanın uygulanacağını belirler (misafir/kullanıcı)
     */
    public boolean limitiDoldu(String anahtar, boolean girisYapmis) {
        int maxIstek = girisYapmis ? kullaniciMaxIstek : misafirMaxIstek;
        Window pencere = denemeler.get(anahtar);
        if (pencere == null) {
            return false;
        }
        if (pencereSuresiDoldu(pencere)) {
            denemeler.remove(anahtar, pencere);
            return false;
        }
        return pencere.sayac().get() >= maxIstek;
    }

    /** Bu anahtarın sayacını bir artırır -- çağrılmadan önce {@link #limitiDoldu} ile kontrol edilmiş olmalı. */
    public void istekKaydet(String anahtar) {
        denemeler.compute(anahtar, (k, mevcut) -> {
            if (mevcut == null || pencereSuresiDoldu(mevcut)) {
                return new Window(new AtomicInteger(1), Instant.now());
            }
            mevcut.sayac().incrementAndGet();
            return mevcut;
        });
    }

    private boolean pencereSuresiDoldu(Window pencere) {
        return Duration.between(pencere.baslangic(), Instant.now()).toMinutes() >= pencereSuresiDakika;
    }

    /** Süresi dolmuş kayıtları temizler -- yoksa hiç dönmeyen IP/kullanıcılar haritada sonsuza dek kalır. */
    @Scheduled(fixedDelayString = "${pet.analysis.rate-limit.cleanup-interval-minutes:60}", timeUnit = TimeUnit.MINUTES)
    void suresiDolanlariTemizle() {
        denemeler.entrySet().removeIf(entry -> pencereSuresiDoldu(entry.getValue()));
    }
}
