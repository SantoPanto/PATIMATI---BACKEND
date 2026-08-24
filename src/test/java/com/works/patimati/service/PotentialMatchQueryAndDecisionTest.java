package com.works.patimati.service;

import com.works.patimati.dto.match.PotentialMatchDecisionRequest;
import com.works.patimati.dto.match.PotentialMatchSummaryResponse;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.PotentialMatchRecipient;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.MatchStatus;
import com.works.patimati.entity.enums.Species;
import com.works.patimati.notification.PushNotificationService;
import com.works.patimati.notification.PushResult;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.PotentialMatchRecipientRepository;
import com.works.patimati.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * GET /api/me/potential-matches ve POST .../decision'ın servis katmanı —
 * gerçek PostgreSQL'e karşı (izolasyon garantisi gerçek bir SORGU sonucu,
 * bir kullanıcının satırlarının başka bir kullanıcıya asla dönmediği ancak
 * gerçek bir WHERE ile ikna edici biçimde kanıtlanabilir).
 */
@SpringBootTest
@TestPropertySource(properties = {
        "jwt.secret-key=dGhpcy1pcy1hLXRlc3Qtc2VjcmV0LWtleS1mb3ItcGF0aW1hdGktYXBwbGljYXRpb24=",
        // Flyway ACIK kalir (gercek SQL/migration soz dizimini dogrular);
        // yalnizca Hibernate'in acilis-sonrasi kati sema tip denetimi
        // kapatiliyor -- password_reset_tokens.id (V4, bu PR'dan once var,
        // canlida uygulanmis) SERIAL/BIGINT uyusmazligi bu testin konusu
        // DEGIL ve ayri ele aliniyor.
        "spring.jpa.hibernate.ddl-auto=none"
})
@Transactional
class PotentialMatchQueryAndDecisionTest {

    @Autowired
    private PotentialMatchService potentialMatchService;
    @Autowired
    private PotentialMatchRecipientRepository recipientRepository;
    @Autowired
    private AdRepository adRepository;
    @Autowired
    private UserRepository userRepository;

    // Bu testin gerçek Firebase yapılandırmasına (ya da yokluğuna) bağlı
    // olmaması için gerçek FirebasePushNotificationService yerine sahtesi
    // kullanılır. AiMatchNotifier artık PushNotificationService üzerinden
    // gönderiyor (main'in e203b28 "B5" düzeltmesinin bu ortama taşınmış
    // hali) -- Firebase gerçekten yapılandırılmışsa/yapılandırılmamışsa
    // PushResult farklı çıkar (PUSH_DISABLED vs SENT/NO_TOKEN), ve bu test
    // bildirim TESLİMATINI değil, alıcı satırının durum geçişini (PENDING ->
    // NOTIFIED) sınıyor -- o yüzden SENT sabitlenir.
    @MockitoBean
    private PushNotificationService pushNotificationService;

    private User ownerA;
    private User ownerB;
    private Ad adA;
    private Ad adB;

    @BeforeEach
    void setUp() {
        when(pushNotificationService.send(any(), anyString(), anyString(), any(), any()))
                .thenReturn(PushResult.SENT);

        ownerA = userRepository.save(User.builder()
                .email("qd-a-" + System.nanoTime() + "@test.patimati")
                .firstName("A").lastName("Test").role(User.Role.USER).build());
        ownerB = userRepository.save(User.builder()
                .email("qd-b-" + System.nanoTime() + "@test.patimati")
                .firstName("B").lastName("Test").role(User.Role.USER).build());

        adA = adRepository.save(Ad.builder()
                .title("Kayıp kedi QD-A").adType(Ad.AdType.LOST)
                .species(Species.CAT).user(ownerA).active(true).build());
        adB = adRepository.save(Ad.builder()
                .title("Bulunan kedi QD-B").adType(Ad.AdType.FOUND)
                .species(Species.CAT).user(ownerB).active(true).build());

        potentialMatchService.recordAdMatch(adA.getId(), adB.getId(), 0.8f, 0.7f, 0.6f, 0.75f, "test/v1");
    }

