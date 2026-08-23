package com.works.patimati.controller;

import com.works.patimati.dto.match.PotentialMatchDecisionRequest;
import com.works.patimati.dto.match.PotentialMatchSummaryResponse;
import com.works.patimati.service.PotentialMatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Kullanıcının kendi olası eşleşmeleri (Faz 2 revize blueprint §10 —
 * frontend minimum). Instagram kaydı burada bile bir PatiMati ilanı gibi
 * SUNULMAZ — {@code counterparty.kind = "EXTERNAL"} ve {@code sourceUrl}
 * her zaman "Instagram'da görüntüle" bağlantısı olarak gösterilmelidir.
 */
@Validated
@RestController
@RequestMapping("/api/me/potential-matches")
@RequiredArgsConstructor
public class PotentialMatchController {

    private final PotentialMatchService potentialMatchService;

    @GetMapping
    public ResponseEntity<List<PotentialMatchSummaryResponse>> getMyPotentialMatches(
            Authentication authentication
    ) {
        return ResponseEntity.ok(potentialMatchService.findForUser(authentication.getName()));
    }

    /**
     * Kullanıcının bir eşleşme hakkındaki kararı. Yalnızca kendi kaydı
     * (403) ve yalnızca NOTIFIED/VIEWED durumundaki bir kayıt (409) için.
     *
     * <p>{@code status = CONFIRMED} "kullanıcı onayladı" demektir, "kesin
     * aynı hayvan" DEMEK DEĞİLDİR — bkz. {@link PotentialMatchSummaryResponse}.
     */
    @PostMapping("/{recipientId}/decision")
    public ResponseEntity<PotentialMatchSummaryResponse> decide(
            Authentication authentication,
            @PathVariable Long recipientId,
            @Valid @RequestBody PotentialMatchDecisionRequest request
    ) {
        return ResponseEntity.ok(potentialMatchService.recordDecision(
                authentication.getName(), recipientId, request.decision()));
    }
}
