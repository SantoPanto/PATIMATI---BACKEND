package com.works.patimati.ai.dto;

import java.util.List;

/** {@code POST /match} cevabı. {@code Match}/{@code SkippedCandidates} şekli
 * kuyruk sonucuyla birebir aynı olduğu için {@link AiAnalysisResult}'taki
 * iç kayıtlar yeniden kullanılır — ikinci bir tanım gerekmez. */
public record AiMatchResponse(
        List<AiAnalysisResult.Match> matches,
        AiAnalysisResult.SkippedCandidates skippedCandidates,
        String modelVersion
) {
}
