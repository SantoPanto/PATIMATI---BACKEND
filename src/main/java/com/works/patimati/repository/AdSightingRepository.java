package com.works.patimati.repository;

import com.works.patimati.entity.AdSighting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdSightingRepository extends JpaRepository<AdSighting, Long> {

    List<AdSighting> findByAd_IdOrderByCreatedAtDescIdDesc(Long adId);
}
