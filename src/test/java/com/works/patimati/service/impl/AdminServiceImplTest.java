package com.works.patimati.service.impl;

import com.works.patimati.entity.Ad;
import com.works.patimati.entity.AdComplaint;
import com.works.patimati.entity.AdoptionComplaint;
import com.works.patimati.entity.PotentialMatch;
import com.works.patimati.entity.User;
import com.works.patimati.entity.UserComplaint;
import com.works.patimati.entity.external.ExternalPetRecord;
import com.works.patimati.entity.external.ExternalSourceMedia;
import com.works.patimati.entity.external.ExternalSourcePost;
import com.works.patimati.entity.enums.ComplaintReason;
import com.works.patimati.dto.admin.AdComplaintAdminResponse;
import com.works.patimati.dto.admin.AdoptionComplaintAdminResponse;
import com.works.patimati.dto.admin.ExternalPostAdminResponse;
import com.works.patimati.dto.admin.UserComplaintAdminResponse;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.AdComplaintRepository;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.AdoptionComplaintRepository;
import com.works.patimati.repository.PotentialMatchRepository;
import com.works.patimati.repository.UserComplaintRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.repository.external.ExternalPetRecordRepository;
import com.works.patimati.repository.external.ExternalSourceMediaRepository;
import com.works.patimati.repository.external.ExternalSourcePostRepository;
import com.works.patimati.service.AdService;
import com.works.patimati.service.ReverseGeocodingService;
import com.works.patimati.storage.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AdminServiceImplTest {

    private UserRepository userRepository;
    private AdRepository adRepository;
    private AdComplaintRepository adComplaintRepository;
    private UserComplaintRepository userComplaintRepository;
    private AdoptionComplaintRepository adoptionComplaintRepository;
    private AdService adService;
    private ExternalSourcePostRepository externalSourcePostRepository;
    private ExternalPetRecordRepository externalPetRecordRepository;
    private ExternalSourceMediaRepository externalSourceMediaRepository;
    private PotentialMatchRepository potentialMatchRepository;
    private ImageStorageService imageStorageService;
    private ReverseGeocodingService reverseGeocodingService;
    private AdminServiceImpl adminService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        adRepository = mock(AdRepository.class);
        adComplaintRepository = mock(AdComplaintRepository.class);
        userComplaintRepository = mock(UserComplaintRepository.class);
        adoptionComplaintRepository = mock(AdoptionComplaintRepository.class);
        adService = mock(AdService.class);
        externalSourcePostRepository = mock(ExternalSourcePostRepository.class);
        externalPetRecordRepository = mock(ExternalPetRecordRepository.class);
        externalSourceMediaRepository = mock(ExternalSourceMediaRepository.class);
        potentialMatchRepository = mock(PotentialMatchRepository.class);
        imageStorageService = mock(ImageStorageService.class);

        reverseGeocodingService = mock(ReverseGeocodingService.class);

        adminService = new AdminServiceImpl(
                userRepository,
                adRepository,
                adComplaintRepository,
                userComplaintRepository,
                adoptionComplaintRepository,
                adService,
                externalSourcePostRepository,
                externalPetRecordRepository,
                externalSourceMediaRepository,
                potentialMatchRepository,
                imageStorageService,
                reverseGeocodingService
        );
    }

    @Test
    void suspendAd_ShouldSetSuspendedTrueAndActiveFalse() {
        Ad ad = Ad.builder()
                .id(1L)
                .active(true)
                .suspended(false)
                .build();

        when(adRepository.findById(1L)).thenReturn(Optional.of(ad));
        when(adRepository.save(any(Ad.class))).thenAnswer(invocation -> invocation.getArgument(0));

        adminService.suspendAd(1L);

        assertThat(ad.isSuspended()).isTrue();
        assertThat(ad.isActive()).isFalse();
        verify(adRepository).save(ad);
    }

    @Test
    void unhideAd_ShouldSetSuspendedFalseAndActiveTrue() {
        Ad ad = Ad.builder()
                .id(1L)
                .active(false)
                .suspended(true)
                .build();

        when(adRepository.findById(1L)).thenReturn(Optional.of(ad));
        when(adRepository.save(any(Ad.class))).thenAnswer(invocation -> invocation.getArgument(0));

        adminService.unhideAd(1L);

        assertThat(ad.isSuspended()).isFalse();
        assertThat(ad.isActive()).isTrue();
        verify(adRepository).save(ad);
    }

    @Test
    void suspendAd_ShouldThrowResourceNotFoundException_WhenAdNotFound() {
        when(adRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.suspendAd(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ------------------------------------------------------------------
    // N+1 düzeltmeleri: DTO içeriği AYNI kalmalı, ama artık findAllById ile
    // TEK sorguda -- findById/findByPost(...) satır başına HİÇ çağrılmamalı.
    // ------------------------------------------------------------------

    private User kullanici(long uid, String ad, String soyad, String email) {
        return User.builder().uid(uid).firstName(ad).lastName(soyad).email(email).build();
    }

    @Test
    void getAdComplaints_findAllByIdIleTopluCeker_findByIdHicCagrilmaz() {
        Ad ad1 = Ad.builder().id(10L).title("Kayıp Kedi").user(kullanici(100L, "Sahip", "Bir", "sahip1@test.com")).build();
        Ad ad2 = Ad.builder().id(20L).title("Bulunan Köpek").user(kullanici(200L, "Sahip", "İki", "sahip2@test.com")).build();
        List<AdComplaint> sikayetler = List.of(
                AdComplaint.builder().id(1L).reporterId(1L).adId(10L).reason(ComplaintReason.SAHTE_ILAN).build(),
                AdComplaint.builder().id(2L).reporterId(2L).adId(20L).reason(ComplaintReason.DIGER).build()
        );
        Pageable pageable = PageRequest.of(0, 20);
        when(adComplaintRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable))).thenReturn(new PageImpl<>(sikayetler, pageable, 2));
        when(userRepository.findAllById(any())).thenReturn(List.of(
                kullanici(1L, "Şikayetçi", "Bir", "sik1@test.com"),
                kullanici(2L, "Şikayetçi", "İki", "sik2@test.com")
        ));
        when(adRepository.findAllById(any())).thenReturn(List.of(ad1, ad2));

        Page<AdComplaintAdminResponse> sonuc = adminService.getAdComplaints(null, pageable);

        assertThat(sonuc.getContent()).hasSize(2);
        AdComplaintAdminResponse ilk = sonuc.getContent().get(0);
        assertThat(ilk.adTitle()).isEqualTo("Kayıp Kedi");
        assertThat(ilk.adOwnerId()).isEqualTo(100L);
        assertThat(ilk.reporterFullName()).isEqualTo("Şikayetçi Bir");

        verify(userRepository, times(1)).findAllById(any());
        verify(adRepository, times(1)).findAllById(any());
        verify(userRepository, never()).findById(any());
        verify(adRepository, never()).findById(any());
    }

    @Test
    void getUserComplaints_findAllByIdIleTopluCeker_findByIdHicCagrilmaz() {
        List<UserComplaint> sikayetler = List.of(
                UserComplaint.builder().id(1L).reporterId(1L).reportedUserId(2L).reason(ComplaintReason.KOTU_DIL_KULLANIMI).build(),
                UserComplaint.builder().id(2L).reporterId(3L).reportedUserId(1L).reason(ComplaintReason.DIGER).build()
        );
        Pageable pageable = PageRequest.of(0, 20);
        when(userComplaintRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable))).thenReturn(new PageImpl<>(sikayetler, pageable, 2));
        when(userRepository.findAllById(any())).thenReturn(List.of(
                kullanici(1L, "Bir", "Kullanıcı", "u1@test.com"),
                kullanici(2L, "İki", "Kullanıcı", "u2@test.com"),
                kullanici(3L, "Üç", "Kullanıcı", "u3@test.com")
        ));

        Page<UserComplaintAdminResponse> sonuc = adminService.getUserComplaints(null, pageable);

        assertThat(sonuc.getContent()).hasSize(2);
        assertThat(sonuc.getContent().get(0).reporterFullName()).isEqualTo("Bir Kullanıcı");
        assertThat(sonuc.getContent().get(0).reportedUserFullName()).isEqualTo("İki Kullanıcı");

        // Bir kullanıcının hem reporter hem reported olarak birden fazla kez
        // geçtiği durumlarda bile TEK çağrı olmalı (distinct edilmiş id listesi).
        verify(userRepository, times(1)).findAllById(any());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void getAdoptionComplaints_findAllByIdIleTopluCeker_findByIdHicCagrilmaz() {
        Ad ad1 = Ad.builder().id(10L).title("Sahiplendirme İlanı").user(kullanici(100L, "Sahip", "Bir", "sahip1@test.com")).build();
        List<AdoptionComplaint> sikayetler = List.of(
                AdoptionComplaint.builder().id(1L).reporterId(1L).adId(10L).reason(ComplaintReason.UYGUNSUZ_ICERIK).build()
        );
        Pageable pageable = PageRequest.of(0, 20);
        when(adoptionComplaintRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable))).thenReturn(new PageImpl<>(sikayetler, pageable, 1));
        when(userRepository.findAllById(any())).thenReturn(List.of(kullanici(1L, "Şikayetçi", "Bir", "sik1@test.com")));
        when(adRepository.findAllById(any())).thenReturn(List.of(ad1));

        Page<AdoptionComplaintAdminResponse> sonuc = adminService.getAdoptionComplaints(null, pageable);

        assertThat(sonuc.getContent()).hasSize(1);
        assertThat(sonuc.getContent().get(0).adTitle()).isEqualTo("Sahiplendirme İlanı");

        verify(userRepository, times(1)).findAllById(any());
        verify(adRepository, times(1)).findAllById(any());
        verify(userRepository, never()).findById(any());
        verify(adRepository, never()).findById(any());
    }

    @Test
    void getExternalPosts_topluSorgularKullanir_satirBasinaSorguHicCagrilmaz() {
        ExternalSourcePost post1 = ExternalSourcePost.builder().id(1L).sourcePostId("A1").build();
        ExternalSourcePost post2 = ExternalSourcePost.builder().id(2L).sourcePostId("A2").build();
        ExternalPetRecord record1 = ExternalPetRecord.builder().id(50L).post(post1).petIndex((short) 0).species("cat").build();
        ExternalSourceMedia media1 = ExternalSourceMedia.builder().post(post1).ordinal((short) 0).storageKey("external/a1-0.jpg").build();
        ExternalSourceMedia media2 = ExternalSourceMedia.builder().post(post2).ordinal((short) 0).storageKey("external/a2-0.jpg").build();
        PotentialMatch eslesme = mock(PotentialMatch.class);
        when(eslesme.getExternalRecord()).thenReturn(record1);

        Pageable pageable = PageRequest.of(0, 20);
        when(externalSourcePostRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(post1, post2), pageable, 2));
        // post2 için pet-record YOK -- toplu sorgu yalnızca post1'i döner.
        when(externalPetRecordRepository.findByPostInAndPetIndex(any(), eq((short) 0)))
                .thenReturn(List.of(record1));
        when(externalSourceMediaRepository.findByPostInOrderByOrdinalAsc(any()))
                .thenReturn(List.of(media1, media2));
        when(potentialMatchRepository.findByCandidateKindAndExternalRecordIn(any(), any()))
                .thenReturn(List.of(eslesme));
        when(imageStorageService.createTemporaryReadUrl(any())).thenAnswer(inv -> "https://cdn.test/" + inv.getArgument(0));

        Page<ExternalPostAdminResponse> sonuc = adminService.getExternalPosts(pageable);

        assertThat(sonuc.getContent()).hasSize(2);
        ExternalPostAdminResponse r1 = sonuc.getContent().stream().filter(r -> r.id().equals(1L)).findFirst().orElseThrow();
        ExternalPostAdminResponse r2 = sonuc.getContent().stream().filter(r -> r.id().equals(2L)).findFirst().orElseThrow();
        assertThat(r1.species()).isEqualTo("cat");
        assertThat(r1.hasMatch()).isTrue();
        assertThat(r1.photoUrl()).isEqualTo("https://cdn.test/external/a1-0.jpg");
        // post2'nin pet-record'u yok -- eşleşme de yok, ama medyası (dolayısıyla fotoğrafı) var.
        assertThat(r2.species()).isNull();
        assertThat(r2.hasMatch()).isFalse();
        assertThat(r2.photoUrl()).isEqualTo("https://cdn.test/external/a2-0.jpg");

        verify(externalPetRecordRepository, times(1)).findByPostInAndPetIndex(any(), eq((short) 0));
        verify(externalSourceMediaRepository, times(1)).findByPostInOrderByOrdinalAsc(any());
        verify(potentialMatchRepository, times(1)).findByCandidateKindAndExternalRecordIn(any(), any());
        verify(externalPetRecordRepository, never()).findByPostAndPetIndex(any(), anyShort());
        verify(externalSourceMediaRepository, never()).findByPostOrderByOrdinalAsc(any());
        verify(potentialMatchRepository, never()).existsByCandidateKindAndExternalRecord(any(), any());
    }
}
