package com.works.patimati.dto.vetcustomer;

import jakarta.validation.constraints.NotNull;

public record SendVetCustomerRequestRequest(
        @NotNull(message = "Veteriner kimliği zorunludur")
        Long vetId
) {
}
