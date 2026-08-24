package com.works.patimati.service;

import com.works.patimati.dto.ad.AdResponse;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.AdResolutionStatus;
import com.works.patimati.mapper.AdMapper;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.storage.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * "İlanlarım > Tümü" gerçekten TÜM ilanları döndürüyor mu?
 *
 * <p><b>Ölçülen kusur (21.08, canlı):</b> kullanıcı ilanını "bulundu" diye
 * kapattıktan sonra ilan "Tümü" sekmesinde <b>kayboluyordu</b>; yalnız
 * "Yayından Kaldırılan" sekmesinde görünüyordu. Sebep: uçtaki
 * {@code @RequestParam(defaultValue = "true") boolean active}. Ön yüz "Tümü"
 * sekmesinde alanı hiç göndermiyor, sunucu da sessizce {@code true} varsayıp
 * <i>yalnız yayındakileri</i> döndürüyordu.
 *
 * <p><b>Neden servis katmanında ölçülüyor:</b> kusur "hangi repository
 * çağrısının seçildiği" kararında. Uç testi bunu göremez (aynı JSON döner),
 * repository testi de göremez (iki metot da doğru çalışıyor). Yanlış olan
 * seçimin kendisiydi.
 */
@ExtendWith(MockitoExtension.class)
class IlanlarimTumuTest {

    @Mock
    private AdRepository adRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdMapper adMapper;

    @Mock
    private ImageStorageService imageStorageService;

    @InjectMocks
    private AdService adService;

    private User sahip;
    private Ad kapanmisIlan;

    @BeforeEach
    void setUp() {
        sahip = User.builder().uid(1L).email("sahip@patimati.me").build();

        // Bulundu diye kapanmış ilan: active=false AMA resolutionStatus=FOUND.
        // "Sahibi kaldırdı" ile karışmaması gereken durum tam bu.
        kapanmisIlan = Ad.builder()
                .id(100L)
                .user(sahip)
                .title("Kayıp tekir")
                .adType(Ad.AdType.LOST)
                .active(false)
                .resolutionStatus(AdResolutionStatus.FOUND)
                .build();
    }

    /** Sahip aramasi yalnizca servis yolundan gecen vakalarda gerekiyor. */
    private void sahibiTanit() {
        when(userRepository.findByEmail("sahip@patimati.me"))
                .thenReturn(Optional.of(sahip));
    }

    @Test
    @DisplayName("active NULL ise aktiflik suzgeci HIC uygulanmaz")
    void tumuSuzgecsizSorar() {
        sahibiTanit();
        Pageable sayfa = PageRequest.of(0, 20);
        when(adRepository.findAllByUser_Uid(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(kapanmisIlan)));

        Page<AdResponse> sonuc = adService.getUserAds("sahip@patimati.me", null, sayfa);

        assertThat(sonuc.getContent()).hasSize(1);
        // İkizi: süzgeçli metoda düşülürse kapanmış ilan listeden düşer.
        verify(adRepository, never())
                .findAllByUser_UidAndActive(any(), anyBoolean(), any(Pageable.class));
    }

    @Test
    @DisplayName("active verilirse suzgecli sorgu kullanilir")
    void suzgecliSorar() {
        sahibiTanit();
        Pageable sayfa = PageRequest.of(0, 20);
        when(adRepository.findAllByUser_UidAndActive(eq(1L), eq(true), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        adService.getUserAds("sahip@patimati.me", true, sayfa);

        verify(adRepository, never()).findAllByUser_Uid(any(), any(Pageable.class));
    }

    @Test
    @DisplayName("AdResponse ilanin NASIL kapandigini tasir")
    void cozumDurumuTelegeCikar() {
        // Mapper'ın kendisi ölçülüyor: alan sözleşmede yoksa arayüz
        // "yayından kaldırıldı" ile "bulundu"yu ayırt edemez.
        AdMapper gercekMapper = new AdMapper();
        AdResponse cevap = gercekMapper.toResponse(kapanmisIlan, List.of());

        assertThat(cevap.resolutionStatus()).isEqualTo(AdResolutionStatus.FOUND);
        assertThat(cevap.active()).isFalse();
    }
}
