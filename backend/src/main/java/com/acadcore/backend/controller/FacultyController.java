package com.acadcore.backend.controller;

import com.acadcore.backend.entity.Role;
import com.acadcore.backend.entity.Subject;
import com.acadcore.backend.entity.User;
import com.acadcore.backend.repository.SubjectRepository;
import com.acadcore.backend.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/faculty")
public class FacultyController {

    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;

    public FacultyController(
            UserRepository userRepository,
            SubjectRepository subjectRepository) {

        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
    }

    // ============================================================
    // GET ALL ACTIVE FACULTY
    // ============================================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAllFaculty() {

        List<Map<String, Object>> facultyList = new ArrayList<>();

        List<User> facultyMembers =
                userRepository.findByRole(Role.FACULTY);

        for (User faculty : facultyMembers) {

            Map<String, Object> data = new HashMap<>();

            data.put("id", faculty.getId());
            data.put("facultyId", faculty.getId());
            data.put("fullName", faculty.getFullName());
            data.put("email", faculty.getEmail());
            data.put("role", faculty.getRole().name());

            // Default values
            data.put("classId", null);
            data.put("className", null);

            data.put("subjectId", null);
            data.put("subjectName", null);

            // ----------------------------------------------------
            // Find subject assigned to this faculty
            // ----------------------------------------------------

            List<Subject> assignedSubjects =
                    subjectRepository.findByFacultyId(faculty.getId());

            if (!assignedSubjects.isEmpty()) {

                Subject subject = assignedSubjects.get(0);

                // Subject ID
                data.put(
                        "subjectId",
                        subject.getId()
                );

                // Subject Name
                data.put(
                        "subjectName",
                        subject.getName()
                );

                // Class ID
                if (subject.getAcademicClass() != null) {

                    data.put(
                            "classId",
                            subject.getAcademicClass().getId()
                    );

                    // Class Name
                    data.put(
                            "className",
                            subject.getAcademicClass().getName()
                    );
                }
            }

            // Active faculty
            data.put("status", "ACTIVE");

            facultyList.add(data);
        }

        return ResponseEntity.ok(facultyList);
    }


    // ============================================================
    // EDIT ACTIVE FACULTY
    // ============================================================

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> updateFaculty(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        User faculty =
                userRepository.findById(id).orElse(null);

        if (faculty == null ||
                faculty.getRole() != Role.FACULTY) {

            return ResponseEntity.notFound().build();
        }

        String fullName =
                request.get("fullName") != null
                        ? request.get("fullName").toString().trim()
                        : null;

        String email =
                request.get("email") != null
                        ? request.get("email").toString().trim()
                        : null;

        Long classId = null;
        Long subjectId = null;

        if (request.get("classId") != null &&
                !request.get("classId").toString().isBlank()) {

            classId = Long.valueOf(
                    request.get("classId").toString()
            );
        }

        if (request.get("subjectId") != null &&
                !request.get("subjectId").toString().isBlank()) {

            subjectId = Long.valueOf(
                    request.get("subjectId").toString()
            );
        }

        // --------------------------------------------------------
        // Validation
        // --------------------------------------------------------

        if (fullName == null || fullName.isBlank()) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Full name is required"
                    ));
        }

        if (email == null || email.isBlank()) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Email is required"
                    ));
        }

        // --------------------------------------------------------
        // Duplicate email
        // --------------------------------------------------------

        if (!faculty.getEmail().equalsIgnoreCase(email)
                && userRepository.existsByEmail(email)) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Email already registered"
                    ));
        }

        // --------------------------------------------------------
        // Update faculty information
        // --------------------------------------------------------

        faculty.setFullName(fullName);
        faculty.setEmail(email);

        userRepository.save(faculty);

        // --------------------------------------------------------
        // Change assignment
        // --------------------------------------------------------

        if (classId != null && subjectId != null) {

            Subject newSubject =
                    subjectRepository.findById(subjectId)
                            .orElse(null);

            if (newSubject == null) {

                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "message",
                                "Subject not found"
                        ));
            }

            if (newSubject.getAcademicClass() == null ||
                    !newSubject.getAcademicClass()
                            .getId()
                            .equals(classId)) {

                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "message",
                                "Subject does not belong to selected class"
                        ));
            }

            // Remove old assignments
            List<Subject> oldSubjects =
                    subjectRepository
                            .findByFacultyId(faculty.getId());

            for (Subject oldSubject : oldSubjects) {

                if (!oldSubject.getId()
                        .equals(newSubject.getId())) {

                    oldSubject.setFaculty(null);

                    subjectRepository.save(oldSubject);
                }
            }

            // Assign new subject
            newSubject.setFaculty(faculty);

            subjectRepository.save(newSubject);
        }

        // --------------------------------------------------------
        // Return updated faculty
        // --------------------------------------------------------

        Map<String, Object> response =
                new HashMap<>();

        response.put(
                "id",
                faculty.getId()
        );

        response.put(
                "facultyId",
                faculty.getId()
        );

        response.put(
                "fullName",
                faculty.getFullName()
        );

        response.put(
                "email",
                faculty.getEmail()
        );

        response.put(
                "role",
                faculty.getRole().name()
        );

        response.put(
                "status",
                "ACTIVE"
        );

        // Get current assignment
        List<Subject> assignedSubjects =
                subjectRepository.findByFacultyId(
                        faculty.getId()
                );

        if (!assignedSubjects.isEmpty()) {

            Subject subject =
                    assignedSubjects.get(0);

            response.put(
                    "subjectId",
                    subject.getId()
            );

            response.put(
                    "subjectName",
                    subject.getName()
            );

            if (subject.getAcademicClass() != null) {

                response.put(
                        "classId",
                        subject.getAcademicClass().getId()
                );

                response.put(
                        "className",
                        subject.getAcademicClass().getName()
                );

            } else {

                response.put("classId", null);
                response.put("className", null);
            }

        } else {

            response.put("subjectId", null);
            response.put("subjectName", null);
            response.put("classId", null);
            response.put("className", null);
        }

        return ResponseEntity.ok(response);
    }


    // ============================================================
    // DELETE ACTIVE FACULTY
    // ============================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> deleteFaculty(
            @PathVariable Long id) {

        User faculty =
                userRepository.findById(id).orElse(null);

        if (faculty == null ||
                faculty.getRole() != Role.FACULTY) {

            return ResponseEntity.notFound().build();
        }

        // Remove faculty from subjects first
        List<Subject> assignedSubjects =
                subjectRepository.findByFacultyId(id);

        for (Subject subject : assignedSubjects) {

            subject.setFaculty(null);
        }

        subjectRepository.saveAll(assignedSubjects);

        // Delete faculty account
        userRepository.delete(faculty);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Faculty deleted successfully"
                )
        );
    }
}