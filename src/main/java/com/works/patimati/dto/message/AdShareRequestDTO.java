package com.works.patimati.dto.message;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;

public record AdShareRequestDTO(
        @NotNull(message = "Hedef kullanıcı ID bilgisi zorunludur")
        @JsonAlias({"recipientId", "partnerId"})
        Long targetUserId,

        @NotNull(message = "İlan ID bilgisi zorunludur")
        Long adId
) {
}
