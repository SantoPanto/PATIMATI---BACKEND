package com.works.patimati.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * EmailService'in üç hâli: yapılandırılmamış, çalışan, hata veren.
 *
 * <p>"Yapılandırılmamış" vakası bu sınıfın varlık sebebi — canlıda SMTP
 * kurulana kadar bu daldan geçilecek ve akışın 500'e düşmemesi buna bağlı.
 */
class EmailServiceTest {

    @SuppressWarnings("unchecked")
    private static ObjectProvider<JavaMailSender> saglayici(JavaMailSender sender) {
        ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(sender);
        return provider;
    }

    @Test
    @DisplayName("Mail sunucusu yapılandırılmamışsa: fırlatmaz, false döner")
    void yapilandirmaYoksaSessizceVazgecer() {
        EmailService service = new EmailService(saglayici(null), "");

        boolean sonuc = service.sendResetPasswordEmail(
                "biri@ornek.com", "https://patimati.me/reset-password?token=x");

        assertThat(sonuc).isFalse();
    }

    @Test
    @DisplayName("Yapılandırılmışsa: alıcı, konu, bağlantı ve gönderen doğru gider")
    void mesajDogruAlanlarlaGonderilir() {
        JavaMailSender sender = mock(JavaMailSender.class);
        EmailService service = new EmailService(saglayici(sender), "noreply@patimati.me");

        boolean sonuc = service.sendResetPasswordEmail(
                "biri@ornek.com", "https://patimati.me/reset-password?token=abc123");

        assertThat(sonuc).isTrue();

        ArgumentCaptor<SimpleMailMessage> mesajYakala =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(sender).send(mesajYakala.capture());
        SimpleMailMessage mesaj = mesajYakala.getValue();

        assertThat(mesaj.getTo()).containsExactly("biri@ornek.com");
        assertThat(mesaj.getFrom()).isEqualTo("noreply@patimati.me");
        assertThat(mesaj.getSubject()).contains("Şifre Sıfırlama");
        assertThat(mesaj.getText())
                .contains("https://patimati.me/reset-password?token=abc123")
                .contains("15 dakika");
    }

    @Test
    @DisplayName("Gönderim hata verirse: fırlatmaz, false döner")
    void gonderimHatasiYutulur() {
        JavaMailSender sender = mock(JavaMailSender.class);
        doThrow(new MailSendException("SMTP ulaşılamıyor"))
                .when(sender).send(any(SimpleMailMessage.class));
        EmailService service = new EmailService(saglayici(sender), "");

        boolean sonuc = service.sendResetPasswordEmail(
                "biri@ornek.com", "https://patimati.me/reset-password?token=x");

        assertThat(sonuc).isFalse();
    }
}
