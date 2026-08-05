package com.works.patimati;

import com.works.patimati.repository.*;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=none",
        "jwt.secret-key=dGhpcy1pcy1hLXRlc3Qtc2VjcmV0LWtleS1mb3ItcGF0aW1hdGktYXBwbGljYXRpb24=",
        "DB_PASSWORD=dummy_test_password"
})
class PatiMatiApplicationTests {

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

    @Test
    void contextLoads() {
    }

}
