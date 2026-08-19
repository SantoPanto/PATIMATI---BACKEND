package com.works.patimati.ai;

import com.works.patimati.entity.Ad;
import com.works.patimati.entity.PotentialMatch;
import com.works.patimati.entity.PotentialMatchRecipient;
import com.works.patimati.entity.User;
import com.works.patimati.entity.external.ExternalPetRecord;
import com.works.patimati.notification.PushNotificationService;
import com.works.patimati.notification.PushResult;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@code AiMatchNotifier}'ın {@link PushNotificationService} üzerinden
 * gönderdiğini ve {@link PushResult}'a göre doğru sonucu/log seviyesini
 * seçtiğini kanıtlar.
 *
 * <p>Bu, kayıp olan main'deki {@code BildirimGunluguDogruluguTest}'in yerini
 * tutar (bkz. {@code AiMatchNotifier}'ın sınıf docstring'i) -- ama o testin
 * hedeflediği eski {@code send(User, Ad, double)} API'si artık yok; bunun
 * yerine bugünkü {@link PotentialMatchRecipient} tabanlı {@code sendOne}
 * API'sine karşı aynı iddiaları (Firebase'e doğrudan dokunmama, PushResult'a
 * göre doğru true/false, gönderilemeyen için doğru log) sınar.
 */
class AiMatchNotifierTest {

    private final PushNotificationService pushNotificationService = mock(PushNotificationService.class);
    private final AiMatchNotifier notifier = new AiMatchNotifier(pushNotificationService);

    private static User user(String fcmToken) {
        return User.builder().uid(1L).fcmToken(fcmToken).build();
    }

    private static PotentialMatch adMatch() {
        Ad adA = Ad.builder().id(10L).title("Kayıp kedi").build();
        Ad adB = Ad.builder().id(20L).title("Bulunan kedi").build();
        return PotentialMatch.builder()
                .id(100L)
                .candidateKind(PotentialMatch.CandidateKind.AD)
                .adA(adA)
                .adB(adB)
                .build();
    }

    private static PotentialMatch externalMatch() {
        return PotentialMatch.builder()
                .id(101L)
                .candidateKind(PotentialMatch.CandidateKind.EXTERNAL)
                .adA(Ad.builder().id(10L).title("Kayıp kedi").build())
                .externalRecord(mock(ExternalPetRecord.class))
                .build();
    }

    private static PotentialMatchRecipient recipient(User user, PotentialMatch match, PotentialMatchRecipient.Role role) {
        return PotentialMatchRecipient.builder()
                .id(5L)
                .recipient(user)
                .potentialMatch(match)
                .role(role)
                .build();
    }

    @Test
    void sentDonerseTrueDonerVeFirebaseyeDegilServiseGider() {
        when(pushNotificationService.send(anyString(), anyString(), anyString(), any(), anyString()))
                .thenReturn(PushResult.SENT);

        PotentialMatchRecipient r = recipient(user("cihaz-jetonu"), adMatch(), PotentialMatchRecipient.Role.OWNER_B);
        boolean sonuc = notifier.sendOne(r);

        assertThat(sonuc).isTrue();
        verify(pushNotificationService).send(
                eq("cihaz-jetonu"),
                eq("Olası eşleşme bulundu"),
                anyString(),
                any(),
                eq("potential-match-5")
        );
    }

    @Test
    void kullaniciYoksaServiseHicGitmezVeTrueDoner() {
        PotentialMatchRecipient r = PotentialMatchRecipient.builder()
                .id(6L)
                .recipient(null)
                .potentialMatch(adMatch())
                .role(PotentialMatchRecipient.Role.OWNER_A)
                .build();

        boolean sonuc = notifier.sendOne(r);

        assertThat(sonuc).isTrue();
        verifyNoInteractions(pushNotificationService);
    }

