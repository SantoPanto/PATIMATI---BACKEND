package com.works.patimati.controller;

import com.works.patimati.dto.poi.PoiSyncResult;
import com.works.patimati.service.PoiSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** /api/admin/** zaten SecurityConfig'te ROLE_ADMIN'e kısıtlı. */
@RestController
@RequestMapping("/api/admin/pois")
@RequiredArgsConstructor
public class PoiAdminController {

    private final PoiSyncService poiSyncService;

    /** Zamanlanmış senkronizasyonu beklemeden anlık tetikler (test/ilk yükleme için). */
    @PostMapping("/sync")
    public ResponseEntity<PoiSyncResult> triggerSync() {
        return ResponseEntity.ok(poiSyncService.syncFromOverpass());
    }
}
