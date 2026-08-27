package com.works.patimati.repository;

import com.works.patimati.entity.BusinessApplication;
import com.works.patimati.entity.enums.BusinessApplicationStatus;
import com.works.patimati.entity.enums.BusinessType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusinessApplicationRepository extends JpaRepository<BusinessApplication, Long> {

    Page<BusinessApplication> findAllByStatus(BusinessApplicationStatus status, Pageable pageable);

    Page<BusinessApplication> findAllByBusinessType(BusinessType businessType, Pageable pageable);

    Page<BusinessApplication> findAllByStatusAndBusinessType(
            BusinessApplicationStatus status, BusinessType businessType, Pageable pageable);

    Optional<BusinessApplication> findFirstByApplicant_UidAndStatus(Long applicantUid, BusinessApplicationStatus status);

    boolean existsByApplicant_UidAndStatus(Long applicantUid, BusinessApplicationStatus status);

    Optional<BusinessApplication> findFirstByApplicant_UidOrderByCreatedAtDesc(Long applicantUid);
}
