package com.works.patimati.dto.match;

import jakarta.validation.constraints.NotNull;

public record PotentialMatchDecisionRequest(
        @NotNull Decision decision
) {
    public enum Decision {
        CONFIRMED,
        REJECTED
    }
}
