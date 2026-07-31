package com.works.patimati.config;

import com.works.patimati.security.WebSocketChannelInterceptor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * PatiMati'nin STOMP tabanlı WebSocket mesajlaşma altyapısını yapılandırıyor.
 */
@Configuration(proxyBeanMethods = false)
@EnableWebSocketMessageBroker
@EnableConfigurationProperties(WebSocketProperties.class)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // Frontend'in WebSocket/SockJS bağlantısını başlatacağı HTTP endpoint'i.
    static final String WEBSOCKET_ENDPOINT = "/ws-connect";

    // Controller tarafındaki @MessageMapping metotlarına gönderilen mesajların ön eki.
    static final String APPLICATION_DESTINATION_PREFIX = "/app";

    // Her kullanıcıya özel hedeflerin Spring tarafından ayırt edilmesini sağlayan ön ek.
    static final String USER_DESTINATION_PREFIX = "/user";

    // Özel mesaj aboneliklerinin dahili basit broker tarafından yönetileceği kanal.
    static final String PRIVATE_QUEUE_PREFIX = "/queue";

    private final WebSocketProperties properties;
    private final WebSocketChannelInterceptor webSocketChannelInterceptor;

    public WebSocketConfig(
            WebSocketProperties properties,
            WebSocketChannelInterceptor webSocketChannelInterceptor)
    {
        this.properties = properties;
        this.webSocketChannelInterceptor = webSocketChannelInterceptor;
    }

    /*
     * STOMP mesajlarının uygulama metotlarına mı yoksa broker'a mı
     * yönlendirileceğini belirler.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        /*
         * /queue ile başlayan hedefleri Spring'in bellek içi broker'ı yönetir.
         * Faz 2 kapsamındaki kullanıcıya özel mesajlar /user/queue/... adresinden
         * dinlenecek ve gerçek oturuma özel kuyruğa Spring tarafından çevrilecektir.
         */
        registry.enableSimpleBroker(PRIVATE_QUEUE_PREFIX);

        // /app ile başlayan mesajlar ileride yazılacak @MessageMapping metotlarına gider.
        registry.setApplicationDestinationPrefixes(APPLICATION_DESTINATION_PREFIX);

        // Kullanıcıya özel mesaj hedeflerinin genel ön ekini açıkça tanımlar.
        registry.setUserDestinationPrefix(USER_DESTINATION_PREFIX);
    }

    /**
     * Frontend istemcilerinin STOMP bağlantısını başlatacağı endpoint'i kaydeder.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        /*
         * Origin listesi application.yml üzerinden gelir. Böylece bilinmeyen
         * web sitelerinin tarayıcı üzerinden mesajlaşma bağlantısı kurması engellenir.
         *
         * withSockJS(), tarayıcı veya ağ ortamı doğrudan WebSocket kullanamadığında
         * SockJS'in uygun geri dönüş taşıma yöntemini seçmesini sağlar.
         */
        registry.addEndpoint(WEBSOCKET_ENDPOINT)
                .setAllowedOrigins(properties.allowedOrigins().toArray(String[]::new))
                .withSockJS();
    }

    /*
     * İstemciden sunucuya gelen STOMP paketlerinin güvenlik
     * interceptor'ından geçmesini sağlar.
     */

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        /*
         * Bu kayıt yapılmazsa WebSocketChannelInterceptor bir Spring bean'i
         * olsa bile CONNECT paketlerini yakalayamaz ve JWT kontrolü çalışmaz.
         */
        registration.interceptors(webSocketChannelInterceptor);
    }
}