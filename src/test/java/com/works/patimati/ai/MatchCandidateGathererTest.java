package com.works.patimati.ai;

import com.works.patimati.ai.dto.AiCandidate;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.AiStatus;
import com.works.patimati.entity.enums.ExternalCategory;
import com.works.patimati.entity.enums.Species;
import com.works.patimati.entity.external.ExternalPetRecord;
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
        // Flyway ACIK kalir (gercek SQL/migration soz dizimini dogrular);
        // yalnizca Hibernate'in acilis-sonrasi kati sema tip denetimi
        // kapatiliyor -- password_reset_tokens.id (V4, bu PR'dan once var,
        // canlida uygulanmis) SERIAL/BIGINT uyusmazligi bu testin konusu
        // DEGIL ve ayri ele aliniyor.
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

    @Test
    void uncertainExternalRecordSearchesBothLostAndFoundPools() {
        // DÜZELTME (2026-08-19, kullanıcı raporu): kategorisi UNCERTAIN kalan
        // bir Instagram kaydı, hangi yönde (kayıp mı bulundu mu) olduğunu
        // bilmediğimiz için eskiden HİÇ aday toplamıyordu (aday havuzu daima
        // boş). Artık hem LOST hem FOUND ilan havuzunu birden tarıyor.
        User lostOwner = userRepository.save(User.builder()
                .email("uncertain-lost-" + System.nanoTime() + "@test.patimati")
                .firstName("D").lastName("Test").role(User.Role.USER).build());
        User foundOwner = userRepository.save(User.builder()
                .email("uncertain-found-" + System.nanoTime() + "@test.patimati")
                .firstName("E").lastName("Test").role(User.Role.USER).build());

        Ad lostAd = adRepository.save(Ad.builder()
                .title("Konumsuz kayıp kedi (uncertain testi)").adType(Ad.AdType.LOST)
                .species(Species.CAT).user(lostOwner).active(true)
                .aiStatus(AiStatus.DONE)
                .aiEmbeddings(List.of(new Ad.AiPhotoVector("s3://test/lost.jpg", new float[]{0.1f, 0.2f, 0.3f})))
                .aiSpecies("cat")
                .build());
        Ad foundAd = adRepository.save(Ad.builder()
                .title("Konumsuz bulunan kedi (uncertain testi)").adType(Ad.AdType.FOUND)
                .species(Species.CAT).user(foundOwner).active(true)
                .aiStatus(AiStatus.DONE)
                .aiEmbeddings(List.of(new Ad.AiPhotoVector("s3://test/found.jpg", new float[]{0.4f, 0.5f, 0.6f})))
                .aiSpecies("cat")
                .build());

        ExternalPetRecord uncertainRecord = ExternalPetRecord.builder()
                .id(1L).category(ExternalCategory.UNCERTAIN).species("cat").build();

        List<AiCandidate> candidates = gatherer.findCandidatesForExternalRecord(uncertainRecord);

        assertThat(candidates)
                .withFailMessage("UNCERTAIN kategori LOST havuzunu aramadı")
                .anyMatch(c -> lostAd.getId().equals(c.adId()));
        assertThat(candidates)
                .withFailMessage("UNCERTAIN kategori FOUND havuzunu aramadı")
                .anyMatch(c -> foundAd.getId().equals(c.adId()));
    }

    @Test
    void adoptionExternalRecordSearchesOnlyLostPoolNotFound() {
        // DÜZELTME (2026-08-19, kullanıcı raporu #2): ADOPTION kategorili bir
        // Instagram kaydı ("yuva arıyoruz") artık LOST havuzunu tarıyor --
        // ama FOUND'u DEĞİL ("ben bunu buldum, sahibi arıyorum" farklı bir
        // niyettir, bkz. compatibleAdTypesForCategory'nin gerekçesi).
        User lostOwner = userRepository.save(User.builder()
                .email("adoption-lost-" + System.nanoTime() + "@test.patimati")
                .firstName("F").lastName("Test").role(User.Role.USER).build());
        User foundOwner = userRepository.save(User.builder()
                .email("adoption-found-" + System.nanoTime() + "@test.patimati")
                .firstName("G").lastName("Test").role(User.Role.USER).build());

        Ad lostAd = adRepository.save(Ad.builder()
                .title("Konumsuz kayıp kedi (adoption testi)").adType(Ad.AdType.LOST)
                .species(Species.CAT).user(lostOwner).active(true)
                .aiStatus(AiStatus.DONE)
                .aiEmbeddings(List.of(new Ad.AiPhotoVector("s3://test/lost2.jpg", new float[]{0.1f, 0.2f, 0.3f})))
                .aiSpecies("cat")
                .build());
        Ad foundAd = adRepository.save(Ad.builder()
                .title("Konumsuz bulunan kedi (adoption testi)").adType(Ad.AdType.FOUND)
                .species(Species.CAT).user(foundOwner).active(true)
                .aiStatus(AiStatus.DONE)
                .aiEmbeddings(List.of(new Ad.AiPhotoVector("s3://test/found2.jpg", new float[]{0.4f, 0.5f, 0.6f})))
                .aiSpecies("cat")
                .build());

        ExternalPetRecord adoptionRecord = ExternalPetRecord.builder()
                .id(3L).category(ExternalCategory.ADOPTION).species("cat").build();

        List<AiCandidate> candidates = gatherer.findCandidatesForExternalRecord(adoptionRecord);

        assertThat(candidates)
                .withFailMessage("ADOPTION, LOST havuzunu aramadı")
                .anyMatch(c -> lostAd.getId().equals(c.adId()));
        assertThat(candidates)
                .withFailMessage("ADOPTION, FOUND havuzunu da taramamalıydı ama taradı")
                .noneMatch(c -> foundAd.getId().equals(c.adId()));
    }

    @Test
    void irrelevantExternalRecordNeverSearchesAnyPool() {
        ExternalPetRecord irrelevantRecord = ExternalPetRecord.builder()
                .id(2L).category(ExternalCategory.IRRELEVANT).species("cat").build();

        assertThat(gatherer.findCandidatesForExternalRecord(irrelevantRecord)).isEmpty();
    }
}
