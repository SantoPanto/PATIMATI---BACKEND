package com.works.patimati.controller;

import com.works.patimati.dto.AuthResponse;
import com.works.patimati.dto.GoogleAuthRequest;
import com.works.patimati.entity.User;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@RequestBody GoogleAuthRequest request) {

        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            user.setFcmToken(request.getFcmToken());
            userRepository.save(user);
        } else {
            user = User.builder()
                    .email(request.getEmail())
                    .googleId(request.getGoogleId())
                    .fullName(request.getFullName())
                    .role(User.Role.USER)
                    .fcmToken(request.getFcmToken())
                    .build();
            userRepository.save(user);
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());

        return ResponseEntity.ok(new AuthResponse(token, user));
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = authentication.getName();
        Optional<User> userOptional = userRepository.findByEmail(email);

        return userOptional.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}