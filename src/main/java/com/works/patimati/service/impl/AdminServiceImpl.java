package com.works.patimati.service.impl;

import com.works.patimati.dto.ad.AdResponse;
import com.works.patimati.dto.admin.AdComplaintAdminResponse;
import com.works.patimati.dto.admin.ExternalPostAdminResponse;
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
import com.works.patimati.storage.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import com.works.patimati.dto.admin.AdoptionComplaintAdminResponse;
import com.works.patimati.entity.AdoptionComplaint;
import com.works.patimati.repository.AdoptionComplaintRepository;

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

    @Transactional(readOnly = true)
    @Override
    public Page<UserDetailForAdminDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(user -> new UserDetailForAdminDTO(
                        user.getUid(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getPhone(),
                        user.isEnabled(),
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

    @Transactional(readOnly = true)
    @Override
    public Page<AdResponse> getAllAds(Pageable pageable) {
        return adRepository.findAll(pageable)
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

    @Transactional(readOnly = true)
    @Override
    public Page<AdComplaintAdminResponse> getAdComplaints(Pageable pageable) {
        Page<AdComplaint> complaints = adComplaintRepository.findAll(pageable);

        return complaints.map(complaint -> {
            Optional<User> reporterOpt = userRepository.findById(complaint.getReporterId());
            String reporterFullName = reporterOpt
                    .map(u -> (u.getFirstName() + " " + u.getLastName()).trim())
                    .orElse("Bilinmeyen Kullanıcı");
            String reporterEmail = reporterOpt.map(User::getEmail).orElse("");

            Optional<Ad> adOpt = adRepository.findById(complaint.getAdId());
            String adTitle = adOpt.map(Ad::getTitle).orElse("Silinmiş / Bulunamayan İlan");
            Long adOwnerId = adOpt.filter(a -> a.getUser() != null).map(a -> a.getUser().getUid()).orElse(null);
            String adOwnerFullName = adOpt.filter(a -> a.getUser() != null)
                    .map(a -> (a.getUser().getFirstName() + " " + a.getUser().getLastName()).trim())
                    .orElse("Bilinmeyen Sahip");

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
    public Page<UserComplaintAdminResponse> getUserComplaints(Pageable pageable) {
        Page<UserComplaint> complaints = userComplaintRepository.findAll(pageable);

        return complaints.map(complaint -> {
            Optional<User> reporterOpt = userRepository.findById(complaint.getReporterId());
            String reporterFullName = reporterOpt
                    .map(u -> (u.getFirstName() + " " + u.getLastName()).trim())
                    .orElse("Bilinmeyen Kullanıcı");
            String reporterEmail = reporterOpt.map(User::getEmail).orElse("");

            Optional<User> reportedOpt = userRepository.findById(complaint.getReportedUserId());
            String reportedUserFullName = reportedOpt
                    .map(u -> (u.getFirstName() + " " + u.getLastName()).trim())
                    .orElse("Bilinmeyen Kullanıcı");
            String reportedUserEmail = reportedOpt.map(User::getEmail).orElse("");

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
    public Page<AdoptionComplaintAdminResponse> getAdoptionComplaints(Pageable pageable) {
        Page<AdoptionComplaint> complaints = adoptionComplaintRepository.findAll(pageable);

        return complaints.map(complaint -> {
            Optional<User> reporterOpt = userRepository.findById(complaint.getReporterId());
            String reporterFullName = reporterOpt
                    .map(u -> (u.getFirstName() + " " + u.getLastName()).trim())
                    .orElse("Bilinmeyen Kullanıcı");
            String reporterEmail = reporterOpt.map(User::getEmail).orElse("");

            Optional<Ad> adOpt = adRepository.findById(complaint.getAdId());
            String adTitle = adOpt.map(Ad::getTitle).orElse("Silinmiş / Bulunamayan İlan");
            Long adOwnerId = adOpt.filter(a -> a.getUser() != null).map(a -> a.getUser().getUid()).orElse(null);
            String adOwnerFullName = adOpt.filter(a -> a.getUser() != null)
                    .map(a -> (a.getUser().getFirstName() + " " + a.getUser().getLastName()).trim())
                    .orElse("Bilinmeyen Sahip");

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
        return externalSourcePostRepository.findAll(pageable).map(this::toExternalPostAdminResponse);
    }

    private ExternalPostAdminResponse toExternalPostAdminResponse(ExternalSourcePost post) {
        Optional<ExternalPetRecord> recordOpt =
                externalPetRecordRepository.findByPostAndPetIndex(post, (short) 0);

        String photoUrl = externalSourceMediaRepository.findByPostOrderByOrdinalAsc(post).stream()
                .filter(m -> m.getStorageKey() != null)
                .findFirst()
                .map(this::toTemporaryReadUrl)
                .orElse(null);

        boolean hasMatch = recordOpt
                .map(r -> potentialMatchRepository.existsByCandidateKindAndExternalRecord(
                        PotentialMatch.CandidateKind.EXTERNAL, r))
                .orElse(false);

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
}
