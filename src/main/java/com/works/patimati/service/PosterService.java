package com.works.patimati.service;

public interface PosterService {
    /**
     * Belirtilen ilan ID'sine göre dinamik PDF afişi üretir ve byte dizisi olarak döndürür.
     *
     * @param adId İlan ID'si
     * @return PDF dosyasının byte dizisi
     */
    byte[] generateAdPosterPdf(Long adId);

    /**
     * Belirtilen ilan ID'sine ve isteği atan kullanıcıya göre dinamik PDF afişi üretir.
     * Güvenlik kurallarını (isPosterAllowed, showEmailOnPoster, showPhoneOnPoster) uygular.
     *
     * @param adId İlan ID'si
     * @param requestingUserEmail İsteği atan kullanıcının e-posta adresi (oturum yoksa null)
     * @return PDF dosyasının byte dizisi
     */
    byte[] generateAdPosterPdf(Long adId, String requestingUserEmail);
}
