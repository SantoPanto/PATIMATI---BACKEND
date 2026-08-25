package com.works.patimati.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

/**
 * {@link GoogleIdTokenVerifier} tek sefer kurulur (HttpTransport/JsonFactory
 * ağır nesneler) ve {@code /api/auth/google}'daki idToken doğrulaması bunu
 * kullanır — bkz. UserService#verifyGoogleIdToken.
 *
 * <p>Audience, aynı {@code GOOGLE_CLIENT_ID} ortam değişkenidir ki
 * {@code spring.security.oauth2.client.registration.google.client-id}
 * (application.yml) zaten bundan besleniyor — ayrı bir değişken
 * tanımlanmadı. Değişken DOĞRUDAN okunuyor, o YAML anahtarının üzerinden
 * DEĞİL: application.yml'de {@code client-id: ${GOOGLE_CLIENT_ID}} yazıyor
 * (varsayılansız), yani o anahtarın Environment'taki değeri -- değişken
 * ayarlanmamışken bile -- ÇÖZÜLEMEMİŞ {@code "${GOOGLE_CLIENT_ID}"} metninin
 * ta kendisi olur; {@code @Value}'nin kendi {@code :} varsayılanı yalnızca
 * anahtar TAMAMEN YOKSA devreye girer, iç içe geçmiş çözülemeyen bir
 * placeholder'ı KURTARMAZ (doğrulandı: bu şekilde denenince
 * {@code PlaceholderResolutionException} ile bağlam hiç açılmadı). Ortam
 * değişkenini doğrudan okumak bu dolaylamayı tamamen atlıyor.
 *
 * <p>Değer boşsa (ör. {@code GOOGLE_CLIENT_ID} ayarlanmamış bir ortamda)
 * uygulama BAŞLAMAYA devam eder — bu bean'i zorunlu kılıp yoksa context'i
 * çökertmek, şu an zaten bu değişken hiç ayarlanmadan çalışan ortamları
 * (bkz. yerel geliştirme) gereksiz yere kırardı. Bunun yerine audience
 * listesine boş string konur: gerçek bir Google idToken'ının {@code aud}
 * alanı asla boş string olmaz, yani doğrulama GÜVENLİ biçimde KAPALI kalır
 * (her istek reddedilir) — açık kalıp her token'ı kabul etmek yerine.
 */
@Configuration(proxyBeanMethods = false)
public class GoogleAuthConfig {

    private static final Logger log = LoggerFactory.getLogger(GoogleAuthConfig.class);

    @Bean
    public GoogleIdTokenVerifier googleIdTokenVerifier(
            @Value("${GOOGLE_CLIENT_ID:}") String googleClientId
    ) {
        if (googleClientId == null || googleClientId.isBlank()) {
            log.warn("GOOGLE_CLIENT_ID ayarlanmamış -- /api/auth/google, gerçek değer "
                    + "verilene kadar HER idToken'ı geçersiz sayacak (audience hiçbir "
                    + "gerçek token'la eşleşemeyen boş bir değere kilitlendi).");
        }

        return new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId == null ? "" : googleClientId))
                .build();
    }
}
