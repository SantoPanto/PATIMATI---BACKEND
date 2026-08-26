package com.works.patimati.repository;

import com.works.patimati.entity.VetClinicReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VetClinicReviewRepository extends JpaRepository<VetClinicReview, Long> {

    Optional<VetClinicReview> findByVetClinic_IdAndAuthor_Uid(Long vetClinicId, Long authorUid);

    Page<VetClinicReview> findByVetClinic_IdOrderByCreatedAtDesc(Long vetClinicId, Pageable pageable);

    /** Yorum yoksa {@code null} döner -- DTO'da {@code 0.0} değil {@code null} olarak taşınmalı. */
    @Query("SELECT AVG(r.rating) FROM VetClinicReview r WHERE r.vetClinic.id = :clinicId")
    Double findAverageRating(@Param("clinicId") Long clinicId);

    long countByVetClinic_Id(Long vetClinicId);
}
