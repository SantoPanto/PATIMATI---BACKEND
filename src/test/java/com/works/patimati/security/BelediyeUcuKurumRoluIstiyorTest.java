package com.works.patimati.security;

import com.works.patimati.entity.User;
import com.works.patimati.repository.AdComplaintRepository;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.MessageRepository;
import com.works.patimati.repository.PasswordResetTokenRepository;
import com.works.patimati.repository.UserComplaintRepository;
import com.works.patimati.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * {@code /api/municipality/**} yalnız kurum (belediye) hesaplarına açık —
 * B parçasının taşıyıcı iddiası.
 *
 * <p><b>Neden gerçek filtre zinciri:</b> bu kural yalnızca SecurityConfig'de
 * bir satır. Standalone MockMvc kurulumu güvenlik filtresini hiç çalıştırmaz,
 * yani o satırı ölçemez. Satır yanlışlıkla silinir ya da {@code permitAll}
 * listesine {@code /api/municipality} eklenirse, giriş yapmış HERHANGİ bir
 * kullanıcı belediye panelini ve ihbar kuyruğunu okur.
 *
 * <p><b>Neden uç henüz yokken yazıldı:</b> yetkilendirme, isteği işleyecek
 * denetleyiciden ÖNCE çalışıyor — kural olmadığında cevap 403 değil 404 olur.
 * Bu yüzden A (panel) ve C2 (ihbar kuyruğu) uçları yazılmadan da kapının
 * kapalı olduğu ölçülebiliyor. Uçlar geldiğinde bu test aynen geçerli kalır.
 *
 * <p><b>Pozitif kontrol şart</b> ({@link #kurum_hesabi_yetkiden_gecer()} ve
 * {@link #acik_uc_jetonsuz_erisilebilir()}): yalnız "USER reddedildi" ölçülürse,
 * her isteği reddeden bozuk bir yapılandırma bu testi YANLIŞ SEBEPLE yeşil
 * gösterirdi. İki yön aynı koşuda ölçülüyor.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=none",
        "jwt.secret-key=dGhpcy1pcy1hLXRlc3Qtc2VjcmV0LWtleS1mb3ItcGF0aW1hdGktYXBwbGljYXRpb24=",
        "DB_PASSWORD=dummy_test_password"
})
class BelediyeUcuKurumRoluIstiyorTest {

    /** Kişi 2 ve Kişi 3'ün uçları buraya gelecek; yol yeter, denetleyici gerekmiyor. */
    private static final String BELEDIYE_UCU = "/api/municipality/panel/ozet";

    @MockitoBean
    private DataSource dataSource;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private AdRepository adRepository;
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

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void kullaniciVar() {
        // JwtAuthFilter jetonu doğruladıktan sonra kullanıcıyı veritabanında
        // arıyor ve "enabled" olmasını istiyor; bulunmazsa kimlik hiç kurulmaz
        // ve testin ölçtüğü şey ROL DEĞİL kullanıcının yokluğu olurdu.
        when(userRepository.findByEmail(anyString())).thenAnswer(cagri -> {
            User kullanici = new User();
            kullanici.setUid(1L);
            kullanici.setEmail(cagri.getArgument(0));
            kullanici.setEnabled(true);
            return Optional.of(kullanici);
        });
    }

    private MvcResult istek(String rol) throws Exception {
        String jeton = jwtService.generateToken(rol.toLowerCase() + "@patimati.local", rol);
        return mockMvc.perform(get(BELEDIYE_UCU).header("Authorization", "Bearer " + jeton))
                .andReturn();
    }

    @Test
    @DisplayName("Sıradan kullanıcı belediye ucuna 403 alır")
    void siradan_kullanici_reddedilir() throws Exception {
        int durum = istek("USER").getResponse().getStatus();

        assertThat(durum)
                .withFailMessage("""
                        Giriş yapmış sıradan kullanıcı %s adresine %d ile ulaştı.
                        Beklenen 403. Bu kural yalnızca SecurityConfig'deki
                        "/api/municipality/** -> hasAnyRole(INSTITUTION, ADMIN)"
                        satırı; silinmiş ya da /api/public/** benzeri bir permitAll
                        deseninin altına düşmüş olabilir. Kural yoksa belediye
                        paneli ve ihbar kuyruğu herkese açıktır.""",
                        BELEDIYE_UCU, durum)
                .isEqualTo(403);
    }

    @Test
    @DisplayName("Jetonsuz istek reddedilir")
    void jetonsuz_reddedilir() throws Exception {
        int durum = mockMvc.perform(get(BELEDIYE_UCU)).andReturn().getResponse().getStatus();

        assertThat(durum)
                .withFailMessage("Jetonsuz istek %d ile karşılandı; 401/403 bekleniyordu.", durum)
                .isIn(401, 403);
    }

    @Test
    @DisplayName("Pozitif kontrol: kurum hesabı yetkiden geçer (404 = kapı açık, uç henüz yok)")
    void kurum_hesabi_yetkiden_gecer() throws Exception {
        int durum = istek("INSTITUTION").getResponse().getStatus();

        assertThat(durum)
                .withFailMessage("""
                        Kurum hesabı da %d aldı. O zaman yukarıdaki "reddedildi"
                        iddiası kuralın DOĞRU çalıştığını değil, yapılandırmanın
                        bu yolu herkese kapattığını gösteriyor olabilir.
                        Beklenen: yetkiden geçmek (uç henüz yazılmadığı için 404).""",
                        durum)
                .isNotIn(401, 403);
    }

    @Test
    @DisplayName("Pozitif kontrol: ADMIN de yetkiden geçer (prova için bilerek)")
    void yonetici_yetkiden_gecer() throws Exception {
        int durum = istek("ADMIN").getResponse().getStatus();

        assertThat(durum)
                .withFailMessage("""
                        ADMIN %d aldı. Yönetici, prova ve sunumda paneli kurum
                        hesabı açmadan görebilmek için bilerek listede
                        (SecurityConfig: hasAnyRole("INSTITUTION","ADMIN")).
                        Kapsam yine de ilçesiz açılmaz — onu MunicipalityScopeService
                        engelliyor, kapı değil.""", durum)
                .isNotIn(401, 403);
    }

    @Test
    @DisplayName("Pozitif kontrol: açık uç hâlâ jetonsuz erişilebilir")
    void acik_uc_jetonsuz_erisilebilir() throws Exception {
        int durum = mockMvc.perform(get("/api/public/ads/nearby")
                        .param("latitude", "40.0")
                        .param("longitude", "29.0"))
                .andReturn().getResponse().getStatus();

        assertThat(durum)
                .withFailMessage("""
                        Açık uç da %d verdi — yapılandırma her isteği reddediyor
                        olabilir; o hâlde bu dosyadaki hiçbir "reddedildi" iddiası
                        bir şey kanıtlamaz.""", durum)
                .isNotIn(401, 403);
    }
}
