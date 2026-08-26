package com.works.patimati.repository;

import com.works.patimati.entity.PetVaccination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetVaccinationRepository extends JpaRepository<PetVaccination, Long> {

    List<PetVaccination> findByPet_IdOrderByAdministeredDateDesc(Long petId);
}
