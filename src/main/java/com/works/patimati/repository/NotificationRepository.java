package com.works.patimati.repository;

import com.works.patimati.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUser_UidOrderByCreatedAtDescIdDesc(Long userUid);

    /**
     * Kayan pencere sayımı — NearbyAlertNotifier'ın günlük tavanı için:
     * kullanıcıya son 24 saatte bu tipten kaç bildirim yazıldı.
     */
    long countByUser_UidAndTypeAndCreatedAtAfter(Long userUid, String type, java.time.OffsetDateTime after);

    Optional<Notification> findByIdAndUser_Uid(Long notificationId, Long userUid);

    @Query(value = """
            SELECT *
            FROM notifications
            WHERE user_id = :userUid
              AND type = :type
              AND data ->> 'dedupeKey' = :dedupeKey
            ORDER BY created_at DESC, id DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<Notification> findLatestByDedupeKey(
            @Param("userUid") Long userUid,
            @Param("type") String type,
            @Param("dedupeKey") String dedupeKey
    );

    @Query(value = """
            SELECT *
            FROM notifications
            WHERE user_id = :userUid
              AND type = :type
              AND read = false
              AND data ->> 'senderId' = :senderId
            ORDER BY created_at DESC, id DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<Notification> findLatestUnreadBySender(
            @Param("userUid") Long userUid,
            @Param("type") String type,
            @Param("senderId") String senderId
    );

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("""
            UPDATE Notification notification
            SET notification.read = true
            WHERE notification.user.uid = :userUid
              AND notification.read = false
            """)
    int markAllAsRead(@Param("userUid") Long userUid);
}
