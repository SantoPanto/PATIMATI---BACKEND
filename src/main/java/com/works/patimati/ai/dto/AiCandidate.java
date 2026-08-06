package com.works.patimati.ai.dto;

import java.util.List;

/**
 * Karşılaştırılacak aday ilan (entegrasyon sözleşmesi §3).
 *
 * <p>Adayları <b>Java süzer</b>, AI değil (§5): PostGIS, ilan tipi ve tarih
 * bilgisi bizdedir, AI'ın veritabanına erişimi yoktur.
 *
 * @param adId          aday ilanın kimliği
 * @param embeddings    ilanın HER fotoğrafı için bir vektör. Görsel skor tüm
 *                      çiftlerin en iyisinden alınır; tek fotoğrafla eşleşme
 *                      oranı gerçek veride %24'te kaldığı için çoklu zorunlu
 * @param labels        adayın {@code ai_labels} sütunu
 * @param species       adayın {@code ai_species} sütunu
 * @param distanceKm    PostGIS ile hesaplanan gerçek mesafe
 * @param modelVersion  <b>zorunlu.</b> Servisin sürümüyle aynı değilse aday
 *                      atlanır — farklı sürümlerin vektörleri kıyaslanamaz ve
 *                      hata vermeden yanlış benzerlik üretir
 */
public record AiCandidate(
        Long adId,
        List<float[]> embeddings,
        List<String> labels,
        String species,
        double distanceKm,
        String modelVersion
) {
}
