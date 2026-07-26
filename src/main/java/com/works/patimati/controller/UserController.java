package com.works.patimati.controller;

import com.works.patimati.dto.GoogleAuthRequest;
import com.works.patimati.dto.LoginRequest;
import com.works.patimati.dto.RegisterRequest;
import com.works.patimati.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
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
}