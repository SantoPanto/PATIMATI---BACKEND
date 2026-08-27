package com.works.patimati.dto.response;

import com.works.patimati.entity.enums.AnimalType;
import com.works.patimati.entity.enums.BusinessApplicationStatus;
import com.works.patimati.entity.enums.BusinessType;

import java.time.OffsetDateTime;
import java.util.Set;

public record BusinessApplicationResponse(
        Long id,
        BusinessType businessType,
        String name,
        String address,
        String city,
        String district,
        String phone,
        String workingHours,
        String photoUrl,
        Double latitude,
        Double longitude,
        Set<AnimalType> animalTypes,
        BusinessApplicationStatus status,
        String rejectionReason,
        OffsetDateTime createdAt,
        OffsetDateTime decidedAt,
        Long applicantUserId,
        String applicantName,
        String applicantEmail
) {
}
