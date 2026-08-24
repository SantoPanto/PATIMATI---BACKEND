package com.works.patimati.config;

import com.works.patimati.security.InternalServiceAuthFilter;
import com.works.patimati.security.JwtAuthFilter;
import com.works.patimati.security.OAuth2AuthenticationSuccessHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CorsConfigTest {

    @Mock
    private JwtAuthFilter jwtAuthFilter;

    @Mock
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Mock
    private InternalServiceAuthFilter internalServiceAuthFilter;

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(jwtAuthFilter, oAuth2AuthenticationSuccessHandler, internalServiceAuthFilter);
    }

    @Test
    @DisplayName("corsConfigurationSource, dinamik olarak atanan allowedOrigins listesini ve CORS kurallarını doğru aktarmalıdır")
    void shouldConfigureCorsSourceWithDynamicAllowedOrigins() {
        List<String> mockOrigins = List.of("http://localhost:5173", "https://patimati.me");
        ReflectionTestUtils.setField(securityConfig, "allowedOrigins", mockOrigins);

        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/public/test");
        CorsConfiguration config = source.getCorsConfiguration(request);

        assertThat(config).isNotNull();
        assertThat(config.getAllowedOrigins()).containsExactlyElementsOf(mockOrigins);
        assertThat(config.getAllowedMethods()).contains("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
        assertThat(config.getAllowedHeaders()).contains("Authorization", "Content-Type");
        assertThat(config.getAllowCredentials()).isTrue();
    }
}
