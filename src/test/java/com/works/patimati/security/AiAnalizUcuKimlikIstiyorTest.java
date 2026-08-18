package com.works.patimati.security;

import com.works.patimati.repository.AdComplaintRepository;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.MessageRepository;
import com.works.patimati.repository.PasswordResetTokenRepository;
import com.works.patimati.repository.UserComplaintRepository;
import com.works.patimati.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

/**
 * {@code /api/ai/analyze} <b>kimlik ister</b> — A1'in taşıyıcı iddiası.
 *
 * <p><b>Neden gerçek filtre zinciri:</b> bu uç, tarayıcının AI'ya doğrudan
 * gitmesini bitirmek için var. Kimlik istemezse hiçbir şey kazanılmaz: analiz
 * yine herkese açık kalır, yalnız kapı değişmiş olur. Standalone MockMvc kurulumu
 * güvenlik filtresini hiç çalıştırmadığı için bu iddiayı ölçemez —
 * {@code AiAnalyzeControllerTest} bilerek başka bir şeyi ölçüyor.
 *
 * <p><b>Koruma listeye yazılarak değil, varsayılanla geliyor:</b>
 * {@code SecurityConfig} sonunda {@code anyRequest().authenticated()} var ve bu
 * uç {@code permitAll} listesinde yok. Test tam da bunu ölçüyor — biri ileride
 * {@code /api/ai/**}'ı o listeye eklerse kırmızı yanar.
 *
 * <p><b>Pozitif kontrol şart:</b> açık bir uç da sınanmazsa, her isteği reddeden
 * bozuk bir yapılandırma bu testi <i>yanlış sebeple</i> yeşil gösterirdi.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=none",
        "jwt.secret-key=dGhpcy1pcy1hLXRlc3Qtc2VjcmV0LWtleS1mb3ItcGF0aW1hdGktYXBwbGljYXRpb24=",
        "DB_PASSWORD=dummy_test_password"
})
class AiAnalizUcuKimlikIstiyorTest {

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
    @DisplayName("Jetonsuz analiz isteği reddedilir")
    void jetonsuzIstekReddedilir() throws Exception {
        MvcResult sonuc = mockMvc.perform(multipart("/api/ai/analyze")
                        .file(new MockMultipartFile("file", "kedi.jpg", "image/jpeg", new byte[]{1, 2, 3})))
                .andReturn();

        assertThat(sonuc.getResponse().getStatus())
                .withFailMessage("""
                        /api/ai/analyze jetonsuz isteği %d ile karşıladı.
                        Bu uç kimlik istemezse A1 hiçbir şey kazandırmaz: analiz
                        yine herkese açık kalır, yalnız kapı değişir. permitAll
                        listesine /api/ai eklenmiş olabilir.""",
                        sonuc.getResponse().getStatus())
                .isIn(401, 403);
    }

    @Test
    @DisplayName("Pozitif kontrol: açık uç hâlâ jetonsuz erişilebilir")
    void acikUcJetonsuzErisilebilir() throws Exception {
        MvcResult sonuc = mockMvc.perform(get("/api/public/ads/nearby")
                        .param("latitude", "40.0")
                        .param("longitude", "29.0")).andReturn();

        assertThat(sonuc.getResponse().getStatus())
                .withFailMessage("""
                        Açık uç da %d verdi. O zaman yukarıdaki "reddedildi"
                        iddiası bu ucun korunduğunu DEĞİL, yapılandırmanın her
                        isteği reddettiğini gösteriyor olabilir.""",
                        sonuc.getResponse().getStatus())
                .isNotIn(401, 403);
    }
}
