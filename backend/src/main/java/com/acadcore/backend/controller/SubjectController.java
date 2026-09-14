package com.acadcore.backend.controller;

import com.acadcore.backend.dto.AssignFacultyRequest;
import com.acadcore.backend.dto.SubjectResponse;
import com.acadcore.backend.entity.AcademicClass;
import com.acadcore.backend.entity.Role;
import com.acadcore.backend.entity.Subject;
import com.acadcore.backend.entity.User;
import com.acadcore.backend.repository.AcademicClassRepository;
import com.acadcore.backend.repository.SubjectRepository;
import com.acadcore.backend.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/classes/{classId}/subjects")
public class SubjectController {

    private final SubjectRepository subjectRepository;
    private final AcademicClassRepository classRepository;
    private final UserRepository userRepository;

    public SubjectController(
            SubjectRepository subjectRepository,
            AcademicClassRepository classRepository,
            UserRepository userRepository) {

        this.subjectRepository = subjectRepository;
        this.classRepository = classRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createSubject(
            @PathVariable Long classId,
            @RequestBody Map<String, String> request) {

        String name = request.get("name");

        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Subject name is required"
                    ));
        }

        AcademicClass academicClass =
                classRepository.findById(classId)
                        .orElse(null);

        if (academicClass == null) {
            return ResponseEntity.notFound().build();
        }

        if (subjectRepository.existsByNameAndAcademicClassId(
                name,
                classId)) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Subject already exists in this class"
                    ));
        }

        Subject subject =
                new Subject(name, academicClass);

        Subject savedSubject =
                subjectRepository.save(subject);

        return ResponseEntity.ok(
                toResponse(savedSubject)
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SubjectResponse>> getSubjects(
            @PathVariable Long classId) {

        List<SubjectResponse> subjects =
                subjectRepository
                        .findByAcademicClassId(classId)
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(subjects);
    }

    @PutMapping("/{subjectId}/faculty")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignFaculty(
            @PathVariable Long classId,
            @PathVariable Long subjectId,
            @RequestBody AssignFacultyRequest request) {

        Subject subject =
                subjectRepository.findById(subjectId)
                        .orElse(null);

        if (subject == null) {
            return ResponseEntity.notFound().build();
        }

        if (!subject.getAcademicClass().getId().equals(classId)) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Subject does not belong to this class"
                    ));
        }

        if (request.getFacultyId() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Faculty ID is required"
                    ));
        }

        User faculty =
                userRepository.findById(request.getFacultyId())
                        .orElse(null);

        if (faculty == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Faculty user not found"
                    ));
        }

        if (faculty.getRole() != Role.FACULTY) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Selected user is not a faculty member"
                    ));
        }

        subject.setFaculty(faculty);

        Subject savedSubject =
                subjectRepository.save(subject);

        return ResponseEntity.ok(
                toResponse(savedSubject)
        );
    }

    private SubjectResponse toResponse(
            Subject subject) {

        Long facultyId = null;
        String facultyName = "";

        if (subject.getFaculty() != null) {
            facultyId = subject.getFaculty().getId();
            facultyName = subject.getFaculty().getFullName();
        }

        return new SubjectResponse(
                subject.getId(),
                subject.getName(),
                subject.getAcademicClass().getId(),
                subject.getAcademicClass().getName(),
                facultyId,
                facultyName
        );
    }
}