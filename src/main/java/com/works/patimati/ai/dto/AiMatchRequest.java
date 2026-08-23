package com.works.patimati.ai.dto;

import java.util.List;

/**
 * AI'nın MEVCUT {@code POST /match} uç noktasına gönderilen istek —
 * yeni bir AI ucu değil, ilk gerçek çağıranı (Faz 2 revize blueprint §1).
 *
 * <p>Aşama 2 (eşleştirme) senkron HTTP üzerinden buraya gider; kuyruk
 * tarafı yalnızca Aşama 1'de (analiz) kullanılır ve adaylar hep boştur.
 */
public record AiMatchRequest(
        List<float[]> embeddings,
        List<String> labels,
        String species,
        List<AiCandidate> candidates,
        Long adId,
        Long externalRecordId,
        Double matchThreshold
) {
}
