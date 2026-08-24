package com.works.patimati.config;

import com.works.patimati.security.InternalServiceAuthFilter;
import com.works.patimati.security.JwtAuthFilter;
import com.works.patimati.security.OAuth2AuthenticationSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final InternalServiceAuthFilter internalServiceAuthFilter;

    @Value("${app.cors.allowed-origins:${app.frontend.url:http://localhost:5173}}")
    private List<String> allowedOrigins;

    // Constructor (Yapıcı Metot) ile Spring'in bu sınıfları otomatik enjekte etmesini sağlıyoruz
    public SecurityConfig(JwtAuthFilter jwtAuthFilter, OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler,
                           InternalServiceAuthFilter internalServiceAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
        this.internalServiceAuthFilter = internalServiceAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CORS ayarlarını entegre et
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 1. REST API olduğu için CSRF korumasını kapatıyoruz
                .csrf(AbstractHttpConfigurer::disable)

                // Oturum yönetimini STATELESS olarak ayarla (JWT için gerekli)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 2. Hangi isteklere izin verileceğini belirliyoruz
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/public/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/api/ads/*/poster",
                                "/api/v1/ads/*/poster",
                                "/error",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                /*
                                 * WebSocket ve SockJS HTTP handshake isteklerinin
                                 * Spring Security filtresinden geçmesine izin verilir.
                                 *
                                 * Bu permitAll ayarı mesajlaşmayı güvenliksiz yapmaz.
                                 * Kullanıcının JWT doğrulaması STOMP CONNECT paketinde
                                 * WebSocketChannelInterceptor tarafından gerçekleştirilir.
                                 */
                                "/ws-connect", // Doğrudan WebSocket bağlantısını kapsar.
                                "/ws-connect/**" // SockJS’in kullandığı alt adresleri kapsar.
                        ).permitAll() // Kayıt, giriş, OAuth2 ve açık uçlara HERKES erişebilsin
                        .requestMatchers("/api/auth/userlist", "/api/admin/**").hasRole("ADMIN") // Yöneticilere özel uç noktalar
                        // /internal/** normal kullanıcı JWT'si DEĞİL, paylaşılan-sır
                        // başlığı ister (InternalServiceAuthFilter). Burada permitAll
                        // GÖRÜNMÜYOR bilerek: filtre, anahtar tutmazsa isteği zaten
                        // 401 ile keser; tutarsa bir Authentication kurar ve
                        // aşağıdaki anyRequest().authenticated() doğal olarak geçer.
                        .anyRequest().authenticated() // Diğer tüm uç noktalar için token/giriş zorunlu olsun
                )

                // 3. EN ÖNEMLİ KISIM: Yetkisiz ve Yetkisiz Erişim (401 ve 403) Durumlarında ProblemDetail JSON dön
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                            // RFC 7807 (ProblemDetail) standardına uygun JSON formatı
                            String problemDetailJson = String.format(
                                    "{" +
                                            "\"type\":\"about:blank\"," +
                                            "\"title\":\"Yetkisiz Erişim\"," +
                                            "\"status\":%d," +
                                            "\"detail\":\"%s\"," +
                                            "\"instance\":\"%s\"" +
                                            "}",
                                    HttpStatus.UNAUTHORIZED.value(),
                                    "Bu işlemi gerçekleştirmek için geçerli bir kimlik doğrulama token'ı gereklidir.",
                                    request.getRequestURI()
                            );

                            response.getWriter().write(problemDetailJson);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);

                            String problemDetailJson = String.format(
                                    "{" +
                                            "\"type\":\"about:blank\"," +
                                            "\"title\":\"Erişim Reddedildi\"," +
                                            "\"status\":%d," +
                                            "\"detail\":\"%s\"," +
                                            "\"instance\":\"%s\"" +
                                            "}",
                                    HttpStatus.FORBIDDEN.value(),
                                    "Bu kaynağa erişmek için yönetici (ADMIN) yetkisine sahip olmalısınız.",
                                    request.getRequestURI()
                                );

                            response.getWriter().write(problemDetailJson);
                        })
                )

                // 4. OAuth2 Giriş Ayarları ve Başarı Yöneticisi
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                );

        // JWT filtresini UsernamePasswordAuthenticationFilter'dan önce çalışacak şekilde ekliyoruz
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        // /internal/** için paylaşılan-sır doğrulaması JWT filtresinden ÖNCE çalışır —
        // Collector kullanıcı JWT'si taşımaz, kendi dar kapsamlı anahtarını taşır.
        http.addFilterBefore(internalServiceAuthFilter, JwtAuthFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Konfigürasyondan (application.yml/prod.yml veya CORS_ALLOWED_ORIGINS ortam değişkeni) okunan dinamik origin listesi
        configuration.setAllowedOrigins(allowedOrigins);

        // İzin verilen HTTP metodları
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // İzin verilen ve dışarıya açılan HTTP başlıkları (Headers)
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Location"));

        // Kimlik bilgilerinin (credentials/cookies/headers) gönderilmesine izin ver
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                "/v3/api-docs/**",
                "/v3/api-docs",
                "/swagger-ui/**",
                "/swagger-ui.html"
        );
    }
}