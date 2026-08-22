package com.works.patimati.dto.ad;

/**
 * Kayıp ilanı "bulundu" diye kapatma isteği.
 *
 * <p><b>İki alan da isteğe bağlı.</b> Kullanıcı hayvanını kendi bulmuş
 * olabilir ya da bulanı/ilanını seçmeden kapatmak isteyebilir; kapanışın
 * kendisi bu bilgilere bağlı değildir. Eski istemciler yalnız
 * {@code finderId} gönderiyor ve göndermeye devam edebilir — {@code foundAdId}
 * eklenmesi sözleşmeyi bozmaz.
 *
 * @param finderId  hayvanı bulan kullanıcı. Ödül puanı bunun üzerinden
 *                  veriliyordu; artık ilana da <b>yazılıyor</b> (eskiden
 *                  puan verilip atılıyordu, "kim buldu" sonradan
 *                  cevaplanamıyordu).
 * @param foundAdId eşleşen <b>bulundu ilanının</b> kimliği. Bu alan
 *                  olmadığı için sistemde "şu kayıp ilan şu bulundu ilanıyla
 *                  eşleşti" bilgisi hiç yoktu; eşleşme skorundaki konum
 *                  ağırlığı da bu yüzden ölçümle tartışılamıyordu.
 */
public record ResolveLostAdRequest(
        Long finderId,
        Long foundAdId
) {
    public Long getFinderId() {
        return finderId;
    }

    public Long getFoundAdId() {
        return foundAdId;
    }
}
