package com.works.patimati.dto.request;

import com.works.patimati.entity.enums.ReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnimalReportCreateRequest {

    @NotBlank(message = "İletişim bilgisi zorunludur")
    private String reporterContact;

    @NotNull(message = "İhbar türü zorunludur")
    private ReportType type;

    private String note;

    @NotNull(message = "Enlem (latitude) zorunludur")
    private Double latitude;

    @NotNull(message = "Boylam (longitude) zorunludur")
    private Double longitude;
}
