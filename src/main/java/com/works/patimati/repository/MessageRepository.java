package com.works.patimati.repository;

import com.works.patimati.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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
            ORDER BY message.timestamp ASC, message.id ASC
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
}