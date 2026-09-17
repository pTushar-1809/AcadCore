package com.acadcore.backend.controller;

import com.acadcore.backend.dto.AcceptFacultyInvitationRequest;
import com.acadcore.backend.service.FacultyInvitationAcceptanceService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/faculty-invitations")
public class FacultyInvitationAcceptanceController {

    private final FacultyInvitationAcceptanceService acceptanceService;

    public FacultyInvitationAcceptanceController(
            FacultyInvitationAcceptanceService acceptanceService) {

        this.acceptanceService = acceptanceService;
    }

    @PostMapping("/accept")
    public ResponseEntity<?> acceptInvitation(
            @RequestBody AcceptFacultyInvitationRequest request) {

        if (request.getToken() == null ||
                request.getToken().isBlank()) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Invitation token is required"
                    ));
        }

        try {

            return ResponseEntity.ok(
                    acceptanceService.acceptInvitation(
                            request
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            e.getMessage()
                    ));
        }
    }
}