package com.works.patimati.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Yapay zeka analiz durumu", allowableValues = {"PENDING", "DONE", "FAILED", "NOT_APPLICABLE"})
public enum AiStatus {
    PENDING,
    DONE,
    FAILED,
    NOT_APPLICABLE
}
