package com.works.patimati.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        try {

            InputStream serviceAccount = new ClassPathResource("firebase-adminsdk.json").getInputStream();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            // Eğer Firebase daha önce başlatılmamışsa başlat
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase Admin SDK başarıyla başlatıldı!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Firebase başlatılırken bir hata oluştu: " + e.getMessage());
        }
    }
}