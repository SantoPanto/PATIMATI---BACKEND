package com.works.patimati.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Uygulamanın e-posta çıkışı (bugün tek kullanım: şifre sıfırlama).
 *
 * <p><b>Mail sunucusu isteğe bağlıdır.</b> {@link JavaMailSender} bean'i
 * ancak {@code spring.mail.host} tanımlıysa oluşur (Spring'in kendi koşulu);
 * bu yüzden bean {@link ObjectProvider} ile alınır ve yokluğu bir hata değil
 * bilinen bir durumdur: gönderim istenirse "yapılandırılmadı" günlüğü basılır
 * ve {@code false} dönülür. Böylece mail altyapısı kurulmamış ortamlarda
 * (yerel, prova) uygulama ayağa kalkmaya ve şifre sıfırlama ucu aynı cevabı
 * vermeye devam eder.
 *
 * <p><b>Hata fırlatmaz.</b> Çağıranın (şifre sıfırlama akışı) kullanıcıya
 * hesabın varlığını sezdirmeme sözü var; buradan sızan bir istisna o sözü
 * 500'e çevirirdi. Başarısızlık günlüğe yazılır, çağırana boolean döner.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String gonderen;

    public EmailService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.mail.from:}") String gonderen
    ) {
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.gonderen = gonderen;
    }

    /**
     * Şifre sıfırlama bağlantısını gönderir.
     *
     * @return gönderim gerçekten yapıldıysa {@code true}; mail sunucusu
     *         yapılandırılmamışsa ya da gönderim hata verdiyse {@code false}
     */
    public boolean sendResetPasswordEmail(String to, String resetUrl) {
        if (mailSender == null) {
            log.warn("Şifre sıfırlama e-postası GÖNDERİLMEDİ: mail sunucusu "
                    + "yapılandırılmamış (SPRING_MAIL_HOST boş). alici={}", to);
            return false;
        }

        SimpleMailMessage mesaj = new SimpleMailMessage();
        if (gonderen != null && !gonderen.isBlank()) {
            mesaj.setFrom(gonderen);
        }
        mesaj.setTo(to);
        mesaj.setSubject("PATIMATI — Şifre Sıfırlama");
        mesaj.setText("""
                Merhaba,

                PATIMATI hesabın için şifre sıfırlama isteği aldık. Yeni \
                şifreni belirlemek için aşağıdaki bağlantıya tıkla \
                (bağlantı 15 dakika geçerlidir):

                %s

                Bu isteği sen yapmadıysan bu e-postayı yok sayabilirsin; \
                şifren değişmeden kalır.

                PATIMATI ekibi""".formatted(resetUrl));

        try {
            mailSender.send(mesaj);
            log.info("Şifre sıfırlama e-postası gönderildi: alici={}", to);
            return true;
        } catch (MailException exception) {
            log.error("Şifre sıfırlama e-postası gönderilemedi: alici={}", to, exception);
            return false;
        }
    }
}
