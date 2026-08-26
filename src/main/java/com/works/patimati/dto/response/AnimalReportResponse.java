package com.works.patimati.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnimalReportResponse {
    private Long id;
    private String reporterContact;
    private String type;
    private String note;
    private String photoUrl;
    private Double latitude;
    private Double longitude;
    private String city;
    private String district;
    private String status;
    private LocalDateTime createdAt;
}