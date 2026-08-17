package com.works.patimati.ai.dto;

import java.util.List;

/**
 * Karşılaştırılacak aday ilan (entegrasyon sözleşmesi §3).
 *
 * <p>Adayları <b>Java süzer</b>, AI değil (§5): PostGIS, ilan tipi ve tarih
 * bilgisi bizdedir, AI'ın veritabanına erişimi yoktur.
 *
 * @param adId          aday ilanın kimliği. NATIVE aday için dolu, EXTERNAL
 *                      aday için null. Faz 2 revize blueprint §5/§31: tam
 *                      olarak biri ({@code adId}, {@code externalRecordId})
 *                      dolu olmalıdır.
 * @param externalRecordId aday bir Instagram kaydıysa external_pet_records
 *                      kimliği; native aday için null. EKLENDİ — eski
 *                      alıcılar bu alanı tanımadan yok sayar (§9).
 * @param embeddings    ilanın HER fotoğrafı için bir vektör. Görsel skor tüm
 *                      çiftlerin en iyisinden alınır; tek fotoğrafla eşleşme
 *                      oranı gerçek veride %24'te kaldığı için çoklu zorunlu
 * @param labels        adayın {@code ai_labels} sütunu
 * @param species       adayın {@code ai_species} sütunu
 * @param distanceKm    PostGIS ile hesaplanan gerçek mesafe. Konumsuz
 *                      fallback'te yapılandırılan aday yarıçapı kadar
 *                      (nötr/temkinli bir varsayılan — bkz.
 *                      {@code MatchCandidateGatherer}) kullanılır.
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
        String modelVersion,
        Long externalRecordId
) {
}
