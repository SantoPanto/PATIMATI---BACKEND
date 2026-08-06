package com.works.patimati.security;

import com.works.patimati.entity.User;
import com.works.patimati.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Optional;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public OAuth2LoginSuccessHandler(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional // Veritabanı kayıt işlemi için Transactional eklenmeli
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 1. Google'dan gelen net öznitelikleri alıyoruz
        String email = oAuth2User.getAttribute("email");
        String googleId = oAuth2User.getAttribute("sub"); // Google'ın benzersiz kullanıcı ID'si
        String givenName = oAuth2User.getAttribute("given_name"); // Sadece Ad
        String familyName = oAuth2User.getAttribute("family_name"); // Sadece Soyad
        String fullName = oAuth2User.getAttribute("name"); // Tam Ad (Yedek olarak)

        // Eğer ad veya soyad ayrı gelmediyse (nadir durum), null olmasın diye yedekleme yapıyoruz
        String firstName = (givenName != null && !givenName.isBlank()) ? givenName : fullName;
        String lastName = (familyName != null && !familyName.isBlank()) ? familyName : "";

        // 2. Kullanıcıyı veritabanında kontrol et
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isEmpty()) {
            // YENİ KULLANICI KAYDI
            user = new User();
            user.setEmail(email);
            user.setGoogleId(googleId); // Google ID kaydediliyor
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setRole(User.Role.USER);
            user.setEnabled(true); //Hesap aktif olarak oluşturuluyor
            userRepository.save(user);
        } else {
            // MEVCUT KULLANICI (Daha önce normal üye olduysa Google ID'sini eşleştiriyoruz)
            user = userOptional.get();
            if (user.getGoogleId() == null) {
                user.setGoogleId(googleId);
                userRepository.save(user);
            }
        }

        // 3. JWT Üret
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());

        // 4. Frontend'e JWT ile yönlendir (Örn: http://localhost:4200/login?token=abc...)
        String targetUrl = frontendUrl + "/login?token=" + token;
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}