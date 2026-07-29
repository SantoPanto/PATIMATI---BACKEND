package com.works.patimati.config;

import com.works.patimati.security.JwtAuthFilter;
import com.works.patimati.security.OAuth2LoginSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Bağımlılıkları tanımlıyoruz
    private final JwtAuthFilter jwtAuthFilter;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    // Constructor (Yapıcı Metot) ile Spring'in bu sınıfları otomatik enjekte etmesini sağlıyoruz
    public SecurityConfig(JwtAuthFilter jwtAuthFilter, OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
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
                                "/v3/api-docs/**",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password"
                        ).permitAll() // Kayıt, giriş ve açık uçlara HERKES erişebilsin
                        .anyRequest().authenticated() // Diğer tüm uç noktalar için token/giriş zorunlu olsun
                )

                // 3. EN ÖNEMLİ KISIM: Yetkisiz erişimlerde ProblemDetail formatında JSON dön
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
                )

                // 4. OAuth2 Giriş Ayarları ve Başarı Yöneticisi
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2LoginSuccessHandler)
                );

        // JWT filtresini UsernamePasswordAuthenticationFilter'dan önce çalışacak şekilde ekliyoruz
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Frontend'in çalıştığı adreslere izin ver
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:4200"));

        // İzin verilen HTTP metodları
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // İzin verilen başlıklar
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));

        // Kimlik bilgilerinin (credentials) gönderilmesine izin ver
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}