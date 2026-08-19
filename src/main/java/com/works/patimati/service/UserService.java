package com.works.patimati.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.works.patimati.dto.AuthResponse;
import com.works.patimati.dto.FcmTokenUpdateDTO;
import com.works.patimati.dto.GoogleAuthRequest;
import com.works.patimati.dto.LoginRequest;
import com.works.patimati.dto.RegisterRequest;
import com.works.patimati.dto.User.UpdateProfileRequest;
import com.works.patimati.dto.User.UserResponseDTO;
import com.works.patimati.entity.User;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.security.JwtService;
import com.works.patimati.security.LoginRateLimiter;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
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
    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final LoginRateLimiter loginRateLimiter;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

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

        return ResponseEntity.ok().body(new AuthResponse(token, convertToUserResponseDTO(user)));
    }

    // --- MANUEL GİRİŞ ---
    public ResponseEntity<?> login(LoginRequest request, String clientIp) {
        // IP bazlı brute-force koruması -- bkz. LoginRateLimiter javadoc'u
        // (bilinçli olarak e-posta/hesap bazlı DEĞİL, hesap-kilitleme DoS'una
        // açık kapı bırakmamak için). Hiçbir DB sorgusundan/şifre
        // karşılaştırmasından ÖNCE kontrol edilir.
        if (loginRateLimiter.isBlocked(clientIp)) {
            Map<String, Object> errorResponse = Map.of(
                    "success", false,
                    "message", "Çok fazla başarısız giriş denemesi. Lütfen bir süre sonra tekrar deneyin."
            );
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
        }

        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            if (!user.isEnabled()) {
                loginRateLimiter.recordFailure(clientIp);
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
                    loginRateLimiter.recordSuccess(clientIp);
                    String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
                    return ResponseEntity.ok().body(new AuthResponse(token, convertToUserResponseDTO(user)));
                }
            }
        }

        loginRateLimiter.recordFailure(clientIp);
        // Güvenlik: User Enumeration zafiyetini önlemek için standart yanıt
        Map<String, Object> errorResponse = Map.of(
                "success", false,
                "message", "Geçersiz e-posta adresi veya şifre."
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    // --- GOOGLE GİRİŞİ ---
    public ResponseEntity<?> googleLogin(GoogleAuthRequest request) {
        // request.getEmail()/getGoogleId()/getFirstName()/getLastName() ARTIK
        // HİÇ okunmuyor -- öncesinde bu alanlara client'ın gönderdiği hâliyle
        // doğrudan güveniliyordu: var olan bir kullanıcının e-postasını
        // gönderen biri, hiçbir şifre girmeden o hesabın JWT'sini alabiliyordu
        // (uç nokta permitAll, idToken hiç doğrulanmıyordu). Artık TEK kimlik
        // kaynağı, Google'a karşı doğrulanmış idToken'ın payload'ı.
        GoogleIdToken.Payload payload = verifyGoogleIdToken(request.getIdToken());

        String email = payload.getEmail();
        if (email == null || email.isBlank()) {
            throw new AccessDeniedException("Google idToken'ında e-posta bilgisi yok.");
        }
        if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
            throw new AccessDeniedException("Google e-postası doğrulanmamış.");
        }
        String googleId = payload.getSubject();
        String[] name = resolveName(payload);
        String firstName = name[0];
        String lastName = name[1];

        Optional<User> optionalUser = userRepository.findByEmail(email);
        User user;

        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            user.setFcmToken(request.getFcmToken());
            user.setFirstName(firstName);
            user.setLastName(lastName);

            if (user.getGoogleId() == null) {
                user.setGoogleId(googleId);
            }
            userRepository.save(user);
        } else {
            user = User.builder()
                    .email(email)
                    .googleId(googleId)
                    .firstName(firstName)
                    .lastName(lastName)
                    .role(User.Role.USER)
                    .fcmToken(request.getFcmToken())
                    .enabled(true)
                    .build();
            userRepository.save(user);
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return ResponseEntity.ok().body(new AuthResponse(token, convertToUserResponseDTO(user)));
    }

    /**
     * idToken'ı Google'a karşı doğrular ({@link GoogleAuthConfig}'teki
     * {@code googleIdTokenVerifier} bean'i -- imza, süre ve audience
     * (GOOGLE_CLIENT_ID) kontrolünü tek seferde yapar). Geçersiz, süresi
     * dolmuş ya da doğrulanamayan her durumda {@link AccessDeniedException}
     * fırlatır -- GlobalExceptionHandler bunu zaten 403'e çeviriyor.
     */
    private GoogleIdToken.Payload verifyGoogleIdToken(String idTokenString) {
        GoogleIdToken idToken;
        try {
            idToken = googleIdTokenVerifier.verify(idTokenString);
        } catch (GeneralSecurityException | IOException | IllegalArgumentException e) {
            throw new AccessDeniedException("Google idToken doğrulanamadı: " + e.getMessage(), e);
        }

        if (idToken == null) {
            throw new AccessDeniedException("Google idToken geçersiz veya süresi dolmuş.");
        }
        return idToken.getPayload();
    }

    /**
     * given_name/family_name standart claim'leri varsa onları kullanır;
     * yoksa (ör. istemci yalnızca "email" scope'u istediyse) "name"
     * claim'ini boşluktan ikiye böler; o da yoksa sabit bir yer tutucuya
     * düşer -- User.firstName/lastName NOT NULL, boş bırakılamaz.
     */
    private String[] resolveName(GoogleIdToken.Payload payload) {
        String given = (String) payload.get("given_name");
        String family = (String) payload.get("family_name");
        if (isPresent(given) && isPresent(family)) {
            return new String[]{truncate(given), truncate(family)};
        }

        String fullName = (String) payload.get("name");
        String[] parts = isPresent(fullName) ? fullName.trim().split("\\s+", 2) : new String[0];

        String firstName = isPresent(given) ? given : parts.length > 0 ? parts[0] : "Google";
        String lastName = isPresent(family) ? family : parts.length > 1 ? parts[1] : "Kullanıcı";
        return new String[]{truncate(firstName), truncate(lastName)};
    }

    private boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }

    private String truncate(String value) {
        return value.length() > 50 ? value.substring(0, 50) : value;
    }

    // --- GOOGLE OAUTH2 SUCCESS HANDLER KULLANICI İŞLEME ---
    @Transactional
    public User processOAuth2User(String email, String googleId, String firstName, String lastName) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isPresent()) {
            User existingUser = optionalUser.get();
            if (existingUser.getGoogleId() == null) {
                existingUser.setGoogleId(googleId);
                return userRepository.save(existingUser);
            }
            return existingUser;
        }

        User newUser = User.builder()
                .email(email)
                .googleId(googleId)
                .firstName(firstName)
                .lastName(lastName)
                .role(User.Role.USER)
                .enabled(true)
                .password(null) // SSO kullanıcılarında varsayılan olarak şifre yok
                .phone(null)    // Varsayılan olarak boş telefon no
                .build();

        return userRepository.save(newUser);
    }

    private UserResponseDTO convertToUserResponseDTO(User user) {
        if (user == null) {
            return null;
        }
        Double latitude = null;
        Double longitude = null;
        if (user.getLocation() != null) {
            longitude = user.getLocation().getX();
            latitude = user.getLocation().getY();
        }
        return UserResponseDTO.builder()
                .uid(user.getUid())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .phone(user.getPhone())
                .enabled(user.isEnabled())
                .latitude(latitude)
                .longitude(longitude)
                .lostPoints(user.getLostPoints())
                .adoptionPoints(user.getAdoptionPoints())
                .build();
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
            return ResponseEntity.ok().body(convertToUserResponseDTO(optionalUser.get()));
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
                .map(this::convertToUserResponseDTO)
                .collect(Collectors.toList());
    }

    public ResponseEntity<?> updateProfile(UpdateProfileRequest request) {
        try {
            // 1. Sisteme giriş yapmış olan kullanıcının email'ini SecurityContext'ten al
            String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

            // 2. Kullanıcıyı veritabanından bul (Repository adın userRepository olmayabilir, kendi projene göre uyarla)
            User user = userRepository.findByEmail(currentUserEmail)
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

            // 3. Bilgileri güncelle
            String phone = request.getPhone();
            if (phone != null && phone.isBlank()) {
                phone = null;
            }
            if (phone != null && userRepository.existsByPhoneAndEmailNot(phone, currentUserEmail)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Bu telefon numarası zaten kullanımda."));
            }

            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setPhone(phone);

            if (request.getLatitude() != null && request.getLongitude() != null) {
                if (request.getLatitude() < -90.0 || request.getLatitude() > 90.0) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Enlem (latitude) -90 ile 90 arasında olmalıdır."));
                }
                if (request.getLongitude() < -180.0 || request.getLongitude() > 180.0) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Boylam (longitude) -180 ile 180 arasında olmalıdır."));
                }
                Point userPoint = geometryFactory.createPoint(new Coordinate(request.getLongitude(), request.getLatitude()));
                user.setLocation(userPoint);
            }

            // Not: Email güncellemeyi destekliyorsan user.setEmail(request.getEmail()) yapabilirsin,
            // ancak email genelde benzersiz (unique) olduğu için veritabanında çakışma kontrolü yapman gerekebilir.

            // 4. Kaydet
            userRepository.save(user);

            // 5. Güncel bilgileri frontend'e döndür
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profil başarıyla güncellendi");
            response.put("user", convertToUserResponseDTO(user));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Profil güncellenirken bir hata oluştu: " + e.getMessage()));
        }
    }

    @Transactional
    public ResponseEntity<?> updateFcmToken(FcmTokenUpdateDTO request) {
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
            User user = optionalUser.get();
            user.setFcmToken(request.getFcmToken());
            userRepository.save(user);

            Map<String, Object> successResponse = Map.of(
                    "success", true,
                    "message", "FCM token başarıyla güncellendi."
            );
            return ResponseEntity.ok().body(successResponse);
        }

        Map<String, Object> notFoundResponse = Map.of(
                "success", false,
                "message", "Kullanıcı kaydı bulunamadı."
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundResponse);
    }
}