    @Test
    void userOnlySeesTheirOwnRecipientRows() {
        List<PotentialMatchSummaryResponse> forA = potentialMatchService.findForUser(ownerA.getEmail());
        List<PotentialMatchSummaryResponse> forB = potentialMatchService.findForUser(ownerB.getEmail());

        assertThat(forA).hasSize(1);
        assertThat(forB).hasSize(1);

        // A'nın kaydındaki karşı taraf B'nin ilanı olmalı, kendi ilanı DEĞİL.
        assertThat(forA.get(0).counterparty().kind()).isEqualTo("AD");
        assertThat(forA.get(0).counterparty().id()).isEqualTo(adB.getId());
        assertThat(forB.get(0).counterparty().id()).isEqualTo(adA.getId());

        // Bir kullanıcının listesi diğerinin recipientId'sini asla İÇERMEMELİ.
        Long recipientIdForA = forA.get(0).recipientId();
        assertThat(forB).extracting(PotentialMatchSummaryResponse::recipientId)
                .doesNotContain(recipientIdForA);
    }

    @Test
    void decidingOnSomeoneElsesRecipientRowIsForbidden() {
        Long recipientIdForA = potentialMatchService.findForUser(ownerA.getEmail())
                .get(0).recipientId();

        assertThatThrownBy(() -> potentialMatchService.recordDecision(
                ownerB.getEmail(), recipientIdForA, PotentialMatchDecisionRequest.Decision.CONFIRMED))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void validTransitionFromNotifiedToConfirmedSucceeds() {
        Long recipientIdForA = potentialMatchService.findForUser(ownerA.getEmail())
                .get(0).recipientId();
        // Test kullanıcılarının fcmToken'ı yok -> AiMatchNotifier.sendOne
        // "gönderilecek bir şey yok" deyip true döner -> recordAdMatch
        // sırasında satır zaten NOTIFIED'e geçmiş olmalı.
        assertThat(recipientRepository.findById(recipientIdForA).orElseThrow().getStatus())
                .isEqualTo(MatchStatus.NOTIFIED);

        PotentialMatchSummaryResponse result = potentialMatchService.recordDecision(
                ownerA.getEmail(), recipientIdForA, PotentialMatchDecisionRequest.Decision.CONFIRMED);

        assertThat(result.status()).isEqualTo("CONFIRMED");
        assertThat(recipientRepository.findById(recipientIdForA).orElseThrow().getDecidedAt()).isNotNull();
    }

    @Test
    void decidingOnAnExpiredRowIsRejectedAsConflict() {
        Long recipientIdForA = potentialMatchService.findForUser(ownerA.getEmail())
                .get(0).recipientId();

        PotentialMatchRecipient recipient = recipientRepository.findById(recipientIdForA).orElseThrow();
        recipient.setStatus(MatchStatus.EXPIRED);
        recipientRepository.save(recipient);

        assertThatThrownBy(() -> potentialMatchService.recordDecision(
                ownerA.getEmail(), recipientIdForA, PotentialMatchDecisionRequest.Decision.REJECTED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("EXPIRED");
    }

    @Test
    void decidingTwiceIsRejectedTheSecondTime() {
        Long recipientIdForA = potentialMatchService.findForUser(ownerA.getEmail())
                .get(0).recipientId();

        potentialMatchService.recordDecision(
                ownerA.getEmail(), recipientIdForA, PotentialMatchDecisionRequest.Decision.CONFIRMED);

        assertThatThrownBy(() -> potentialMatchService.recordDecision(
                ownerA.getEmail(), recipientIdForA, PotentialMatchDecisionRequest.Decision.REJECTED))
                .isInstanceOf(IllegalStateException.class);
    }
}
