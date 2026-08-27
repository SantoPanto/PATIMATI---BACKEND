package com.works.patimati.dto.vetcustomer;

import com.works.patimati.entity.enums.VetCustomerRequestStatus;

import java.time.OffsetDateTime;

/** Vet'in "Gelen İstekler" listesindeki bir satır. */
public record VetCustomerRequestResponse(
        Long id,
        Long requesterId,
        String requesterName,
        VetCustomerRequestStatus status,
        OffsetDateTime createdAt
) {
}
