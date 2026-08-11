package com.works.patimati.service;

import com.works.patimati.dto.AuthResponse;
import com.works.patimati.dto.GoogleAuthRequest;
import com.works.patimati.dto.LoginRequest;
import com.works.patimati.dto.RegisterRequest;
import com.works.patimati.dto.SafeUserDTO;
import com.works.patimati.dto.User.UserResponseDTO;
import com.works.patimati.entity.User;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    // --- MANUEL KAYIT ---
    public ResponseEntity<?> register(RegisterRequest request) {
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());

        if (existingUser.isPresent()) {
            Map<String, Object> errorResponse = Map.of(
                    "success", false,
                    "message", "Bu e-posta adresi zaten kullanımda."
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }

        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            Map<String, Object> errorResponse = Map.of(
                    "success", false,
                    "message", "Bu telefon numarası zaten kullanımda."
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(User.Role.USER)
                .enabled(true)
                .build();

        userRepository.save(user);
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());

        return ResponseEntity.ok().body(new AuthResponse(token, convertToSafeUser(user)));
    }

    // --- MANUEL GİRİŞ ---
    public ResponseEntity<?> login(LoginRequest request) {
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            if (!user.isEnabled()) {
                Map<String, Object> errorResponse = Map.of(
                        "success", false,
                        "message", "Hesabınız askıya alınmıştır/engellenmiştir."
                );
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }

            // Kullanıcının şifresi varsa (Sadece Google ile girmemişse) doğrula
            if (user.getPassword() != null) {
                boolean isMatch = passwordEncoder.matches(request.getPassword(), user.getPassword());

                if (isMatch) {
                    String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
                    return ResponseEntity.ok().body(new AuthResponse(token, convertToSafeUser(user)));
                }
            }
        }

        // Güvenlik: User Enumeration zafiyetini önlemek için standart yanıt
        Map<String, Object> errorResponse = Map.of(
                "success", false,
                "message", "Geçersiz e-posta adresi veya şifre."
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    // --- GOOGLE GİRİŞİ ---
    public ResponseEntity<?> googleLogin(GoogleAuthRequest request) {
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());
        User user;

        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            user.setFcmToken(request.getFcmToken());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());

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
                    .enabled(true)
                    .build();
            userRepository.save(user);
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return ResponseEntity.ok().body(new AuthResponse(token, convertToSafeUser(user)));
    }

    private SafeUserDTO convertToSafeUser(User user) {
        if (user == null) {
            return null;
        }
        return new SafeUserDTO(
                user.getUid(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole() != null ? user.getRole().name() : null
        );
    }

    // --- OTURUM SAHİBİNİ GETİR ---
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            Map<String, Object> errorResponse = Map.of(
                    "success", false,
                    "message", "Oturum süreniz dolmuş veya yetkisiz erişim."
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        String email = authentication.getName();
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isPresent()) {
            return ResponseEntity.ok().body(convertToSafeUser(optionalUser.get()));
        }

        Map<String, Object> notFoundResponse = Map.of(
                "success", false,
                "message", "Kullanıcı kaydı bulunamadı."
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundResponse);
    }
    // --- ÇIKIŞ YAP (LOGOUT) ---
    @Transactional
    public ResponseEntity<?> logout() {
        // 1. O anki giriş yapmış kullanıcıyı bul
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();

            // 2. KULLANICININ BİLDİRİM TOKEN'INI (FCM TOKEN) SİL
            // Böylece çıkış yapmış telefona artık PatiMati anlık bildirimi (push notification) GİTMEZ!
            userRepository.findByEmail(username).ifPresent(user -> {
                user.setFcmToken(null);
                userRepository.save(user);
            });
        }

        SecurityContextHolder.clearContext();

        Map<String, Object> successResponse = Map.of(
                "success", true,
                "message", "Başarıyla çıkış yapıldı ve bildirimler durduruldu."
        );

        return ResponseEntity.ok().body(successResponse);
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> UserResponseDTO.builder()
                        .uid(user.getUid()) // Hata veren getId() burasıydı
                        .email(user.getEmail())
                        .build())
                .collect(Collectors.toList());
    }
}