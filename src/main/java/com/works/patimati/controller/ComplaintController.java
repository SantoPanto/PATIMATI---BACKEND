package com.works.patimati.controller;

import com.works.patimati.dto.complaint.ComplaintRequest;
import com.works.patimati.dto.complaint.ComplaintResponse;
import com.works.patimati.service.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@Validated
@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    @PostMapping
    public ResponseEntity<ComplaintResponse> createComplaint(
            Authentication authentication,
            @Valid @RequestBody ComplaintRequest request
    ) {
        ComplaintResponse response = complaintService.createComplaint(
                authentication.getName(),
                request
        );

        return ResponseEntity
                .created(URI.create("/api/complaints/" + response.id()))
                .body(response);
    }
}
