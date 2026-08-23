package com.works.patimati.entity.enums;

/**
 * external_source_posts.processing_status — Faz 2 blueprint §27'deki
 * sadeleştirilmiş state machine.
 *
 * <p>{@code AUTH_REQUIRED} kasıtlı olarak burada YOK: o, Collector'ın kendi
 * yerel durumudur ve hiçbir zaman bu DB sütununa yazılmaz (blueprint §14).
 */
public enum ExternalProcessingStatus {
    DISCOVERED,
    COLLECTED,
    MEDIA_STORED,
    ANALYZING,
    ANALYZED,
    MATCHING,
    COMPLETED,
    FAILED,
    NEEDS_REVIEW
}
