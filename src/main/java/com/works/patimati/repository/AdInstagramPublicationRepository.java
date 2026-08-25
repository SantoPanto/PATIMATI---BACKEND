package com.works.patimati.repository;

import com.works.patimati.entity.AdInstagramPublication;
import com.works.patimati.entity.enums.InstagramPublishStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdInstagramPublicationRepository extends JpaRepository<AdInstagramPublication, Long> {

    Page<AdInstagramPublication> findAllByStatus(InstagramPublishStatus status, Pageable pageable);

    Optional<AdInstagramPublication> findByAdId(Long adId);
}
