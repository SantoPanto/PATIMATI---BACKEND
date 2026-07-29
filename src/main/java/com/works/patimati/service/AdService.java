package com.works.patimati.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.works.patimati.dto.AdCreateDto;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.User;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdService {

    private final AdRepository adRepository;
    private final UserRepository userRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public List<Ad> findNearbyAds(double latitude, double longitude, double radiusInMeters) {
        Point userPoint = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        return adRepository.findNearbyAds(userPoint, radiusInMeters);
    }
    // DRAFT
    public Ad createDraftAd(AdCreateDto dto, User user) {
        Point adPoint = geometryFactory.createPoint(new Coordinate(dto.getLongitude(), dto.getLatitude()));
        Ad ad = Ad.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .adType(dto.getAdType())
                .location(adPoint)
                .user(user)
                .status(Ad.AdStatus.DRAFT)
                .build();
        return adRepository.save(ad);
    }

    // PUBLISHED
    public Ad publishAd(Long adId, User currentUser) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("İlan bulunamadı."));

        if (!ad.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Bu ilanı yayınlama yetkiniz yok.");
        }

        if (ad.getStatus() == Ad.AdStatus.PUBLISHED) {
            throw new IllegalArgumentException("İlan zaten yayınlanmış.");
        }

        ad.setStatus(Ad.AdStatus.PUBLISHED);
        Ad publishedAd = adRepository.save(ad);

        //Push Bildirimi
        List<User> nearbyUsers = userRepository.findUsersNearby(ad.getLocation(), 5000.0);
        for (User u : nearbyUsers) {
            if (u.getFcmToken() != null && !u.getFcmToken().isEmpty() && !u.getId().equals(currentUser.getId())) {
                sendPushNotification(
                        u.getFcmToken(),
                        "Yakınınızda Olası İlan Eşleşmesi",
                        "Yakınınızda yeni bir ilan paylaşıldı: " + publishedAd.getTitle() + ". Kontrol etmek ister misiniz?"
                );
            }
        }

        return publishedAd;
    }

    //İlan Düzenleme İsteği
    public Ad updateAd(Long adId, AdCreateDto dto, User currentUser) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("İlan bulunamadı."));

        if (!ad.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Bu ilanı düzenleme yetkiniz yok.");
        }

        if (ad.getStatus() == Ad.AdStatus.PUBLISHED) {
            //400 hatası
            throw new IllegalArgumentException("Yayınlanmış ilanlar tekrar düzenlenemez. İlanı silip yeniden oluşturabilirsiniz.");
        }

        Point adPoint = geometryFactory.createPoint(new Coordinate(dto.getLongitude(), dto.getLatitude()));
        ad.setTitle(dto.getTitle());
        ad.setDescription(dto.getDescription());
        ad.setAdType(dto.getAdType());
        ad.setLocation(adPoint);

        return adRepository.save(ad);
    }

    private void sendPushNotification(String token, String title, String body) {
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();
            FirebaseMessaging.getInstance().send(message);
        } catch (Exception e) {
            System.err.println("Push notification error: " + e.getMessage());
        }
    }
}