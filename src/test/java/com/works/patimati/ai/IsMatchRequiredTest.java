package com.works.patimati.ai;

import com.works.patimati.ai.dto.AiAnalysisRequest;
import com.works.patimati.ai.dto.AiAnalysisResult;
import com.works.patimati.dto.ad.AdCreateRequest;
import com.works.patimati.dto.ad.AdoptionAdCreateRequest;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.enums.AdResolutionStatus;
import com.works.patimati.entity.enums.PetColor;
import com.works.patimati.entity.enums.Species;
import com.works.patimati.external.ExternalMatchingService;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.external.ExternalPetRecordRepository;
import com.works.patimati.repository.external.ExternalSourceMediaRepository;
import com.works.patimati.repository.external.ExternalSourcePostRepository;
import com.works.patimati.service.PotentialMatchService;
import com.works.patimati.storage.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class IsMatchRequiredTest {

    private RabbitTemplate aiRabbitTemplate;
    private AdRepository adRepository;
    private ImageStorageService imageStorageService;
    private AiMatchNotifier matchNotifier;
    private AiAnalysisPublisher publisher;
    private AiAnalysisListener listener;

    @BeforeEach
    void setUp() {
        aiRabbitTemplate = mock(RabbitTemplate.class);
        adRepository = mock(AdRepository.class);
        imageStorageService = mock(ImageStorageService.class);
        matchNotifier = mock(AiMatchNotifier.class);

        publisher = new AiAnalysisPublisher(aiRabbitTemplate, adRepository, imageStorageService, mock(MatchCandidateGatherer.class));
        listener = new AiAnalysisListener(
                adRepository,
                mock(ExternalPetRecordRepository.class),
                mock(ExternalSourcePostRepository.class),
                mock(ExternalSourceMediaRepository.class),
                mock(PotentialMatchService.class),
                mock(ExternalMatchingService.class),
                matchNotifier);
    }

    @Test
    @DisplayName("AdCreateRequest null isMatchRequired geldiğinde varsayılan olarak TRUE kabul etmeli")
    void adCreateRequestDefaultsToTrue() {
        AdCreateRequest request = new AdCreateRequest(
                "Kayıp Kedi", "Açıklama", Ad.AdType.LOST, Species.CAT, "Tekir",
                Set.of(PetColor.BLACK), null, null, null, null, null, null,
                null, null, null, null, "2026-08-22", null,
                new BigDecimal("41.0"), new BigDecimal("29.0"), null, null, null, null
        );

        assertThat(request.isMatchRequired()).isTrue();
    }

    @Test
    @DisplayName("AdoptionAdCreateRequest null isMatchRequired geldiğinde varsayılan olarak FALSE kabul etmeli")
    void adoptionAdCreateRequestDefaultsToFalse() {
        AdoptionAdCreateRequest request = new AdoptionAdCreateRequest(
                "Sahiplendirilecek Kedi", "Açıklama", Species.CAT, "Tekir",
                null, null, Set.of(), null, null, null,
                "2026-08-22", new BigDecimal("41.0"), new BigDecimal("29.0"),
                null, null, null, null
        );

        assertThat(request.isMatchRequired()).isFalse();
    }

    @Test
    @DisplayName("isMatchRequired false ise RabbitMQ mesajına isMatchRequired=false olarak gitmeli ve aday toplanmamalı")
    void publisherBypassesCandidatesWhenMatchRequiredFalse() {
        Ad ad = Ad.builder()
                .id(100L)
                .adType(Ad.AdType.ADOPTION)
                .species(Species.CAT)
                .photoUrls(List.of("ads/1.jpg"))
                .isMatchRequired(false)
                .build();

        when(imageStorageService.createTemporaryReadUrl(any())).thenReturn("https://s3.example.com/1.jpg");

        publisher.publish(ad);

        ArgumentCaptor<AiAnalysisRequest> captor = ArgumentCaptor.forClass(AiAnalysisRequest.class);
        verify(aiRabbitTemplate).convertAndSend(
                eq(AiRabbitConfig.EXCHANGE),
                eq(AiRabbitConfig.REQUEST_ROUTING_KEY),
                captor.capture(),
                (org.springframework.amqp.core.MessagePostProcessor) any()
        );

        AiAnalysisRequest request = captor.getValue();
        assertThat(request.isMatchRequired()).isFalse();
        assertThat(request.candidates()).isEmpty();
    }

    @Test
    @DisplayName("isMatchRequired false olan ilanın AI sonucu geldiğinde veritabanına embedding/vektör yazılmamalı ve eşleşme bildirimi atlanmalı")
    void listenerBypassesEmbeddingsAndNotifierWhenMatchRequiredFalse() {
        Ad ad = Ad.builder()
                .id(200L)
                .title("Sahiplendirme İlanı")
                .adType(Ad.AdType.ADOPTION)
                .species(Species.CAT)
                .photoUrls(List.of("ads/cat.jpg"))
                .isMatchRequired(false)
                .build();

        when(adRepository.findById(200L)).thenReturn(Optional.of(ad));

        float[] sampleEmbedding = new float[]{0.1f, 0.2f, 0.3f};
        AiAnalysisResult.Analysis analysis = new AiAnalysisResult.Analysis(
                List.of(sampleEmbedding),
                "cat",
                0.95,
                true,
                "Tekir",
                0.90,
                "striped",
                List.of(),
                List.of("cat")
        );

        AiAnalysisResult result = new AiAnalysisResult(
                1,
                "req-1",
                200L,
                "ok",
                "siglip2",
                analysis,
                List.of(),
                0.8,
                null,
                null,
                List.of(),
                Instant.now(),
                null,
                null
        );

        listener.onResult(result);

        assertThat(ad.getAiEmbeddings()).isNull();
        assertThat(ad.getAiSpecies()).isEqualTo("cat");
        verify(matchNotifier, never()).recordAndNotify(any(), any(), anyDouble());
        verify(adRepository).save(ad);
    }
}
