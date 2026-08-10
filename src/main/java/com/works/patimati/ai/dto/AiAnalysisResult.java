package com.works.patimati.ai.dto;

import java.time.Instant;
import java.util.List;

/**
 * AI'dan dönen sonuç (entegrasyon sözleşmesi §4).
 *
 * <p>{@code status} iki değer alır: {@code "ok"} ya da {@code "error"}.
 * Hata durumunda {@link #analysis()} boştur ve {@link #error()} doludur;
 * ilan yayında kalır, yalnızca {@code ai_status = FAILED} yazılır.
 *
 * <p><b>Hata mesajlarında bile {@code adId} ve {@code requestId} döner</b> —
 * mesaj şemaya hiç uymasa bile, okunabildiği kadarıyla. Dönmeseydi hangi ilanın
 * başarısız olduğunu bilemez, o ilan sonsuza kadar PENDING kalırdı.
 */
public record AiAnalysisResult(
        int schemaVersion,
        String requestId,
        Long adId,
        String status,
        String modelVersion,
        Analysis analysis,
        List<Match> matches,
        SkippedCandidates skippedCandidates,
        AiError error,
        List<FailedPhoto> failedPhotos,
        Instant processedAt
) {

    public boolean isOk() {
        return "ok".equals(status);
    }

    /**
     * Fotoğraflardan çıkarılan bilgiler.
     *
     * @param embeddings       her fotoğraf için bir vektör; sırası {@code photo_urls} ile aynı
     * @param species          cat | dog | unknown — güven düşükse unknown
     * @param isPet            {@code false} → fotoğrafta kedi/köpek görünmüyor.
     *                         <b>Sarmalayıcı {@code Boolean}, ilkel değil:</b> alan
     *                         mesajda hiç gelmezse Jackson ilkel tipe {@code false}
     *                         yazardı ve "AI söylemedi" ile "AI hayvan görmedi"
     *                         aynı değere düşerdi. {@code null} = bilinmiyor.
     * @param breed            bilgi amaçlı; filtre olarak KULLANILMAZ (§7 kural 1)
     * @param pattern          tabby | spotted | solid | bicolor
     * @param labels           eşleştirme skorunun %30'unu oluşturan etiketler
     */
    public record Analysis(
            List<float[]> embeddings,
            String species,
            double speciesConfidence,
            Boolean isPet,
            String breed,
            double breedConfidence,
            String pattern,
            List<Color> colors,
            List<String> labels
    ) {
    }

    public record Color(String name, double score) {
    }

    /**
     * Bir aday ilanla karşılaştırma sonucu.
     *
     * <p>{@code match = true}, <b>"kesin aynı hayvan" demek değildir</b>;
     * "bildirim gönderilecek kadar eminiz" demektir (§7 kural 3). Gerçek
     * fotoğraflarda aynı/farklı hayvan skorları çakıştığı için hiçbir eşik
     * ikisini temiz ayırmıyor. Düşük skorlu adaylar da listede gösterilmeli,
     * yalnızca bildirim tetiklememelidir.
     *
     * @param photoA hangi fotoğraf çiftinin eşleştiği (0 tabanlı); arayüzde
     *               "bu iki fotoğraf benziyor" diye gösterilebilir
     */
    public record Match(
            Long adId,
            double score,
            double visual,
            double label,
            double location,
            boolean match,
            Integer photoA,
            Integer photoB
    ) {
    }

    /**
     * Elenen adayların gerekçeli sayımı.
     *
     * <p>"Hiç eşleşme çıkmadı" durumunun sebebi görünür olsun diye var.
     * {@code modelSurumuUyusmuyor} sıfırdan büyükse ilgili ilanların yeniden
     * analiz edilmesi gerekir — vektörleri bayatlamıştır.
     *
     * <p>Alan adları Python tarafında Türkçe olduğu için burada da öyle.
     */
    public record SkippedCandidates(
            int toplam,
            int kendisi,
            int tekrarEden,
            int modelSurumuUyusmuyor,
            int gecersizEmbedding,
            int adaySiniriAsildi
    ) {
    }

    /**
     * @param code NO_PHOTOS | PHOTO_DOWNLOAD_FAILED | INVALID_IMAGE |
     *             UNSUPPORTED_SCHEMA | INVALID_REQUEST | MODEL_ERROR | INTERNAL
     */
    public record AiError(String code, String message) {
    }

    /** İndirilemeyen fotoğraflar — sessizce kaybolmasınlar diye raporlanır. */
    public record FailedPhoto(String url, String error) {
    }
}
