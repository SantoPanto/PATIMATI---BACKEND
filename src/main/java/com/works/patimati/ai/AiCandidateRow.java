package com.works.patimati.ai;

/**
 * Aday sorgusunun döndürdüğü hafif satır: ilan kimliği + gerçek mesafe.
 *
 * <p>Neden tüm entity değil: aday listesi 100 kayda kadar çıkabiliyor ve
 * mesafe yalnızca PostGIS tarafında hesaplanabiliyor. Önce hafif satırları
 * alıp sonra entity'leri toplu yüklemek, her satır için ayrı sorgu
 * çalıştırmaktan (N+1) kaçınmayı da kolaylaştırıyor.
 */
public interface AiCandidateRow {

    Long getAdId();

    /** PostGIS'in hesapladığı gerçek yeryüzü mesafesi, kilometre. */
    Double getDistanceKm();
}
