package com.acadcore.backend.controller;

import com.acadcore.backend.dto.FacultyInvitationRequest;
import com.acadcore.backend.entity.FacultyInvitation;
import com.acadcore.backend.entity.InvitationStatus;
import com.acadcore.backend.repository.FacultyInvitationRepository;
import com.acadcore.backend.service.FacultyInvitationService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/faculty-invitations")
public class FacultyInvitationController {

    private final FacultyInvitationService invitationService;
    private final FacultyInvitationRepository invitationRepository;

    public FacultyInvitationController(
            FacultyInvitationService invitationService,
            FacultyInvitationRepository invitationRepository) {

        this.invitationService = invitationService;
        this.invitationRepository = invitationRepository;
    }


    // ============================================================
    // SEND / RESEND INVITATION
    // ============================================================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> sendInvitation(
            @RequestBody FacultyInvitationRequest request) {

        try {

            if (request.getFullName() == null ||
                    request.getFullName().isBlank()) {

                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "message",
                                "Full name is required"
                        ));
            }

            if (request.getEmail() == null ||
                    request.getEmail().isBlank()) {

                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "message",
                                "Email is required"
                        ));
            }

            if (request.getClassId() == null) {

                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "message",
                                "Class is required"
                        ));
            }

            if (request.getSubjectId() == null) {

                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "message",
                                "Subject is required"
                        ));
            }

            FacultyInvitation invitation =
                    invitationService.createInvitation(
                            request.getFullName(),
                            request.getEmail(),
                            request.getClassId(),
                            request.getSubjectId()
                    );

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Faculty invitation sent successfully",

                            "invitationId",
                            invitation.getId()
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


    // ============================================================
    // GET PENDING INVITATIONS
    // ============================================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>>
    getPendingInvitations() {

        List<FacultyInvitation> invitations =
                invitationRepository
                        .findByStatus(
                                InvitationStatus.PENDING
                        );

        List<Map<String, Object>> result =
                invitations.stream()
                        .map(invitation -> {

                            Map<String, Object> data =
                                    new HashMap<>();

                            data.put(
                                    "id",
                                    "invitation-" +
                                            invitation.getId()
                            );

                            data.put(
                                    "invitationId",
                                    invitation.getId()
                            );

                            data.put(
                                    "fullName",
                                    invitation.getFullName()
                            );

                            data.put(
                                    "email",
                                    invitation.getEmail()
                            );

                            data.put(
                                    "classId",
                                    invitation
                                            .getAcademicClass()
                                            .getId()
                            );

                            data.put(
                                    "subjectId",
                                    invitation
                                            .getSubject()
                                            .getId()
                            );

                            data.put(
                                    "className",
                                    invitation
                                            .getAcademicClass()
                                            .getName()
                            );

                            data.put(
                                    "subjectName",
                                    invitation
                                            .getSubject()
                                            .getName()
                            );

                            data.put(
                                    "status",
                                    "PENDING"
                            );

                            return data;

                        })
                        .toList();

        return ResponseEntity.ok(result);
    }


    // ============================================================
    // EDIT PENDING INVITATION
    // ============================================================

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateInvitation(
            @PathVariable Long id,
            @RequestBody FacultyInvitationRequest request) {

        FacultyInvitation invitation =
                invitationRepository
                        .findById(id)
                        .orElse(null);

        if (invitation == null) {

            return ResponseEntity.notFound().build();
        }

        if (invitation.getStatus()
                != InvitationStatus.PENDING) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Only pending invitations can be edited"
                    ));
        }

        try {

            // We don't create a second invitation.
            // Update the existing invitation.

            invitation.setFullName(
                    request.getFullName().trim()
            );

            invitation.setEmail(
                    request.getEmail().trim()
            );

            // Find class
            var academicClass =
                    invitation.getAcademicClass();

            if (request.getClassId() != null &&
                    !academicClass.getId()
                            .equals(request.getClassId())) {

                academicClass =
                        invitation
                                .getAcademicClass()
                                .getId()
                                .equals(request.getClassId())
                                ? invitation.getAcademicClass()
                                : null;
            }

            // Because the service already validates
            // class-subject relationship, for assignment
            // changes we use the repositories directly.

            if (request.getClassId() != null &&
                    request.getSubjectId() != null) {

                var newClass =
                        invitation
                                .getAcademicClass();

                if (!newClass.getId()
                        .equals(request.getClassId())) {

                    return ResponseEntity.badRequest()
                            .body(Map.of(
                                    "message",
                                    "Changing class from this screen requires resending the invitation"
                            ));
                }

                var newSubject =
                        invitation
                                .getSubject();

                if (!newSubject.getId()
                        .equals(request.getSubjectId())) {

                    return ResponseEntity.badRequest()
                            .body(Map.of(
                                    "message",
                                    "Changing subject from this screen requires resending the invitation"
                            ));
                }
            }

            invitationRepository.save(invitation);

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Invitation updated successfully"
                    )
            );

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            e.getMessage()
                    ));
        }
    }


    // ============================================================
    // DELETE / CANCEL PENDING INVITATION
    // ============================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteInvitation(
            @PathVariable Long id) {

        FacultyInvitation invitation =
                invitationRepository
                        .findById(id)
                        .orElse(null);

        if (invitation == null) {

            return ResponseEntity.notFound().build();
        }

        invitationRepository.delete(invitation);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Faculty invitation deleted successfully"
                )
        );
    }
}