package com.works.patimati.controller;

import com.works.patimati.dto.pet.AddTreatmentNoteRequest;
import com.works.patimati.dto.pet.AddVaccinationRequest;
import com.works.patimati.dto.pet.AddWeightLogRequest;
import com.works.patimati.dto.pet.PetResponse;
import com.works.patimati.dto.pet.PetTreatmentNoteResponse;
import com.works.patimati.dto.pet.PetUpsertRequest;
import com.works.patimati.dto.pet.PetVaccinationResponse;
import com.works.patimati.dto.pet.PetWeightLogResponse;
import com.works.patimati.dto.pet.SaveAiReportRequest;
import com.works.patimati.service.PetService;
import com.works.patimati.service.PetTreatmentNoteService;
import com.works.patimati.service.PetVaccinationService;
import com.works.patimati.service.PetWeightLogService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * "Evcil Hayvanlarım" uçları -- sahiplik path id ile DEĞİL, JWT'den gelen
 * e-posta ile belirlenir ({@code VetClinicController} ile AYNI ilke).
 *
 * <p>Not/aşı/kilo uçları hem hayvanın sahibine hem de sahiple ACCEPTED
 * ilişkisi olan vet'e açıktır -- rol ayrımı path'te DEĞİL, servis
 * katmanında yapılır (mevcut {@code listTreatmentNotes} ile AYNI ilke).</p>
 */
@Validated
@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;
    private final PetTreatmentNoteService petTreatmentNoteService;
    private final PetVaccinationService petVaccinationService;
    private final PetWeightLogService petWeightLogService;

    @GetMapping("/me")
    public ResponseEntity<List<PetResponse>> listMine(Authentication authentication) {
        return ResponseEntity.ok(petService.listMine(authentication.getName()));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PetResponse> create(
            @Valid @RequestPart("data") PetUpsertRequest data,
            @RequestPart(value = "photo", required = false) MultipartFile photo,
            @RequestPart(value = "extraPhotos", required = false) List<MultipartFile> extraPhotos,
            Authentication authentication
    ) {
        return ResponseEntity.ok(petService.create(authentication.getName(), data, photo, extraPhotos));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PetResponse> update(
            @PathVariable @Min(1) Long id,
            @Valid @RequestPart("data") PetUpsertRequest data,
            @RequestPart(value = "photo", required = false) MultipartFile photo,
            @RequestPart(value = "extraPhotos", required = false) List<MultipartFile> extraPhotos,
            Authentication authentication
    ) {
        return ResponseEntity.ok(petService.update(authentication.getName(), id, data, photo, extraPhotos));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Min(1) Long id, Authentication authentication) {
        petService.delete(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

    /** "Ben Neyim?" raporunu (ham JSON) hayvana kaydeder -- yalnızca sahip. */
    @PutMapping("/{id}/ai-report")
    public ResponseEntity<PetResponse> saveAiReport(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody SaveAiReportRequest body,
            Authentication authentication
    ) {
        return ResponseEntity.ok(petService.saveAiReport(authentication.getName(), id, body.reportJson()));
    }

    /**
     * Notları hem hayvanın sahibi hem de sahiple ACCEPTED ilişkisi olan bir
     * vet görebilir -- yetki kontrolü servis katmanında.
     */
    @GetMapping("/{id}/treatment-notes")
    public ResponseEntity<List<PetTreatmentNoteResponse>> listTreatmentNotes(
            @PathVariable @Min(1) Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(petTreatmentNoteService.listNotes(authentication.getName(), id));
    }

    /** Sahip kendi hayvanına kendi gözlemini ekler (vet'in ekleme ucu: {@code VetCustomerController}). */
    @PostMapping("/{id}/treatment-notes")
    public ResponseEntity<PetTreatmentNoteResponse> addOwnerNote(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody AddTreatmentNoteRequest body,
            Authentication authentication
    ) {
        return ResponseEntity.ok(petTreatmentNoteService.addOwnerNote(authentication.getName(), id, body.content()));
    }

    /** Notu yalnızca ORİJİNAL yazarı (vet ya da sahip) düzenleyebilir. */
    @PutMapping("/{id}/treatment-notes/{noteId}")
    public ResponseEntity<PetTreatmentNoteResponse> updateTreatmentNote(
            @PathVariable @Min(1) Long id,
            @PathVariable @Min(1) Long noteId,
            @Valid @RequestBody AddTreatmentNoteRequest body,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                petTreatmentNoteService.updateNote(authentication.getName(), id, noteId, body.content())
        );
    }

    /** Notu yalnızca ORİJİNAL yazarı (vet ya da sahip) silebilir. */
    @DeleteMapping("/{id}/treatment-notes/{noteId}")
    public ResponseEntity<Void> deleteTreatmentNote(
            @PathVariable @Min(1) Long id,
            @PathVariable @Min(1) Long noteId,
            Authentication authentication
    ) {
        petTreatmentNoteService.deleteNote(authentication.getName(), id, noteId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/vaccinations")
    public ResponseEntity<List<PetVaccinationResponse>> listVaccinations(
            @PathVariable @Min(1) Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(petVaccinationService.list(authentication.getName(), id));
    }

    @PostMapping("/{id}/vaccinations")
    public ResponseEntity<PetVaccinationResponse> addVaccination(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody AddVaccinationRequest body,
            Authentication authentication
    ) {
        return ResponseEntity.ok(petVaccinationService.add(authentication.getName(), id, body));
    }

    @GetMapping("/{id}/weight-logs")
    public ResponseEntity<List<PetWeightLogResponse>> listWeightLogs(
            @PathVariable @Min(1) Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(petWeightLogService.list(authentication.getName(), id));
    }

    @PostMapping("/{id}/weight-logs")
    public ResponseEntity<PetWeightLogResponse> addWeightLog(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody AddWeightLogRequest body,
            Authentication authentication
    ) {
        return ResponseEntity.ok(petWeightLogService.add(authentication.getName(), id, body));
    }
}
