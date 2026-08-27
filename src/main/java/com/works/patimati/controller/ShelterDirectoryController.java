package com.works.patimati.controller;

import com.works.patimati.dto.ad.AdResponse;
import com.works.patimati.dto.shelter.ShelterPublicResponse;
import com.works.patimati.service.AdoptionService;
import com.works.patimati.service.ShelterService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * "Hizmetler &gt; Barınak" herkese açık dizini -- SecurityConfig'de GET-only
 * permitAll (bkz. {@code /api/shelters}). Sahip/kullanıcı bilgisi dönmez,
 * yalnızca {@link ShelterPublicResponse}. {@code PetShopDirectoryController}
 * ile AYNI desen, ek olarak bir barınağın herkese açık sahiplendirme ilanı
 * listesi.
 */
@Validated
@RestController
@RequestMapping("/api/shelters")
@RequiredArgsConstructor
public class ShelterDirectoryController {

    private static final int MAX_PAGE_SIZE = 100;

    private final ShelterService shelterService;
    private final AdoptionService adoptionService;

    @GetMapping("/{id}")
    public ResponseEntity<ShelterPublicResponse> getShelter(@PathVariable @Min(1) Long id) {
        return ResponseEntity.ok(shelterService.getPublicById(id));
    }

    @GetMapping
    public ResponseEntity<Page<ShelterPublicResponse>> listShelters(
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(MAX_PAGE_SIZE) int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return ResponseEntity.ok(shelterService.listPublic(pageable, city));
    }

    /**
     * Bir barınağın herkese açık, güncel sahiplendirme ilanları --
     * {@code Ad}'de {@code shelter_id} FK'ı YOK, bu yüzden önce kart id'si
     * sahip uid'ine çözülür ({@code ShelterService#resolveOwnerUid}), sonra
     * {@code AdoptionService#getPublicAdoptionAdsByOwner} çağrılır.
     */
    @GetMapping("/{id}/adoptions")
    public ResponseEntity<Page<AdResponse>> getAdoptions(
            @PathVariable @Min(1) Long id,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(MAX_PAGE_SIZE) int size
    ) {
        Long ownerUid = shelterService.resolveOwnerUid(id);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(adoptionService.getPublicAdoptionAdsByOwner(ownerUid, pageable));
    }
}
