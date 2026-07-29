package com.works.patimati.dto.complaint;

import com.works.patimati.entity.enums.ComplaintReason;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintRequest {

    private Long reportedAdId;

    private Long reportedUserId;

    @NotNull(message = "Şikayet sebebi boş olamaz")
    private ComplaintReason reason;

    @NotBlank(message = "Şikayet açıklaması boş olamaz")
    @Size(max = 1000, message = "Açıklama en fazla 1000 karakter olabilir")
    private String description;
}
