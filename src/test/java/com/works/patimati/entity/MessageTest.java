package com.works.patimati.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Message entity'sinin temel davranışlarını kontrol eder.
 *
 * Bu test Spring uygulamasını ve veritabanını başlatmaz.
 * Bu nedenle JWT, PostgreSQL veya Flyway ayarlarına ihtiyaç duymaz.
 */
class MessageTest {

    /**
     * Yeni oluşturulan bir mesajın varsayılan olarak
     * okunmamış durumda olması gerektiğini doğrular.
     */
    @Test
    void shouldCreateMessageAsUnreadByDefault() {

        // Test sırasında gönderen kullanıcıyı temsil eder.
        User sender = User.builder()
                .uid(1L)
                .build();

        // Test sırasında mesajı alan kullanıcıyı temsil eder.
        User recipient = User.builder()
                .uid(2L)
                .build();

        /*
         * read alanını özellikle set etmiyoruz.
         * Message entity'sindeki @Builder.Default değerinin
         * false olarak uygulanmasını bekliyoruz.
         */
        Message message = Message.builder()
                .sender(sender)
                .recipient(recipient)
                .content("Merhaba, kayıp ilanındaki hayvanı gördüm.")
                .build();

        // Mesaj ilişkilerinin doğru kullanıcılarla kurulduğunu kontrol eder.
        assertSame(sender, message.getSender());
        assertSame(recipient, message.getRecipient());

        // Mesaj içeriğinin entity'ye doğru aktarıldığını kontrol eder.
        assertEquals(
                "Merhaba, kayıp ilanındaki hayvanı gördüm.",
                message.getContent()
        );

        // Yeni mesajın varsayılan olarak okunmamış olması gerekir.
        assertFalse(message.isRead());
    }

    /**
     * Kullanıcı mesajı görüntülediğinde mesajın
     * okundu durumuna geçirilebildiğini doğrular.
     */
    @Test
    void shouldMarkMessageAsRead() {

        Message message = Message.builder()
                .sender(User.builder().uid(1L).build())
                .recipient(User.builder().uid(2L).build())
                .content("Test mesajı")
                .build();

        // Mesajın başlangıçta okunmamış olduğunu doğrular.
        assertFalse(message.isRead());

        // Mesaj görüntülendiğinde servis katmanı bu alanı true yapacaktır.
        message.setRead(true);

        // Durum değişikliğinin entity üzerinde gerçekleştiğini doğrular.
        assertTrue(message.isRead());
    }
}