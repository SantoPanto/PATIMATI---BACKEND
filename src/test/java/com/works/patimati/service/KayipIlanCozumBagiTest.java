package com.works.patimati.service;

import com.works.patimati.ai.AiAnalysisPublisher;
import com.works.patimati.dto.ad.ResolveLostAdRequest;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.AdResolutionStatus;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.mapper.AdMapper;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.storage.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Kayıp ilan kapanırken <b>hangi ilanla eşleştiği</b> kaydedilmeli.
 *
 * <p><b>Neden var:</b> kapanışta yalnız {@code resolutionStatus = FOUND}
 * yazılıyordu. İsteğe gelen {@code finderId} ödül puanı verilip <b>atılıyordu</b>
 * ve eşleşen ilanın kimliği hiç sorulmuyordu. Sonuç: sistemde <i>"şu kayıp ilan
 * şu bulundu ilanıyla eşleşti"</i> bilgisi <b>hiç yoktu.</b>
 *
 * <p>Bunun ölçülen bedeli: eşleşme skorundaki konum kanalının ağırlığı (0.15)
 * tartışılıyor ama karar verilemiyor, çünkü <i>gerçekte eşleşen bir çiftin
 * arası kaç km</i> sorusu cevapsız. Elimizdeki ölçümde pozitif ve negatif
 * çiftlere <b>aynı</b> mesafe verilmek zorunda kalındı, dolayısıyla konumun
 * <b>ayırt etme</b> değeri ölçülemedi. Bu alanlar dolmaya başlayınca soru
 * ölçümle cevaplanabilir.
 *
 * <p>Bu yüzden bekçi "alan yazıldı mı" diye bakıyor: alan <b>sessizce</b>
 * düştüğünde hiçbir şey kırılmaz, yalnızca veri birikmez — ve bu, aylar sonra
 * ölçüm yapılmak istendiğinde fark edilir.
 */
class KayipIlanCozumBagiTest {

    private static final String SAHIP_EPOSTA = "sahip@patimati.local";
    private static final long SAHIP_UID = 7L;
    private static final long KAYIP_ILAN_ID = 55L;
    private static final long BULUNDU_ILAN_ID = 91L;
    private static final long BULAN_UID = 42L;

    private AdRepository adRepository;
    private AdService adService;
    private Ad kayipIlan;

    @BeforeEach
    void hazirla() {
        adRepository = mock(AdRepository.class);
        UserRepository userRepository = mock(UserRepository.class);

        adService = new AdService(
                adRepository,
                userRepository,
                mock(AdMapper.class),
                mock(ImageStorageService.class),
                mock(AiAnalysisPublisher.class),
                mock(RewardService.class)
        );

        User sahip = User.builder().uid(SAHIP_UID).email(SAHIP_EPOSTA).build();
        when(userRepository.findByEmail(SAHIP_EPOSTA)).thenReturn(Optional.of(sahip));
        when(adRepository.saveAndFlush(any(Ad.class))).thenAnswer(c -> c.getArgument(0));

        kayipIlan = ilan(KAYIP_ILAN_ID, Ad.AdType.LOST, sahip);
        when(adRepository.findByIdAndUser_UidAndActiveTrue(KAYIP_ILAN_ID, SAHIP_UID))
                .thenReturn(Optional.of(kayipIlan));
    }

    @Test
    @DisplayName("Eşleşen ilan bildirilirse İLANA YAZILIR — ölçüm bunu okuyacak")
    void eslesenIlanKimligiKaydedilmeli() {
        when(adRepository.findById(BULUNDU_ILAN_ID))
                .thenReturn(Optional.of(ilan(BULUNDU_ILAN_ID, Ad.AdType.FOUND, null)));

        adService.resolveLostAd(SAHIP_EPOSTA, KAYIP_ILAN_ID,
                new ResolveLostAdRequest(BULAN_UID, BULUNDU_ILAN_ID));

        Ad yazilan = yazilanIlan();
        assertThat(yazilan.getResolvedByAdId())
                .withFailMessage("""
                        Eşleşen ilanın kimliği kaydedilmedi. Alan sessizce
                        düştüğünde hiçbir şey kırılmaz, yalnızca veri birikmez —
                        ve bu, aylar sonra eşleşme eşiği ölçülmek istendiğinde
                        fark edilir.""")
                .isEqualTo(BULUNDU_ILAN_ID);
        assertThat(yazilan.getResolutionStatus()).isEqualTo(AdResolutionStatus.FOUND);
        assertThat(yazilan.isActive()).isFalse();
    }

