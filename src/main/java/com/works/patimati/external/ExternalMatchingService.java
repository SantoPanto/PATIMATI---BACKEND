package com.works.patimati.external;

import com.works.patimati.ai.AiMatchClient;
import com.works.patimati.ai.MatchCandidateGatherer;
import com.works.patimati.ai.dto.AiAnalysisResult;
import com.works.patimati.ai.dto.AiCandidate;
import com.works.patimati.ai.dto.AiMatchRequest;
import com.works.patimati.ai.dto.AiMatchResponse;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.enums.ExternalProcessingStatus;
import com.works.patimati.entity.external.ExternalPetRecord;
import com.works.patimati.entity.external.ExternalSourcePost;
import com.works.patimati.repository.external.ExternalSourcePostRepository;
import com.works.patimati.service.PotentialMatchService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * AŞAMA 2 — yalnızca kategori-uyumlu (LOST/FOUND), incelemeye düşmemiş,
 * başarıyla analiz edilmiş kayıtlar için çağrılır (Faz 2 revize blueprint
 * §1/§2). Var olan {@code matcher.py} YENİDEN YAZILMAZ — AI'nın zaten var
 * olan {@code POST /match} ucuna senkron bir çağrıdır.
 *
 * <p><b>Aşama 1'in sonucu asla kaybolmaz:</b> herhangi bir hata (zaman
 * aşımı, bağlantı reddi, 5xx) kaydı {@code ANALYZED} durumunda bırakır —
 * {@code ExternalIngestionMaintenanceJob} bunu daha sonra yeniden dener.
 */
@Service
@RequiredArgsConstructor
public class ExternalMatchingService {

    private static final Logger log = LoggerFactory.getLogger(ExternalMatchingService.class);

    private final MatchCandidateGatherer candidateGatherer;
    private final AiMatchClient matchClient;
    private final PotentialMatchService potentialMatchService;
    private final ExternalSourcePostRepository postRepository;

    /** Kaynak bazlı eşik geçersiz kılma — 0.80 native değeri Instagram için doğrulanmış kabul edilmez (blueprint §22). */
    @Value("${ai.matching.instagram-threshold:#{null}}")
    private Double instagramThreshold;

    @Transactional
    public void attemptMatching(ExternalPetRecord record) {
        ExternalSourcePost post = record.getPost();
        try {
            List<AiCandidate> candidates = candidateGatherer.findCandidatesForExternalRecord(record);

            post.setProcessingStatus(ExternalProcessingStatus.MATCHING);
            postRepository.save(post);

            AiMatchRequest request = new AiMatchRequest(
                    embeddingsOf(record),
                    record.getAiLabels() == null ? List.of() : record.getAiLabels(),
                    record.getSpecies() == null ? "unknown" : record.getSpecies(),
                    candidates,
                    null,
                    record.getId(),
                    instagramThreshold);

            AiMatchResponse response = matchClient.match(request);

            for (AiAnalysisResult.Match match : response.matches()) {
                if (!match.match() || match.adId() == null) {
                    continue; // yalnızca eşiği geçen VE native bir ilana karşılık gelen sonuçlar
                }
                potentialMatchService.recordExternalMatch(
                        match.adId(), record.getId(),
                        (float) match.visual(), (float) match.label(), (float) match.location(),
                        (float) match.score(), response.modelVersion());
            }

            post.setProcessingStatus(ExternalProcessingStatus.COMPLETED);
            postRepository.save(post);

            log.info("Aşama 2 tamamlandı: externalRecordId={} aday={} eşleşme={}",
                    record.getId(), candidates.size(), response.matches().size());

        } catch (Exception e) {
            log.warn("Aşama 2 (eşleştirme) başarısız — Aşama 1 sonucu KORUNUYOR, "
                            + "kayıt ANALYZED'de bırakılıp süpürücüye devrediliyor: externalRecordId={} hata={}",
                    record.getId(), e.getMessage());
            post.setProcessingStatus(ExternalProcessingStatus.ANALYZED);
            postRepository.save(post);
        }
    }

    private List<float[]> embeddingsOf(ExternalPetRecord record) {
        if (record.getAiEmbeddings() == null) {
            return List.of();
        }
        return record.getAiEmbeddings().stream()
                .map(Ad.AiPhotoVector::embedding)
                .filter(Objects::nonNull)
                .toList();
    }
}
