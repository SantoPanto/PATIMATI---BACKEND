package com.works.patimati.repository;

import com.works.patimati.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Message entity'si için veritabanı işlemlerini yöneten repository katmanıdır.
 *
 * JpaRepository sayesinde mesaj kaydetme, silme, ID ile bulma ve listeleme
 * gibi standart işlemler ek kod yazılmadan kullanılabilir.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * İki kullanıcı arasındaki bütün mesajları gönderim zamanına göre
     * eskiden yeniye doğru getirir.
     *
     * Sohbet iki yönlü olduğu için iki durum birlikte kontrol edilir:
     *
     * 1. Birinci kullanıcı gönderici, ikinci kullanıcı alıcı olabilir.
     * 2. İkinci kullanıcı gönderici, birinci kullanıcı alıcı olabilir.
     *
     * timestamp değerleri aynı olduğunda id alanı ikinci sıralama ölçütü
     * olarak kullanılır. Böylece sonuç sırası her sorguda tutarlı kalır.
     */
    @Query("""
            SELECT message
            FROM Message message
            WHERE (
                message.sender.uid = :firstUserId
                AND message.recipient.uid = :secondUserId
            )
            OR (
                message.sender.uid = :secondUserId
                AND message.recipient.uid = :firstUserId
            )
            """)
    Page<Message> findChatHistory(
            @Param("firstUserId") Long firstUserId,
            @Param("secondUserId") Long secondUserId,
            Pageable pageable
    );

    /**
     * Belirli bir kullanıcının henüz okumadığı mesajların sayısını döndürür.
     *
     * Bu bir Spring Data JPA derived query metodudur. Spring metodun
     * isminden aşağıdaki koşulu otomatik olarak oluşturur:
     *
     * recipient.uid = recipientId AND read = false
     *
     * Kullanıcı entity'sini önce veritabanından getirmek yerine doğrudan
     * kullanıcı ID'siyle sorgu yapılması gereksiz sorguyu engeller.
     */
    long countByRecipient_UidAndReadFalse(Long recipientId);

    /**
     * Belirli bir kullanıcının taraf olduğu her bir sohbet için en son mesajı getirir.
     * Odaları en son mesaj tarihine göre DESC sıralar.
     */
    @Query("""
            SELECT m FROM Message m
            WHERE m.id IN (
                SELECT MAX(m2.id) FROM Message m2
                WHERE m2.sender.uid = :userId OR m2.recipient.uid = :userId
                GROUP BY CASE WHEN m2.sender.uid = :userId THEN m2.recipient.uid ELSE m2.sender.uid END
            )
            ORDER BY m.timestamp DESC
            """)
    List<Message> findRecentMessagesPerConversation(@Param("userId") Long userId);

    /**
     * Belirli bir göndericiden gelen ve kullanıcının henüz okumadığı mesajların sayısı.
     */
    long countBySender_UidAndRecipient_UidAndReadFalse(Long senderId, Long recipientId);

    /**
     * Belirli bir göndericiden alıcıya atılan en son mesajı getirir (çift tıklama / duplicate koruması için).
     */
    Optional<Message> findTopBySender_UidAndRecipient_UidOrderByIdDesc(Long senderId, Long recipientId);
}