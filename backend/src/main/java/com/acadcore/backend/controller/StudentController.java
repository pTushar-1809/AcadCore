package com.acadcore.backend.controller;

import com.acadcore.backend.dto.StudentRequest;
import com.acadcore.backend.entity.AcademicClass;
import com.acadcore.backend.entity.Role;
import com.acadcore.backend.entity.StudentProfile;
import com.acadcore.backend.entity.User;
import com.acadcore.backend.repository.AcademicClassRepository;
import com.acadcore.backend.repository.StudentProfileRepository;
import com.acadcore.backend.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final AcademicClassRepository classRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentController(
            UserRepository userRepository,
            StudentProfileRepository studentProfileRepository,
            AcademicClassRepository classRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.classRepository = classRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createStudent(
            @RequestBody StudentRequest request) {

        if (request.getEmail() == null ||
                request.getEmail().isBlank() ||
                request.getPassword() == null ||
                request.getPassword().isBlank() ||
                request.getFullName() == null ||
                request.getFullName().isBlank() ||
                request.getEnrollmentNumber() == null ||
                request.getEnrollmentNumber().isBlank() ||
                request.getClassId() == null) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Name, email, password, enrollment number and class are required"
                    ));
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email already registered"));
        }

        if (studentProfileRepository
                .existsByEnrollmentNumber(request.getEnrollmentNumber())) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Enrollment number already exists"
                    ));
        }

        AcademicClass academicClass =
                classRepository.findById(request.getClassId())
                        .orElse(null);

        if (academicClass == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Class not found"));
        }

        User student = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getFullName(),
                Role.STUDENT
        );

        User savedStudent = userRepository.save(student);

        StudentProfile profile =
                new StudentProfile(
                        savedStudent,
                        request.getEnrollmentNumber(),
                        request.getPhone(),
                        academicClass
                );

        StudentProfile savedProfile =
                studentProfileRepository.save(profile);

        return ResponseEntity.ok(
                Map.of(
                        "id", savedProfile.getId(),
                        "studentId", savedStudent.getId(),
                        "fullName", savedStudent.getFullName(),
                        "email", savedStudent.getEmail(),
                        "enrollmentNumber",
                        savedProfile.getEnrollmentNumber(),
                        "phone",
                        savedProfile.getPhone() == null
                                ? ""
                                : savedProfile.getPhone(),
                        "classId", academicClass.getId(),
                        "className", academicClass.getName()
                )
        );
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getMyProfile(
            Authentication authentication) {

        User student =
                userRepository.findByEmail(authentication.getName())
                        .orElse(null);

        if (student == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("message", "Student not found"));
        }

        StudentProfile profile =
                studentProfileRepository.findByUserId(student.getId())
                        .orElse(null);

        if (profile == null) {
            return ResponseEntity.status(404)
                    .body(Map.of(
                            "message",
                            "Student profile not found"
                    ));
        }

        return ResponseEntity.ok(
                Map.of(
                        "studentId", student.getId(),
                        "fullName", student.getFullName(),
                        "email", student.getEmail(),
                        "enrollmentNumber",
                        profile.getEnrollmentNumber(),
                        "phone",
                        profile.getPhone() == null
                                ? ""
                                : profile.getPhone(),
                        "classId",
                        profile.getAcademicClass().getId(),
                        "className",
                        profile.getAcademicClass().getName(),
                        "academicYear",
                        profile.getAcademicClass().getAcademicYear()
                )
        );
    }

    @GetMapping("/class/{classId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public ResponseEntity<?> getStudentsByClass(
            @PathVariable Long classId) {

        if (!classRepository.existsById(classId)) {
            return ResponseEntity.notFound().build();
        }

        List<Map<String, Object>> students =
                studentProfileRepository
                        .findByAcademicClassId(classId)
                        .stream()
                        .map(profile -> Map.<String, Object>of(
                                "studentId",
                                profile.getUser().getId(),
                                "fullName",
                                profile.getUser().getFullName(),
                                "email",
                                profile.getUser().getEmail(),
                                "enrollmentNumber",
                                profile.getEnrollmentNumber(),
                                "phone",
                                profile.getPhone() == null
                                        ? ""
                                        : profile.getPhone()
                        ))
                        .toList();

        return ResponseEntity.ok(students);
    }
}