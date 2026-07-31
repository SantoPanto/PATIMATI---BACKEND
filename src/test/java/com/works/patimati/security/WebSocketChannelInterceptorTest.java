package com.works.patimati.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * WebSocketChannelInterceptor sınıfının STOMP CONNECT paketlerindeki
 * JWT doğrulama davranışını test eder.
 *
 * Bu test gerçek bir WebSocket bağlantısı açmaz. STOMP paketlerini
 * programatik olarak oluşturarak interceptor'ın güvenlik
 * kararlarını birim testi seviyesinde doğrular.
 */
@ExtendWith(MockitoExtension.class)
class WebSocketChannelInterceptorTest {

    private static final String TOKEN = "valid.jwt.token";
    private static final String USER_EMAIL = "user@patimati.com";
    private static final String USER_ROLE = "USER";

    @Mock
    private JwtService jwtService;

    @Mock
    private MessageChannel messageChannel;

    private WebSocketChannelInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new WebSocketChannelInterceptor(jwtService);
    }

    @Test
    void shouldAuthenticateValidConnectFrame() {
        Message<byte[]> message = createMessage(
                StompCommand.CONNECT,
                "Bearer " + TOKEN
        );

        // Geçerli token senaryosu JwtService mock'u üzerinde hazırlanır.
        when(jwtService.validateToken(TOKEN)).thenReturn(true);
        when(jwtService.extractEmail(TOKEN)).thenReturn(USER_EMAIL);
        when(jwtService.extractRole(TOKEN)).thenReturn(USER_ROLE);

        Message<?> result = interceptor.preSend(message, messageChannel);

        // Interceptor mevcut STOMP mesajını kanala geri vermelidir.
        assertSame(message, result);

        /*
         * Interceptor tarafından güncellenen gerçek accessor alınır.
         * Yeni bir accessor oluşturmak yerine mesajdaki mevcut accessor
         * kullanılmalıdır.
         */
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                message,
                StompHeaderAccessor.class
        );

        assertNotNull(accessor);

        Authentication authentication =
                (Authentication) accessor.getUser();

        assertNotNull(authentication);
        assertEquals(USER_EMAIL, authentication.getName());
        assertTrue(authentication.isAuthenticated());

        // JWT içerisindeki USER rolü ROLE_USER yetkisine çevrilmelidir.
        assertTrue(
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority().equals("ROLE_USER")
                        )
        );

        verify(jwtService).validateToken(TOKEN);
        verify(jwtService).extractEmail(TOKEN);
        verify(jwtService).extractRole(TOKEN);
    }

    @Test
    void shouldRejectConnectFrameWithoutAuthorizationHeader() {
        Message<byte[]> message = createMessage(
                StompCommand.CONNECT,
                null
        );

        /*
         * Authorization başlığı bulunmayan CONNECT paketinin
         * bağlantıya devam etmesine izin verilmemelidir.
         */
        assertThrows(
                AuthenticationCredentialsNotFoundException.class,
                () -> interceptor.preSend(message, messageChannel)
        );

        // Header bulunmadığı için JwtService çağrılmamalıdır.
        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldRejectConnectFrameWithInvalidToken() {
        Message<byte[]> message = createMessage(
                StompCommand.CONNECT,
                "Bearer " + TOKEN
        );

        // JwtService token'ın geçersiz olduğunu bildirir.
        when(jwtService.validateToken(TOKEN)).thenReturn(false);

        assertThrows(
                BadCredentialsException.class,
                () -> interceptor.preSend(message, messageChannel)
        );

        /*
         * Geçersiz olduğu bilinen bir token içerisinden kullanıcı
         * bilgileri okunmaya çalışılmamalıdır.
         */
        verify(jwtService, never()).extractEmail(TOKEN);
        verify(jwtService, never()).extractRole(TOKEN);
    }

    @Test
    void shouldIgnoreFramesOtherThanConnect() {
        Message<byte[]> message = createMessage(
                StompCommand.SEND,
                null
        );

        Message<?> result =
                interceptor.preSend(message, messageChannel);

        /*
         * Kullanıcı CONNECT sırasında doğrulandığı için sonraki SEND
         * paketlerinde JWT tekrar tekrar kontrol edilmez.
         */
        assertSame(message, result);
        verifyNoInteractions(jwtService);
    }

    /**
     * Testler için gerçek STOMP başlık yapısını kullanan mesaj oluşturur.
     */
    private Message<byte[]> createMessage(
            StompCommand command,
            String authorizationHeader
    ) {
        StompHeaderAccessor accessor =
                StompHeaderAccessor.create(command);

        if (authorizationHeader != null) {
            accessor.setNativeHeader(
                    "Authorization",
                    authorizationHeader
            );
        }

        /*
         * Interceptor'ın accessor.setUser(...) ile mesaj başlıklarını
         * güncelleyebilmesi için accessor değiştirilebilir bırakılır.
         */
        accessor.setLeaveMutable(true);

        return MessageBuilder.createMessage(
                new byte[0],
                accessor.getMessageHeaders()
        );
    }
}