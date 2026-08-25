package com.works.patimati.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Admin'in "Instagram'a Gönder" işlemine gönderdiği, düzenlenmiş olabilecek
 * son caption -- {@code AdInstagramPublication.suggestedCaption} yalnızca
 * bir ÖNERİdir, admin göndermeden önce değiştirebilir.
 */
public record InstagramPublishRequest(
        @NotBlank(message = "Caption boş olamaz")
        @Size(max = 2200, message = "Caption en fazla 2200 karakter olabilir")
        String caption
) {
}
