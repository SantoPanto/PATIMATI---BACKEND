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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * {@code POST /api/auth/google} <b>artık yok</b> — hesap devralma açığının bekçisi.
 *
 * <p><b>Açık neydi:</b> uç, gelen {@code idToken}'ı hiç doğrulamıyordu; kimliği
 * isteğin gövdesindeki {@code email} alanından alıyordu. Kayıt herkese açık
 * olduğu için herhangi biri bedava bir hesap açıp <i>kendi</i> jetonuyla bu uca
 * gidiyor, gövdeye başkasının e-postasını yazıyor ve o kişinin oturumunu
 * alıyordu — yönetici dâhil. Yerelde üç kez çalıştırılarak doğrulandı.
 *
 * <p><b>Neden doğrulama eklenmedi de uç silindi:</b> uç kullanılmıyordu. Ön
 * yüzdeki {@code googleAuth()} hiçbir yerden çağrılmıyor ve canlıya çıkan
 * pakette {@code api/auth/google} dizgisi hiç geçmiyor. Çalışan Google girişi
 * ayrı bir yol: {@code /oauth2/authorization/google}. Yani doğrulama eklemek,
 * kimsenin kullanmadığı bir yolu ayakta tutmak olurdu.
 *
 * <p><b>Neden kimlikli istek atılıyor:</b> uç {@code permitAll} listesinde
 * olmadığı için jetonsuz istek {@code 401} alır — ve 401, "uç yok"tan ayırt
 * edilemez. Test bu yüzden kimlikli istiyor: kimlik geçtikten sonra dönen
 * {@code 404}, yönlendirmenin gerçekten kalkmış olduğunu gösterir.
 *
 * <p><b>Karşılaştırma şart:</b> var olan bir uç da sınanmazsa, her isteği 404
 * yapan bozuk bir yapılandırma bu testi <i>yanlış sebeple</i> yeşil gösterirdi.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=none",
        "jwt.secret-key=dGhpcy1pcy1hLXRlc3Qtc2VjcmV0LWtleS1mb3ItcGF0aW1hdGktYXBwbGljYXRpb24=",
        "DB_PASSWORD=dummy_test_password"
})
class HesapDevralmaUcuKapaliTest {

    /** Açığın sömürüldüğü gövdenin birebir şekli. */
    private static final String SOMURU_GOVDESI = """
            {"idToken":"uydurma","email":"admin@patimati.local","googleId":"1",\
            "firstName":"a","lastName":"b"}""";

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
    @WithMockUser
    @DisplayName("POST /api/auth/google artık yönlendirilmiyor")
    void googleUcuYok() throws Exception {
        MvcResult sonuc = mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SOMURU_GOVDESI))
                .andReturn();

        assertThat(sonuc.getResponse().getStatus())
                .withFailMessage("""
                        /api/auth/google kimlikli isteğe %d döndü, beklenen 404.
                        Bu uç GERİ GELMİŞ demektir. Geri getirilecekse idToken
                        sunucu tarafında doğrulanmadan açılmamalı: e-posta
                        DOĞRULANMIŞ jetondan okunsun, istekteki email alanına
                        güvenilmesin, rol veritabanından gelsin. Aksi hâlde
                        siteye kaydolan herkes yöneticinin oturumunu alabilir.""",
                        sonuc.getResponse().getStatus())
                .isEqualTo(404);
    }

    @Test
    @WithMockUser
    @DisplayName("Karşılaştırma: var olan uç hâlâ yönlendiriliyor")
    void varOlanUcHalaYonlendiriliyor() throws Exception {
        MvcResult sonuc = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andReturn();

        assertThat(sonuc.getResponse().getStatus())
                .withFailMessage("""
                        Var olan bir uç da 404 verdi. O zaman yukarıdaki "uç yok"
                        iddiası ucun kaldırıldığını DEĞİL, yönlendirmenin toptan
                        çalışmadığını gösteriyor olabilir.""")
                .isNotEqualTo(404);
    }
}
