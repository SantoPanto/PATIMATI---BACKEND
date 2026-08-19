package com.works.patimati.controller;

import com.works.patimati.service.AiMatchService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * İlan oluşturma ekranının fotoğraf analizi — <b>AI'ya backend üzerinden</b>.
 *
 * <p><b>Neden bu uç var (A1):</b> tarayıcı bugüne kadar AI servisini
 * <b>doğrudan</b> çağırıyordu ({@code AddListingPage.tsx}, adres kodun içine
 * gömülüydü). İki sonucu vardı:
 * <ol>
 *   <li>AI servisi <b>internete açık olmak zorundaydı</b> ve uçlarına kimlik
 *       konulamıyordu — konsaydı ilan oluşturma ekranı kırılırdı. Sözleşme §10
 *       ise AI'nın iç ağda kalmasını, açılacaksa paylaşılan anahtar istemesini
 *       söylüyor.</li>
 *   <li>Analiz isteği <b>kimliksizdi</b>: giriş yapmamış biri de modeli
 *       çalıştırabiliyordu.</li>
 * </ol>
 * Bu uç ikisini birden kapatır: istek JWT ister ({@code SecurityConfig}'te
 * {@code permitAll} listesinde <b>yok</b>, {@code anyRequest().authenticated()}
 * yakalar) ve AI'ya yalnız backend gider.
 *
 * <p><b>Cevap AI'dan geldiği gibi aktarılır.</b> Alanları burada yeniden
 * tanımlamak, AI'ya eklenen her yeni alanın sessizce düşmesi demek olurdu;
 * ekran da alan alan kırılırdı. AI'nın {@code /analyze} cevabı zaten dışarıya
 * gösterilmek üzere tanımlanmış temiz bir sözleşmedir (iç bilgi taşımaz).
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiAnalyzeController {

    private static final Logger log = LoggerFactory.getLogger(AiAnalyzeController.class);

    private final AiMatchService aiMatchService;

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> analyze(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Analiz edilecek fotoğraf gerekli."));
        }

        try {
            Map<String, Object> sonuc = aiMatchService.analyzeImage(file);
            if (sonuc == null) {
                return ResponseEntity.status(502).body(Map.of("message", AI_ULASILAMIYOR));
            }
            return ResponseEntity.ok(sonuc);
        } catch (Exception e) {
            // İç ayrıntı istemciye GİTMEZ: yığın izi, adres ve sürüm bilgisi
            // sızdırırdı. Günlüğe tam hâliyle yazılır, cevaba sabit metin gider.
            log.warn("AI fotoğraf analizi başarısız", e);
            return ResponseEntity.status(502).body(Map.of("message", AI_ULASILAMIYOR));
        }
    }

    private static final String AI_ULASILAMIYOR = "AI servisi geçici olarak hizmet veremiyor.";
}