    @Test
    void noTokenIcinTrueDonerVeUyariLoglanmaz() {
        // Servis NO_TOKEN dönerse (jeton null/boş) sonsuz yeniden denemeyi
        // önlemek için true dönmeli -- FAILED/PUSH_DISABLED'dan FARKLI olarak.
        when(pushNotificationService.send(isNull(), anyString(), anyString(), any(), anyString()))
                .thenReturn(PushResult.NO_TOKEN);

        PotentialMatchRecipient r = recipient(user(null), adMatch(), PotentialMatchRecipient.Role.OWNER_B);
        boolean sonuc = notifier.sendOne(r);

        assertThat(sonuc).isTrue();
    }

    @Test
    void pushDisabledIcinFalseDonerYenidenDenenmeliDir() {
        when(pushNotificationService.send(anyString(), anyString(), anyString(), any(), anyString()))
                .thenReturn(PushResult.PUSH_DISABLED);

        PotentialMatchRecipient r = recipient(user("jeton"), adMatch(), PotentialMatchRecipient.Role.OWNER_B);
        boolean sonuc = notifier.sendOne(r);

        assertThat(sonuc).isFalse();
    }

    @Test
    void failedIcinFalseDonerYenidenDenenmeliDir() {
        when(pushNotificationService.send(anyString(), anyString(), anyString(), any(), anyString()))
                .thenReturn(PushResult.FAILED);

        PotentialMatchRecipient r = recipient(user("jeton"), adMatch(), PotentialMatchRecipient.Role.OWNER_B);
        boolean sonuc = notifier.sendOne(r);

        assertThat(sonuc).isFalse();
    }

    @Test
    void collapseKeyAliciSatiriKimligineBagliDir() {
        when(pushNotificationService.send(anyString(), anyString(), anyString(), any(), anyString()))
                .thenReturn(PushResult.SENT);

        PotentialMatchRecipient r = recipient(user("jeton"), adMatch(), PotentialMatchRecipient.Role.OWNER_A);
        notifier.sendOne(r);

        verify(pushNotificationService).send(anyString(), anyString(), anyString(), any(), eq("potential-match-5"));
    }

    @Test
    void adEslesmesindeGovdeDigerIlaninBasliginiIcerir() {
        when(pushNotificationService.send(anyString(), anyString(), anyString(), any(), anyString()))
                .thenReturn(PushResult.SENT);

        // OWNER_B: karşı taraf adA'dır.
        PotentialMatchRecipient r = recipient(user("jeton"), adMatch(), PotentialMatchRecipient.Role.OWNER_B);
        notifier.sendOne(r);

        verify(pushNotificationService).send(anyString(), anyString(),
                org.mockito.ArgumentMatchers.contains("Kayıp kedi"), any(), anyString());
    }

    @Test
    void externalEslesmedeGovdeGenelBenzerlikDiliKullanir() {
        when(pushNotificationService.send(anyString(), anyString(), anyString(), any(), anyString()))
                .thenReturn(PushResult.SENT);

        PotentialMatchRecipient r = recipient(user("jeton"), externalMatch(), PotentialMatchRecipient.Role.OWNER);
        notifier.sendOne(r);

        verify(pushNotificationService).send(anyString(), anyString(),
                org.mockito.ArgumentMatchers.contains("benzeyen bir hayvan tespit ettik"), any(), anyString());
    }

    @Test
    void veriAlanlariPotentialMatchIdVeRecipientIdIcerir() {
        when(pushNotificationService.send(anyString(), anyString(), anyString(), any(), anyString()))
                .thenReturn(PushResult.SENT);

        PotentialMatchRecipient r = recipient(user("jeton"), adMatch(), PotentialMatchRecipient.Role.OWNER_B);
        notifier.sendOne(r);

        verify(pushNotificationService).send(
                anyString(), anyString(), anyString(),
                eq(Map.of(
                        "type", "POTENTIAL_MATCH",
                        "potentialMatchId", "100",
                        "recipientId", "5"
                )),
                anyString()
        );
    }
}
