package com.works.patimati.repository;

import com.works.patimati.entity.PetShopProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PetShopProductReviewRepository extends JpaRepository<PetShopProductReview, Long> {

    Optional<PetShopProductReview> findByProduct_IdAndAuthor_Uid(Long productId, Long authorUid);

    Page<PetShopProductReview> findByProduct_IdOrderByCreatedAtDesc(Long productId, Pageable pageable);

    /** Yorum yoksa {@code null} döner -- DTO'da {@code 0.0} değil {@code null} olarak taşınmalı. */
    @Query("SELECT AVG(r.rating) FROM PetShopProductReview r WHERE r.product.id = :productId")
    Double findAverageRating(@Param("productId") Long productId);

    long countByProduct_Id(Long productId);
}
