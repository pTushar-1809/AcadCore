package com.acadcore.backend.controller;

import com.acadcore.backend.dto.DashboardStatsResponse;
import com.acadcore.backend.entity.Role;
import com.acadcore.backend.repository.AcademicClassRepository;
import com.acadcore.backend.repository.AssessmentRepository;
import com.acadcore.backend.repository.AttemptRepository;
import com.acadcore.backend.repository.StudentProfileRepository;
import com.acadcore.backend.repository.SubjectRepository;
import com.acadcore.backend.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final AcademicClassRepository classRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final AssessmentRepository assessmentRepository;
    private final AttemptRepository attemptRepository;

    public DashboardController(
            AcademicClassRepository classRepository,
            SubjectRepository subjectRepository,
            UserRepository userRepository,
            AssessmentRepository assessmentRepository,
            AttemptRepository attemptRepository) {

        this.classRepository = classRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.assessmentRepository = assessmentRepository;
        this.attemptRepository = attemptRepository;
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardStatsResponse> getAdminDashboard() {

        long totalClasses =
                classRepository.count();

        long totalSubjects =
                subjectRepository.count();

        long totalFaculty =
                userRepository.findByRole(Role.FACULTY).size();

        long totalStudents =
                userRepository.findByRole(Role.STUDENT).size();

        long totalAssessments =
                assessmentRepository.count();

        long totalAttempts =
                attemptRepository.count();

        DashboardStatsResponse response =
                new DashboardStatsResponse(
                        totalClasses,
                        totalSubjects,
                        totalFaculty,
                        totalStudents,
                        totalAssessments,
                        totalAttempts
                );

        return ResponseEntity.ok(response);
    }
}