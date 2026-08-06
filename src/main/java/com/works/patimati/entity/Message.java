package com.works.patimati.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * İki kullanıcı arasında gönderilen bir mesajı temsil eden JPA entity'sidir.
 *
 * Her mesajın göndereni, alıcısı, metin içeriği, gönderilme zamanı
 * ve okunma durumu bulunur.
 *
 * Bu entity doğrudan API isteği veya cevabı olarak kullanılmayacaktır.
 * Görev 2 kapsamında mesajlaşma DTO'ları ayrıca oluşturulmalıdır.
 */
@Entity
@Table(
        name = "messages",
        indexes = {
                /*
                 * İki kullanıcı arasındaki sohbet geçmişinin
                 * tarih sırasına göre hızlı getirilmesini destekler.
                 */
                @Index(
                        name = "idx_messages_chat_history",
                        columnList = "sender_id, recipient_id, sent_at, id"
                ),

                /*
                 * Bir kullanıcının okunmamış mesajlarının
                 * hızlı şekilde bulunmasını destekler.
                 */
                @Index(
                        name = "idx_messages_recipient_read",
                        columnList = "recipient_id, is_read"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    /**
     * Mesajın veritabanındaki benzersiz kimliği.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Mesajı gönderen kullanıcı.
     *
     * LAZY kullanılması, mesaj getirilirken kullanıcı bilgilerinin
     * ihtiyaç olmadığı sürece ayrıca yüklenmesini engeller.
     *
     * Cascade kullanılmaz; mesaj kaydedilirken veya silinirken
     * kullanıcı kaydının etkilenmemesi gerekir.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /**
     * Mesajı alan kullanıcı.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    /**
     * Kullanıcının gönderdiği mesaj metni.
     *
     * Mesaj uzunluğu veritabanı seviyesinde 2000 karakterle
     * sınırlandırılır. DTO katmanında da aynı sınır uygulanmalıdır.
     */
    @Column(nullable = false, length = 2000)
    private String content;

    /**
     * Mesajın oluşturulduğu zaman.
     *
     * CreationTimestamp sayesinde yeni mesaj ilk kez kaydedilirken
     * Hibernate bu alanı otomatik olarak doldurur. Daha sonra
     * değiştirilmemesi için updatable false kullanılır.
     */
    @CreationTimestamp
    @Column(name = "sent_at", nullable = false, updatable = false)
    private Instant timestamp;

    /**
     * Mesajın alıcı tarafından okunup okunmadığını belirtir.
     *
     * Alanın adı Java tarafında read olarak tutulur. Lombok bunun
     * için isRead() getter metodunu üretir. Veritabanındaki kolon
     * ise is_read olarak adlandırılır.
     */
    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private boolean read = false;
}