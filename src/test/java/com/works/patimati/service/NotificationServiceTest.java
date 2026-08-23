package com.works.patimati.service;

import com.works.patimati.entity.Notification;
import com.works.patimati.entity.User;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.notification.PushNotificationService;
import com.works.patimati.notification.PushResult;
import com.works.patimati.repository.NotificationRepository;
import com.works.patimati.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationServiceTest {

    private NotificationRepository notificationRepository;
    private UserRepository userRepository;
    private PushNotificationService pushNotificationService;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationRepository = mock(NotificationRepository.class);
        userRepository = mock(UserRepository.class);
        pushNotificationService = mock(PushNotificationService.class);
        notificationService = new NotificationService(
                notificationRepository,
                userRepository,
                pushNotificationService
        );
    }

    @Test
    void persistsBeforePushAndKeepsRowWhenFirebaseFails() {
        User recipient = user(7L, "recipient@patimati.com");

        when(notificationRepository.saveAndFlush(any(Notification.class)))
                .thenAnswer(invocation -> {
                    Notification notification = invocation.getArgument(0);
                    ReflectionTestUtils.setField(notification, "id", 42L);
                    return notification;
                });
        when(pushNotificationService.send(
                eq("fcm-token"),
                eq("Başlık"),
                eq("Gövde"),
                anyMap()
        )).thenThrow(new IllegalStateException("Firebase unavailable"));

        PushResult result = notificationService.createAndSend(
                recipient,
                "Başlık",
                "Gövde",
                "MESSAGE",
                Map.of("type", "MESSAGE")
        );

        assertThat(result).isEqualTo(PushResult.FAILED);
        verify(notificationRepository).saveAndFlush(any(Notification.class));
        verify(pushNotificationService).send(
                eq("fcm-token"),
                eq("Başlık"),
                eq("Gövde"),
                eq(Map.of("type", "MESSAGE", "notificationId", "42"))
        );
    }

    @Test
    void readsOnlyTheAuthenticatedUsersHistory() {
        User recipient = user(7L, "recipient@patimati.com");
        Notification notification = new Notification();
        notification.setUser(recipient);
        notification.setTitle("Başlık");
        notification.setBody("Gövde");
        notification.setType("MESSAGE");
        notification.setData(Map.of("type", "MESSAGE", "senderId", "42", "referenceId", "42"));

        when(userRepository.findByEmail(recipient.getEmail()))
                .thenReturn(Optional.of(recipient));
        when(notificationRepository.findByUser_UidOrderByCreatedAtDescIdDesc(7L))
                .thenReturn(List.of(notification));

        assertThat(notificationService.getNotifications(recipient.getEmail()))
                .hasSize(1)
                .first()
                .satisfies(response -> {
                    assertThat(response.title()).isEqualTo("Başlık");
                    assertThat(response.referenceId()).isEqualTo("42");
                    assertThat(response.data()).containsEntry("type", "MESSAGE");
                });

        verify(notificationRepository)
                .findByUser_UidOrderByCreatedAtDescIdDesc(7L);
    }

    @Test
    void firstMessageFromSenderCreatesANewNotification() {
        User recipient = user(7L, "recipient@patimati.com");
        User sender = User.builder()
                .uid(9L)
                .email("sender@patimati.com")
                .firstName("Ayşe")
                .lastName("Yılmaz")
                .build();

        when(notificationRepository.findLatestUnreadBySender(7L, "MESSAGE", "9"))
                .thenReturn(Optional.empty());
        when(notificationRepository.saveAndFlush(any(Notification.class)))
                .thenAnswer(invocation -> {
                    Notification notification = invocation.getArgument(0);
                    ReflectionTestUtils.setField(notification, "id", 42L);
                    return notification;
                });
        when(pushNotificationService.send(any(), any(), any(), anyMap()))
                .thenReturn(PushResult.SENT);

        notificationService.createOrStackMessageNotification(recipient, sender, 100L);

        verify(notificationRepository).saveAndFlush(argThat(notification ->
                notification.getBody().equals("Ayşe size yeni bir mesaj gönderdi.")
                        && notification.getData().get("count").equals("1")));
    }

    @Test
    void secondUnreadMessageFromSameSenderStacksIntoTheExistingNotification() {
        User recipient = user(7L, "recipient@patimati.com");
        User sender = User.builder()
                .uid(9L)
                .email("sender@patimati.com")
                .firstName("Ayşe")
                .lastName("Yılmaz")
                .build();

        Notification existing = new Notification();
        ReflectionTestUtils.setField(existing, "id", 5L);
        existing.setUser(recipient);
        existing.setType("MESSAGE");
        existing.setRead(false);
        existing.setBody("Ayşe size yeni bir mesaj gönderdi.");
        existing.setData(new HashMap<>(Map.of(
                "type", "MESSAGE",
                "senderId", "9",
                "messageId", "100",
                "count", "1"
        )));

        when(notificationRepository.findLatestUnreadBySender(7L, "MESSAGE", "9"))
                .thenReturn(Optional.of(existing));
        when(notificationRepository.saveAndFlush(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(pushNotificationService.send(any(), any(), any(), anyMap()))
                .thenReturn(PushResult.SENT);

        notificationService.createOrStackMessageNotification(recipient, sender, 101L);

        assertThat(existing.getBody()).isEqualTo("Ayşe size 2 yeni mesaj gönderdi.");
        assertThat(existing.getData())
                .containsEntry("count", "2")
                .containsEntry("messageId", "101");
        verify(notificationRepository, never()).save(any(Notification.class));
        verify(notificationRepository).saveAndFlush(existing);
    }

    @Test
    void cannotMarkAnotherUsersNotificationAsRead() {
        User recipient = user(7L, "recipient@patimati.com");
        when(userRepository.findByEmail(recipient.getEmail()))
                .thenReturn(Optional.of(recipient));
        when(notificationRepository.findByIdAndUser_Uid(99L, 7L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(
                recipient.getEmail(),
                99L
        )).isInstanceOf(ResourceNotFoundException.class);

        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void marksOneNotificationReadForItsAuthenticatedOwner() {
        User recipient = user(7L, "recipient@patimati.com");
        Notification notification = new Notification();
        notification.setRead(false);

        when(userRepository.findByEmail(recipient.getEmail()))
                .thenReturn(Optional.of(recipient));
        when(notificationRepository.findByIdAndUser_Uid(42L, 7L))
                .thenReturn(Optional.of(notification));

        notificationService.markAsRead(recipient.getEmail(), 42L);

        assertThat(notification.isRead()).isTrue();
        verify(notificationRepository).save(notification);
    }

    @Test
    void marksAllAuthenticatedUsersNotificationsAsReadInOneScopedUpdate() {
        User recipient = user(7L, "recipient@patimati.com");
        when(userRepository.findByEmail(recipient.getEmail()))
                .thenReturn(Optional.of(recipient));

        notificationService.markAllAsRead(recipient.getEmail());

        verify(notificationRepository).markAllAsRead(7L);
    }

    private User user(Long uid, String email) {
        return User.builder()
                .uid(uid)
                .email(email)
                .firstName("Test")
                .lastName("User")
                .fcmToken("fcm-token")
                .build();
    }
}
