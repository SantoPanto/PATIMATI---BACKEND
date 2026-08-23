package com.works.patimati.ai.dto;

import java.util.List;

/**
 * AI'ya gönderilen analiz isteği (entegrasyon sözleşmesi §3).
 *
 * <p>Alan adları tel üzerinde <b>snake_case</b> olmalıdır ({@code ad_id},
 * {@code photo_urls}...). Bunu {@code AiRabbitConfig} içindeki AI'ya özel
 * JSON dönüştürücü sağlar; global bir ayar mevcut REST sözleşmelerini kırardı.
 *
 * <p><b>Faz 2 revize blueprint §5/§9 — eklenen alanlar, hepsi opsiyonel ve
 * geriye dönük uyumlu:</b> eski mesajlar (bu alanları hiç göndermeyen)
 * anlam değişmeden aynı şekilde işlenir.
 *
 * @param schemaVersion    şu an 1; tanınmayan sürümde AI hata döndürür
 * @param requestId        izleme için UUID, sonuçta aynen geri döner
 * @param adId             analiz edilen ilan. NATIVE istek için dolu,
 *                         EXTERNAL istek için null (bkz. {@code externalRecordId})
 * @param adType           LOST | FOUND — ADOPTION gönderilmez (§3)
 * @param declaredSpecies  kullanıcının beyanı; AI tahmininden ÖNCELİKLİDİR (§7)
 * @param photoUrls        en az 1, en fazla 5 adres
 * @param candidates       Java'nın süzdüğü adaylar; boş olabilir (ilk ilan,
 *                         veya EXTERNAL Aşama 1 — bkz. {@code source})
 * @param source           EKLENDİ. "PATIMATI" (varsayılan, eski davranışla
 *                         aynı) | "INSTAGRAM". Yalnızca izleme/yönlendirme
 *                         amaçlı — AI tarafı hiçbir farklı davranış
 *                         sergilemez, adayları boşsa zaten hiç eşleştirmez.
 * @param externalRecordId EKLENDİ. İstek bir external_pet_records analizi
 *                         içinse dolu, native ilan isteğiyse null.
 * @param caption          EKLENDİ. Yalnızca EXTERNAL istekte dolu — metin
 *                         analizi tetikleyicisi.
 * @param triggeringComment EKLENDİ. Yalnızca EXTERNAL istekte, varsa dolu.
 * @param matchThreshold   EKLENDİ. Kaynak bazlı eşik geçersiz kılma; null ise
 *                         AI kendi ortam değişkenindeki varsayılanı kullanır.
 * @param isMatchRequired  EKLENDİ (develop, popup-eşik-konum). İlan eşleştirme
 *                         istemiyorsa false; candidates zaten boş gönderilir,
 *                         bu alan yalnızca AI tarafına niyeti açıkça bildirir.
 */
public record AiAnalysisRequest(
        int schemaVersion,
        String requestId,
        Long adId,
        String adType,
        String declaredSpecies,
        List<String> photoUrls,
        List<AiCandidate> candidates,
        String source,
        Long externalRecordId,
        String caption,
        String triggeringComment,
        Double matchThreshold,
        Boolean isMatchRequired
) {
    /** Native (mevcut) istekler için — kaynak her zaman PatiMati'dir. */
    public static AiAnalysisRequest forAd(
            int schemaVersion, String requestId, Long adId, String adType,
            String declaredSpecies, List<String> photoUrls, List<AiCandidate> candidates,
            Boolean isMatchRequired) {
        return new AiAnalysisRequest(schemaVersion, requestId, adId, adType, declaredSpecies,
                photoUrls, candidates, "PATIMATI", null, null, null, null, isMatchRequired);
    }
}
