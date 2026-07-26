package com.works.patimati.controller;

import com.works.patimati.dto.AuthResponse;
import com.works.patimati.dto.GoogleAuthRequest;
import com.works.patimati.dto.LoginRequest;
import com.works.patimati.dto.RegisterRequest;
import com.works.patimati.entity.User;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    // --- MANUEL KAYIT (REGISTER) ---
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {

        // E-posta kullanımda mı kontrolü
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            // Frontend'in (örn: Flutter veya React) kolayca parse edebilmesi için JSON dönüyoruz
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Bu e-posta adresi sistemde zaten kayıtlı."));
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.USER)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return ResponseEntity.ok(new AuthResponse(token, user));
    }

    // --- MANUEL GİRİŞ (LOGIN) ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        // GÜVENLİK GÜNCELLEMESİ: User Enumeration zafiyetini önlemek için
        // kullanıcı bulunamasa da, şifresi olmasa da veya şifre yanlış olsa da
        // HER ZAMAN aynı jenerik hata mesajı ve aynı HTTP 401 kodu dönülür.
        if (userOptional.isEmpty()) {
            return getGenericAuthError();
        }

        User user = userOptional.get();

        if (user.getPassword() == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return getGenericAuthError();
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return ResponseEntity.ok(new AuthResponse(token, user));
    }

    // --- GOOGLE GİRİŞİ ---
    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@RequestBody GoogleAuthRequest request) {

        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            user.setFcmToken(request.getFcmToken());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());

            // Kullanıcı manuel kayıt olmuş ama ilk defa Google ile giriyorsa kimliği bağla
            if (user.getGoogleId() == null) {
                user.setGoogleId(request.getGoogleId());
            }

            userRepository.save(user);
        } else {
            user = User.builder()
                    .email(request.getEmail())
                    .googleId(request.getGoogleId())
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .role(User.Role.USER)
                    .fcmToken(request.getFcmToken())
                    .build();
            userRepository.save(user);
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return ResponseEntity.ok(new AuthResponse(token, user));
    }

    // --- MEVCUT KULLANICIYI GETİR ---
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Oturum süreniz dolmuş veya yetkisiz erişim."));
        }

        String email = authentication.getName();
        Optional<User> userOptional = userRepository.findByEmail(email);

        return userOptional.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Kullanıcı kaydı bulunamadı.")));
    }

    // --- YARDIMCI METOTLAR ---

    /**
     * Güvenlik açıklarını önlemek için kullanılan jenerik giriş hatası yanıtı.
     */
    private ResponseEntity<Map<String, String>> getGenericAuthError() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Geçersiz e-posta adresi veya şifre."));
    }
}