package com.works.patimati.service.impl;

import com.works.patimati.entity.Ad;
import com.works.patimati.entity.AdInstagramPublication;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.InstagramPublishStatus;
import com.works.patimati.entity.enums.Species;
import com.works.patimati.instagram.InstagramCaptionAiClient;
import com.works.patimati.instagram.InstagramGraphClient;
import com.works.patimati.instagram.InstagramGraphResult;
import com.works.patimati.repository.AdInstagramPublicationRepository;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.storage.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InstagramPublishServiceImplTest {

    private AdInstagramPublicationRepository publicationRepository;
    private AdRepository adRepository;
    private UserRepository userRepository;
    private ImageStorageService imageStorageService;
    private InstagramCaptionAiClient instagramCaptionAiClient;
    private InstagramGraphClient instagramGraphClient;
    private InstagramPublishServiceImpl service;

    @BeforeEach
    void setUp() {
        publicationRepository = mock(AdInstagramPublicationRepository.class);
        adRepository = mock(AdRepository.class);
        userRepository = mock(UserRepository.class);
        imageStorageService = mock(ImageStorageService.class);
        instagramCaptionAiClient = mock(InstagramCaptionAiClient.class);
        instagramGraphClient = mock(InstagramGraphClient.class);

        service = new InstagramPublishServiceImpl(
                publicationRepository, adRepository, userRepository, imageStorageService,
                instagramCaptionAiClient, instagramGraphClient);
        setEnabled(true);
    }

    private void setEnabled(boolean enabled) {
        try {
            var field = InstagramPublishServiceImpl.class.getDeclaredField("publishEnabled");
            field.setAccessible(true);
            field.set(service, enabled);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private Ad ilan(boolean consent, Ad.AdType type) {
        return Ad.builder()
                .id(1L)
                .adType(type)
                .species(Species.CAT)
                .breed("MIXED_OR_UNKNOWN")
                .photoUrls(List.of("s3://kova/ads/1.jpg"))
                .instagramShareConsent(consent)
                .build();
    }

    @Test
    void ozellikKapaliysaKuyruaHicEklemez() {
        setEnabled(false);
        service.queueForReview(ilan(true, Ad.AdType.LOST));
        verify(publicationRepository, never()).save(any());
    }

    @Test
    void izinVerilmemisIlanKuyruaEklenmez() {
        service.queueForReview(ilan(false, Ad.AdType.LOST));
        verify(publicationRepository, never()).save(any());
    }

    @Test
    void izinVerilmisIlanPendingOlarakKuyruaEklenir() {
        when(instagramCaptionAiClient.generateCaption(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn("AI'nin urettigi caption");

        service.queueForReview(ilan(true, Ad.AdType.LOST));

        ArgumentCaptor<AdInstagramPublication> captor = ArgumentCaptor.forClass(AdInstagramPublication.class);
        verify(publicationRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(InstagramPublishStatus.PENDING);
        assertThat(captor.getValue().getSuggestedCaption()).isEqualTo("AI'nin urettigi caption");
    }

    @Test
    void aiBosDonerseSablonCaptionaDuser() {
        when(instagramCaptionAiClient.generateCaption(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(null);

        service.queueForReview(ilan(true, Ad.AdType.FOUND));

        ArgumentCaptor<AdInstagramPublication> captor = ArgumentCaptor.forClass(AdInstagramPublication.class);
        verify(publicationRepository).save(captor.capture());
        assertThat(captor.getValue().getSuggestedCaption()).contains("Bulundu ilanı").contains("#patimati");
    }

    @Test
    void aiIstisnaFirlatirsaKuyrugaEklemeYineDeBasarili() {
        when(instagramCaptionAiClient.generateCaption(any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("ai servisi cevap vermedi"));

        service.queueForReview(ilan(true, Ad.AdType.ADOPTION));

        verify(publicationRepository).save(any());
    }

    @Test
    void yayinlamaBasarilOlursaPublishedOlarakKaydedilirVeTrueDoner() {
        Ad ad = ilan(true, Ad.AdType.LOST);
        AdInstagramPublication publication = AdInstagramPublication.builder()
                .id(5L).adId(ad.getId()).status(InstagramPublishStatus.PENDING).build();
        when(publicationRepository.findById(5L)).thenReturn(Optional.of(publication));
        when(adRepository.findById(ad.getId())).thenReturn(Optional.of(ad));
        when(imageStorageService.createTemporaryReadUrl(anyString())).thenReturn("https://cdn.test/1.jpg");
        when(instagramGraphClient.publish(any(), anyString()))
                .thenReturn(InstagramGraphResult.success("ig123", "https://instagram.com/p/abc"));
        when(userRepository.findByEmail("admin@patimati.me"))
                .thenReturn(Optional.of(User.builder().uid(9L).email("admin@patimati.me").build()));

        boolean sonuc = service.publish(5L, "final caption", "admin@patimati.me");

        assertThat(sonuc).isTrue();
        assertThat(publication.getStatus()).isEqualTo(InstagramPublishStatus.PUBLISHED);
        assertThat(publication.getIgMediaId()).isEqualTo("ig123");
        assertThat(publication.getPublishedByAdminId()).isEqualTo(9L);
    }

    @Test
    void yayinlamaBasarisizOlursaFailedOlarakKaydedilirVeFalseDoner() {
        Ad ad = ilan(true, Ad.AdType.LOST);
        AdInstagramPublication publication = AdInstagramPublication.builder()
                .id(6L).adId(ad.getId()).status(InstagramPublishStatus.PENDING).build();
        when(publicationRepository.findById(6L)).thenReturn(Optional.of(publication));
        when(adRepository.findById(ad.getId())).thenReturn(Optional.of(ad));
        when(imageStorageService.createTemporaryReadUrl(anyString())).thenReturn("https://cdn.test/1.jpg");
        when(instagramGraphClient.publish(any(), anyString()))
                .thenReturn(InstagramGraphResult.failure("Instagram API hatası"));

        boolean sonuc = service.publish(6L, "final caption", "admin@patimati.me");

        assertThat(sonuc).isFalse();
        assertThat(publication.getStatus()).isEqualTo(InstagramPublishStatus.FAILED);
        assertThat(publication.getFailureReason()).isEqualTo("Instagram API hatası");
    }

    @Test
    void atlamaSkippedOlarakKaydedilir() {
        Ad ad = ilan(true, Ad.AdType.LOST);
        AdInstagramPublication publication = AdInstagramPublication.builder()
                .id(7L).adId(ad.getId()).status(InstagramPublishStatus.PENDING).build();
        when(publicationRepository.findById(7L)).thenReturn(Optional.of(publication));

        service.skip(7L, "admin@patimati.me");

        assertThat(publication.getStatus()).isEqualTo(InstagramPublishStatus.SKIPPED);
    }
}
