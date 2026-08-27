package com.works.patimati.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * POI senkronizasyonunu düzenli tetikler (varsayılan: haftada bir, gece).
 * Overpass'ın fair-use politikası nedeniyle sık çalıştırılmaz; anlık test
 * için {@code POST /api/admin/pois/sync} kullanılır.
 */
@Component
@RequiredArgsConstructor
public class PoiSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(PoiSyncScheduler.class);

    private final PoiSyncService poiSyncService;

    @Scheduled(cron = "${app.poi.sync.cron}")
    public void syncScheduled() {
        try {
            poiSyncService.syncFromOverpass();
        } catch (RuntimeException exception) {
            log.error("Zamanlanmış POI senkronizasyonu başarısız", exception);
        }
    }
}
