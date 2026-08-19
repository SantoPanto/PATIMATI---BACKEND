package com.works.patimati.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

/**
 * {@code /internal/**} için paylaşılan-sır kimlik doğrulaması (Faz 2 revize
 * blueprint §48). Kullanıcı JWT'siyle KARIŞTIRILMAZ — bilinçli olarak ayrı
 * ve en dar kapsamlı: Collector'ın kimlik bilgisi sızsa bile yalnızca bu
 * tek uç noktaya erişim sağlar, kullanıcı hesaplarına, DB şifresine ya da
 * tam AWS yetkisine DEĞİL (en az yetki ilkesi).
 *
 * <p>Bu, mevcut kodda hiç bulunmayan bir desendir (yalnızca kullanıcı-JWT
 * ve rol tabanlı URL eşleştirme vardı) — bilinçli olarak yeni ve minimaldir.
 */
@Component
public class InternalServiceAuthFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Internal-Api-Key";

    private final String expectedKey;

    public InternalServiceAuthFilter(@Value("${internal.ingestion.api-key:}") String expectedKey) {
        this.expectedKey = expectedKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        if (!request.getRequestURI().startsWith("/internal/")) {
            chain.doFilter(request, response);
            return;
        }

        String provided = request.getHeader(HEADER);
        // MessageDigest.isEqual: sabit-zamanlı karşılaştırma -- düz .equals()
        // ilk uyuşmayan karakterde erken çıkar, bu da yanıt süresinden anahtarı
        // karakter karakter tahmin etmeye izin veren bir timing-attack yüzeyi
        // açar. Paylaşılan sır bir X-Internal-Api-Key olduğu için (kullanıcı
        // şifresi değil, ama yine de gizli bir kimlik bilgisi) aynı özenle
        // karşılaştırılmalı.
        if (expectedKey == null || expectedKey.isBlank() || provided == null
                || !MessageDigest.isEqual(
                        provided.getBytes(StandardCharsets.UTF_8),
                        expectedKey.getBytes(StandardCharsets.UTF_8))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Bu isteği yetkilendirilmiş kabul et — kullanıcı Principal'ı YOK,
        // yalnızca dahili servis kimliği.
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "internal-service", null, List.of()));

        chain.doFilter(request, response);
    }
}
