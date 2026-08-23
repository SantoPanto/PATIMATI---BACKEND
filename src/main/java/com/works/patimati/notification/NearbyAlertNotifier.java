package com.works.patimati.notification;

import com.works.patimati.entity.Ad;
import com.works.patimati.entity.AlertSubscription;
import com.works.patimati.entity.User;
import com.works.patimati.repository.AlertSubscriptionRepository;
import com.works.patimati.repository.NotificationRepository;
import com.works.patimati.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Yeni KAYIP ilanını, ilanı kendi yarıçapının içinde gören uyarı
 * abonelerine bildirir.
 *
 * <p>AdService.notifyNearbyUsersSafely'nin yerine geçti. Eski davranış
 * aboneliksizdi: her türden ilan, konumu dolu TÜM kullanıcılara sabit 5 km
 * yarıçapla ve yanlış bir metinle ("Kayıp ilanınızla uyuşabilecek…")
 * gidiyordu; kapatma imkânı yoktu. Artık bildirim yalnız ayarlardan
 * abone olan kullanıcıya, onun seçtiği merkez + yarıçapla gider ve konu
 * vaade uygun biçimde KAYIP ilanlarıyla sınırlıdır.
 *
 * <p>Bildirim tipi {@code NEARBY_AD} olarak korunuyor: ön yüzün bildirim
 * tıklama/yönlendirme sözleşmesi tip + adId üzerinden çalışır, teslimat
 * süzgecinin değişmesi tipin anlamını değiştirmez.
 */
@Component
public class NearbyAlertNotifier {

    static final String BILDIRIM_TIPI = "NEARBY_AD";
    private static final String BILDIRIM_BASLIGI = "Çevrende kayıp ilanı";

    private static final Logger log = LoggerFactory.getLogger(NearbyAlertNotifier.class);

    private final AlertSubscriptionRepository alertSubscriptionRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final int gunlukTavan;

    public NearbyAlertNotifier(
            AlertSubscriptionRepository alertSubscriptionRepository,
            NotificationRepository notificationRepository,
            NotificationService notificationService,
            // Yoğun bölgede abone bildirim yağmuruna tutulup özelliği
            // kapatmasın diye kayan 24 saatte kişi başı üst sınır.
            @Value("${app.nearby-alert.daily-cap:10}") int gunlukTavan
    ) {
        this.alertSubscriptionRepository = alertSubscriptionRepository;
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
        this.gunlukTavan = gunlukTavan;
    }

    /**
     * İlan kaydını asla düşürmez: bildirim tarafında ne olursa olsun
     * yutulur ve loglanır (AdService'in eski try/catch sözleşmesiyle aynı).
     */
    public void yeniIlaniBildir(Ad ad) {
        if (ad.getAdType() != Ad.AdType.LOST) {
            // Abonelik vaadi "çevremde KAYIP ilanı çıkınca"; bulundu ve
            // sahiplendirme ilanları bu kanaldan bildirilmez.
            return;
        }
        if (ad.getLocation() == null) {
            return;
        }

        try {
            Long ownerUid = ad.getUser() != null ? ad.getUser().getUid() : null;
            List<AlertSubscription> aboneler = alertSubscriptionRepository
                    .findEnabledWithinOwnRadius(ad.getLocation(), ownerUid == null ? -1L : ownerUid);

            String govde = "Yakınında yeni bir kayıp ilanı: " + ad.getTitle();
            OffsetDateTime tavanPenceresi = OffsetDateTime.now().minusHours(24);

            for (AlertSubscription abonelik : aboneler) {
                User alici = abonelik.getUser();

                // Abone başına ayrı sayım sorgusu (N+1): bir ilanın yarıçap
                // içinde bulduğu abone sayısı küçük, toplu gönderim zaten
                // FCM'e teker teker gidiyor (FirebasePushNotificationService).
                long songunku = notificationRepository.countByUser_UidAndTypeAndCreatedAtAfter(
                        alici.getUid(), BILDIRIM_TIPI, tavanPenceresi);
                if (songunku >= gunlukTavan) {
                    log.debug("Günlük uyarı tavanı dolu, atlanıyor. uid={} adId={}",
                            alici.getUid(), ad.getId());
                    continue;
                }

                notificationService.createAndSend(
                        alici,
                        BILDIRIM_BASLIGI,
                        govde,
                        BILDIRIM_TIPI,
                        Map.of(
                                "type", BILDIRIM_TIPI,
                                "adId", String.valueOf(ad.getId())
                        ),
                        BILDIRIM_TIPI + ":" + ad.getId() + ":" + alici.getUid()
                );
            }
        } catch (RuntimeException exception) {
            log.warn("İlan oluşturuldu ancak uyarı aboneleri bilgilendirilemedi. adId={}",
                    ad.getId(), exception);
        }
    }
}
