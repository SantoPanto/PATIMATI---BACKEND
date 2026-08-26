package com.works.patimati.repository;

import com.works.patimati.entity.PetWeightLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetWeightLogRepository extends JpaRepository<PetWeightLog, Long> {

    List<PetWeightLog> findByPet_IdOrderByRecordedAtAsc(Long petId);
}
