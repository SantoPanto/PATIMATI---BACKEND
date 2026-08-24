package com.works.patimati.security;

import com.works.patimati.entity.User;
import com.works.patimati.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserService userService;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public OAuth2AuthenticationSuccessHandler(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 1. Google'dan gelen kullanıcı bilgilerini alıyoruz
        String email = oAuth2User.getAttribute("email");
        String googleId = oAuth2User.getAttribute("sub"); // Google benzersiz kullanıcı kimliği
        String givenName = oAuth2User.getAttribute("given_name");
        String familyName = oAuth2User.getAttribute("family_name");
        String fullName = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture"); // Profil görseli URL'i

        // Ad - Soyad ayrıştırma ve varsayılan yedekleme mantığı
        String firstName = (givenName != null && !givenName.isBlank()) ? givenName :
                ((fullName != null && !fullName.isBlank()) ? fullName : "Google");
        String lastName = (familyName != null && !familyName.isBlank()) ? familyName : "User";

        // 2. Kullanıcı arama / oluşturma iş mantığı (UserService üzerinden SOLID - SRP uyumlu)
        User user = userService.processOAuth2User(email, googleId, firstName, lastName);

        // 3. Mevcut JwtService kullanılarak JWT üretilmesi
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());

        // 4. Frontend adresine güvenli URL yönlendirmesi
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                .path("/oauth-redirect")
                .queryParam("token", token)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
