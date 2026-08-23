package com.works.patimati.service;

import com.works.patimati.dto.match.AdMatchResponseDTO;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.AdMatch;
import com.works.patimati.entity.User;
import com.works.patimati.repository.AdMatchRepository;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.storage.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Eşleşme listesinin çift tekilleştirmesi.
 *
 * <p>Yeniden analiz aynı ilan çiftini <b>ters yönde ikinci bir satırla</b>
 * yazabilir (AdMatchRepository sınıf notundaki koşul: "satır tek yönde
 * yazılır" varsayımı yalnız ilan bir kez analiz edildiği sürece geçerli).
 * İki satır da aynı kullanıcıya ait olduğundan {@code getUserMatches} ikisini
 * birden döndürür ve Eşleşmelerim aynı çifti iki kez gösterirdi. Bu test o
 * durumu kurar ve listenin çifti BİR kez, en yüksek skorlu satırıyla
 * gösterdiğini doğrular.
 */
class AdMatchServiceTest {

    private AdMatchRepository adMatchRepository;
    private UserRepository userRepository;
    private AdMatchService adMatchService;

    private User kullanici;
    private Ad kayipIlan;
    private Ad bulunduIlan;

    @BeforeEach
    void setUp() {
        adMatchRepository = mock(AdMatchRepository.class);
        userRepository = mock(UserRepository.class);
        adMatchService = new AdMatchService(
                adMatchRepository,
                mock(AdRepository.class),
                userRepository,
                mock(ImageStorageService.class)
        );

        kullanici = User.builder().uid(42L).email("owner@patimati.com").build();
        kayipIlan = Ad.builder().id(25L).user(kullanici).adType(Ad.AdType.LOST).build();
        bulunduIlan = Ad.builder().id(26L).user(kullanici).adType(Ad.AdType.FOUND).build();

        when(userRepository.findByEmail(kullanici.getEmail()))
                .thenReturn(Optional.of(kullanici));
    }

    private AdMatch satir(Long id, Ad source, Ad matched, double skor) {
        return AdMatch.builder()
                .id(id)
                .user(kullanici)
                .sourceAd(source)
                .matchedAd(matched)
                .totalScore(skor)
                .passedThreshold(skor >= 0.8)
                .build();
    }

    @Test
    @DisplayName("Aynı çiftin ters yönlü ikinci satırı listede tekrar görünmez")
    void shouldCollapseReversedDuplicatePairIntoSingleEntry() {
        // Yeniden analiz senaryosu: önce 26→25 yazılmış (skor 0.86), sonra
        // 25 yeniden analiz edilip 25→26 yazılmış (skor 0.84). Liste skora
        // göre sıralı gelir; yüksek olan kazanmalı, düşük olan elenmeli.
        AdMatch ilkYon = satir(1L, bulunduIlan, kayipIlan, 0.86);
        AdMatch tersYon = satir(2L, kayipIlan, bulunduIlan, 0.84);

        when(adMatchRepository.findByUserIdOrderByTotalScoreDesc(kullanici.getUid()))
                .thenReturn(List.of(ilkYon, tersYon));

        List<AdMatchResponseDTO> liste = adMatchService.getUserMatches(kullanici.getEmail());

        assertThat(liste).hasSize(1);
        assertThat(liste.get(0).getTotalScore()).isEqualTo(0.86);
    }

    @Test
    @DisplayName("Farklı çiftler tekilleştirmeden etkilenmez")
    void shouldKeepDistinctPairsUntouched() {
        Ad baskaIlan = Ad.builder().id(29L).user(kullanici).adType(Ad.AdType.FOUND).build();
        AdMatch cift1 = satir(1L, bulunduIlan, kayipIlan, 0.86);
        AdMatch cift2 = satir(2L, baskaIlan, kayipIlan, 0.78);

        when(adMatchRepository.findByUserIdOrderByTotalScoreDesc(kullanici.getUid()))
                .thenReturn(List.of(cift1, cift2));

        List<AdMatchResponseDTO> liste = adMatchService.getUserMatches(kullanici.getEmail());

        assertThat(liste).hasSize(2);
    }
}
