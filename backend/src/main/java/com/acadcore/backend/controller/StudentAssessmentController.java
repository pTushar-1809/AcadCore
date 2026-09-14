package com.acadcore.backend.controller;

import com.acadcore.backend.dto.AssessmentResponse;
import com.acadcore.backend.entity.Assessment;
import com.acadcore.backend.entity.Role;
import com.acadcore.backend.entity.StudentProfile;
import com.acadcore.backend.entity.User;
import com.acadcore.backend.repository.AssessmentRepository;
import com.acadcore.backend.repository.StudentProfileRepository;
import com.acadcore.backend.repository.UserRepository;
import com.acadcore.backend.repository.SubjectRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student/assessments")
public class StudentAssessmentController {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final SubjectRepository subjectRepository;
    private final AssessmentRepository assessmentRepository;

    public StudentAssessmentController(
            UserRepository userRepository,
            StudentProfileRepository studentProfileRepository,
            SubjectRepository subjectRepository,
            AssessmentRepository assessmentRepository) {

        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.subjectRepository = subjectRepository;
        this.assessmentRepository = assessmentRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getAvailableAssessments(
            Authentication authentication) {

        User student =
                userRepository.findByEmail(authentication.getName())
                        .orElse(null);

        if (student == null || student.getRole() != Role.STUDENT) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Student not found"));
        }

        StudentProfile profile =
                studentProfileRepository.findByUserId(student.getId())
                        .orElse(null);

        if (profile == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Student profile not found"
                    ));
        }

        Long classId =
                profile.getAcademicClass().getId();

        List<AssessmentResponse> result =
                new ArrayList<>();

        subjectRepository.findByAcademicClassId(classId)
                .forEach(subject -> {

                    List<Assessment> assessments =
                            assessmentRepository
                                    .findBySubjectId(subject.getId());

                    assessments.forEach(assessment ->
                            result.add(
                                    new AssessmentResponse(
                                            assessment.getId(),
                                            assessment.getTitle(),
                                            assessment.getDescription() == null
                                                    ? ""
                                                    : assessment.getDescription(),
                                            assessment.getTotalMarks(),
                                            assessment.getDurationMinutes(),
                                            assessment.getType().name(),
                                            subject.getId(),
                                            subject.getName(),
                                            assessment.getFaculty().getId(),
                                            assessment.getFaculty().getFullName()
                                    )
                            )
                    );
                });

        return ResponseEntity.ok(result);
    }
}