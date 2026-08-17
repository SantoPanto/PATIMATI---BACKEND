package com.works.patimati.service;

import com.works.patimati.entity.Ad;
import com.works.patimati.entity.PotentialMatch;
import com.works.patimati.entity.PotentialMatchRecipient;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.ExternalCategory;
import com.works.patimati.entity.enums.ExternalSource;
import com.works.patimati.entity.enums.Species;
import com.works.patimati.entity.external.ExternalPetRecord;
import com.works.patimati.entity.external.ExternalSourcePost;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.PotentialMatchRecipientRepository;
import com.works.patimati.repository.PotentialMatchRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.repository.external.ExternalPetRecordRepository;
import com.works.patimati.repository.external.ExternalSourcePostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GERÇEK PostgreSQL'e karşı çalışır (yerel docker: patimati-postgres).
 *
 * <p><b>Neden mock değil:</b> {@code recordAdMatch}/{@code recordExternalMatch}'in
 * asıl garantisi — aynı çift için ikinci kez çağrılınca yeni satır oluşmaması —
 * {@code potential_matches} üzerindeki kısmi benzersiz indekslere ({@code
 * ON CONFLICT DO NOTHING}) dayanır. Repository'yi taklit etmek yalnızca "servis
 * repository metodunu çağırdı mı" sorusunu yanıtlar, "veritabanı gerçekten
 * tekrarı engelliyor mu" sorusunu YANITLAMAZ — bu yüzden bu test gerçek SQL
 * çalıştırır.
 *
 * <p>Her test {@code @Transactional} ile sarılıp geri alınır; kalıcı veri
 * bırakmaz. Flyway migration'ları (V9-V11) context açılışında gerçek DB'ye
 * uygulanır — bu da migration'ların söz dizimini doğrular.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "jwt.secret-key=dGhpcy1pcy1hLXRlc3Qtc2VjcmV0LWtleS1mb3ItcGF0aW1hdGktYXBwbGljYXRpb24="
})
@Transactional
class PotentialMatchServiceTest {

    @Autowired
    private PotentialMatchService potentialMatchService;
    @Autowired
    private PotentialMatchRepository potentialMatchRepository;
    @Autowired
    private PotentialMatchRecipientRepository recipientRepository;
    @Autowired
    private AdRepository adRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ExternalPetRecordRepository externalPetRecordRepository;
    @Autowired
    private ExternalSourcePostRepository externalSourcePostRepository;

    private User ownerA;
    private User ownerB;
    private Ad adA;
    private Ad adB;

    @BeforeEach
    void setUp() {
        // fcmToken bilerek boş: AiMatchNotifier.sendOne token yoksa gerçek
        // Firebase'e hiç dokunmadan "gönderilecek bir şey yok" der ve true
        // döner — bu testler yalnızca kalıcılık/dedup'ı doğrular, teslimatı
        // değil (o PotentialMatchServiceRetryTest'te, taklitlerle).
        ownerA = userRepository.save(User.builder()
                .email("owner-a-" + System.nanoTime() + "@test.patimati")
                .firstName("A").lastName("Test").role(User.Role.USER).build());
        ownerB = userRepository.save(User.builder()
                .email("owner-b-" + System.nanoTime() + "@test.patimati")
                .firstName("B").lastName("Test").role(User.Role.USER).build());

        adA = adRepository.save(Ad.builder()
                .title("Kayıp kedi A").adType(Ad.AdType.LOST)
                .species(Species.CAT).user(ownerA).active(true).build());
        adB = adRepository.save(Ad.builder()
                .title("Bulunan kedi B").adType(Ad.AdType.FOUND)
                .species(Species.CAT).user(ownerB).active(true).build());
    }

    @Test
    void nativeAdAdMatchCreatesOneMatchAndTwoRecipients() {
        potentialMatchService.recordAdMatch(adA.getId(), adB.getId(), 0.8f, 0.7f, 0.6f, 0.75f, "test/v1");

        List<PotentialMatch> matches = matchesFor(adA, adB);
        assertThat(matches).hasSize(1);
        assertThat(matches.get(0).getCandidateKind()).isEqualTo(PotentialMatch.CandidateKind.AD);

        List<PotentialMatchRecipient> recipients = recipientRepository.findAll().stream()
                .filter(r -> r.getPotentialMatch().getId().equals(matches.get(0).getId()))
                .toList();
        assertThat(recipients).hasSize(2);
        assertThat(recipients).extracting(r -> r.getRecipient().getUid())
                .containsExactlyInAnyOrder(ownerA.getUid(), ownerB.getUid());
    }

