package com.works.patimati.repository;

import com.works.patimati.entity.PetShopProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PetShopProductRepository extends JpaRepository<PetShopProduct, Long> {

    /** Sahibin kendi listesi + herkese açık dükkan-ürün listesi ORTAK kullanır. */
    Page<PetShopProduct> findByPetShop_IdOrderByCreatedAtDesc(Long petShopId, Pageable pageable);

    /**
     * Güncelleme/silme için sahiplik-sınırlı arama -- IDOR güvenli, client'tan
     * gelen dükkan id'sine hiç güvenilmiyor, çağıranın KENDİ dükkanının
     * id'siyle sınırlanıyor.
     */
    Optional<PetShopProduct> findByPetShop_IdAndId(Long petShopId, Long productId);
}
