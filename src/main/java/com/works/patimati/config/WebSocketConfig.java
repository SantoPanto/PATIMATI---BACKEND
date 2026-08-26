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

    /*
     * Herkese açık (kullanıcıya özel olmayan) yayınların kanalı -- ör.
     * /topic/user-status. enableSimpleBroker'a verilen ön ekler dışındaki
     * hedefler basit broker tarafından SESSIZCE YOK SAYILIR (ne abonelik
     * kaydedilir ne de convertAndSend ile gönderilen mesaj dağıtılır);
     * bu yüzden burada AYRICA listelenmesi gerekiyor -- /queue'nun kapsamına
     * girmiyor.
     */
    // Genel yayın (Broadcast) aboneliklerinin dahili basit broker tarafından yönetileceği kanal.
    static final String PUBLIC_TOPIC_PREFIX = "/topic";

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
        org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler taskScheduler =
                new org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(1);
        taskScheduler.setThreadNamePrefix("wss-heartbeat-thread-");
        taskScheduler.initialize();

        registry.enableSimpleBroker(PRIVATE_QUEUE_PREFIX, PUBLIC_TOPIC_PREFIX)
                .setTaskScheduler(taskScheduler)
                .setHeartbeatValue(new long[] { 10000, 10000 });

        registry.setApplicationDestinationPrefixes(APPLICATION_DESTINATION_PREFIX);
        registry.setUserDestinationPrefix(USER_DESTINATION_PREFIX);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(WEBSOCKET_ENDPOINT)
                .setAllowedOrigins(properties.allowedOrigins().toArray(String[]::new))
                .withSockJS()
                .setHeartbeatTime(10000);
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