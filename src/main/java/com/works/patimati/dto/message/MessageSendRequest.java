package com.works.patimati.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MessageSendRequest(
        @NotNull(message = "Alıcı ID bilgisi zorunludur")
        Long recipientId,

        @NotBlank(message = "Mesaj içeriği boş olamaz")
        @Size(max = 2000, message = "Mesaj içeriği en fazla 2000 karakter olabilir")
        String content
) {
}