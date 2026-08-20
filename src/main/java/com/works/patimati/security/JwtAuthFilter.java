package com.works.patimati.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.util.AntPathMatcher;

import com.works.patimati.repository.UserRepository;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        AntPathMatcher pathMatcher = new AntPathMatcher();


        return pathMatcher.match("/api/auth/forgot-password", path) ||
                pathMatcher.match("/api/auth/reset-password", path);
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 1. Authorization başlığını kontrol et
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            if (logger.isDebugEnabled()) {
                logger.debug("No Authorization Bearer header found for path: " + request.getRequestURI());
            }
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Token'ı ayır
        jwt = authHeader.substring(7);

        try {
            userEmail = jwtService.extractEmail(jwt);

            // 3. Email alınabildiyse ve SecurityContext henüz boşsa doğrulamaya geç
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                if (jwtService.validateToken(jwt)) {
                    var userOpt = userRepository.findByEmail(userEmail);
                    if (userOpt.isEmpty()) {
                        logger.warn("JWT validation succeeded but user not found in database: email=" + userEmail + ", path=" + request.getRequestURI());
                    } else if (!userOpt.get().isEnabled()) {
                        logger.warn("Disabled user attempted access: email=" + userEmail + ", path=" + request.getRequestURI());
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"title\":\"Hesap Engellendi\",\"status\":403,\"detail\":\"Hesabınız engellenmiştir.\"}");
                        return;
                    } else {
                        String role = jwtService.extractRole(jwt);

                        // Rol bilgisiyle yetkilendirme objesini oluştur
                        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userEmail,
                                null, // Parola yok çünkü JWT tabanlı
                                Collections.singletonList(authority)
                        );

                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // 4. SecurityContext'e yerleştir
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Successfully set SecurityContext for user email=" + userEmail + ", path=" + request.getRequestURI());
                        }
                    }
                } else {
                    logger.warn("JWT token is expired or invalid for path: " + request.getRequestURI());
                }
            }
        } catch (Exception ex) {
            // Token süresi geçmiş veya geçersiz
            logger.error("JWT Authentication failed for path " + request.getRequestURI() + ": " + ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