    @Test
    @DisplayName("Bulan kullanıcı da İLANA yazılır — eskiden puan verilip atılıyordu")
    void bulanKullaniciKaydedilmeli() {
        when(adRepository.findById(BULUNDU_ILAN_ID))
                .thenReturn(Optional.of(ilan(BULUNDU_ILAN_ID, Ad.AdType.FOUND, null)));

        adService.resolveLostAd(SAHIP_EPOSTA, KAYIP_ILAN_ID,
                new ResolveLostAdRequest(BULAN_UID, BULUNDU_ILAN_ID));

        assertThat(yazilanIlan().getFinderUserId())
                .withFailMessage("""
                        finderId yine atılıyor. Ödül puanı veriliyor ama "kim
                        buldu" sorusu sonradan hâlâ cevaplanamaz.""")
                .isEqualTo(BULAN_UID);
    }

    @Test
    @DisplayName("Eşleşen ilan BİLDİRİLMEZSE kapanış yine çalışır — alanlar isteğe bağlı")
    void eslesenIlansizKapanisCalismali() {
        adService.resolveLostAd(SAHIP_EPOSTA, KAYIP_ILAN_ID,
                new ResolveLostAdRequest(null, null));

        Ad yazilan = yazilanIlan();
        // Eski istemciler yalnız finderId gönderiyordu; sözleşmeyi bozmamalıyız.
        assertThat(yazilan.getResolutionStatus()).isEqualTo(AdResolutionStatus.FOUND);
        assertThat(yazilan.getResolvedByAdId()).isNull();
    }

    @Test
    @DisplayName("İlan KENDİSİYLE eşleştirilemez")
    void kendisiyleEslesmeReddedilmeli() {
        assertThatThrownBy(() -> adService.resolveLostAd(SAHIP_EPOSTA, KAYIP_ILAN_ID,
                new ResolveLostAdRequest(BULAN_UID, KAYIP_ILAN_ID)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("kendisiyle");
    }

    @Test
    @DisplayName("Var olmayan ilan bildirilirse SESSİZCE yutulmaz")
    void varOlmayanIlanReddedilmeli() {
        when(adRepository.findById(BULUNDU_ILAN_ID)).thenReturn(Optional.empty());

        // Sessizce null bırakmak, ölçüm sorgusunda "bağ kurulmamış" ile
        // "yanlış id gönderilmiş" durumlarını ayırt edilemez yapardı.
        assertThatThrownBy(() -> adService.resolveLostAd(SAHIP_EPOSTA, KAYIP_ILAN_ID,
                new ResolveLostAdRequest(BULAN_UID, BULUNDU_ILAN_ID)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Eşleşen ilan 'bulundu' türünde değilse reddedilir")
    void yanlisTurdekiIlanReddedilmeli() {
        when(adRepository.findById(BULUNDU_ILAN_ID))
                .thenReturn(Optional.of(ilan(BULUNDU_ILAN_ID, Ad.AdType.ADOPTION, null)));

        assertThatThrownBy(() -> adService.resolveLostAd(SAHIP_EPOSTA, KAYIP_ILAN_ID,
                new ResolveLostAdRequest(BULAN_UID, BULUNDU_ILAN_ID)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("bulundu");
    }

    // ---------------------------------------------------------------------

    private Ad yazilanIlan() {
        ArgumentCaptor<Ad> yakalayici = ArgumentCaptor.forClass(Ad.class);
        verify(adRepository).saveAndFlush(yakalayici.capture());
        return yakalayici.getValue();
    }

    private Ad ilan(long id, Ad.AdType tur, User sahip) {
        return Ad.builder()
                .id(id)
                .adType(tur)
                .user(sahip)
                .active(true)
                .build();
    }
}
