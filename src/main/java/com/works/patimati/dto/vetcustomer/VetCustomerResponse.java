package com.works.patimati.dto.vetcustomer;

import com.works.patimati.entity.enums.VetCustomerRequestStatus;

import java.time.OffsetDateTime;

/** Vet'in "Müşterilerim" listesindeki bir satır (status=ACCEPTED). */
public record VetCustomerResponse(
        Long id,
        Long requesterId,
        String requesterName,
        VetCustomerRequestStatus status,
        OffsetDateTime createdAt
) {
}
