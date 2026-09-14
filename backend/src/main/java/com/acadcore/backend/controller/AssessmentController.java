package com.acadcore.backend.controller;

import com.acadcore.backend.dto.AssessmentRequest;
import com.acadcore.backend.dto.AssessmentResponse;
import com.acadcore.backend.entity.Assessment;
import com.acadcore.backend.entity.Role;
import com.acadcore.backend.entity.Subject;
import com.acadcore.backend.entity.User;
import com.acadcore.backend.repository.AssessmentRepository;
import com.acadcore.backend.repository.SubjectRepository;
import com.acadcore.backend.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assessments")
public class AssessmentController {

    private final AssessmentRepository assessmentRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    public AssessmentController(
            AssessmentRepository assessmentRepository,
            SubjectRepository subjectRepository,
            UserRepository userRepository) {

        this.assessmentRepository = assessmentRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<?> createAssessment(
            @RequestBody AssessmentRequest request,
            Authentication authentication) {

        if (request.getTitle() == null ||
                request.getTitle().isBlank()) {

            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Title is required"));
        }

        if (request.getSubjectId() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Subject ID is required"
                    ));
        }

        if (request.getTotalMarks() == null ||
                request.getTotalMarks() <= 0) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Total marks must be greater than 0"
                    ));
        }

        if (request.getDurationMinutes() == null ||
                request.getDurationMinutes() <= 0) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Duration must be greater than 0"
                    ));
        }

        if (request.getType() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Assessment type is required"
                    ));
        }

        Subject subject =
                subjectRepository.findById(request.getSubjectId())
                        .orElse(null);

        if (subject == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Subject not found"
                    ));
        }

        User faculty =
                userRepository.findByEmail(authentication.getName())
                        .orElse(null);

        if (faculty == null ||
                faculty.getRole() != Role.FACULTY) {

            return ResponseEntity.status(403)
                    .body(Map.of(
                            "message",
                            "Faculty user not found"
                    ));
        }

        if (subject.getFaculty() == null ||
                !subject.getFaculty().getId()
                        .equals(faculty.getId())) {

            return ResponseEntity.status(403)
                    .body(Map.of(
                            "message",
                            "You are not assigned to this subject"
                    ));
        }

        Assessment assessment =
                new Assessment(
                        request.getTitle(),
                        request.getDescription(),
                        request.getTotalMarks(),
                        request.getDurationMinutes(),
                        request.getType(),
                        subject,
                        faculty
                );

        Assessment saved =
                assessmentRepository.save(assessment);

        return ResponseEntity.ok(
                toResponse(saved)
        );
    }

    @GetMapping("/subject/{subjectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public ResponseEntity<List<AssessmentResponse>> getBySubject(
            @PathVariable Long subjectId) {

        List<AssessmentResponse> assessments =
                assessmentRepository
                        .findBySubjectId(subjectId)
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(assessments);
    }

    private AssessmentResponse toResponse(
            Assessment assessment) {

        return new AssessmentResponse(
                assessment.getId(),
                assessment.getTitle(),
                assessment.getDescription() == null
                        ? ""
                        : assessment.getDescription(),
                assessment.getTotalMarks(),
                assessment.getDurationMinutes(),
                assessment.getType().name(),
                assessment.getSubject().getId(),
                assessment.getSubject().getName(),
                assessment.getFaculty().getId(),
                assessment.getFaculty().getFullName()
        );
    }
}