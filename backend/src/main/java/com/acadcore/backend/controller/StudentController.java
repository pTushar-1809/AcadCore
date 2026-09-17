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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
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

    // ============================================================
    // CREATE STUDENT
    // ============================================================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
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

        String email = request.getEmail().trim();
        String fullName = request.getFullName().trim();
        String enrollmentNumber =
                request.getEnrollmentNumber().trim();

        if (userRepository.existsByEmail(email)) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Email already registered"
                    ));
        }

        if (studentProfileRepository
                .existsByEnrollmentNumber(enrollmentNumber)) {

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
                    .body(Map.of(
                            "message",
                            "Class not found"
                    ));
        }

        User student = new User(
                email,
                passwordEncoder.encode(
                        request.getPassword()
                ),
                fullName,
                Role.STUDENT
        );

        User savedStudent =
                userRepository.save(student);

        StudentProfile profile =
                new StudentProfile(
                        savedStudent,
                        enrollmentNumber,
                        request.getPhone(),
                        academicClass
                );

        StudentProfile savedProfile =
                studentProfileRepository.save(profile);

        return ResponseEntity.ok(
                buildStudentResponse(
                        savedProfile
                )
        );
    }

    // ============================================================
    // GET ALL STUDENTS - ADMIN
    // ============================================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAllStudents() {

        List<Map<String, Object>> students =
                new ArrayList<>();

        List<StudentProfile> profiles =
                studentProfileRepository
                        .findByUserRole(Role.STUDENT);

        for (StudentProfile profile : profiles) {

            students.add(
                    buildStudentResponse(profile)
            );
        }

        return ResponseEntity.ok(students);
    }

    // ============================================================
    // GET MY PROFILE
    // ============================================================

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getMyProfile(
            Authentication authentication) {

        User student =
                userRepository
                        .findByEmail(authentication.getName())
                        .orElse(null);

        if (student == null) {

            return ResponseEntity.status(404)
                    .body(Map.of(
                            "message",
                            "Student not found"
                    ));
        }

        StudentProfile profile =
                studentProfileRepository
                        .findByUserId(student.getId())
                        .orElse(null);

        if (profile == null) {

            return ResponseEntity.status(404)
                    .body(Map.of(
                            "message",
                            "Student profile not found"
                    ));
        }

        return ResponseEntity.ok(
                buildStudentResponse(profile)
        );
    }

    // ============================================================
    // GET STUDENTS BY CLASS
    // ============================================================

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
                        .map(profile -> {

                            Map<String, Object> data =
                                    new HashMap<>();

                            data.put(
                                    "studentId",
                                    profile.getUser().getId()
                            );

                            data.put(
                                    "fullName",
                                    profile.getUser().getFullName()
                            );

                            data.put(
                                    "email",
                                    profile.getUser().getEmail()
                            );

                            data.put(
                                    "enrollmentNumber",
                                    profile.getEnrollmentNumber()
                            );

                            data.put(
                                    "phone",
                                    profile.getPhone() == null
                                            ? ""
                                            : profile.getPhone()
                            );

                            return data;

                        })
                        .toList();

        return ResponseEntity.ok(students);
    }

    // ============================================================
    // UPDATE STUDENT
    // ============================================================

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> updateStudent(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        User student =
                userRepository.findById(id)
                        .orElse(null);

        if (student == null ||
                student.getRole() != Role.STUDENT) {

            return ResponseEntity.notFound().build();
        }

        StudentProfile profile =
                studentProfileRepository
                        .findByUserId(id)
                        .orElse(null);

        if (profile == null) {

            return ResponseEntity.status(404)
                    .body(Map.of(
                            "message",
                            "Student profile not found"
                    ));
        }

        String fullName =
                request.get("fullName") == null
                        ? null
                        : request.get("fullName")
                                .toString()
                                .trim();

        String email =
                request.get("email") == null
                        ? null
                        : request.get("email")
                                .toString()
                                .trim();

        String enrollmentNumber =
                request.get("enrollmentNumber") == null
                        ? null
                        : request.get("enrollmentNumber")
                                .toString()
                                .trim();

        String phone =
                request.get("phone") == null
                        ? ""
                        : request.get("phone")
                                .toString()
                                .trim();

        Long classId = null;

        if (request.get("classId") != null &&
                !request.get("classId")
                        .toString()
                        .isBlank()) {

            classId = Long.valueOf(
                    request.get("classId")
                            .toString()
            );
        }

        // --------------------------------------------------------
        // Validation
        // --------------------------------------------------------

        if (fullName == null ||
                fullName.isBlank()) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Full name is required"
                    ));
        }

        if (email == null ||
                email.isBlank()) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Email is required"
                    ));
        }

        if (enrollmentNumber == null ||
                enrollmentNumber.isBlank()) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Enrollment number is required"
                    ));
        }

        // --------------------------------------------------------
        // Email duplicate check
        // --------------------------------------------------------

        if (!student.getEmail()
                .equalsIgnoreCase(email)
                &&
                userRepository.existsByEmail(email)) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Email already registered"
                    ));
        }

        // --------------------------------------------------------
        // Enrollment duplicate check
        // --------------------------------------------------------

        if (!profile.getEnrollmentNumber()
                .equalsIgnoreCase(enrollmentNumber)
                &&
                studentProfileRepository
                        .existsByEnrollmentNumberAndIdNot(
                                enrollmentNumber,
                                profile.getId()
                        )) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Enrollment number already exists"
                    ));
        }

        // --------------------------------------------------------
        // Class
        // --------------------------------------------------------

        if (classId == null) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Class is required"
                    ));
        }

        AcademicClass academicClass =
                classRepository.findById(classId)
                        .orElse(null);

        if (academicClass == null) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Class not found"
                    ));
        }

        // --------------------------------------------------------
        // Update User
        // --------------------------------------------------------

        student.setFullName(fullName);
        student.setEmail(email);

        userRepository.save(student);

        // --------------------------------------------------------
        // Update Student Profile
        // --------------------------------------------------------

        profile.setEnrollmentNumber(
                enrollmentNumber
        );

        profile.setPhone(phone);

        profile.setAcademicClass(
                academicClass
        );

        StudentProfile updatedProfile =
                studentProfileRepository.save(profile);

        return ResponseEntity.ok(
                buildStudentResponse(
                        updatedProfile
                )
        );
    }

    // ============================================================
    // DELETE STUDENT
    // ============================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> deleteStudent(
            @PathVariable Long id) {

        User student =
                userRepository.findById(id)
                        .orElse(null);

        if (student == null ||
                student.getRole() != Role.STUDENT) {

            return ResponseEntity.notFound().build();
        }

        StudentProfile profile =
                studentProfileRepository
                        .findByUserId(id)
                        .orElse(null);

        if (profile != null) {

            studentProfileRepository.delete(profile);
        }

        userRepository.delete(student);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Student deleted successfully"
                )
        );
    }

    // ============================================================
    // COMMON RESPONSE
    // ============================================================

    private Map<String, Object> buildStudentResponse(
            StudentProfile profile) {

        Map<String, Object> data =
                new HashMap<>();

        User student =
                profile.getUser();

        data.put(
                "id",
                profile.getId()
        );

        data.put(
                "studentId",
                student.getId()
        );

        data.put(
                "fullName",
                student.getFullName()
        );

        data.put(
                "email",
                student.getEmail()
        );

        data.put(
                "enrollmentNumber",
                profile.getEnrollmentNumber()
        );

        data.put(
                "phone",
                profile.getPhone() == null
                        ? ""
                        : profile.getPhone()
        );

        if (profile.getAcademicClass() != null) {

            data.put(
                    "classId",
                    profile.getAcademicClass().getId()
            );

            data.put(
                    "className",
                    profile.getAcademicClass().getName()
            );

            data.put(
                    "academicYear",
                    profile.getAcademicClass()
                            .getAcademicYear()
            );

        } else {

            data.put("classId", null);
            data.put("className", null);
            data.put("academicYear", null);
        }

        data.put(
                "status",
                "ACTIVE"
        );

        data.put(
                "role",
                student.getRole().name()
        );

        return data;
    }
}