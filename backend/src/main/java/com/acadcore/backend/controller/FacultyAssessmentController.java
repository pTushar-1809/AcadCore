package com.acadcore.backend.controller;

import com.acadcore.backend.dto.AssessmentResponse;
import com.acadcore.backend.dto.ResultResponse;
import com.acadcore.backend.entity.Assessment;
import com.acadcore.backend.entity.Attempt;
import com.acadcore.backend.entity.Role;
import com.acadcore.backend.entity.User;
import com.acadcore.backend.repository.AssessmentRepository;
import com.acadcore.backend.repository.AttemptRepository;
import com.acadcore.backend.repository.StudentProfileRepository;
import com.acadcore.backend.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/faculty/assessments")
public class FacultyAssessmentController {

    private final AssessmentRepository assessmentRepository;
    private final AttemptRepository attemptRepository;
    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;

    public FacultyAssessmentController(
            AssessmentRepository assessmentRepository,
            AttemptRepository attemptRepository,
            UserRepository userRepository,
            StudentProfileRepository studentProfileRepository) {

        this.assessmentRepository = assessmentRepository;
        this.attemptRepository = attemptRepository;
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<?> getMyAssessments(
            Authentication authentication) {

        User faculty =
                userRepository.findByEmail(authentication.getName())
                        .orElse(null);

        if (faculty == null || faculty.getRole() != Role.FACULTY) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Faculty not found"));
        }

        List<AssessmentResponse> assessments =
                assessmentRepository
                        .findByFacultyId(faculty.getId())
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(assessments);
    }

    @GetMapping("/{assessmentId}/results")
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<?> getAssessmentResults(
            @PathVariable Long assessmentId,
            Authentication authentication) {

        User faculty =
                userRepository.findByEmail(authentication.getName())
                        .orElse(null);

        if (faculty == null) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Faculty not found"));
        }

        Assessment assessment =
                assessmentRepository.findById(assessmentId)
                        .orElse(null);

        if (assessment == null) {
            return ResponseEntity.notFound().build();
        }

        if (!assessment.getFaculty().getId().equals(faculty.getId())) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Access denied"));
        }

        List<ResultResponse> results =
                attemptRepository
                        .findByAssessmentId(assessmentId)
                        .stream()
                        .map(attempt -> {

                            String enrollmentNumber =
                                    studentProfileRepository
                                            .findByUserId(
                                                    attempt.getStudent().getId())
                                            .map(profile ->
                                                    profile.getEnrollmentNumber())
                                            .orElse("");

                            double percentage =
                                    attempt.getTotalMarks() == 0
                                            ? 0
                                            : attempt.getObtainedMarks()
                                            * 100.0
                                            / attempt.getTotalMarks();

                            return new ResultResponse(
                                    attempt.getId(),
                                    assessment.getId(),
                                    assessment.getTitle(),
                                    attempt.getStudent().getId(),
                                    attempt.getStudent().getFullName(),
                                    enrollmentNumber,
                                    attempt.getObtainedMarks(),
                                    attempt.getTotalMarks(),
                                    Math.round(percentage * 100.0) / 100.0,
                                    attempt.getSubmittedAt().toString()
                            );
                        })
                        .toList();

        return ResponseEntity.ok(results);
    }

    private AssessmentResponse toResponse(Assessment assessment) {

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