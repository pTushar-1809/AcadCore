package com.acadcore.backend.controller;

import com.acadcore.backend.dto.StudentResultResponse;
import com.acadcore.backend.entity.Attempt;
import com.acadcore.backend.entity.Role;
import com.acadcore.backend.entity.StudentProfile;
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
@RequestMapping("/api/student/results")
public class StudentResultController {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final AttemptRepository attemptRepository;
    private final AssessmentRepository assessmentRepository;

    public StudentResultController(
            UserRepository userRepository,
            StudentProfileRepository studentProfileRepository,
            AttemptRepository attemptRepository,
            AssessmentRepository assessmentRepository) {

        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.attemptRepository = attemptRepository;
        this.assessmentRepository = assessmentRepository;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getResultSummary(
            Authentication authentication) {

        User student =
                userRepository.findByEmail(authentication.getName())
                        .orElse(null);

        if (student == null || student.getRole() != Role.STUDENT) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Student not found"));
        }

        StudentProfile profile =
                studentProfileRepository
                        .findByUserId(student.getId())
                        .orElse(null);

        if (profile == null) {
            return ResponseEntity.notFound().build();
        }

        List<Attempt> attempts =
                attemptRepository.findByStudentId(student.getId());

        int obtainedMarks = attempts.stream()
                .mapToInt(Attempt::getObtainedMarks)
                .sum();

        int totalMarks = attempts.stream()
                .mapToInt(Attempt::getTotalMarks)
                .sum();

        double averagePercentage =
                totalMarks == 0
                        ? 0
                        : obtainedMarks * 100.0 / totalMarks;

        long totalAssessments =
                assessmentRepository.count();

        StudentResultResponse response =
                new StudentResultResponse(
                        student.getFullName(),
                        profile.getEnrollmentNumber(),
                        profile.getAcademicClass().getName(),
                        totalAssessments,
                        attempts.size(),
                        totalMarks,
                        obtainedMarks,
                        Math.round(averagePercentage * 100.0) / 100.0
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getDetailedResults(
            Authentication authentication) {

        User student =
                userRepository.findByEmail(authentication.getName())
                        .orElse(null);

        if (student == null) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Student not found"));
        }

        List<Map<String, Object>> results =
                attemptRepository.findByStudentId(student.getId())
                        .stream()
                        .map(attempt -> {

                            double percentage =
                                    attempt.getTotalMarks() == 0
                                            ? 0
                                            : attempt.getObtainedMarks()
                                            * 100.0
                                            / attempt.getTotalMarks();

                            return Map.<String, Object>of(
                                    "attemptId",
                                    attempt.getId(),
                                    "assessmentId",
                                    attempt.getAssessment().getId(),
                                    "assessmentTitle",
                                    attempt.getAssessment().getTitle(),
                                    "subjectName",
                                    attempt.getAssessment()
                                            .getSubject()
                                            .getName(),
                                    "obtainedMarks",
                                    attempt.getObtainedMarks(),
                                    "totalMarks",
                                    attempt.getTotalMarks(),
                                    "percentage",
                                    Math.round(percentage * 100.0) / 100.0,
                                    "submittedAt",
                                    attempt.getSubmittedAt().toString()
                            );
                        })
                        .toList();

        return ResponseEntity.ok(results);
    }
}