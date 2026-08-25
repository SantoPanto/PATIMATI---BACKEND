package com.works.patimati.controller;

import com.works.patimati.security.PetAnalysisRateLimiter;
import com.works.patimati.service.AiMatchService;
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
 * <p><b>Yalnızca girişli kullanıcılar:</b> {@code /api/public/**} altında,
 * {@code SecurityConfig}'te {@code permitAll} olduğu için istek buraya
 * misafirken de ulaşır -- ama giriş zorunluluğu burada, uygulama seviyesinde
 * denetlenir (bkz. {@link #analyze}): misafir isteği 401 ile reddedilir.
 * Her istek gerçek para maliyeti taşıyan bir LLM çağrısı tetiklediği için
 * girişli kullanıcı da {@link PetAnalysisRateLimiter} ile günde 3 istekle
 * sınırlanır (adminler hariç).
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
            Authentication authentication
    ) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Analiz edilecek fotoğraf gerekli."));
        }

        boolean girisYapmis = authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());

        // Bu özellik yalnızca kayıtlı kullanıcılara açık -- /api/public/**
        // altında permitAll olsa da (bkz. sınıf javadoc'u) misafir isteği
        // burada, uygulama seviyesinde reddedilir.
        if (!girisYapmis) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Bu özelliği kullanmak için giriş yapmalısınız."));
        }

        // Adminler sınırsız -- AdService.isCurrentUserAdmin() ile AYNI kontrol
        // deseni (ROLE_ADMIN/ADMIN authority taraması). Rate limiter'a hiç
        // uğranmıyor: ne sayaç kontrolü ne artırma yapılır, kullanıcı kararı.
        boolean adminMi = authentication.getAuthorities() != null
                && authentication.getAuthorities().stream()
                        .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ADMIN".equals(a.getAuthority()));

        // JwtAuthFilter'ın SecurityContext'e koyduğu principal (e-posta) --
        // kullanıcı ID'si DEĞİL, ama hesap başına biriciktir, aynı amaca
        // hizmet eder.
        String anahtar = "user:" + authentication.getName();

        if (!adminMi && rateLimiter.limitiDoldu(anahtar)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Günlük analiz hakkınız doldu. Yarın tekrar deneyebilirsiniz."));
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
