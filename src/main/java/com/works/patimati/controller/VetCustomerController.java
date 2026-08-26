package com.works.patimati.controller;

import com.works.patimati.dto.pet.AddTreatmentNoteRequest;
import com.works.patimati.dto.pet.PetResponse;
import com.works.patimati.dto.pet.PetTreatmentNoteResponse;
import com.works.patimati.dto.vetcustomer.VetCustomerRequestResponse;
import com.works.patimati.dto.vetcustomer.VetCustomerResponse;
import com.works.patimati.service.PetTreatmentNoteService;
import com.works.patimati.service.VetCustomerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Vet'e özel müşteri yönetimi uçları -- {@code /api/vet/**}, SecurityConfig'te
 * ZATEN {@code hasRole("VET")} (bkz. VetClinicController javadoc'u).
 */
@Validated
@RestController
@RequestMapping("/api/vet")
@RequiredArgsConstructor
public class VetCustomerController {

    private static final int MAX_PAGE_SIZE = 100;

    private final VetCustomerService vetCustomerService;
    private final PetTreatmentNoteService petTreatmentNoteService;

    @GetMapping("/customer-requests")
    public ResponseEntity<List<VetCustomerRequestResponse>> listIncomingRequests(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(MAX_PAGE_SIZE) int size,
            Authentication authentication
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        return ResponseEntity.ok(vetCustomerService.listIncomingRequests(authentication.getName(), pageable));
    }

    @PostMapping("/customer-requests/{id}/accept")
    public ResponseEntity<VetCustomerRequestResponse> accept(@PathVariable @Min(1) Long id, Authentication authentication) {
        return ResponseEntity.ok(vetCustomerService.accept(authentication.getName(), id));
    }

    @PostMapping("/customer-requests/{id}/reject")
    public ResponseEntity<VetCustomerRequestResponse> reject(@PathVariable @Min(1) Long id, Authentication authentication) {
        return ResponseEntity.ok(vetCustomerService.reject(authentication.getName(), id));
    }

    @GetMapping("/customers")
    public ResponseEntity<List<VetCustomerResponse>> listCustomers(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(MAX_PAGE_SIZE) int size,
            Authentication authentication
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        return ResponseEntity.ok(vetCustomerService.listCustomers(authentication.getName(), pageable));
    }

    @GetMapping("/customers/{customerId}/pets")
    public ResponseEntity<List<PetResponse>> getCustomerPets(
            @PathVariable @Min(1) Long customerId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(vetCustomerService.getCustomerPets(authentication.getName(), customerId));
    }

    @PostMapping("/pets/{petId}/treatment-notes")
    public ResponseEntity<PetTreatmentNoteResponse> addTreatmentNote(
            @PathVariable @Min(1) Long petId,
            @Valid @RequestBody AddTreatmentNoteRequest body,
            Authentication authentication
    ) {
        return ResponseEntity.ok(petTreatmentNoteService.addNote(authentication.getName(), petId, body.content()));
    }
}
