package com.works.patimati.service;

public interface PosterService {
    /**
     * Belirtilen ilan ID'sine göre dinamik PDF afişi üretir ve byte dizisi olarak döndürür.
     *
     * @param adId İlan ID'si
     * @return PDF dosyasının byte dizisi
     */
    byte[] generateAdPosterPdf(Long adId);
}
