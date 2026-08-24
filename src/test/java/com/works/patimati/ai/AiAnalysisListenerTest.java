package com.works.patimati.ai;

import com.works.patimati.ai.dto.AiAnalysisResult;
import com.works.patimati.entity.enums.ExternalProcessingStatus;
import com.works.patimati.entity.external.ExternalPetRecord;
import com.works.patimati.entity.external.ExternalSourcePost;
import com.works.patimati.external.ExternalMatchingService;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.external.ExternalPetRecordRepository;
import com.works.patimati.repository.external.ExternalSourceMediaRepository;
import com.works.patimati.repository.external.ExternalSourcePostRepository;
import com.works.patimati.service.PotentialMatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Pure Mockito — kategori kapısının ({@code ExternalMatchingService}'i ne
 * zaman çağırıp çağırmadığı) davranışını doğrular. Gerçek DB gerektirmez;
 * {@code AdServiceTest} ile aynı taklit-tabanlı üslup.
 */
class AiAnalysisListenerTest {

    private AdRepository adRepository;
    private ExternalPetRecordRepository externalPetRecordRepository;
    private ExternalSourcePostRepository externalSourcePostRepository;
    private ExternalSourceMediaRepository externalSourceMediaRepository;
    private PotentialMatchService potentialMatchService;
    private ExternalMatchingService externalMatchingService;
    private AiAnalysisListener listener;

    private ExternalSourcePost post;
    private ExternalPetRecord record;

