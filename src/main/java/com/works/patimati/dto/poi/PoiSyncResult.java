package com.works.patimati.dto.poi;

public record PoiSyncResult(
        boolean ranSync,
        int created,
        int updated,
        int skipped
) {
    public static PoiSyncResult disabled() {
        return new PoiSyncResult(false, 0, 0, 0);
    }
}
