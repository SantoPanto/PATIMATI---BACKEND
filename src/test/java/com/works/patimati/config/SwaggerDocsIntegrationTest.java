package com.works.patimati.config;

import com.works.patimati.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=none",
        "jwt.secret-key=dGhpcy1pcy1hLXRlc3Qtc2VjcmV0LWtleS1mb3ItcGF0aW1hdGktYXBwbGljYXRpb24=",
        "DB_PASSWORD=dummy_test_password"
})
class SwaggerDocsIntegrationTest {

    @MockitoBean
    private DataSource dataSource;
    @MockitoBean
    private AdRepository adRepository;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private AdComplaintRepository adComplaintRepository;
    @MockitoBean
    private UserComplaintRepository userComplaintRepository;
    @MockitoBean
    private MessageRepository messageRepository;
    @MockitoBean
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("/v3/api-docs adresi 200 OK ile OpenAPI JSON şeması döndürür")
    void shouldReturnOpenApiJsonDocs() throws Exception {
        MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).contains("openapi");
        assertThat(responseBody).contains("PatiMati API Dokümantasyonu");
    }

    @Test
    @DisplayName("/swagger-ui.html veya /swagger-ui/index.html erişilebilir (200 OK veya 302 Redirect)")
    void shouldAccessSwaggerUi() throws Exception {
        MvcResult result = mockMvc.perform(get("/swagger-ui/index.html"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isIn(200, 302);
    }
}
