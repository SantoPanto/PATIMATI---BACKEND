package com.works.patimati.service;

import com.works.patimati.dto.ad.AdResponse;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.FavoriteAd;
import com.works.patimati.entity.User;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.repository.FavoriteAdRepository;
import com.works.patimati.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteAdService {

    private final FavoriteAdRepository favoriteAdRepository;
    private final UserRepository userRepository;
    private final AdRepository adRepository;
    private final AdService adService;

    @Transactional
    public void addFavorite(String email, Long adId) {
        if (favoriteAdRepository.existsByUserEmailAndAdId(email, adId)) {
            return; // Zaten favorilerde ekli
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı"));
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("İlan bulunamadı"));

        FavoriteAd favoriteAd = FavoriteAd.builder()
                .user(user)
                .ad(ad)
                .build();

        favoriteAdRepository.save(favoriteAd);
    }

    @Transactional
    public void removeFavorite(String email, Long adId) {
        favoriteAdRepository.findByUserEmailAndAdId(email, adId)
                .ifPresent(favoriteAdRepository::delete);
    }

    @Transactional(readOnly = true)
    public Page<AdResponse> getMyFavorites(String email, Pageable pageable) {
        return favoriteAdRepository.findByUserEmail(email, pageable)
                .map(favoriteAd -> adService.toResponseWithTemporaryPhotoUrls(favoriteAd.getAd()));
    }
}
