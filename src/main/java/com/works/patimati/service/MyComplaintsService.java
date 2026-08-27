package com.works.patimati.service;

import com.works.patimati.dto.complaint.MyComplaintResponse;
import com.works.patimati.entity.User;
import com.works.patimati.repository.AdComplaintRepository;
import com.works.patimati.repository.AdoptionComplaintRepository;
import com.works.patimati.repository.UserComplaintRepository;
import com.works.patimati.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * "Şikayetlerim" (S7, 27.08): kullanıcının kendi açtığı şikayetler.
 *
 * <p><b>Neden var:</b> profil altındaki Şikayet Sistemi ekranı bugüne dek
 * statik bir taslaktı — hiçbir uca bağlı değildi ve hep "şikayetiniz yok"
 * diyordu; kullanıcı açtığı şikayetin akıbetini hiçbir yerden göremiyordu.
 *
 * <p>Üç tablo (kullanıcı / ilan / sahiplendirme şikayetleri) burada tek
 * listede birleşir ve en yeni en üstte döner. Kimlik e-postadan çözülür —
 * çağıran controller {@code authentication.getName()} geçirir; servis
 * SecurityContext'e uzanmaz ki gerçek-DB testi düz parametreyle koşabilsin.
 */
@Service
@RequiredArgsConstructor
public class MyComplaintsService {

    private final UserComplaintRepository userComplaintRepository;
    private final AdComplaintRepository adComplaintRepository;
    private final AdoptionComplaintRepository adoptionComplaintRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<MyComplaintResponse> benimSikayetlerim(String email) {
        User kullanici = userRepository.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("Oturum sahibi kullanıcı bulunamadı."));
        Long uid = kullanici.getUid();

        List<MyComplaintResponse> hepsi = new ArrayList<>();

        userComplaintRepository.findByReporterIdOrderByCreatedAtDesc(uid).forEach(s ->
                hepsi.add(new MyComplaintResponse(
                        s.getId(), "KULLANICI", s.getReportedUserId(),
                        s.getReason(), s.getDescription(), s.getStatus(), s.getCreatedAt())));

        adComplaintRepository.findByReporterIdOrderByCreatedAtDesc(uid).forEach(s ->
                hepsi.add(new MyComplaintResponse(
                        s.getId(), "ILAN", s.getAdId(),
                        s.getReason(), s.getDescription(), s.getStatus(), s.getCreatedAt())));

        adoptionComplaintRepository.findByReporterIdOrderByCreatedAtDesc(uid).forEach(s ->
                hepsi.add(new MyComplaintResponse(
                        s.getId(), "SAHIPLENDIRME", s.getAdId(),
                        s.getReason(), s.getDescription(), s.getStatus(), s.getCreatedAt())));

        // Üç kaynak kendi içinde sıralı gelir ama birleşim yeniden sıralanmalı.
        hepsi.sort(Comparator.comparing(
                MyComplaintResponse::createdAt,
                Comparator.nullsLast(Comparator.comparing(Instant::toEpochMilli))).reversed());
        return hepsi;
    }
}
