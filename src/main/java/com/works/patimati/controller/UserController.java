package com.works.patimati.controller;

import com.works.patimati.dto.*;
import com.works.patimati.dto.User.UpdateProfileRequest;
import com.works.patimati.dto.User.UserResponseDTO;
import com.works.patimati.service.PasswordResetService;
import com.works.patimati.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        // getRemoteAddr() BİLEREK doğrudan okunuyor, X-Forwarded-For BURADA
        // elle AYRIŞTIRILMIYOR: başlık istemcinin kendisi tarafından
        // uydurulabilir, elle okumak LoginRateLimiter'ı tamamen atlatılabilir
        // kılardı (canlı testle doğrulandı). Bunun yerine
        // application.yml'deki `server.forward-headers-strategy: native`
        // gömülü Tomcat'e bu işi güvenilir biçimde yaptırıyor -- yalnızca
        // uygulamaya DOĞRUDAN bağlanan (üretimde: güvenilen ters proxy)
        // taraf tarafından ayarlanan başlığa göre getRemoteAddr()'ı yeniden
        // yazar; istemcinin ucundan gelen bir X-Forwarded-For proxy
        // tarafından zaten üzerine yazılmış/değiştirilmiş olur.
        return authService.login(request, httpRequest.getRemoteAddr());
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@Valid @RequestBody GoogleAuthRequest request) {
        return authService.googleLogin(request);
    }

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