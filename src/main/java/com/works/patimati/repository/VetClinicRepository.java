package com.works.patimati.repository;

import com.works.patimati.entity.VetClinic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VetClinicRepository extends JpaRepository<VetClinic, Long> {

    Optional<VetClinic> findByUser_Uid(Long userUid);

    Page<VetClinic> findByCityIgnoreCase(String city, Pageable pageable);
}
