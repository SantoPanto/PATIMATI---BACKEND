package com.works.patimati.security;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket istemcisinden gelen STOMP CONNECT paketini JWT ile doğrular.
 *
 * Tarayıcı tabanlı WebSocket ve SockJS istemcileri handshake isteğine
 * uygulamaya özel Authorization başlığı ekleyemediği için kimlik doğrulama
 * STOMP protokolünün CONNECT aşamasında gerçekleştirilir.
 *
 * Doğrulanan kullanıcı StompHeaderAccessor#setUser ile bağlantının
 * Principal bilgisine atanır. Spring bu kullanıcıyı aynı WebSocket
 * oturumundaki sonraki SEND ve SUBSCRIBE paketleriyle ilişkilendirir.
 */
@Slf4j
@Component
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public WebSocketChannelInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * İstemciden inbound kanala gönderilen her STOMP paketinden önce çalışır.
     *
     * Yalnızca CONNECT paketi kimlik doğrulamasına tabi tutulur.
     * Doğrulanan kullanıcı sonraki paketlerde Spring tarafından
     * WebSocket oturumuna bağlı tutulur.
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                message,
                StompHeaderAccessor.class
        );

        // Heartbeat veya STOMP dışı mesajlarda accessor bulunmayabilir.
        if (accessor == null || (accessor.getCommand() != StompCommand.CONNECT && accessor.getCommand() != StompCommand.STOMP)) {
            return message;
        }

        // SockJS kısıtlamalarına karşın STOMP frame native header listesi okunur
        List<String> authHeaders = accessor.getNativeHeader(AUTHORIZATION_HEADER);
        if (authHeaders == null || authHeaders.isEmpty()) {
            authHeaders = accessor.getNativeHeader("authorization");
        }
        if (authHeaders == null || authHeaders.isEmpty()) {
            authHeaders = accessor.getNativeHeader("passcode");
        }

        String authorizationHeader = (authHeaders != null && !authHeaders.isEmpty()) ? authHeaders.get(0) : null;

        String token = extractBearerToken(authorizationHeader);

        /*
         * JwtService, token imzasını ve geçerlilik süresini REST
         * güvenliğiyle aynı şekilde kontrol eder.
         */
        if (!jwtService.validateToken(token)) {
            throw new BadCredentialsException(
                    "WebSocket bağlantısı için JWT geçersiz veya süresi dolmuş."
            );
        }

        String email = jwtService.extractEmail(token);
        String role = jwtService.extractRole(token);

        // Principal oluşturmak için e-posta ve rol bilgileri bulunmalıdır.
        if (!StringUtils.hasText(email) || !StringUtils.hasText(role)) {
            throw new BadCredentialsException(
                    "JWT gerekli kullanıcı bilgilerini içermiyor."
            );
        }

        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority("ROLE_" + role);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        email,
                        null, // Parola taşınmaz; kullanıcı JWT ile doğrulanmıştır.
                        List.of(authority)
                );

        /*
         * Kritik satır:
         * Spring, /user ile başlayan kullanıcıya özel mesaj hedeflerini
         * bu Principal nesnesinin getName() değeri üzerinden çözümler.
         *
         * Bizim Principal ismimiz kullanıcının e-posta adresidir.
         */
        accessor.setUser(authentication);
        log.info("[WebSocket Debug] WebSocket CONNECT authenticated: user={}", email);

        return message;
    }

    /**
     * STOMP CONNECT native header'ından Bearer token değerini ayırır.
     */
    private String extractBearerToken(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader)) {
            throw new AuthenticationCredentialsNotFoundException(
                    "WebSocket bağlantısı için Authorization başlığı gereklidir."
            );
        }

        String trimmed = authorizationHeader.trim();
        if (trimmed.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            trimmed = trimmed.substring(BEARER_PREFIX.length()).trim();
        }

        if (!StringUtils.hasText(trimmed)) {
            throw new AuthenticationCredentialsNotFoundException(
                    "WebSocket bağlantısındaki token boş olamaz."
            );
        }

        return trimmed;
    }
}