package com.works.patimati.instagram;

/**
 * {@code InstagramGraphClient.publish()} sonucu -- başarı ({@code mediaId}
 * dolu) ya da başarısızlık ({@code errorMessage} dolu), asla ikisi birden.
 */
public record InstagramGraphResult(
        boolean success,
        String mediaId,
        String permalink,
        String errorMessage
) {
    public static InstagramGraphResult success(String mediaId, String permalink) {
        return new InstagramGraphResult(true, mediaId, permalink, null);
    }

    public static InstagramGraphResult failure(String errorMessage) {
        return new InstagramGraphResult(false, null, null, errorMessage);
    }
}
