package com.works.patimati.service;

import com.works.patimati.dto.ForgotPasswordRequest;
import com.works.patimati.dto.ResetPasswordRequest;
import com.works.patimati.entity.PasswordResetToken;
import com.works.patimati.entity.User;
import com.works.patimati.repository.PasswordResetTokenRepository;
import com.works.patimati.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Şifre sıfırlama akışı (B: "şifremi unuttum maili çalışmıyor" ekip maddesi).
 *
 * <p>Buradaki en önemli iddia tek tek vakalar değil, <b>vakaların birbirine
 * eşitliği</b>: unutulan-şifre ucu kullanıcı VAR / YOK / mail PATLADI
 * durumlarının üçünde de AYNI cevabı vermek zorunda — fark, hangi e-postaların
 * kayıtlı olduğunu dışarı saydırır. Bu yüzden ilk test üç durumu tek çıktıda
 * kıyaslar (pozitif ve negatif aynı ölçümde).
 */
class PasswordResetServiceTest {

    private static final String ON_YUZ = "https://patimati.me";

    private UserRepository userRepository;
    private PasswordResetTokenRepository tokenRepository;
    private PasswordEncoder passwordEncoder;
    private EmailService emailService;
    private PasswordResetService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        tokenRepository = mock(PasswordResetTokenRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        emailService = mock(EmailService.class);

        service = new PasswordResetService(
                userRepository,
                tokenRepository,
                passwordEncoder,
                emailService
        );
        ReflectionTestUtils.setField(service, "frontendUrl", ON_YUZ);
    }

    @Test
    @DisplayName("Cevap üç durumda da AYNI: kullanıcı var / yok / mail hata verdi")
    void unutmaCevabiHesapVarligini_Sizdirmaz() {
        User kayitli = User.builder().uid(7L).email("var@patimati.me").build();

        when(userRepository.findByEmail("var@patimati.me"))
                .thenReturn(Optional.of(kayitli));
        when(userRepository.findByEmail("yok@patimati.me"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("patlak@patimati.me"))
                .thenReturn(Optional.of(User.builder().uid(8L).email("patlak@patimati.me").build()));
        when(emailService.sendResetPasswordEmail(eq("patlak@patimati.me"), anyString()))
                .thenThrow(new IllegalStateException("SMTP çöktü"));

        ResponseEntity<?> varCevap = service.processForgotPassword(istek("var@patimati.me"));
        ResponseEntity<?> yokCevap = service.processForgotPassword(istek("yok@patimati.me"));
        ResponseEntity<?> patlakCevap = service.processForgotPassword(istek("patlak@patimati.me"));

        assertThat(varCevap.getStatusCode().value()).isEqualTo(200);
        assertThat(yokCevap.getStatusCode()).isEqualTo(varCevap.getStatusCode());
        assertThat(patlakCevap.getStatusCode()).isEqualTo(varCevap.getStatusCode());
        assertThat(yokCevap.getBody()).isEqualTo(varCevap.getBody());
        assertThat(patlakCevap.getBody()).isEqualTo(varCevap.getBody());
    }

    @Test
    @DisplayName("Kullanıcı varsa: token kaydedilir, mail o token'lı bağlantıyla gider")
    void tokenKaydedilirVeMailDogruBaglantiylaGider() {
        User kayitli = User.builder().uid(7L).email("var@patimati.me").build();
        when(userRepository.findByEmail("var@patimati.me"))
                .thenReturn(Optional.of(kayitli));

        Instant oncesi = Instant.now();
        service.processForgotPassword(istek("var@patimati.me"));

        ArgumentCaptor<PasswordResetToken> tokenYakala =
                ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(tokenYakala.capture());
        PasswordResetToken kayit = tokenYakala.getValue();

        assertThat(kayit.getToken()).isNotBlank();
        assertThat(kayit.getUser()).isSameAs(kayitli);
        // 15 dakikalık pencere: uçlar test süresine tolerans bırakır.
        assertThat(kayit.getExpiryDate())
                .isAfter(oncesi.plus(14, ChronoUnit.MINUTES))
                .isBefore(oncesi.plus(16, ChronoUnit.MINUTES));

        verify(emailService).sendResetPasswordEmail(
                "var@patimati.me",
                ON_YUZ + "/reset-password?token=" + kayit.getToken()
        );
    }

    @Test
    @DisplayName("Kullanıcı yoksa ne token yazılır ne mail denemesi yapılır")
    void kullaniciYoksaYanEtkiYok() {
        when(userRepository.findByEmail("yok@patimati.me"))
                .thenReturn(Optional.empty());

        service.processForgotPassword(istek("yok@patimati.me"));

        verify(tokenRepository, never()).save(any());
        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("Geçerli token: şifre kodlanıp kaydedilir, token imha edilir")
    void gecerliTokenSifreyiGunceller() {
        User sahip = User.builder().uid(7L).email("var@patimati.me").build();
        PasswordResetToken token = new PasswordResetToken();
        token.setToken("gecerli-token");
        token.setUser(sahip);
        token.setExpiryDate(Instant.now().plus(5, ChronoUnit.MINUTES));

        when(tokenRepository.findByToken("gecerli-token"))
                .thenReturn(Optional.of(token));
        when(passwordEncoder.encode("YeniSifre1")).thenReturn("kodlanmis");

        ResponseEntity<?> cevap = service.processResetPassword(
                sifirlamaIstegi("gecerli-token", "YeniSifre1"));

        assertThat(cevap.getStatusCode().value()).isEqualTo(200);
        assertThat(sahip.getPassword()).isEqualTo("kodlanmis");
        verify(userRepository).save(sahip);
        verify(tokenRepository).delete(token);
    }

    @Test
    @DisplayName("Süresi dolmuş token: 400 döner, token silinir, şifreye dokunulmaz")
    void suresiDolmusTokenReddedilir() {
        PasswordResetToken token = new PasswordResetToken();
        token.setToken("bayat-token");
        token.setUser(User.builder().uid(7L).build());
        token.setExpiryDate(Instant.now().minus(1, ChronoUnit.MINUTES));

        when(tokenRepository.findByToken("bayat-token"))
                .thenReturn(Optional.of(token));

        ResponseEntity<?> cevap = service.processResetPassword(
                sifirlamaIstegi("bayat-token", "YeniSifre1"));

        assertThat(cevap.getStatusCode().value()).isEqualTo(400);
        verify(tokenRepository).delete(token);
        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("Bilinmeyen token: 400")
    void bilinmeyenTokenReddedilir() {
        when(tokenRepository.findByToken("uydurma"))
                .thenReturn(Optional.empty());

        ResponseEntity<?> cevap = service.processResetPassword(
                sifirlamaIstegi("uydurma", "YeniSifre1"));

        assertThat(cevap.getStatusCode().value()).isEqualTo(400);
        verify(userRepository, never()).save(any());
    }

    private static ForgotPasswordRequest istek(String email) {
        ForgotPasswordRequest istek = new ForgotPasswordRequest();
        istek.setEmail(email);
        return istek;
    }

    private static ResetPasswordRequest sifirlamaIstegi(String token, String sifre) {
        ResetPasswordRequest istek = new ResetPasswordRequest();
        istek.setToken(token);
        istek.setNewPassword(sifre);
        return istek;
    }
}
