package com.works.patimati.ai.dto;

import java.util.List;

/**
 * AI'ya gönderilen analiz isteği (entegrasyon sözleşmesi §3).
 *
 * <p>Alan adları tel üzerinde <b>snake_case</b> olmalıdır ({@code ad_id},
 * {@code photo_urls}...). Bunu {@code AiRabbitConfig} içindeki AI'ya özel
 * JSON dönüştürücü sağlar; global bir ayar mevcut REST sözleşmelerini kırardı.
 *
 * @param schemaVersion    şu an 1; tanınmayan sürümde AI hata döndürür
 * @param requestId        izleme için UUID, sonuçta aynen geri döner
 * @param adId             analiz edilen ilan
 * @param adType           LOST | FOUND — ADOPTION gönderilmez (§3)
 * @param declaredSpecies  kullanıcının beyanı; AI tahmininden ÖNCELİKLİDİR (§7)
 * @param photoUrls        en az 1, en fazla 5 adres
 * @param candidates       Java'nın süzdüğü adaylar; boş olabilir (ilk ilan)
 */
public record AiAnalysisRequest(
        int schemaVersion,
        String requestId,
        Long adId,
        String adType,
        String declaredSpecies,
        List<String> photoUrls,
        List<AiCandidate> candidates
) {
}
