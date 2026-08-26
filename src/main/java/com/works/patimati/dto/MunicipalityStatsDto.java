package com.works.patimati.dto;

import lombok.Data;

@Data
public class MunicipalityStatsDto {
    private String district;
    private long lostCount;
    private long foundCount;
    private long adoptionCount;
    private long reunionCount;
}
