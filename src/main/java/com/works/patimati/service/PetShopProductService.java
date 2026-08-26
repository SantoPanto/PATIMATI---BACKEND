package com.works.patimati.service;

import com.works.patimati.dto.petshop.PetShopProductResponse;
import com.works.patimati.dto.petshop.PetShopProductUpsertRequest;
import com.works.patimati.entity.PetShop;
import com.works.patimati.entity.PetShopProduct;
import com.works.patimati.entity.User;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.PetShopProductRepository;
import com.works.patimati.repository.PetShopProductReviewRepository;
import com.works.patimati.repository.PetShopRepository;
import com.works.patimati.repository.UserRepository;
import com.works.patimati.storage.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Petshop ürün yönetimi -- ürün ekle/güncelle/sil (sahiplik JWT'den gelen
 * e-posta + {@code findByPetShop_IdAndId} ile sınırlı, IDOR güvenli) ve
 * herkese açık ürün görünümü. {@link VetClinicService} deseniyle AYNI
 * "fotoğraf gönderilmezse korunur" mantığı.
 */
@Service
@RequiredArgsConstructor
public class PetShopProductService {

    private static final String PHOTO_KEY_PREFIX = "petshop-products";

    private final PetShopProductRepository petShopProductRepository;
    private final PetShopRepository petShopRepository;
    private final UserRepository userRepository;
    private final ImageStorageService imageStorageService;
    private final PetShopProductReviewRepository petShopProductReviewRepository;

    @Transactional(readOnly = true)
    public Page<PetShopProductResponse> listMine(String ownerEmail, Pageable pageable) {
        PetShop petShop = findOwnPetShop(ownerEmail);
        return petShopProductRepository.findByPetShop_IdOrderByCreatedAtDesc(petShop.getId(), pageable)
                .map(this::toResponse);
    }

    @Transactional
    public PetShopProductResponse createMine(String ownerEmail, PetShopProductUpsertRequest request, MultipartFile photo) {
        PetShop petShop = findOwnPetShop(ownerEmail);

        PetShopProduct product = PetShopProduct.builder()
                .petShop(petShop)
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .build();

        if (photo != null && !photo.isEmpty()) {
            product.setPhotoReference(imageStorageService.uploadImages(List.of(photo), PHOTO_KEY_PREFIX).get(0));
        }

        return toResponse(petShopProductRepository.save(product));
    }

    @Transactional
    public PetShopProductResponse updateMine(String ownerEmail, Long productId, PetShopProductUpsertRequest request, MultipartFile photo) {
        PetShop petShop = findOwnPetShop(ownerEmail);
        PetShopProduct product = petShopProductRepository.findByPetShop_IdAndId(petShop.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Ürün bulunamadı: " + productId));

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());

        if (photo != null && !photo.isEmpty()) {
            String eskiReferans = product.getPhotoReference();
            String yeniReferans = imageStorageService.uploadImages(List.of(photo), PHOTO_KEY_PREFIX).get(0);
            product.setPhotoReference(yeniReferans);
            if (eskiReferans != null) {
                imageStorageService.deleteImages(List.of(eskiReferans));
            }
        }

        product.setUpdatedAt(OffsetDateTime.now());

        return toResponse(petShopProductRepository.save(product));
    }

    @Transactional
    public void deleteMine(String ownerEmail, Long productId) {
        PetShop petShop = findOwnPetShop(ownerEmail);
        PetShopProduct product = petShopProductRepository.findByPetShop_IdAndId(petShop.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Ürün bulunamadı: " + productId));

        petShopProductRepository.delete(product);
    }

    /** Ürün id'leri global benzersiz, ekstra dükkan-scope gerekmiyor -- herkese açık tekil ürün görünümü. */
    @Transactional(readOnly = true)
    public PetShopProductResponse getPublicById(Long productId) {
        PetShopProduct product = petShopProductRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Ürün bulunamadı: " + productId));
        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public Page<PetShopProductResponse> listPublicForShop(Long shopId, Pageable pageable) {
        return petShopProductRepository.findByPetShop_IdOrderByCreatedAtDesc(shopId, pageable)
                .map(this::toResponse);
    }

    private PetShop findOwnPetShop(String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + ownerEmail));
        return petShopRepository.findByUser_Uid(owner.getUid())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Petshop kartı henüz oluşturulmamış — önce kartınızı oluşturun"));
    }

    private String resolvePhotoUrl(String photoReference) {
        return photoReference == null ? null : imageStorageService.createTemporaryReadUrl(photoReference);
    }

    private PetShopProductResponse toResponse(PetShopProduct product) {
        Double averageRating = petShopProductReviewRepository.findAverageRating(product.getId());
        long reviewCount = petShopProductReviewRepository.countByProduct_Id(product.getId());

        return new PetShopProductResponse(
                product.getId(),
                product.getPetShop().getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                resolvePhotoUrl(product.getPhotoReference()),
                averageRating,
                (int) reviewCount,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
