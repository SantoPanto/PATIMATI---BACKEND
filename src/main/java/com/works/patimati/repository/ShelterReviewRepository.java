package com.works.patimati.repository;

import com.works.patimati.entity.ShelterReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShelterReviewRepository extends JpaRepository<ShelterReview, Long> {

    Optional<ShelterReview> findByShelter_IdAndAuthor_Uid(Long shelterId, Long authorUid);

    Page<ShelterReview> findByShelter_IdOrderByCreatedAtDesc(Long shelterId, Pageable pageable);

    /** Yorum yoksa {@code null} döner -- DTO'da {@code 0.0} değil {@code null} olarak taşınmalı. */
    @Query("SELECT AVG(r.rating) FROM ShelterReview r WHERE r.shelter.id = :shelterId")
    Double findAverageRating(@Param("shelterId") Long shelterId);

    long countByShelter_Id(Long shelterId);
}
