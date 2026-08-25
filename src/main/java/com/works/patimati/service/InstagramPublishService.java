package com.works.patimati.service;

import com.works.patimati.dto.admin.InstagramPublishQueueAdminResponse;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.enums.InstagramPublishStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * İlanların PatiMati'nin Instagram hesabında paylaşılma kuyruğu.
 *
 * <p>Akış: {@link #queueForReview} ilan oluşturulunca (izin verilmişse) bir
 * kuyruk satırı açar; admin panelinden {@link #publish} ya da {@link #skip}
 * ile karara bağlanır.
 */
public interface InstagramPublishService {

    /**
     * İlan kaydedildikten sonra çağrılır. Yalnızca
     * {@code ad.isInstagramShareConsent()} true ve ilan türü LOST/FOUND/
     * ADOPTION ise ve özellik açıksa ({@code instagram.publish.enabled}) bir
     * kuyruk satırı açar. <b>Hiçbir zaman istisna fırlatmaz</b> --
     * {@code AiAnalysisPublisher.publish()} ile AYNI ilke: bu bir ek, ilan
     * oluşturma akışını asla etkilememeli.
     */
    void queueForReview(Ad savedAd);

    Page<InstagramPublishQueueAdminResponse> listQueue(Pageable pageable, InstagramPublishStatus status);

    /**
     * @param finalCaption admin'in gönderdiği (düzenlenmiş olabilecek) son metin
     * @return true ise Instagram'da gerçekten yayınlandı, false ise
     *         {@code InstagramPublishStatus.FAILED} olarak kaydedildi (satır
     *         YİNE DE güncellenir/commit edilir, yalnız çağıran -- controller
     *         -- kullanıcıya hangi HTTP durumunu döneceğini bu değere bakarak
     *         karar verir).
     */
    boolean publish(Long queueItemId, String finalCaption, String adminEmail);

    void skip(Long queueItemId, String adminEmail);
}
