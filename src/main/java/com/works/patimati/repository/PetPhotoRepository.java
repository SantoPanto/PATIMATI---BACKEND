package com.works.patimati.repository;

import com.works.patimati.entity.PetPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetPhotoRepository extends JpaRepository<PetPhoto, Long> {

    List<PetPhoto> findByPet_IdOrderBySortOrderAsc(Long petId);

    void deleteByPet_Id(Long petId);
}
