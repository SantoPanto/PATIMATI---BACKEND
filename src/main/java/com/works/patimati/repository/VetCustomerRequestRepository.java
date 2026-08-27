package com.works.patimati.repository;

import com.works.patimati.entity.VetCustomerRequest;
import com.works.patimati.entity.enums.VetCustomerRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VetCustomerRequestRepository extends JpaRepository<VetCustomerRequest, Long> {

    Optional<VetCustomerRequest> findByRequester_UidAndVet_Uid(Long requesterUid, Long vetUid);

    Page<VetCustomerRequest> findByVet_UidAndStatus(Long vetUid, VetCustomerRequestStatus status, Pageable pageable);
}
