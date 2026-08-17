package com.works.patimati.external.dto;

import java.time.Instant;
import java.util.List;

/**
 * Collector → Backend olay mesajı (Faz 2 revize blueprint §5, §19).
 *
 * <p>Yalnızca metadata taşır — hiçbir görsel bayt burada gitmez (§16/§20).
 * {@code media[].sourceReference} yalnızca denetim amaçlıdır, gerçek
 * transfer ayrı bir HTTP çağrısıyla ({@code ExternalMediaIngestionController})
 * yapılır.
 *
 * @param mediaCount beklenen carousel uzunluğu — {@code media.size()} ile
 *                   TUTARLI OLMALIDIR; tutmuyorsa gönderi FAILED işaretlenir
 *                   (§5 medya doğrulaması, gönderenin hatası kabul edilir).
 */
public record ExternalIngestionEvent(
        int schemaVersion,
        String eventId,
        String source,
        String detectionType,
        String sourcePostId,
        String canonicalUrl,
        String authorUsername,
        String caption,
        String triggeringComment,
        String externalTriggerId,
        Instant publishedAt,
        Instant detectedAt,
        String locationText,
        int mediaCount,
        List<MediaDescriptor> media,
        String correlationId
) {
    public record MediaDescriptor(int ordinal, String sourceReference) {
    }
}
