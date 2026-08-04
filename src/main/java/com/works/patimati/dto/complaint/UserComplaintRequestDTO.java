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
 * Kullanıcı profili şikayeti istek DTO'su.
 * İstemciden (client) reportedAdId ALINMAZ, sadece şikayet edilen kullanıcının uid'si alınır.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserComplaintRequestDTO {

    /**
     * Şikayet edilen kullanıcının benzersiz kimlik numarası (uid).
     */
    @NotNull(message = "Şikayet edilecek kullanıcı ID'si boş olamaz")
    private Long reportedUserId;

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
