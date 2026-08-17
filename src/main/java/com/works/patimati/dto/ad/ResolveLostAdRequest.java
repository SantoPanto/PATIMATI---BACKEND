package com.works.patimati.dto.ad;

public record ResolveLostAdRequest(
        Long finderId
) {
    public Long getFinderId() {
        return finderId;
    }
}
