package com.works.patimati.dto;

import com.works.patimati.entity.Ad;
import lombok.Data;

@Data
public class AdCreateDto {
    private String title;
    private String description;
    private Ad.AdType adType;
    private double latitude;
    private double longitude;
}