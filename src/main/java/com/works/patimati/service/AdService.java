package com.works.patimati.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
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

    //KISIM 3
    public List<Ad> findNearbyAds(double latitude, double longitude, double radiusInMeters) {
        Point userPoint = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        return adRepository.findNearbyAds(userPoint, radiusInMeters);
    }

    //
    public Ad createAdAndNotifyNearbyUsers(Ad ad, double latitude, double longitude) {
        Point adPoint = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        ad.setLocation(adPoint);
        Ad savedAd = adRepository.save(ad);

        // 5 km
        List<User> nearbyUsers = userRepository.findUsersNearby(adPoint, 5000.0);
        for (User user : nearbyUsers) {
            if (user.getFcmToken() != null && !user.getFcmToken().isEmpty()) {
                sendPushNotification(user.getFcmToken(), savedAd.getTitle(), savedAd.getDescription());
            }
        }

        return savedAd;
    }

    private void sendPushNotification(String token, String title, String body) {
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle("Yakınınızda Yeni İlan: " + title)
                            .setBody(body)
                            .build())
                    .build();
            FirebaseMessaging.getInstance().send(message);
        } catch (Exception e) {

            System.err.println("Push notification gönderilemedi: " + e.getMessage());
        }
    }
}