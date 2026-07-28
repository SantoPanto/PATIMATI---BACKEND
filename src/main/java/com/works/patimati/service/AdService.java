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

    public Ad createAdAndNotifyNearbyUsers(AdCreateDto dto, User user) {
        Point adPoint = geometryFactory.createPoint(new Coordinate(dto.getLongitude(), dto.getLatitude()));

        Ad ad = Ad.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .adType(dto.getAdType())
                .location(adPoint)
                .user(user)
                .active(true)
                .build();

        Ad savedAd = adRepository.save(ad);

        // 5 km (5000 meter) radius push notifications
        List<User> nearbyUsers = userRepository.findUsersNearby(adPoint, 5000.0);

        for (User u : nearbyUsers) {
            if (u.getFcmToken() != null && !u.getFcmToken().isEmpty()) {
                sendPushNotification(
                        u.getFcmToken(),
                        "Yakınınızda Olası İlan Eşleşmesi",
                        "Yakınınızda yeni bir ilan paylaşıldı: " + savedAd.getTitle() + ". Kontrol etmek ister misiniz?"
                );
            }
        }

        return savedAd;
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