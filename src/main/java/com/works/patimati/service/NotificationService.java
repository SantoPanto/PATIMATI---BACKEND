package com.works.patimati.service;

import com.works.patimati.dto.NotificationResponse;
import com.works.patimati.entity.Notification;
import com.works.patimati.entity.User;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.notification.PushNotificationService;
import com.works.patimati.notification.PushResult;
import com.works.patimati.repository.NotificationRepository;
import com.works.patimati.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Owns notification history and delegates delivery to the existing FCM port.
 * Database persistence happens before delivery, and delivery failures are
 * converted to a result so they cannot roll back the history row.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final PushNotificationService pushNotificationService;

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(String userEmail) {
        User user = findUserByEmail(userEmail);
        return notificationRepository.findByUser_UidOrderByCreatedAtDescIdDesc(user.getUid())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Saves one notification and then attempts delivery. REQUIRES_NEW keeps
     * notification history independent from an outer ad/message transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PushResult createAndSend(
            User recipient,
            String title,
            String body,
            String type,
            Map<String, String> data
    ) {
        return createAndSend(recipient, title, body, type, data, null);
    }

    /**
     * The optional dedupe key is used by retryable AI-match processing. A
     * retry reuses the existing history row and only retries delivery.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PushResult createAndSend(
            User recipient,
            String title,
            String body,
            String type,
            Map<String, String> data,
            String dedupeKey
    ) {
        if (recipient == null || recipient.getUid() == null) {
            return PushResult.NO_RECIPIENT;
        }

        try {
            Notification notification = findOrCreate(
                    recipient,
                    title,
                    body,
                    type,
                    data,
                    dedupeKey
            );

            Map<String, String> pushData = new HashMap<>();
            if (notification.getData() != null) {
                pushData.putAll(notification.getData());
            }
            pushData.put("notificationId", String.valueOf(notification.getId()));

            try {
                PushResult result = pushNotificationService.send(
                        recipient.getFcmToken(),
                        notification.getTitle(),
                        notification.getBody(),
                        pushData
                );

                if (!result.gonderildi()) {
                    log.warn("Bildirim kalıcı olarak kaydedildi ancak push gönderilemedi: "
                                    + "notificationId={} recipientUid={} reason={}",
                            notification.getId(), recipient.getUid(), result.aciklama());
                }
                return result;
            } catch (RuntimeException exception) {
                log.warn("Bildirim kalıcı olarak kaydedildi ancak push gönderimi hata verdi: "
                                + "notificationId={} recipientUid={}",
                        notification.getId(), recipient.getUid(), exception);
                return PushResult.FAILED;
            }
        } catch (RuntimeException exception) {
            // A notification must not take down the existing ad/message flow
            // if the notification table is temporarily unavailable.
            log.error("Bildirim kalıcılaştırılamadı: recipientUid={}",
                    recipient.getUid(), exception);
            return PushResult.FAILED;
        }
    }

    /**
     * Aynı göndericiden okunmamış birden fazla mesaj geldiğinde her biri için
     * ayrı bir bildirim satırı açmak yerine, mevcut okunmamış satırı güncelleyip
     * üstte tutar — bildirimler listesinde tek bir "N yeni mesaj" satırı olarak
     * yığılır (stack). Satır zaten okunmuşsa (veya hiç yoksa) yeni bir yığın
     * başlar.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PushResult createOrStackMessageNotification(
            User recipient,
            User sender,
            Long messageId
    ) {
        if (recipient == null || recipient.getUid() == null) {
            return PushResult.NO_RECIPIENT;
        }

        try {
            Notification notification = findOrStackMessageNotification(recipient, sender, messageId);

            Map<String, String> pushData = new HashMap<>();
            if (notification.getData() != null) {
                pushData.putAll(notification.getData());
            }
            pushData.put("notificationId", String.valueOf(notification.getId()));

            try {
                PushResult result = pushNotificationService.send(
                        recipient.getFcmToken(),
                        notification.getTitle(),
                        notification.getBody(),
                        pushData
                );

                if (!result.gonderildi()) {
                    log.warn("Bildirim kalıcı olarak kaydedildi ancak push gönderilemedi: "
                                    + "notificationId={} recipientUid={} reason={}",
                            notification.getId(), recipient.getUid(), result.aciklama());
                }
                return result;
            } catch (RuntimeException exception) {
                log.warn("Bildirim kalıcı olarak kaydedildi ancak push gönderimi hata verdi: "
                                + "notificationId={} recipientUid={}",
                        notification.getId(), recipient.getUid(), exception);
                return PushResult.FAILED;
            }
        } catch (RuntimeException exception) {
            log.error("Bildirim kalıcılaştırılamadı: recipientUid={}",
                    recipient.getUid(), exception);
            return PushResult.FAILED;
        }
    }

    private Notification findOrStackMessageNotification(User recipient, User sender, Long messageId) {
        String senderId = String.valueOf(sender.getUid());

        Optional<Notification> existing = notificationRepository.findLatestUnreadBySender(
                recipient.getUid(),
                "MESSAGE",
                senderId
        );

        if (existing.isPresent()) {
            Notification notification = existing.get();
            Map<String, String> data = notification.getData() == null
                    ? new HashMap<>()
                    : new HashMap<>(notification.getData());

            int count = parseMessageCount(data.get("count")) + 1;
            data.put("type", "MESSAGE");
            data.put("senderId", senderId);
            data.put("messageId", String.valueOf(messageId));
            data.put("count", String.valueOf(count));

            notification.setBody(sender.getFirstName() + " size " + count + " yeni mesaj gönderdi.");
            notification.setData(data);
            notification.setCreatedAt(OffsetDateTime.now());

            return notificationRepository.saveAndFlush(notification);
        }

        Map<String, String> data = new HashMap<>();
        data.put("type", "MESSAGE");
        data.put("senderId", senderId);
        data.put("messageId", String.valueOf(messageId));
        data.put("count", "1");

        Notification notification = new Notification();
        notification.setUser(recipient);
        notification.setTitle("Yeni mesaj");
        notification.setBody(sender.getFirstName() + " size yeni bir mesaj gönderdi.");
        notification.setType("MESSAGE");
        notification.setData(data);
        notification.setRead(false);

        return notificationRepository.saveAndFlush(notification);
    }

    private int parseMessageCount(String value) {
        if (value == null) {
            return 1;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return 1;
        }
    }

    @Transactional
    public void markAsRead(String userEmail, Long notificationId) {
        User user = findUserByEmail(userEmail);
        Notification notification = notificationRepository
                .findByIdAndUser_Uid(notificationId, user.getUid())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bildirim bulunamadı: " + notificationId
                ));

        if (!notification.isRead()) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void markAllAsRead(String userEmail) {
        User user = findUserByEmail(userEmail);
        notificationRepository.markAllAsRead(user.getUid());
    }

    private Notification findOrCreate(
            User recipient,
            String title,
            String body,
            String type,
            Map<String, String> data,
            String dedupeKey
    ) {
        if (dedupeKey != null && !dedupeKey.isBlank()) {
            var existing = notificationRepository.findLatestByDedupeKey(
                    recipient.getUid(),
                    type,
                    dedupeKey
            );
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        Map<String, String> persistentData = data == null
                ? null
                : new HashMap<>(data);
        if (dedupeKey != null && !dedupeKey.isBlank()) {
            if (persistentData == null) {
                persistentData = new HashMap<>();
            }
            persistentData.put("dedupeKey", dedupeKey);
        }

        Notification notification = new Notification();
        notification.setUser(recipient);
        notification.setTitle(title);
        notification.setBody(body);
        notification.setType(type);
        notification.setData(persistentData);
        notification.setRead(false);

        // Flush before invoking Firebase so the generated ID is available in
        // the push payload and the database write is attempted first.
        return notificationRepository.saveAndFlush(notification);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Kullanıcı bulunamadı: " + email
                ));
    }

    private NotificationResponse toResponse(Notification notification) {
        String referenceId = null;
        if (notification.getData() != null) {
            referenceId = notification.getData().getOrDefault("referenceId", notification.getData().get("senderId"));
        }

        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getBody(),
                notification.getType(),
                referenceId,
                notification.getData(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
