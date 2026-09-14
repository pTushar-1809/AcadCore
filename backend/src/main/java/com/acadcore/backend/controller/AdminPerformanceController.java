package com.acadcore.backend.controller;

import com.acadcore.backend.dto.ClassPerformanceResponse;
import com.acadcore.backend.entity.AcademicClass;
import com.acadcore.backend.entity.Attempt;
import com.acadcore.backend.repository.AcademicClassRepository;
import com.acadcore.backend.repository.AssessmentRepository;
import com.acadcore.backend.repository.AttemptRepository;
import com.acadcore.backend.repository.StudentProfileRepository;
import com.acadcore.backend.repository.SubjectRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin/performance")
public class AdminPerformanceController {

    private final AcademicClassRepository classRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final SubjectRepository subjectRepository;
    private final AssessmentRepository assessmentRepository;
    private final AttemptRepository attemptRepository;

    public AdminPerformanceController(
            AcademicClassRepository classRepository,
            StudentProfileRepository studentProfileRepository,
            SubjectRepository subjectRepository,
            AssessmentRepository assessmentRepository,
            AttemptRepository attemptRepository) {

        this.classRepository = classRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.subjectRepository = subjectRepository;
        this.assessmentRepository = assessmentRepository;
        this.attemptRepository = attemptRepository;
    }

    @GetMapping("/classes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClassPerformanceResponse>>
    getClassPerformance() {

        List<ClassPerformanceResponse> response =
                new ArrayList<>();

        List<AcademicClass> classes =
                classRepository.findAll();

        for (AcademicClass academicClass : classes) {

            long studentCount =
                    studentProfileRepository
                            .findByAcademicClassId(
                                    academicClass.getId())
                            .size();

            long subjectCount =
                    subjectRepository
                            .findByAcademicClassId(
                                    academicClass.getId())
                            .size();

            long assessmentCount = 0;

            for (var subject :
                    subjectRepository.findByAcademicClassId(
                            academicClass.getId())) {

                assessmentCount +=
                        assessmentRepository
                                .findBySubjectId(subject.getId())
                                .size();
            }

            long attemptCount = 0;
            int totalObtained = 0;
            int totalPossible = 0;

            for (var subject :
                    subjectRepository.findByAcademicClassId(
                            academicClass.getId())) {

                var assessments =
                        assessmentRepository
                                .findBySubjectId(subject.getId());

                for (var assessment : assessments) {

                    List<Attempt> attempts =
                            attemptRepository
                                    .findByAssessmentId(
                                            assessment.getId());

                    attemptCount += attempts.size();

                    for (Attempt attempt : attempts) {
                        totalObtained +=
                                attempt.getObtainedMarks();

                        totalPossible +=
                                attempt.getTotalMarks();
                    }
                }
            }

            double averagePercentage =
                    totalPossible == 0
                            ? 0
                            : totalObtained * 100.0
                            / totalPossible;

            response.add(
                    new ClassPerformanceResponse(
                            academicClass.getId(),
                            academicClass.getName(),
                            academicClass.getAcademicYear(),
                            studentCount,
                            subjectCount,
                            assessmentCount,
                            attemptCount,
                            Math.round(
                                    averagePercentage * 100.0
                            ) / 100.0
                    )
            );
        }

        return ResponseEntity.ok(response);
    }
}