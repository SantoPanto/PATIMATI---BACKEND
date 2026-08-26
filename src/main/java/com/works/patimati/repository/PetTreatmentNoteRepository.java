package com.works.patimati.repository;

import com.works.patimati.entity.PetTreatmentNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PetTreatmentNoteRepository extends JpaRepository<PetTreatmentNote, Long> {

    List<PetTreatmentNote> findByPet_IdOrderByCreatedAtDesc(Long petId);

    /** Vet panelinde "son ziyaret" özeti için. */
    Optional<PetTreatmentNote> findTopByPet_IdOrderByCreatedAtDesc(Long petId);

    long countByPet_Id(Long petId);
}
