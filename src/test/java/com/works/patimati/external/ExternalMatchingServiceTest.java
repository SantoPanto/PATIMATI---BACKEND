package com.works.patimati.external;

import com.works.patimati.ai.AiMatchClient;
import com.works.patimati.ai.MatchCandidateGatherer;
import com.works.patimati.ai.dto.AiAnalysisResult;
import com.works.patimati.ai.dto.AiMatchResponse;
import com.works.patimati.entity.enums.ExternalProcessingStatus;
import com.works.patimati.entity.external.ExternalPetRecord;
import com.works.patimati.entity.external.ExternalSourcePost;
import com.works.patimati.repository.external.ExternalSourcePostRepository;
import com.works.patimati.service.PotentialMatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pure Mockito — {@code AiMatchClient} gerçek bir AI servisine bağlanmaz
 * (bu ortamda çalışan bir Python servisi yok), bu yüzden başarı/başarısızlık
 * senaryoları taklit edilir. Asıl doğrulanan şey: Aşama 2 hatası Aşama 1
 * sonucunu ASLA kaybettirmemeli.
 */
class ExternalMatchingServiceTest {

    private MatchCandidateGatherer candidateGatherer;
    private AiMatchClient matchClient;
    private PotentialMatchService potentialMatchService;
    private ExternalSourcePostRepository postRepository;
    private ExternalMatchingService service;

    private ExternalSourcePost post;
    private ExternalPetRecord record;

    @BeforeEach
    void setUp() {
        candidateGatherer = mock(MatchCandidateGatherer.class);
        matchClient = mock(AiMatchClient.class);
        potentialMatchService = mock(PotentialMatchService.class);
        postRepository = mock(ExternalSourcePostRepository.class);

        service = new ExternalMatchingService(candidateGatherer, matchClient, potentialMatchService, postRepository);

        post = ExternalSourcePost.builder().id(1L).build();
        record = ExternalPetRecord.builder().id(10L).post(post).species("cat").build();

        when(candidateGatherer.findCandidatesForExternalRecord(record)).thenReturn(List.of());
        when(postRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void aiServiceFailureLeavesRecordAnalyzedNotLost() {
        when(matchClient.match(any())).thenThrow(new RuntimeException("connection refused"));

        service.attemptMatching(record);

        ArgumentCaptor<ExternalSourcePost> captor = ArgumentCaptor.forClass(ExternalSourcePost.class);
        verify(postRepository, atLeastOnce()).save(captor.capture());
        assertThat(captor.getValue().getProcessingStatus())
                .withFailMessage("AI servisi hatası Aşama 1 sonucunu ANALYZED'den başka bir yere düşürmemeli")
                .isEqualTo(ExternalProcessingStatus.ANALYZED);
        verify(potentialMatchService, never())
                .recordExternalMatch(any(), any(), anyFloat(), anyFloat(), anyFloat(), anyFloat(), any());
    }

    @Test
    void timeoutAlsoLeavesRecordAnalyzedForLaterRetry() {
        when(matchClient.match(any())).thenThrow(new org.springframework.web.client.ResourceAccessException("timeout"));

        service.attemptMatching(record);

        ArgumentCaptor<ExternalSourcePost> captor = ArgumentCaptor.forClass(ExternalSourcePost.class);
        verify(postRepository, atLeastOnce()).save(captor.capture());
        assertThat(captor.getValue().getProcessingStatus()).isEqualTo(ExternalProcessingStatus.ANALYZED);
    }

    @Test
    void successfulMatchIsRecordedAndPostCompletes() {
        AiAnalysisResult.Match match = new AiAnalysisResult.Match(99L, 0.85, 0.8, 0.7, 0.6, true, 0, 0, null);
        when(matchClient.match(any())).thenReturn(new AiMatchResponse(List.of(match), null, "test/v1"));

        service.attemptMatching(record);

        verify(potentialMatchService).recordExternalMatch(99L, 10L, 0.8f, 0.7f, 0.6f, 0.85f, "test/v1");

        ArgumentCaptor<ExternalSourcePost> captor = ArgumentCaptor.forClass(ExternalSourcePost.class);
        verify(postRepository, atLeastOnce()).save(captor.capture());
        assertThat(captor.getValue().getProcessingStatus()).isEqualTo(ExternalProcessingStatus.COMPLETED);
    }

    @Test
    void nonMatchingResultsAreNotRecorded() {
        AiAnalysisResult.Match match = new AiAnalysisResult.Match(99L, 0.2, 0.1, 0.1, 0.1, false, null, null, null);
        when(matchClient.match(any())).thenReturn(new AiMatchResponse(List.of(match), null, "test/v1"));

        service.attemptMatching(record);

        verify(potentialMatchService, never())
                .recordExternalMatch(any(), any(), anyFloat(), anyFloat(), anyFloat(), anyFloat(), any());
    }

    @Test
    void matchesAgainstExternalCandidatesAreIgnoredHere() {
        // external_record_id dolu (native adı yok) -- Stage 2 yalnızca
        // native-ad karşılığı olan sonuçları kaydeder; external<->external
        // hiçbir zaman oluşmamalı (kapsam dışı, blueprint §31).
        AiAnalysisResult.Match match = new AiAnalysisResult.Match(null, 0.9, 0.9, 0.9, 0.9, true, null, null, 55L);
        when(matchClient.match(any())).thenReturn(new AiMatchResponse(List.of(match), null, "test/v1"));

        service.attemptMatching(record);

        verify(potentialMatchService, never())
                .recordExternalMatch(any(), any(), anyFloat(), anyFloat(), anyFloat(), anyFloat(), any());
    }
}
