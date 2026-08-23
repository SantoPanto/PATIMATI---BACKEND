package com.works.patimati.service;

import com.works.patimati.ai.AiAnalysisPublisher;
import com.works.patimati.dto.ad.AdResponse;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.User;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.mapper.AdMapper;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.storage.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import com.works.patimati.service.NotificationService;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Yayından kaldırılmış ilan sahibi tarafından yeniden yayına alınabilmeli.
 *
 * <p>NEDEN VAR: ilan yaşam döngüsü tek yönlüydü. Kullanıcı ilanını yayından
 * kaldırabiliyor ama geri alamıyordu; kayıp ilanındaki yazım hatası ya da
 * değişen bilgi kalıcı olarak yanlış kalıyordu. Mevcut güncelleme ucu bunu
 * çözemiyordu, İKİ sebepten: {@code AdUpdateRequest} içinde {@code active}
 * alanı yok, üstelik {@code findActiveOwnedAd} yalnız AKTİF ilanı buluyor —
 * yani pasif ilana hiçbir yoldan dokunulamıyordu.
 *
 * <p>Testlerin en kritiği {@link #askiya_alinmis_ilan_yeniden_yayinlanamiyor()}:
 * {@code suspended} alanını yalnız YÖNETİCİ değiştiriyor. Sahibin bu işlemle
 * yönetici kararını geçersiz kılabilmesi yetki aşımı olurdu — yani bu uç,
 * yanlış yazılırsa moderasyonu delen bir kapı hâline gelir.
 */
class IlanYenidenYayinlamaTest {

    private static final String SAHIP_EPOSTA = "sahip@patimati.local";
    private static final long SAHIP_UID = 7L;
    private static final long ILAN_ID = 55L;

    private AdRepository adRepository;
    private UserRepository userRepository;
    private AdMapper adMapper;
    private AdService adService;

    private User sahip;

    @BeforeEach
    void hazirla() {
        adRepository = mock(AdRepository.class);
        userRepository = mock(UserRepository.class);
        adMapper = mock(AdMapper.class);

        adService = new AdService(
                adRepository,
                userRepository,
                adMapper,
                mock(ImageStorageService.class),
                mock(AiAnalysisPublisher.class),
                mock(RewardService.class),
                mock(NotificationService.class),
                mock(ReverseGeocodingService.class),
                mock(com.works.patimati.notification.NearbyAlertNotifier.class)
        );

        sahip = User.builder().uid(SAHIP_UID).email(SAHIP_EPOSTA).build();
        when(userRepository.findByEmail(SAHIP_EPOSTA)).thenReturn(Optional.of(sahip));
        when(adMapper.toResponse(any(Ad.class), anyList())).thenReturn(mock(AdResponse.class));
        when(adRepository.saveAndFlush(any(Ad.class))).thenAnswer(c -> c.getArgument(0));
    }

    private Ad ilan(boolean aktif, boolean askida) {
        return Ad.builder()
                .id(ILAN_ID)
                .user(sahip)
                .active(aktif)
                .suspended(askida)
                .build();
    }

    @Test
    void yayindan_kaldirilmis_ilan_geri_yayina_aliniyor() {
        Ad pasif = ilan(false, false);
        when(adRepository.findByIdAndUser_Uid(ILAN_ID, SAHIP_UID))
                .thenReturn(Optional.of(pasif));

        adService.republishAd(SAHIP_EPOSTA, ILAN_ID);

        assertThat(pasif.isActive())
                .as("maddenin 'bitince' ölçütü: yayından kaldırılan ilan tek tıkla geri gelmeli")
                .isTrue();
        verify(adRepository).saveAndFlush(pasif);
    }

    @Test
    void askiya_alinmis_ilan_yeniden_yayinlanamiyor() {
        Ad askida = ilan(false, true);
        when(adRepository.findByIdAndUser_Uid(ILAN_ID, SAHIP_UID))
                .thenReturn(Optional.of(askida));

        assertThatThrownBy(() -> adService.republishAd(SAHIP_EPOSTA, ILAN_ID))
                .as("""
                        suspended alanını yalnız yönetici değiştiriyor \
                        (AdminServiceImpl). Sahibin bu uçla yönetici kararını \
                        geçersiz kılabilmesi yetki aşımı olurdu.""")
                .isInstanceOf(AccessDeniedException.class);

        assertThat(askida.isActive())
                .as("hata fırlatılsa bile ilan yayına ALINMAMIŞ olmalı")
                .isFalse();
        verify(adRepository, never()).saveAndFlush(any(Ad.class));
    }

    @Test
    void zaten_yayinda_olan_ilan_hata_vermiyor() {
        Ad aktif = ilan(true, false);
        when(adRepository.findByIdAndUser_Uid(ILAN_ID, SAHIP_UID))
                .thenReturn(Optional.of(aktif));

        adService.republishAd(SAHIP_EPOSTA, ILAN_ID);

        assertThat(aktif.isActive()).isTrue();
        verify(adRepository, never()).saveAndFlush(any(Ad.class));
    }

    @Test
    void baskasinin_ilani_yeniden_yayinlanamiyor() {
        // Sorgu sahiplik şartını kendi içinde taşıyor: başkasının ilanı için
        // boş döner. Servis bunu 404'e çevirmeli, sessizce geçmemeli.
        when(adRepository.findByIdAndUser_Uid(ILAN_ID, SAHIP_UID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> adService.republishAd(SAHIP_EPOSTA, ILAN_ID))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(adRepository, never()).saveAndFlush(any(Ad.class));
    }

    @Test
    void aktiflik_sarti_ARAMAYAN_sorgu_kullaniliyor() {
        // Bu testin tek işi: doğru depo metodunun çağrıldığını sabitlemek.
        // findByIdAndUser_UidAndActiveTrue kullanılsaydı pasif ilan HİÇ
        // bulunamaz, özellik sessizce çalışmaz hâle gelirdi — üstelik diğer
        // testler mock'landığı için bunu göremezdi.
        Ad pasif = ilan(false, false);
        when(adRepository.findByIdAndUser_Uid(ILAN_ID, SAHIP_UID))
                .thenReturn(Optional.of(pasif));

        adService.republishAd(SAHIP_EPOSTA, ILAN_ID);

        verify(adRepository).findByIdAndUser_Uid(ILAN_ID, SAHIP_UID);
        verify(adRepository, never()).findByIdAndUser_UidAndActiveTrue(ILAN_ID, SAHIP_UID);
    }
}
