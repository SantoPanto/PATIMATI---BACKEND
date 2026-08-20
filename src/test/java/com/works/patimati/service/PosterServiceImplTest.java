package com.works.patimati.service;

import com.works.patimati.entity.Ad;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.AgeGroup;
import com.works.patimati.entity.enums.PetGender;
import com.works.patimati.entity.enums.Species;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.service.impl.PosterServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PosterServiceImplTest {

    @Mock
    private AdRepository adRepository;

    @InjectMocks
    private PosterServiceImpl posterService;

    private Ad testAd;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .uid(1L)
                .firstName("Ali")
                .lastName("Yilmaz")
                .email("ali@example.com")
                .phone("05551112233")
                .build();

        testAd = Ad.builder()
                .id(100L)
                .title("Kayip Tekir Kedi")
                .description("Kadikoy taraflarinda kayboldu")
                .adType(Ad.AdType.LOST)
                .species(Species.CAT)
                .breed("Tekir")
                .gender(PetGender.MALE)
                .ageGroup(AgeGroup.ADULT)
                .user(user)
                .active(true)
                .isPosterAllowed(true)
                .showEmailOnPoster(true)
                .showPhoneOnPoster(true)
                .build();
    }

    @Test
    void generateAdPosterPdf_ShouldReturnNonEmptyByteArray_WhenAdExistsAndPosterAllowed() {
        when(adRepository.findById(100L)).thenReturn(Optional.of(testAd));

        byte[] pdfBytes = posterService.generateAdPosterPdf(100L, "ali@example.com");

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        verify(adRepository, times(1)).findById(100L);
    }

    @Test
    void generateAdPosterPdf_ShouldThrowAccessDeniedException_WhenNotOwnerAndPosterNotAllowed() {
        testAd.setIsPosterAllowed(false);
        when(adRepository.findById(100L)).thenReturn(Optional.of(testAd));

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> posterService.generateAdPosterPdf(100L, "baskasi@example.com"));
    }

    @Test
    void generateAdPosterPdf_ShouldAllowOwner_EvenIfPosterNotAllowed() {
        testAd.setIsPosterAllowed(false);
        when(adRepository.findById(100L)).thenReturn(Optional.of(testAd));

        byte[] pdfBytes = posterService.generateAdPosterPdf(100L, "ali@example.com");

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void generateAdPosterPdf_ShouldThrowResourceNotFoundException_WhenAdDoesNotExist() {
        when(adRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> posterService.generateAdPosterPdf(999L));
        verify(adRepository, times(1)).findById(999L);
    }
}
