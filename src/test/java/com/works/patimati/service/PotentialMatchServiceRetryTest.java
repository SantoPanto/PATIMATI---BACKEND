package com.works.patimati.service;

import com.works.patimati.entity.PotentialMatch;
import com.works.patimati.entity.PotentialMatchRecipient;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.MatchStatus;
import com.works.patimati.notification.PushNotificationService;
import com.works.patimati.notification.PushResult;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.PotentialMatchRecipientRepository;
import com.works.patimati.repository.PotentialMatchRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.repository.external.ExternalPetRecordRepository;
import com.works.patimati.repository.external.ExternalSourceMediaRepository;
import com.works.patimati.storage.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Pure Mockito — bildirim yeniden-deneme süpürücüsünün ({@code
 * retryPendingNotifications}) orkestrasyon mantığını doğrular. Gerçek FCM'e
 * ya da gerçek DB'ye bağımlı değil: {@code PotentialMatchServiceTest} zaten
 * kalıcılık/dedup'ı gerçek Postgres'e karşı doğruluyor — bu test yalnızca
 * "PENDING kalan bir satır tekrar denenir mi, max deneme sonrası
 * NOTIFICATION_FAILED'e geçer mi" sorusuna odaklanır (send-sonra-işaretle
 * sırasının, Faz 2 revize blueprint §4/§13'teki tersine çevrilmiş hâli).
 */
class PotentialMatchServiceRetryTest {

    private PotentialMatchRecipientRepository recipientRepository;
    private PushNotificationService pushNotificationService;
    private PotentialMatchService service;

    @BeforeEach
    void setUp() {
        PotentialMatchRepository potentialMatchRepository = mock(PotentialMatchRepository.class);
        recipientRepository = mock(PotentialMatchRecipientRepository.class);
        AdRepository adRepository = mock(AdRepository.class);
        ExternalPetRecordRepository externalPetRecordRepository = mock(ExternalPetRecordRepository.class);
        pushNotificationService = mock(PushNotificationService.class);
        UserRepository userRepository = mock(UserRepository.class);
        ExternalSourceMediaRepository externalSourceMediaRepository = mock(ExternalSourceMediaRepository.class);
        ImageStorageService imageStorageService = mock(ImageStorageService.class);

        service = new PotentialMatchService(
                potentialMatchRepository, recipientRepository, adRepository,
                externalPetRecordRepository, pushNotificationService, userRepository,
                externalSourceMediaRepository, imageStorageService);

        ReflectionTestUtils.setField(service, "maxSendAttempts", 2);
        ReflectionTestUtils.setField(service, "retryGracePeriod", Duration.ofSeconds(0));
    }

    @Test
    void stuckPendingRecipientIsRetriedBySweepAndMarkedNotifiedOnSuccess() {
        PotentialMatchRecipient stuck = recipient(1L, MatchStatus.PENDING, (short) 0);

        when(recipientRepository.findPendingForRetry(eq(2), any(Instant.class))).thenReturn(List.of(stuck));
        when(recipientRepository.findExhaustedPending(2)).thenReturn(List.of());
        when(recipientRepository.findById(1L)).thenReturn(Optional.of(stuck));
        when(pushNotificationService.send(anyString(), anyString(), anyString(), any(), anyString()))
                .thenReturn(PushResult.SENT);

        service.retryPendingNotifications();

        verify(pushNotificationService).send(anyString(), anyString(), anyString(), any(), anyString());
        verify(recipientRepository).markNotifiedIfPending(eq(1L), any(Instant.class));
    }

    @Test
    void repeatedSendFailureIncrementsAttemptsWithoutMarkingNotified() {
        PotentialMatchRecipient stuck = recipient(2L, MatchStatus.PENDING, (short) 1);

        when(recipientRepository.findPendingForRetry(eq(2), any(Instant.class))).thenReturn(List.of(stuck));
        when(recipientRepository.findExhaustedPending(2)).thenReturn(List.of());
        when(recipientRepository.findById(2L)).thenReturn(Optional.of(stuck));
        when(pushNotificationService.send(anyString(), anyString(), anyString(), any(), anyString()))
                .thenReturn(PushResult.FAILED);

        service.retryPendingNotifications();

        verify(recipientRepository).incrementSendAttempts(2L);
        verify(recipientRepository, never()).markNotifiedIfPending(eq(2L), any(Instant.class));
    }

    @Test
    void exhaustedAttemptsTransitionsToNotificationFailedWithoutFurtherSendAttempt() {
        PotentialMatchRecipient exhausted = recipient(3L, MatchStatus.PENDING, (short) 2); // max=2'ye ulaştı

        when(recipientRepository.findPendingForRetry(eq(2), any(Instant.class))).thenReturn(List.of());
        when(recipientRepository.findExhaustedPending(2)).thenReturn(List.of(exhausted));
        when(recipientRepository.markNotificationFailedIfPending(3L)).thenReturn(1);

        service.retryPendingNotifications();

        verify(recipientRepository).markNotificationFailedIfPending(3L);
        verifyNoInteractions(pushNotificationService); // tükenmiş satır için ARTIK gönderim denenmez
    }

    private PotentialMatchRecipient recipient(Long id, MatchStatus status, short attempts) {
        PotentialMatch match = PotentialMatch.builder().id(100L)
                .candidateKind(PotentialMatch.CandidateKind.AD).build();
        User user = User.builder().uid(id).fcmToken("fcm-token-" + id).build();
        return PotentialMatchRecipient.builder()
                .id(id).potentialMatch(match).recipient(user).status(status).sendAttempts(attempts).build();
    }
}
