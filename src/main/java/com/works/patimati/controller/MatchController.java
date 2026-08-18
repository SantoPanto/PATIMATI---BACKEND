package com.works.patimati.controller;

import com.works.patimati.dto.match.AdMatchResponseDTO;
import com.works.patimati.service.AdMatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AI eşleşme sonuçlarını sunan REST Kontrolcü sınıfı.
 *
 * <p>Frontend tarafının eşleşme verilerini sorgulaması için gerekli API uç noktalarını sunar.
 */
@Validated
@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final AdMatchService adMatchService;

    /**
     * Oturum açmış kullanıcının ilanlarına ait AI eşleştirme sonuçlarını getirir.
     *
     * <p>Eşleşme sonuçları toplam skora göre azalan (en yüksek skorlu eşleşme ilk sırada)
     * şekilde sıralanır ve güvenlik gereği varlık (Entity) yerine {@link AdMatchResponseDTO}
     * yapısında servis edilir.
     *
     * @param authentication Oturum açan kullanıcının doğrulama bilgisi
     * @return Kullanıcıya ait eşleşme yanıt listesi
     */
    @GetMapping("/my-matches")
    public ResponseEntity<List<AdMatchResponseDTO>> getMyMatches(Authentication authentication) {
        List<AdMatchResponseDTO> matches = adMatchService.getUserMatches(authentication.getName());
        return ResponseEntity.ok(matches);
    }
}
