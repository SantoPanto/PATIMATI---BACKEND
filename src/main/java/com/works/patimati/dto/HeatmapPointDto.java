package com.works.patimati.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeatmapPointDto {
    private Double latitude;
    private Double longitude;
    private String type;
    private LocalDateTime createdAt;
}
