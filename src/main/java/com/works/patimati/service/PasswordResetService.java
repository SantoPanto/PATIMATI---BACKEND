package com.works.patimati.service;

import com.works.patimati.dto.ForgotPasswordRequest;
import com.works.patimati.dto.ResetPasswordRequest;
import com.works.patimati.entity.PasswordResetToken;
import com.works.patimati.entity.User;
import com.works.patimati.repository.PasswordResetTokenRepository;
import com.works.patimati.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /**
     * Sıfırlama bağlantısının açacağı ön yüz adresi. OAuth yönlendirmesiyle
     * aynı kaynaktan gelir; canlıda {@code FRONTEND_URL=https://patimati.me}.
     */
    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * A. Token üretme ve e-posta gönderme.
     *
     * <p><b>Cevap her koşulda aynıdır</b> — kullanıcı yok, mail sunucusu
     * yapılandırılmamış ya da gönderim hata vermiş olsa bile. Farklı cevap,
     * hangi e-postaların kayıtlı olduğunu dışarıya saydırırdı (enumeration).
     * Gönderim sonucu yalnız günlüğe yazılır.</p>
     */
    @Transactional
    public ResponseEntity<?> processForgotPassword(ForgotPasswordRequest request) {
        // 1. Kullanıcıyı e-posta ile veritabanında ara
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // 2. Benzersiz bir token üret
            String generatedToken = UUID.randomUUID().toString();

            // 3. Token'ın süresini belirle (Şu andan itibaren 15 dakika)
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setToken(generatedToken);
            resetToken.setUser(user);
            resetToken.setExpiryDate(Instant.now().plus(15, ChronoUnit.MINUTES));

            // 4. Veritabanına kaydet
            tokenRepository.save(resetToken);

            // 5. Bağlantıyı e-postayla gönder. Başarısızlık cevabı DEĞİŞTİRMEZ
            //    (yukarıdaki sınıf sözleşmesi); EmailService zaten fırlatmaz,
            //    yine de beklenmedik bir istisna 500 olup sözü bozmasın diye
            //    burada da yutulur.
            String resetUrl = frontendUrl + "/reset-password?token=" + generatedToken;
            try {
                emailService.sendResetPasswordEmail(user.getEmail(), resetUrl);
            } catch (RuntimeException exception) {
                log.error("Şifre sıfırlama e-postası akışı beklenmedik hata verdi", exception);
            }
        }

        // Güvenlik gereği: Kullanıcı sistemde olsa da olmasa da hep aynı cevabı dönüyoruz.
        // Bu sayede saldırganlar hangi e-postaların kayıtlı olduğunu tespit edemez.
        // Gövde JSON: ön yüz request() cevabı response.json() ile okuyor,
        // düz metin gövde orada null'a düşüyordu.
        return ResponseEntity.ok(Map.of(
                "message",
                "Eğer sistemimizde kayıtlı bir hesabınız varsa, şifre sıfırlama bağlantısı e-posta adresinize gönderilmiştir."
        ));
    }

    /**
     * B. Şifre Güncelleme Metodu
     */
    @Transactional
    public ResponseEntity<?> processResetPassword(ResetPasswordRequest request) {
        // 1. Frontend'den gelen token'ı veritabanında ara
        Optional<PasswordResetToken> tokenOptional = tokenRepository.findByToken(request.getToken());

        if (tokenOptional.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Geçersiz bir şifre sıfırlama bağlantısı."));
        }

        PasswordResetToken resetToken = tokenOptional.get();

        // 2. Token süresi dolmuş mu kontrol et
        if (resetToken.getExpiryDate().isBefore(Instant.now())) {
            // Süresi dolmuş token'ı veritabanında tutmaya gerek yok, temizleyelim.
            tokenRepository.delete(resetToken);
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Bu şifre sıfırlama bağlantısının süresi dolmuş. Lütfen yeni bir bağlantı talep edin."));
        }

        // 3. Süre dolmamışsa token'a bağlı kullanıcıyı çek
        User user = resetToken.getUser();

        // 4. Yeni şifreyi güvenli bir şekilde şifrele (hash'le) ve set et
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        // 5. Kullanıcıyı yeni şifresiyle kaydet
        userRepository.save(user);

        // 6. ÇOK ÖNEMLİ: Aynı linkle şifrenin tekrar değiştirilememesi için token'ı imha et
        tokenRepository.delete(resetToken);

        return ResponseEntity.ok(Map.of(
                "message",
                "Şifreniz başarıyla güncellenmiştir. Yeni şifrenizle giriş yapabilirsiniz."
        ));
    }
}