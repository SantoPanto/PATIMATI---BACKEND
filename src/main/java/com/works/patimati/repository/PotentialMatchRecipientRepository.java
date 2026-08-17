package com.works.patimati.repository;

import com.works.patimati.entity.PotentialMatchRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * {@link PotentialMatchRecipient} — hem "kime bildirilecek" hem de dayanıklı
 * bildirim teslimat niyeti (Faz 2 revize blueprint §4). Ayrı bir outbox
 * tablosuna gerek yok: bu satırın kendisi niyet + durum kaydıdır.
 */
@Repository
public interface PotentialMatchRecipientRepository extends JpaRepository<PotentialMatchRecipient, Long> {

    // Aynı (match, kullanıcı) çifti için ikinci bir satır asla oluşmaz —
    // tekrar puanlama / tekrar teslim edilen mesaj güvenli bir hiçbir-şeydir.
    @Query(value = """
            INSERT INTO potential_match_recipients (potential_match_id, recipient_user_id, role, status)
            VALUES (:potentialMatchId, :recipientUserId, :role, 'PENDING')
            ON CONFLICT (potential_match_id, recipient_user_id) DO NOTHING
            RETURNING id
            """, nativeQuery = true)
    Optional<Long> insertIfAbsent(
            @Param("potentialMatchId") Long potentialMatchId,
            @Param("recipientUserId") Long recipientUserId,
            @Param("role") String role);

    /**
     * Gönder-SONRA-işaretle sırasının atomik parçası (blueprint §4, taslaktaki
     * işaretle-SONRA-gönder sırasının tersi — çökme anında sessiz kayıp
     * yerine nadir/sınırlı bir tekrar bildirim tercih edilir).
     *
     * <p>Yalnızca hâlâ {@code PENDING} ise geçiş yapar: eşzamanlı süpürücü
     * çalışmasıyla yarışsa bile ikinci çağrı 0 satır etkiler ve durur.
     */
    @Modifying
    @Transactional
    @Query("UPDATE PotentialMatchRecipient r SET r.status = com.works.patimati.entity.enums.MatchStatus.NOTIFIED, " +
            "r.notifiedAt = :now, r.sendAttempts = r.sendAttempts + 1 " +
            "WHERE r.id = :id AND r.status = com.works.patimati.entity.enums.MatchStatus.PENDING")
    int markNotifiedIfPending(@Param("id") Long id, @Param("now") Instant now);

    @Modifying
    @Transactional
    @Query("UPDATE PotentialMatchRecipient r SET r.sendAttempts = r.sendAttempts + 1 WHERE r.id = :id")
    void incrementSendAttempts(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE PotentialMatchRecipient r SET r.status = com.works.patimati.entity.enums.MatchStatus.NOTIFICATION_FAILED " +
            "WHERE r.id = :id AND r.status = com.works.patimati.entity.enums.MatchStatus.PENDING")
    int markNotificationFailedIfPending(@Param("id") Long id);

    // retryPendingNotifications süpürücüsünün taradığı liste — henüz max
    // deneme sayısına ulaşmamış, "hemen gönder" yolunun geçmesi için tanınan
    // grace period'u aşmış PENDING satırlar.
    @Query("SELECT r FROM PotentialMatchRecipient r WHERE r.status = com.works.patimati.entity.enums.MatchStatus.PENDING " +
            "AND r.sendAttempts < :maxAttempts AND r.createdAt < :cutoff")
    List<PotentialMatchRecipient> findPendingForRetry(
            @Param("maxAttempts") int maxAttempts, @Param("cutoff") Instant cutoff);

    // max deneme sayısını AŞMIŞ (artık retry süpürücüsü tarafından
    // yakalanmayacak) satırlar — bunlar NOTIFICATION_FAILED'e geçirilir.
    @Query("SELECT r FROM PotentialMatchRecipient r WHERE r.status = com.works.patimati.entity.enums.MatchStatus.PENDING " +
            "AND r.sendAttempts >= :maxAttempts")
    List<PotentialMatchRecipient> findExhaustedPending(@Param("maxAttempts") int maxAttempts);
}
