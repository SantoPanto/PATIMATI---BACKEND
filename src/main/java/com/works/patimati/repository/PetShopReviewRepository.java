package com.works.patimati.repository;

import com.works.patimati.entity.PetShopReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PetShopReviewRepository extends JpaRepository<PetShopReview, Long> {

    Optional<PetShopReview> findByPetShop_IdAndAuthor_Uid(Long petShopId, Long authorUid);

    Page<PetShopReview> findByPetShop_IdOrderByCreatedAtDesc(Long petShopId, Pageable pageable);

    /** Yorum yoksa {@code null} döner -- DTO'da {@code 0.0} değil {@code null} olarak taşınmalı. */
    @Query("SELECT AVG(r.rating) FROM PetShopReview r WHERE r.petShop.id = :petShopId")
    Double findAverageRating(@Param("petShopId") Long petShopId);

    long countByPetShop_Id(Long petShopId);
}
