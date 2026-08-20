package com.works.patimati.controller;

import com.works.patimati.dto.*;
import com.works.patimati.dto.User.UpdateProfileRequest;
import com.works.patimati.dto.User.UserResponseDTO;
import com.works.patimati.service.PasswordResetService;
import com.works.patimati.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.List;

@RestController
@RequestMapping({"/api/auth", "/api/v1/users"})
@RequiredArgsConstructor
public class UserController {

    private final UserService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /*
     * /api/auth/google KALDIRILDI (D-7 / hesap devralma).
     *
     * NEDEN: uc, gelen idToken'i HIC dogrulamiyordu; kimligi istegin govdesindeki
     * "email" alanindan aliyordu. Kayit herkese acik oldugu icin herhangi biri
     * bedava bir hesap acip kendi jetonuyla bu uca gidiyor ve govdeye baskasinin
     * e-postasini yazarak o kisinin oturumunu aliyordu -- yonetici dahil.
     *
     * NEDEN DOGRULAMA EKLENMEDI DE SILINDI: uc kullanilmiyordu. On yuzdeki
     * googleAuth() fonksiyonu hicbir yerden cagrilmiyor ve canliya cikan pakette
     * "api/auth/google" dizgisi hic gecmiyor. Calisan Google girisi ayri bir yol:
     * /oauth2/authorization/google -> OAuth2AuthenticationSuccessHandler ->
     * UserService.processOAuth2User (bu akista kimlik Google tarafinda dogrulanir).
     *
     * GERI GETIRILECEKSE: idToken sunucu tarafinda dogrulanmadan ACILMASIN.
     * GoogleIdTokenVerifier yeterli; ihtiyaci olan GOOGLE_CLIENT_ID zaten bagli.
     * E-posta DOGRULANMIS jetondan okunsun, istekteki alana guvenilmesin;
     * rol her zaman veritabanindan gelsin. Bekci: HesapDevralmaUcuKapaliTest.
     */

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        return authService.getCurrentUser();
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return authService.logout();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return passwordResetService.processForgotPassword(request);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return passwordResetService.processResetPassword(request);
    }

    /**
     * Giriş yapmış kullanıcının şifresini değiştirir.
     * PUT /api/auth/change-password
     */
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            authService.changePassword(
                    authentication.getName(),
                    request
            );
            return ResponseEntity.noContent().build();
        }
        return authService.changePassword(request);
    }

    @GetMapping("/userlist")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> users = authService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return authService.updateProfile(request);
    }

    @PutMapping("/fcm-token")
    public ResponseEntity<?> updateFcmToken(@Valid @RequestBody FcmTokenUpdateDTO request) {
        return authService.updateFcmToken(request);
    }
}