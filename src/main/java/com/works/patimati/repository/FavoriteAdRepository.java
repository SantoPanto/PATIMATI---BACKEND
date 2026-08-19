package com.works.patimati.repository;

import com.works.patimati.entity.FavoriteAd;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface FavoriteAdRepository extends JpaRepository<FavoriteAd, Long> {

    Page<FavoriteAd> findByUserEmail(String email, Pageable pageable);
    boolean existsByUserEmailAndAdId(String email, Long adId);
    Optional<FavoriteAd> findByUserEmailAndAdId(String email, Long adId);
}
