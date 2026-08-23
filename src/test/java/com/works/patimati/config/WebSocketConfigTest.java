package com.works.patimati.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;

import com.works.patimati.security.WebSocketChannelInterceptor;
import org.springframework.messaging.simp.config.ChannelRegistration;

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
    private org.springframework.messaging.simp.config.SimpleBrokerRegistration simpleBrokerRegistration;

    @Mock
    private org.springframework.web.socket.config.annotation.SockJsServiceRegistration sockJsServiceRegistration;

    @Mock
    private StompEndpointRegistry stompEndpointRegistry;

    @Mock
    private StompWebSocketEndpointRegistration endpointRegistration;
    @Mock
    private WebSocketChannelInterceptor webSocketChannelInterceptor;

    @Mock
    private ChannelRegistration channelRegistration;

    private WebSocketConfig webSocketConfig;

    @BeforeEach
    void setUp() {
        // Her testte üretim koduyla aynı tip güvenli properties nesnesi kullanılır.
        WebSocketProperties properties = new WebSocketProperties(ALLOWED_ORIGINS);
        webSocketConfig = new WebSocketConfig(
                properties,
                webSocketChannelInterceptor
        );
    }

    @Test
    void shouldConfigureApplicationUserAndQueuePrefixes() {
        when(messageBrokerRegistry.enableSimpleBroker(WebSocketConfig.PRIVATE_QUEUE_PREFIX, WebSocketConfig.PUBLIC_TOPIC_PREFIX))
                .thenReturn(simpleBrokerRegistration);
        when(simpleBrokerRegistration.setTaskScheduler(org.mockito.ArgumentMatchers.any()))
                .thenReturn(simpleBrokerRegistration);

        // Mesaj broker yapılandırması çalıştırılır.
        webSocketConfig.configureMessageBroker(messageBrokerRegistry);

        // /queue ve /topic hedeflerinin Spring'in dahili broker'ına yönlendirildiğini doğrular.
        verify(messageBrokerRegistry)
                .enableSimpleBroker(WebSocketConfig.PRIVATE_QUEUE_PREFIX, WebSocketConfig.PUBLIC_TOPIC_PREFIX);

        // /app hedeflerinin ileride yazılacak @MessageMapping metotlarına gideceğini doğrular.
        verify(messageBrokerRegistry)
                .setApplicationDestinationPrefixes(
                        WebSocketConfig.APPLICATION_DESTINATION_PREFIX
                );

        // Kullanıcıya özel mesajların /user ön ekiyle çözümleneceğini doğrular.
        verify(messageBrokerRegistry)
                .setUserDestinationPrefix(WebSocketConfig.USER_DESTINATION_PREFIX);
    }

    /**
     * "Mesajlar canli gelmiyor, F5 gerekiyor, 1-2 mesaj sonra kesiliyor"
     * sikayetinin ikinci ayagi — BEKCISI YOKTU.
     *
     * <p><b>Olculen arıza (23.08, once canli sonra yerel):</b> sunucunun
     * CONNECTED cercevesi <b>heart-beat: 0,0</b> diyordu. STOMP'ta kalp atisi
     * PAZARLIKLIDIR: sunucu 0 derse istemci de gondermez. Iki uctan da trafik
     * akmayan baglanti "yari acik" kalir; tarayici hâlâ bagli sandigi icin
     * stompjs yeniden baglanmaz, mesaj gelmez ve kullanici F5 atmak zorunda
     * kalir. Araya giren Cloudflare/nginx bosta duran WebSocket'i zaten bir
     * sure sonra kapatir.
     *
     * <p><b>Duzeltmeden sonra ayni olcum:</b> heart-beat: 10000,10000 ve
     * iki gercek STOMP istemcisi arasinda teslimat 0 -> 1.
     *
     * <p><b>Neden ayri bir bekci:</b> yukaridaki test zinciri yalnizca
     * CALISTIRIYOR (setTaskScheduler'i stub'liyor) ama kalp atisi degerinin
     * verilip verilmedigine BAKMIYOR. Deger silinse hicbir test kizarmaz ve
     * ariza sessizce geri doner. Ayrica deger tek basina yetmez: TaskScheduler
     * verilmezse Spring'in SimpleBroker'i atisi GONDEREMEZ — bu yuzden ikisi
     * birlikte kilitleniyor.
     */
    @Test
    void shouldEnableBrokerHeartbeatWithScheduler() {
        when(messageBrokerRegistry.enableSimpleBroker(
                WebSocketConfig.PRIVATE_QUEUE_PREFIX, WebSocketConfig.PUBLIC_TOPIC_PREFIX))
                .thenReturn(simpleBrokerRegistration);
        when(simpleBrokerRegistration.setTaskScheduler(org.mockito.ArgumentMatchers.any()))
                .thenReturn(simpleBrokerRegistration);

        webSocketConfig.configureMessageBroker(messageBrokerRegistry);

        org.mockito.ArgumentCaptor<long[]> atis =
                org.mockito.ArgumentCaptor.forClass(long[].class);
        verify(simpleBrokerRegistration).setHeartbeatValue(atis.capture());

        org.assertj.core.api.Assertions.assertThat(atis.getValue()).hasSize(2);
        org.assertj.core.api.Assertions.assertThat(atis.getValue()[0]).isGreaterThan(0L);
        org.assertj.core.api.Assertions.assertThat(atis.getValue()[1]).isGreaterThan(0L);

        // Zamanlayici olmadan yukaridaki deger UYGULANMAZ.
        verify(simpleBrokerRegistration).setTaskScheduler(org.mockito.ArgumentMatchers.any());
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
        when(endpointRegistration.withSockJS())
                .thenReturn(sockJsServiceRegistration);

        // WebSocket/SockJS endpoint kaydı çalıştırılır.
        webSocketConfig.registerStompEndpoints(stompEndpointRegistry);

        verify(stompEndpointRegistry)
                .addEndpoint(WebSocketConfig.WEBSOCKET_ENDPOINT);
        verify(endpointRegistration)
                .setAllowedOrigins(ALLOWED_ORIGINS.toArray(String[]::new));

        // SockJS geri dönüş desteğinin unutulmadığını doğrular.
        verify(endpointRegistration).withSockJS();
    }

    @Test
    void shouldRegisterSecurityInterceptorOnInboundChannel() {
        // İstemciden gelen STOMP paketleri için inbound kanal yapılandırılır.
        webSocketConfig.configureClientInboundChannel(channelRegistration);

        /*
         * JWT kontrolünü gerçekleştiren interceptor'ın
         * inbound kanala eklendiğini doğrular.
         */
        verify(channelRegistration)
                .interceptors(webSocketChannelInterceptor);
    }
}