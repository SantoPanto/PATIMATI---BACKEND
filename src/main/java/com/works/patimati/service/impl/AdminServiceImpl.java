package com.works.patimati.service.impl;

import com.works.patimati.dto.ad.AdResponse;
import com.works.patimati.dto.admin.AdComplaintAdminResponse;
import com.works.patimati.dto.admin.ExternalPostAdminResponse;
import com.works.patimati.dto.admin.InstitutionAssignmentRequest;
import com.works.patimati.dto.admin.UserComplaintAdminResponse;
import com.works.patimati.dto.admin.UserDetailForAdminDTO;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.AdComplaint;
import com.works.patimati.entity.PotentialMatch;
import com.works.patimati.entity.User;
import com.works.patimati.entity.UserComplaint;
import com.works.patimati.entity.external.ExternalPetRecord;
import com.works.patimati.entity.external.ExternalSourceMedia;
import com.works.patimati.entity.external.ExternalSourcePost;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.AdComplaintRepository;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.PotentialMatchRepository;
import com.works.patimati.repository.UserComplaintRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.repository.external.ExternalPetRecordRepository;
import com.works.patimati.repository.external.ExternalSourceMediaRepository;
import com.works.patimati.repository.external.ExternalSourcePostRepository;
import com.works.patimati.service.AdminService;
import com.works.patimati.service.AdService;
import com.works.patimati.service.ReverseGeocodingService;
import com.works.patimati.storage.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.works.patimati.dto.admin.AdoptionComplaintAdminResponse;
import com.works.patimati.entity.AdoptionComplaint;
import com.works.patimati.repository.AdoptionComplaintRepository;
import com.works.patimati.specification.AdSpecification;
import com.works.patimati.specification.ComplaintSpecification;
import com.works.patimati.specification.UserSpecification;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminServiceImpl.class);

    private final UserRepository userRepository;
    private final AdRepository adRepository;
    private final AdComplaintRepository adComplaintRepository;
    private final UserComplaintRepository userComplaintRepository;
    private final AdoptionComplaintRepository adoptionComplaintRepository;
    private final AdService adService;
    private final ExternalSourcePostRepository externalSourcePostRepository;
    private final ExternalPetRecordRepository externalPetRecordRepository;
    private final ExternalSourceMediaRepository externalSourceMediaRepository;
    private final PotentialMatchRepository potentialMatchRepository;
    private final ImageStorageService imageStorageService;
    private final ReverseGeocodingService reverseGeocodingService;

    @Transactional(readOnly = true)
    @Override
    public Page<UserDetailForAdminDTO> getAllUsers(String search, Pageable pageable) {
        return userRepository.findAll(UserSpecification.withSearch(search), pageable)
                .map(user -> new UserDetailForAdminDTO(
                        user.getUid(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getPhone(),
                        user.isEnabled(),
                        user.getRole(),
                        user.getCreatedAt()
                ));
    }

    @Transactional
    @Override
    public void banUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı ID: " + userId));
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Transactional
    @Override
    public void unbanUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı ID: " + userId));
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Transactional
    @Override
    public void assignInstitution(Long userId, InstitutionAssignmentRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı ID: " + userId));

        // YÖNETİCİ ROLÜ KORUNUYOR: bir ADMIN'e ilçe atanmak istenirse rolü
        // INSTITUTION'a çevirmek onu /api/admin/** dışında bırakır — yani
        // yönetici kendi hesabını paneli denemek için hazırlarken yönetici
        // panelini kaybeder. ADMIN'de yalnız kurum alanları dolar; kapsam
        // çözücü ADMIN'i zaten ilçesi doluysa kabul ediyor.
        if (user.getRole() != User.Role.ADMIN) {
            user.setRole(User.Role.INSTITUTION);
        }

        user.setInstitutionName(request.institutionName().trim());
        user.setInstitutionCity(request.institutionCity().trim());
        user.setInstitutionDistrict(request.institutionDistrict().trim());
        userRepository.save(user);

        // Kurum hesabı açmak nadir ve yetki genişleten bir işlem; izi kalsın.
        log.info("Kurum hesabı atandı: uid={} rol={} kurum='{}' il='{}' ilce='{}'",
                user.getUid(), user.getRole(), user.getInstitutionName(),
                user.getInstitutionCity(), user.getInstitutionDistrict());
    }

    @Transactional(readOnly = true)
    @Override
    public Page<AdResponse> getAllAds(String search, Pageable pageable) {
        return adRepository.findAll(AdSpecification.withSearch(search), pageable)
                .map(adService::toResponseWithTemporaryPhotoUrls);
    }

    @Transactional
    @Override
    public void suspendAd(Long adId) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new ResourceNotFoundException("İlan bulunamadı ID: " + adId));
        ad.setSuspended(true);
        ad.setActive(false);
        adRepository.save(ad);
    }

    @Transactional
    @Override
    public void unhideAd(Long adId) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new ResourceNotFoundException("İlan bulunamadı ID: " + adId));
        ad.setSuspended(false);
        ad.setActive(true);
        adRepository.save(ad);
    }

    @Transactional
    @Override
    public void deleteAdAsAdmin(Long adId) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new ResourceNotFoundException("İlan bulunamadı ID: " + adId));
        // Hard Delete
        adRepository.delete(ad);
    }

    @Transactional
    @Override
    public int reanalyzeFailedAds() {
        // Kural ve kuyruk erişimi AdService'te; burada yalnız yetki sınırı var
        // (uç /api/admin altında). Kurallar iki yerde yaşamasın.
        return adService.reanalyzeAllFailed();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<AdComplaintAdminResponse> getAdComplaints(String search, Pageable pageable) {
        Page<AdComplaint> complaints = adComplaintRepository.findAll(ComplaintSpecification.forAd(search), pageable);
        List<AdComplaint> content = complaints.getContent();

        // Sayfadaki tüm reporterId/adId'ler önce toplanıp TEK sorguda çekiliyor
        // -- öncesinde her şikayet için ayrı ayrı findById çağrılıyordu (N+1,
        // 20 satır = 40 ek sorgu).
        Map<Long, User> reportersById = userRepository.findAllById(
                        content.stream().map(AdComplaint::getReporterId).distinct().toList())
                .stream().collect(Collectors.toMap(User::getUid, u -> u));
        Map<Long, Ad> adsById = adRepository.findAllById(
                        content.stream().map(AdComplaint::getAdId).distinct().toList())
                .stream().collect(Collectors.toMap(Ad::getId, a -> a));

        return complaints.map(complaint -> {
            User reporter = reportersById.get(complaint.getReporterId());
            String reporterFullName = reporter != null
                    ? (reporter.getFirstName() + " " + reporter.getLastName()).trim()
                    : "Bilinmeyen Kullanıcı";
            String reporterEmail = reporter != null ? reporter.getEmail() : "";

            Ad ad = adsById.get(complaint.getAdId());
            String adTitle = ad != null ? ad.getTitle() : "Silinmiş / Bulunamayan İlan";
            Long adOwnerId = ad != null && ad.getUser() != null ? ad.getUser().getUid() : null;
            String adOwnerFullName = ad != null && ad.getUser() != null
                    ? (ad.getUser().getFirstName() + " " + ad.getUser().getLastName()).trim()
                    : "Bilinmeyen Sahip";

            return new AdComplaintAdminResponse(
                    complaint.getId(),
                    complaint.getReporterId(),
                    reporterFullName,
                    reporterEmail,
                    complaint.getAdId(),
                    adTitle,
                    adOwnerId,
                    adOwnerFullName,
                    complaint.getReason(),
                    complaint.getDescription(),
                    complaint.getStatus(),
                    complaint.getCreatedAt()
            );
        });
    }

    @Transactional(readOnly = true)
    @Override
    public Page<UserComplaintAdminResponse> getUserComplaints(String search, Pageable pageable) {
        Page<UserComplaint> complaints = userComplaintRepository.findAll(ComplaintSpecification.forUser(search), pageable);
        List<UserComplaint> content = complaints.getContent();

        // Şikayet eden + şikayet edilen kullanıcı id'leri TEK listede toplanıp
        // TEK sorguda çekiliyor (öncesinde satır başına 2 ayrı findById).
        List<Long> userIds = new ArrayList<>();
        content.forEach(c -> {
            userIds.add(c.getReporterId());
            userIds.add(c.getReportedUserId());
        });
        Map<Long, User> usersById = userRepository.findAllById(userIds.stream().distinct().toList())
                .stream().collect(Collectors.toMap(User::getUid, u -> u));

        return complaints.map(complaint -> {
            User reporter = usersById.get(complaint.getReporterId());
            String reporterFullName = reporter != null
                    ? (reporter.getFirstName() + " " + reporter.getLastName()).trim()
                    : "Bilinmeyen Kullanıcı";
            String reporterEmail = reporter != null ? reporter.getEmail() : "";

            User reported = usersById.get(complaint.getReportedUserId());
            String reportedUserFullName = reported != null
                    ? (reported.getFirstName() + " " + reported.getLastName()).trim()
                    : "Bilinmeyen Kullanıcı";
            String reportedUserEmail = reported != null ? reported.getEmail() : "";

            return new UserComplaintAdminResponse(
                    complaint.getId(),
                    complaint.getReporterId(),
                    reporterFullName,
                    reporterEmail,
                    complaint.getReportedUserId(),
                    reportedUserFullName,
                    reportedUserEmail,
                    complaint.getReason(),
                    complaint.getDescription(),
                    complaint.getStatus(),
                    complaint.getCreatedAt()
            );
        });
    }

    @Transactional(readOnly = true)
    @Override
    public Page<AdoptionComplaintAdminResponse> getAdoptionComplaints(String search, Pageable pageable) {
        Page<AdoptionComplaint> complaints = adoptionComplaintRepository.findAll(ComplaintSpecification.forAdoption(search), pageable);
        List<AdoptionComplaint> content = complaints.getContent();

        Map<Long, User> reportersById = userRepository.findAllById(
                        content.stream().map(AdoptionComplaint::getReporterId).distinct().toList())
                .stream().collect(Collectors.toMap(User::getUid, u -> u));
        Map<Long, Ad> adsById = adRepository.findAllById(
                        content.stream().map(AdoptionComplaint::getAdId).distinct().toList())
                .stream().collect(Collectors.toMap(Ad::getId, a -> a));

        return complaints.map(complaint -> {
            User reporter = reportersById.get(complaint.getReporterId());
            String reporterFullName = reporter != null
                    ? (reporter.getFirstName() + " " + reporter.getLastName()).trim()
                    : "Bilinmeyen Kullanıcı";
            String reporterEmail = reporter != null ? reporter.getEmail() : "";

            Ad ad = adsById.get(complaint.getAdId());
            String adTitle = ad != null ? ad.getTitle() : "Silinmiş / Bulunamayan İlan";
            Long adOwnerId = ad != null && ad.getUser() != null ? ad.getUser().getUid() : null;
            String adOwnerFullName = ad != null && ad.getUser() != null
                    ? (ad.getUser().getFirstName() + " " + ad.getUser().getLastName()).trim()
                    : "Bilinmeyen Sahip";

            return new AdoptionComplaintAdminResponse(
                    complaint.getId(),
                    complaint.getReporterId(),
                    reporterFullName,
                    reporterEmail,
                    complaint.getAdId(),
                    adTitle,
                    adOwnerId,
                    adOwnerFullName,
                    complaint.getReason(),
                    complaint.getDescription(),
                    complaint.getStatus(),
                    complaint.getCreatedAt()
            );
        });
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ExternalPostAdminResponse> getExternalPosts(Pageable pageable) {
        Page<ExternalSourcePost> posts = externalSourcePostRepository.findAll(pageable);
        List<ExternalSourcePost> content = posts.getContent();

        // Sayfadaki her gönderi için ayrı ayrı (pet-record + medya + eşleşme
        // var mı) sorgulamak yerine üçü de TEK'er sorguda, sayfanın tamamı
        // için toplu çekiliyor -- öncesinde satır başına 3 sorgu vardı
        // (20 satır = 60 ek sorgu).
        Map<Long, ExternalPetRecord> recordsByPostId = externalPetRecordRepository
                .findByPostInAndPetIndex(content, (short) 0).stream()
                .collect(Collectors.toMap(r -> r.getPost().getId(), r -> r));

        Map<Long, List<ExternalSourceMedia>> mediaByPostId = externalSourceMediaRepository
                .findByPostInOrderByOrdinalAsc(content).stream()
                .collect(Collectors.groupingBy(m -> m.getPost().getId()));

        List<ExternalPetRecord> records = List.copyOf(recordsByPostId.values());
        Set<Long> matchedRecordIds = records.isEmpty() ? Set.of() : potentialMatchRepository
                .findByCandidateKindAndExternalRecordIn(PotentialMatch.CandidateKind.EXTERNAL, records).stream()
                .map(pm -> pm.getExternalRecord().getId())
                .collect(Collectors.toSet());

        return posts.map(post -> toExternalPostAdminResponse(post, recordsByPostId, mediaByPostId, matchedRecordIds));
    }

    private ExternalPostAdminResponse toExternalPostAdminResponse(
            ExternalSourcePost post,
            Map<Long, ExternalPetRecord> recordsByPostId,
            Map<Long, List<ExternalSourceMedia>> mediaByPostId,
            Set<Long> matchedRecordIds) {

        Optional<ExternalPetRecord> recordOpt = Optional.ofNullable(recordsByPostId.get(post.getId()));

        String photoUrl = mediaByPostId.getOrDefault(post.getId(), List.of()).stream()
                .filter(m -> m.getStorageKey() != null)
                .findFirst()
                .map(this::toTemporaryReadUrl)
                .orElse(null);

        boolean hasMatch = recordOpt.map(r -> matchedRecordIds.contains(r.getId())).orElse(false);

        return new ExternalPostAdminResponse(
                post.getId(),
                post.getSource() != null ? post.getSource().name() : null,
                post.getSourcePostId(),
                post.getCanonicalUrl(),
                post.getAuthorUsername(),
                post.getCaption(),
                post.getDetectedAt(),
                post.getProcessingStatus() != null ? post.getProcessingStatus().name() : null,
                post.getFailureReason(),
                photoUrl,
                recordOpt.map(r -> r.getCategory() != null ? r.getCategory().name() : null).orElse(null),
                recordOpt.map(ExternalPetRecord::getCategoryConfidence).orElse(null),
                recordOpt.map(ExternalPetRecord::getSpecies).orElse(null),
                recordOpt.map(ExternalPetRecord::getBreed).orElse(null),
                recordOpt.map(ExternalPetRecord::isNeedsReview).orElse(null),
                hasMatch
        );
    }

    private String toTemporaryReadUrl(ExternalSourceMedia media) {
        try {
            return imageStorageService.createTemporaryReadUrl(media.getStorageKey());
        } catch (RuntimeException e) {
            log.warn("Admin listesi için fotoğraf adresi üretilemedi ({}): {}", media.getStorageKey(), e.getMessage());
            return null;
        }
    }

    /**
     * V19 backfill'i: il'i boş, koordinatı dolu ilanları koordinattan
     * çözüp kaydeder. Nominatim hız politikası (≤1 istek/sn) gereği
     * aramalar arasında bekler — bu yüzden BİLEREK @Transactional değil:
     * uzun süren tek dev transaction yerine kayıt başına kısa save.
     * Çözülemeyen ilan (servis hatası / adressiz nokta) atlanır; uç
     * yeniden çağrılırsa yalnız hâlâ boş olanlar denenir.
     */
    @Override
    public Map<String, Integer> backfillAdLocations() {
        List<Ad> adaylar = adRepository.findByCityIsNullAndLocationIsNotNull();

        int dolan = 0;
        int cozulemeyen = 0;

        for (int i = 0; i < adaylar.size(); i++) {
            Ad ad = adaylar.get(i);
            var cozum = reverseGeocodingService.cozumle(
                    ad.getLocation().getY(),
                    ad.getLocation().getX()
            );

            if (cozum.isPresent()) {
                ad.setCity(cozum.get().il());
                ad.setDistrict(cozum.get().ilce());
                adRepository.save(ad);
                dolan++;
            } else {
                cozulemeyen++;
            }

            boolean sonKayit = i == adaylar.size() - 1;
            if (!sonKayit && !geokodAramalariArasindaBekle()) {
                break;
            }
        }

        log.info("İl/ilçe backfill bitti: toplam={} dolan={} cozulemeyen={}",
                adaylar.size(), dolan, cozulemeyen);

        Map<String, Integer> sonuc = new LinkedHashMap<>();
        sonuc.put("toplam", adaylar.size());
        sonuc.put("dolan", dolan);
        sonuc.put("cozulemeyen", cozulemeyen);
        return sonuc;
    }

    /** @return kesintiye uğradıysa false — döngü erken biter. */
    private static boolean geokodAramalariArasindaBekle() {
        try {
            Thread.sleep(1100);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
