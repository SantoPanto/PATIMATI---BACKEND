package com.works.patimati.ai;

import com.works.patimati.ai.dto.AiCandidate;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.AiStatus;
import com.works.patimati.entity.enums.Species;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GERÇEK PostgreSQL'e karşı çalışır — konumsuz fallback yolu, gerçek native
 * sorgu SQL'ini çalıştırır (PostGIS sorgusuyla aynı sınıfta ama farklı
 * dalda). Amaç: {@code AiAnalysisPublisher.collectCandidates}'te keşfedilen
 * eski hatanın ({@code ad.getLocation() == null} ise doğrudan boş liste)
 * gerçekten düzeldiğini kanıtlamak — bunu mock ile ikna edici biçimde
 * kanıtlamak mümkün değil, gerçek SQL çalışmalı.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "jwt.secret-key=dGhpcy1pcy1hLXRlc3Qtc2VjcmV0LWtleS1mb3ItcGF0aW1hdGktYXBwbGljYXRpb24=",
        // Flyway ACIK kalır (gerçek SQL/migration söz dizimini doğrular);
        // yalnızca Hibernate'in açılış-sonrası katı şema tip denetimi
        // kapatılıyor -- password_reset_tokens.id (V4, bu PR'dan önce var,
        // canlıda uygulanmış) SERIAL/BIGINT uyuşmazlığı bu testin konusu
        // DEĞİL ve ayrı ele alınıyor.
        "spring.jpa.hibernate.ddl-auto=none"
})
@Transactional
class MatchCandidateGathererTest {

    @Autowired
    private MatchCandidateGatherer gatherer;
    @Autowired
    private AdRepository adRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void adWithNoLocationStillGetsCandidatesViaBoundedFallback() {
        User owner1 = userRepository.save(User.builder()
                .email("noloc-1-" + System.nanoTime() + "@test.patimati")
                .firstName("A").lastName("Test").role(User.Role.USER).build());
        User owner2 = userRepository.save(User.builder()
                .email("noloc-2-" + System.nanoTime() + "@test.patimati")
                .firstName("B").lastName("Test").role(User.Role.USER).build());

        // Konumsuz LOST ilan — eski davranışta collectCandidates burada
        // doğrudan List.of() dönerdi.
        Ad lostAd = adRepository.save(Ad.builder()
                .title("Konumsuz kayıp kedi").adType(Ad.AdType.LOST)
                .species(Species.CAT).user(owner1).active(true)
                .build());

        // Uyumlu, analizi bitmiş bir FOUND ilanı (o da konumsuz — fallback'in
        // aday tarafında da çalıştığını göstermek için).
        Ad foundAd = adRepository.save(Ad.builder()
                .title("Konumsuz bulunan kedi").adType(Ad.AdType.FOUND)
                .species(Species.CAT).user(owner2).active(true)
                .aiStatus(AiStatus.DONE)
                .aiEmbeddings(List.of(new Ad.AiPhotoVector("s3://test/1.jpg", new float[]{0.1f, 0.2f, 0.3f})))
                .aiSpecies("cat")
                .build());

        List<AiCandidate> candidates = gatherer.findCandidatesForAd(lostAd);

        assertThat(candidates)
                .withFailMessage("Konumsuz ilan için aday listesi boş döndü — "
                        + "düzeltmeden önceki (eski) davranışa geri dönmüş olabilir")
                .isNotEmpty();
        assertThat(candidates)
                .withFailMessage("Uyumlu, analizi bitmiş FOUND ilanı fallback listesinde bulunamadı")
                .anyMatch(c -> foundAd.getId().equals(c.adId()));
    }

    @Test
    void adWithLocationButNoCompatibleCandidatesReturnsEmptyNotError() {
        User owner = userRepository.save(User.builder()
                .email("nocandidate-" + System.nanoTime() + "@test.patimati")
                .firstName("C").lastName("Test").role(User.Role.USER).build());

        Ad adoptionAd = adRepository.save(Ad.builder()
                .title("Sahiplendirme ilanı").adType(Ad.AdType.ADOPTION)
                .species(Species.CAT).user(owner).active(true)
                .build());

        // ADOPTION hiçbir zaman eşleştirmeye girmez (opposite type yok) — sözleşme §5.
        List<AiCandidate> candidates = gatherer.findCandidatesForAd(adoptionAd);

        assertThat(candidates).isEmpty();
    }
}
