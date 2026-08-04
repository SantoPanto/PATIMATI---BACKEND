package com.works.patimati.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * application.yml içindeki WebSocket ayarlarını tip güvenli bir Java nesnesine bağlar.
 *
 * <p>İzin verilen frontend adreslerinin kaynak kod içine sabitlenmesi yerine
 * yapılandırmadan okunması; geliştirme, test ve canlı ortamların kendi origin
 * listelerini kod değişikliği yapmadan tanımlayabilmesini sağlar.</p>
 */
@Validated
@ConfigurationProperties(prefix = "app.websocket")
public record WebSocketProperties(

        /*
         * WebSocket veya SockJS bağlantısı kurmasına izin verilen frontend adresleri.
         * Liste boş bırakılamaz ve her adres metin içermelidir.
         */
        @NotEmpty
        List<@NotBlank String> allowedOrigins
) {
}