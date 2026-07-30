package com.works.patimati.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * WebSocketConfig sınıfının endpoint ve mesaj yönlendirme sözleşmesini test eder.
 */
@ExtendWith(MockitoExtension.class)
class WebSocketConfigTest {

    private static final List<String> ALLOWED_ORIGINS = List.of(
            "http://localhost:3000",
            "http://localhost:4200"
    );

    @Mock
    private MessageBrokerRegistry messageBrokerRegistry;

    @Mock
    private StompEndpointRegistry stompEndpointRegistry;

    @Mock
    private StompWebSocketEndpointRegistration endpointRegistration;

    private WebSocketConfig webSocketConfig;

    @BeforeEach
    void setUp() {
        // Her testte üretim koduyla aynı tip güvenli properties nesnesi kullanılır.
        WebSocketProperties properties = new WebSocketProperties(ALLOWED_ORIGINS);
        webSocketConfig = new WebSocketConfig(properties);
    }

    @Test
    void shouldConfigureApplicationUserAndQueuePrefixes() {
        // Mesaj broker yapılandırması çalıştırılır.
        webSocketConfig.configureMessageBroker(messageBrokerRegistry);

        // /queue hedeflerinin Spring'in dahili broker'ına yönlendirildiğini doğrular.
        verify(messageBrokerRegistry)
                .enableSimpleBroker(WebSocketConfig.PRIVATE_QUEUE_PREFIX);

        // /app hedeflerinin ileride yazılacak @MessageMapping metotlarına gideceğini doğrular.
        verify(messageBrokerRegistry)
                .setApplicationDestinationPrefixes(
                        WebSocketConfig.APPLICATION_DESTINATION_PREFIX
                );

        // Kullanıcıya özel mesajların /user ön ekiyle çözümleneceğini doğrular.
        verify(messageBrokerRegistry)
                .setUserDestinationPrefix(WebSocketConfig.USER_DESTINATION_PREFIX);
    }

    @Test
    void shouldRegisterSockJsEndpointWithConfiguredOrigins() {
        /*
         * addEndpoint ve setAllowedOrigins zincirinin aynı registration nesnesiyle
         * devam etmesi için Mockito dönüşleri hazırlanır.
         */
        when(stompEndpointRegistry.addEndpoint(WebSocketConfig.WEBSOCKET_ENDPOINT))
                .thenReturn(endpointRegistration);
        when(endpointRegistration.setAllowedOrigins(ALLOWED_ORIGINS.toArray(String[]::new)))
                .thenReturn(endpointRegistration);

        // WebSocket/SockJS endpoint kaydı çalıştırılır.
        webSocketConfig.registerStompEndpoints(stompEndpointRegistry);

        verify(stompEndpointRegistry)
                .addEndpoint(WebSocketConfig.WEBSOCKET_ENDPOINT);
        verify(endpointRegistration)
                .setAllowedOrigins(ALLOWED_ORIGINS.toArray(String[]::new));

        // SockJS geri dönüş desteğinin unutulmadığını doğrular.
        verify(endpointRegistration).withSockJS();
    }
}