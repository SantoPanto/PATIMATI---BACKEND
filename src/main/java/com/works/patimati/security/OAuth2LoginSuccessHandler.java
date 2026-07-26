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
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // Kullanıcıyı veritabanında kontrol et, yoksa kaydet
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isEmpty()) {
            user = new User();
            user.setEmail(email);
            user.setFirstName(name);
            user.setLastName(name);
            user.setRole(User.Role.USER);
            userRepository.save(user);
        } else {
            user = userOptional.get();
        }

        // JWT Üret
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());

        // Frontend'e JWT ile yönlendir (Örn: http://localhost:4200/login?token=abc...)
        String targetUrl = frontendUrl + "/login?token=" + token;
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