    @BeforeEach
    void setUp() {
        adRepository = mock(AdRepository.class);
        externalPetRecordRepository = mock(ExternalPetRecordRepository.class);
        externalSourcePostRepository = mock(ExternalSourcePostRepository.class);
        externalSourceMediaRepository = mock(ExternalSourceMediaRepository.class);
        potentialMatchService = mock(PotentialMatchService.class);
        externalMatchingService = mock(ExternalMatchingService.class);

        listener = new AiAnalysisListener(
                adRepository, externalPetRecordRepository, externalSourcePostRepository,
                externalSourceMediaRepository, potentialMatchService, externalMatchingService);

        post = ExternalSourcePost.builder().id(1L).build();
        record = ExternalPetRecord.builder().id(10L).post(post).build();

        when(externalPetRecordRepository.findById(10L)).thenReturn(Optional.of(record));
        when(externalPetRecordRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(externalSourcePostRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(externalSourceMediaRepository.findByPostOrderByOrdinalAsc(post)).thenReturn(List.of());
    }

    @ParameterizedTest(name = "category={0} never reaches Stage 2")
    @ValueSource(strings = {"IRRELEVANT"})
    void incompatibleCategoryNeverTriggersStage2(String category) {
        listener.onResult(resultWithCategory(category, false));

        verifyNoInteractions(externalMatchingService);
        assertThat(record.getCategory().name()).isEqualTo(category);

        ArgumentCaptor<ExternalSourcePost> captor = ArgumentCaptor.forClass(ExternalSourcePost.class);
        verify(externalSourcePostRepository).save(captor.capture());
        assertThat(captor.getValue().getProcessingStatus()).isEqualTo(ExternalProcessingStatus.COMPLETED);
    }

    @Test
    void adoptionCategoryNowTriggersStage2() {
        // DÜZELTME (2026-08-19, kullanıcı raporu #2): "yuva arıyoruz" diye
        // paylaşılan bir Instagram gönderisi (ADOPTION), başka birinin LOST
        // ilanındaki hayvanıyla aynı olabilir -- canlı bir örnekle doğrulandı
        // (aynı kedi, mazlum kayıp ilanı). Eskiden ADOPTION hiçbir zaman
        // eşleştirmeye girmiyordu (yalnızca LOST/FOUND kategori-uyumlu
        // sayılıyordu); artık ADOPTION da (needsReview=false ise) tetikler.
        // Native ADOPTION ilanları bu değişiklikten ETKİLENMEZ -- ayrı bir
        // kod yolu (bkz. MatchCandidateGatherer.compatibleAdTypesForCategory'nin
        // NOT'u).
        listener.onResult(resultWithCategory("ADOPTION", false));

        verify(externalMatchingService, times(1)).attemptMatching(record);
        assertThat(record.getCategory().name()).isEqualTo("ADOPTION");

        ArgumentCaptor<ExternalSourcePost> captor = ArgumentCaptor.forClass(ExternalSourcePost.class);
        verify(externalSourcePostRepository).save(captor.capture());
        assertThat(captor.getValue().getProcessingStatus()).isEqualTo(ExternalProcessingStatus.ANALYZED);
    }

    @Test
    void uncertainCategoryWithRealAnimalPhotoStillTriggersStage2AsSafetyNet() {
        // DÜZELTME (2026-08-19, kullanıcı raporu): metin analizi kategoriyi
        // (afiş/poster fotoğrafındaki yazı gibi caption/yorumda olmayan bir
        // sebeple) UNCERTAIN bıraksa bile, fotoğrafta gerçek bir hayvan
        // görüldüyse (aiIsPet=true) artık Aşama 2 YİNE tetiklenir --
        // MatchCandidateGatherer bu durumda LOST/FOUND ikisini birden tarar.
        // Eski davranışta bu durum eşleştirmeyi SESSİZCE tamamen atlıyordu.
        listener.onResult(resultWithCategory("UNCERTAIN", false));

        verify(externalMatchingService, times(1)).attemptMatching(record);
        assertThat(record.getCategory().name()).isEqualTo("UNCERTAIN");

        ArgumentCaptor<ExternalSourcePost> captor = ArgumentCaptor.forClass(ExternalSourcePost.class);
        verify(externalSourcePostRepository).save(captor.capture());
        assertThat(captor.getValue().getProcessingStatus()).isEqualTo(ExternalProcessingStatus.ANALYZED);
    }

    @Test
    void uncertainCategoryWithoutAnimalPhotoStillNeverTriggersStage2() {
        // Görselde hayvan bile yoksa (aiIsPet=false) UNCERTAIN'ın yeni
        // "safety net" yolu da devreye girmemeli -- görsel/embedding
        // eşleştirmesinin zaten anlamı yok.
        AiAnalysisResult.Analysis analysis = new AiAnalysisResult.Analysis(
                List.of(new float[]{0.1f, 0.2f}), "unknown", 0.1, false, null, 0.0, null, List.of(), List.of());
        AiAnalysisResult.NlpAttributes nlp = new AiAnalysisResult.NlpAttributes(
                "UNCERTAIN", 0.0, null, null, null, null, null, null, null, null, null, null, null, true);
        AiAnalysisResult result = new AiAnalysisResult(
                1, "req", null, "ok", "test/v1", analysis, List.of(), null, null, null, List.of(),
                Instant.now(), 10L, nlp);

        listener.onResult(result);

        verifyNoInteractions(externalMatchingService);
        assertThat(record.getCategory().name()).isEqualTo("UNCERTAIN");
    }

    @ParameterizedTest(name = "category={0} triggers Stage 2")
    @ValueSource(strings = {"LOST", "FOUND"})
    void compatibleCategoryTriggersStage2(String category) {
        listener.onResult(resultWithCategory(category, false));

        verify(externalMatchingService, times(1)).attemptMatching(record);

        ArgumentCaptor<ExternalSourcePost> captor = ArgumentCaptor.forClass(ExternalSourcePost.class);
        verify(externalSourcePostRepository).save(captor.capture());
        assertThat(captor.getValue().getProcessingStatus()).isEqualTo(ExternalProcessingStatus.ANALYZED);
    }

    @Test
    void needsReviewNeverTriggersStage2EvenIfCategoryCompatible() {
        listener.onResult(resultWithCategory("FOUND", true));

        verifyNoInteractions(externalMatchingService);
    }

    @Test
    void visionSeesNoAnimalDowngradesTextCategoryToUncertain() {
        // Metin FOUND dedi ama görsel fotoğrafta hayvan bile görmedi
        // (füzyon kuralı, blueprint §29) — zorla LOST/FOUND üretilmemeli.
        AiAnalysisResult.Analysis analysis = new AiAnalysisResult.Analysis(
                List.of(new float[]{0.1f, 0.2f}), "unknown", 0.2, false, null, 0.0, null, List.of(), List.of());
        AiAnalysisResult.NlpAttributes nlp = new AiAnalysisResult.NlpAttributes(
                "FOUND", 0.9, null, null, null, null, null, null, null, null, null, null, null, false);
        AiAnalysisResult result = new AiAnalysisResult(
                1, "req", null, "ok", "test/v1", analysis, List.of(), null, null, null, List.of(),
                Instant.now(), 10L, nlp);

        listener.onResult(result);

        assertThat(record.getCategory().name()).isEqualTo("UNCERTAIN");
        assertThat(record.isNeedsReview()).isTrue();
        verifyNoInteractions(externalMatchingService);
    }

    @Test
    void missingRecordIsLoggedAndSkippedWithoutThrowing() {
        AiAnalysisResult result = new AiAnalysisResult(
                1, "req", null, "ok", "test/v1", null, List.of(), null, null, null, List.of(),
                Instant.now(), 999L, null);

        listener.onResult(result); // 999L hiçbir mock'ta yok -> findById boş döner

        verifyNoInteractions(externalMatchingService, potentialMatchService);
    }

    private AiAnalysisResult resultWithCategory(String category, boolean needsReview) {
        AiAnalysisResult.Analysis analysis = new AiAnalysisResult.Analysis(
                List.of(new float[]{0.1f, 0.2f}), "cat", 0.9, true, "Tekir", 0.7, "tabby", List.of(), List.of("cat"));
        AiAnalysisResult.NlpAttributes nlp = new AiAnalysisResult.NlpAttributes(
                category, 0.85, "cat", null, null, null, null, null,
                "Bursa", 0.5, null, false, null, needsReview);
        return new AiAnalysisResult(
                1, "req-1", null, "ok", "test/v1", analysis, List.of(), null, null, null, List.of(),
                Instant.now(), 10L, nlp);
    }
}