    @Test
    void reRecordingSamePairInReverseOrderDoesNotDuplicateMatchOrRecipients() {
        potentialMatchService.recordAdMatch(adA.getId(), adB.getId(), 0.8f, 0.7f, 0.6f, 0.75f, "test/v1");
        // Aynı çift TERS sırayla yeniden puanlanıyor (örn. B'nin kendi analiz
        // turu A'yı aday olarak buldu) — kanonik LEAST/GREATEST sıralaması
        // sayesinde bu hâlâ AYNI satıra düşmeli, ikinci bir satır YARATMAMALI.
        potentialMatchService.recordAdMatch(adB.getId(), adA.getId(), 0.81f, 0.71f, 0.61f, 0.76f, "test/v1");

        List<PotentialMatch> matches = matchesFor(adA, adB);
        assertThat(matches).hasSize(1);

        List<PotentialMatchRecipient> recipients = recipientRepository.findAll().stream()
                .filter(r -> r.getPotentialMatch().getId().equals(matches.get(0).getId()))
                .toList();
        assertThat(recipients).hasSize(2);
    }

    @Test
    void externalMatchCreatesOneMatchAndOnlyOneRecipient() {
        ExternalSourcePost post = externalSourcePostRepository.save(ExternalSourcePost.builder()
                .source(ExternalSource.INSTAGRAM)
                .sourcePostId("shortcode-" + System.nanoTime())
                .canonicalUrl("https://instagram.com/p/x/")
                .detectedAt(Instant.now())
                .build());
        ExternalPetRecord record = externalPetRecordRepository.save(ExternalPetRecord.builder()
                .post(post).petIndex((short) 0).category(ExternalCategory.FOUND).build());

        potentialMatchService.recordExternalMatch(adA.getId(), record.getId(), 0.9f, 0.5f, 0.4f, 0.7f, "test/v1");

        List<PotentialMatch> matches = potentialMatchRepository.findAll().stream()
                .filter(m -> record.getId().equals(
                        m.getExternalRecord() == null ? null : m.getExternalRecord().getId()))
                .toList();
        assertThat(matches).hasSize(1);
        assertThat(matches.get(0).getCandidateKind()).isEqualTo(PotentialMatch.CandidateKind.EXTERNAL);

        List<PotentialMatchRecipient> recipients = recipientRepository.findAll().stream()
                .filter(r -> r.getPotentialMatch().getId().equals(matches.get(0).getId()))
                .toList();
        assertThat(recipients).hasSize(1);
        assertThat(recipients.get(0).getRecipient().getUid()).isEqualTo(ownerA.getUid());
        assertThat(recipients.get(0).getRole()).isEqualTo(PotentialMatchRecipient.Role.OWNER);
    }

    @Test
    void reRecordingExternalMatchDoesNotDuplicateRecipient() {
        ExternalSourcePost post = externalSourcePostRepository.save(ExternalSourcePost.builder()
                .source(ExternalSource.INSTAGRAM)
                .sourcePostId("shortcode-" + System.nanoTime())
                .canonicalUrl("https://instagram.com/p/y/")
                .detectedAt(Instant.now())
                .build());
        ExternalPetRecord record = externalPetRecordRepository.save(ExternalPetRecord.builder()
                .post(post).petIndex((short) 0).category(ExternalCategory.LOST).build());

        potentialMatchService.recordExternalMatch(adB.getId(), record.getId(), 0.9f, 0.5f, 0.4f, 0.7f, "test/v1");
        potentialMatchService.recordExternalMatch(adB.getId(), record.getId(), 0.91f, 0.51f, 0.41f, 0.71f, "test/v1");

        List<PotentialMatchRecipient> recipients = recipientRepository.findAll().stream()
                .filter(r -> r.getRecipient().getUid().equals(ownerB.getUid())
                        && r.getPotentialMatch().getExternalRecord() != null
                        && r.getPotentialMatch().getExternalRecord().getId().equals(record.getId()))
                .toList();
        assertThat(recipients).hasSize(1);
    }

    private List<PotentialMatch> matchesFor(Ad a, Ad b) {
        Long lo = Math.min(a.getId(), b.getId());
        Long hi = Math.max(a.getId(), b.getId());
        return potentialMatchRepository.findAll().stream()
                .filter(m -> m.getCandidateKind() == PotentialMatch.CandidateKind.AD
                        && m.getAdA().getId().equals(lo)
                        && m.getAdB() != null && m.getAdB().getId().equals(hi))
                .toList();
    }
}
