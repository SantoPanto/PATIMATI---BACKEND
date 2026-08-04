package com.works.patimati.dto.complaint;

import com.works.patimati.entity.enums.ComplaintReason;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * İlan şikayeti istek DTO'su.
 * İstemciden (client) reportedUserId ALINMAZ!
 * Backend tarafında reportedAdId ile ilan bulunduktan sonra ilan sahibinin uid'si otomatik çekilip şikayet kaydına atanır.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdComplaintRequestDTO {

    /**
     * Şikayet edilen ilanın ID'si.
     */
    @NotNull(message = "Şikayet edilecek ilan ID'si boş olamaz")
    private Long reportedAdId;

    /**
     * Şikayet sebebi (Enum).
     */
    @NotNull(message = "Şikayet sebebi boş olamaz")
    private ComplaintReason reason;

    /**
     * Şikayet detay açıklaması.
     */
    @NotBlank(message = "Şikayet açıklaması boş olamaz")
    @Size(max = 1000, message = "Açıklama en fazla 1000 karakter olabilir")
    private String description;
}
