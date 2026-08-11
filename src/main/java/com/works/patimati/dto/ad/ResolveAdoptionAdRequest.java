package com.works.patimati.dto.ad;

public record ResolveAdoptionAdRequest(
        Long adopterId
) {
    public Long getAdopterId() {
        return adopterId;
    }
}
