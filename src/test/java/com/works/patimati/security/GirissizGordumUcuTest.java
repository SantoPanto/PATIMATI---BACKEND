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

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

/**
 * {@code POST /api/public/ads/{id}/sightings} <b>girişsiz erişilebilir</b> —
 * özelliğin taşıyıcı iddiası: afişteki QR'ı okutan kişi üye değildir; kayıt
 * bariyeri gelirse özellik ölü doğar (ürün kararı 22.08).
 *
 * <p>Gerçek filtre zinciriyle ölçülüyor (AiAnalizUcuKimlikIstiyorTest'in
 * kalıbı): standalone MockMvc güvenliği hiç çalıştırmadığı için bu iddiayı
 * ölçemezdi. Biri ileride {@code /api/public/**}'ı permitAll listesinden
 * daraltırsa bu test kırmızı yanar.
 *
 * <p>Beklenti yalnız "401/403 DEĞİL": bu bağlamda DataSource mock olduğu
 * için @Transactional servis işlem açamaz (CannotCreateTransactionException
 * → 500) — yani DB'siz 404'e ulaşmak yapısal olarak imkânsız, 500 burada
 * "güvenlikten geçti, servise ulaştı"nın kanıtıdır. Somut 500'ü asserte
 * bağlamıyoruz: o değer ürünün değil test ortamının özelliği.
 * GET tarafının kimlik istediği de ayrıca kilitli (varsayılanla korunuyor).
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=none",
        "jwt.secret-key=dGhpcy1pcy1hLXRlc3Qtc2VjcmV0LWtleS1mb3ItcGF0aW1hdGktYXBwbGljYXRpb24=",
        "DB_PASSWORD=dummy_test_password"
})
class GirissizGordumUcuTest {

    private static final String GECERLI_GOVDE = """
            {"latitude": 40.19, "longitude": 29.06,
             "note": "Parkta gördüm", "reporterContact": "0555 111 22 33"}
            """;

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
    @DisplayName("Jetonsuz görülme bildirimi kapıdan geçer (servis 404'üne ulaşır)")
    void jetonsuzBirakmaKapidanGecer() throws Exception {
        MvcResult sonuc = mockMvc.perform(multipart("/api/public/ads/1/sightings")
                        .file(new MockMultipartFile(
                                "sighting", "", "application/json",
                                GECERLI_GOVDE.getBytes(StandardCharsets.UTF_8))))
                .andReturn();

        assertThat(sonuc.getResponse().getStatus())
                .withFailMessage("""
                        Girişsiz görülme bildirimi %d aldı. 401/403 ise uç artık
                        kimlik istiyor demektir — QR senaryosu ölür; /api/public/**
                        permitAll satırı daraltılmış olabilir.""",
                        sonuc.getResponse().getStatus())
                .isNotIn(401, 403);
    }

    @Test
    @DisplayName("Görülme listesi jetonsuz istekte reddedilir")
    void jetonsuzListelemeReddedilir() throws Exception {
        MvcResult sonuc = mockMvc.perform(get("/api/ads/1/sightings")).andReturn();

        assertThat(sonuc.getResponse().getStatus())
                .withFailMessage("""
                        /api/ads/1/sightings jetonsuz isteği %d ile karşıladı.
                        Görülmeler yalnız ilan sahibine açık olmalı (ürün kararı) —
                        bu uç permitAll'a eklenmiş olabilir.""",
                        sonuc.getResponse().getStatus())
                .isIn(401, 403);
    }
}
