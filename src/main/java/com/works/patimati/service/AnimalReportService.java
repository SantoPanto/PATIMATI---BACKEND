package com.works.patimati.service;

import com.works.patimati.dto.request.AnimalReportCreateRequest;
import com.works.patimati.dto.response.AnimalReportResponse;
import com.works.patimati.entity.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface AnimalReportService {

    AnimalReportResponse createPublicReport(AnimalReportCreateRequest request, MultipartFile photo, String userEmail);

    AnimalReportResponse updateStatus(Long reportId, ReportStatus newStatus);
}
    Page<AnimalReportResponse> getReportsForMunicipality(ReportStatus status, Pageable pageable);
