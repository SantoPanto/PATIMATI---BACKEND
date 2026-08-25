package com.works.patimati.controller;

import com.works.patimati.security.PetAnalysisRateLimiter;
import com.works.patimati.service.AiMatchService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * "Ben Neyim?" -- tek bir kedi/köpek fotoğrafından zengin bir pet raporu.
 *
 * <p><b>Girişsiz kullanılabilir (A1'in TERSİ, bilinçli):</b> {@code /api/public/**}
 * altında, {@code SecurityConfig}'te {@code permitAll}. {@link AiAnalyzeController}
 * (ilan oluşturma analizi) girişi ZORUNLU tutar çünkü AI'yı internete açık
 * bırakmamak için tek koruma oydu (bkz. o sınıfın javadoc'u) -- burada aynı
 * korumayı GİRİŞ ZORUNLULUĞU değil, {@link PetAnalysisRateLimiter} sağlıyor:
 * her istek gerçek para maliyeti taşıyan bir LLM çağrısı tetiklediği için
 * kimliksiz erişim, sınırsız erişim ANLAMINA GELMEMELİ.
 *
 * <p><b>Giriş isteğe bağlı, hatası yok:</b> {@code Authentication} parametresi
 * yalnızca (varsa) daha yüksek bir istek tavanı uygulamak için okunur --
 * {@code AdController.getAdPoster}'daki "opsiyonel kimlik" deseninin AYNISI
 * (bkz. oradaki "authentication != null && isAuthenticated() &&
 * !anonymousUser" kontrolü). Oturum yokluğu asla hata değildir, misafir
 * sayılır.
 *
 * <p>Cevap AI'dan geldiği gibi aktarılır -- {@link AiAnalyzeController} ile
 * AYNI gerekçe (alanları burada yeniden tanımlamak, AI'ya eklenen her yeni
 * alanın sessizce düşmesi demek olurdu).
 */
@RestController
@RequestMapping("/api/public/pet-analiz")
@RequiredArgsConstructor
public class PublicPetAnalysisController {

    private static final Logger log = LoggerFactory.getLogger(PublicPetAnalysisController.class);
    private static final String AI_ULASILAMIYOR = "AI servisi geçici olarak hizmet veremiyor.";

    private final AiMatchService aiMatchService;
    private final PetAnalysisRateLimiter rateLimiter;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> analyze(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "kullanici_notu", required = false) String kullaniciNotu,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Analiz edilecek fotoğraf gerekli."));
        }

        boolean girisYapmis = authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());

        // Adminler sınırsız -- AdService.isCurrentUserAdmin() ile AYNI kontrol
        // deseni (ROLE_ADMIN/ADMIN authority taraması). Rate limiter'a hiç
        // uğranmıyor: ne sayaç kontrolü ne artırma yapılır, kullanıcı kararı.
        boolean adminMi = girisYapmis && authentication.getAuthorities() != null
                && authentication.getAuthorities().stream()
                        .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ADMIN".equals(a.getAuthority()));

        // Giriş yapmışsa e-posta (JwtAuthFilter'ın SecurityContext'e koyduğu
        // principal -- kullanıcı ID'si DEĞİL, ama hesap başına biriciktir,
        // aynı amaca hizmet eder), misafirse IP anahtar olur. IP BİLEREK
        // getRemoteAddr() ile okunuyor, X-Forwarded-For elle ayrıştırılmıyor
        // -- UserController.login()'daki AYNI gerekçe: başlık istemci
        // tarafından uydurulabilir, elle okumak sınırlamayı tamamen
        // atlatılabilir kılardı. application.yml'deki
        // server.forward-headers-strategy: native bu işi güvenilir biçimde
        // gömülü Tomcat'e yaptırıyor.
        String anahtar = girisYapmis
                ? "user:" + authentication.getName()
                : "ip:" + httpRequest.getRemoteAddr();

        if (!adminMi && rateLimiter.limitiDoldu(anahtar, girisYapmis)) {
            String mesaj = girisYapmis
                    ? "Günlük analiz hakkınız doldu. Yarın tekrar deneyebilirsiniz."
                    : "Günlük deneme hakkınız doldu. Giriş yaparak daha fazla analiz yapabilirsiniz.";
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of("message", mesaj));
        }
        if (!adminMi) {
            rateLimiter.istekKaydet(anahtar);
        }

        try {
            Map<String, Object> sonuc = aiMatchService.analyzePet(file, kullaniciNotu);
            if (sonuc == null) {
                return ResponseEntity.status(502).body(Map.of("message", AI_ULASILAMIYOR));
            }
            return ResponseEntity.ok(sonuc);
        } catch (Exception e) {
            // İç ayrıntı istemciye GİTMEZ -- AiAnalyzeController ile aynı ilke.
            log.warn("Pet raporu analizi başarısız", e);
            return ResponseEntity.status(502).body(Map.of("message", AI_ULASILAMIYOR));
        }
    }
}
