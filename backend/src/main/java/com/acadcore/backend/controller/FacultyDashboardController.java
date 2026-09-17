package com.acadcore.backend.controller;

import com.acadcore.backend.entity.Role;
import com.acadcore.backend.entity.StudentProfile;
import com.acadcore.backend.entity.Subject;
import com.acadcore.backend.entity.User;
import com.acadcore.backend.repository.StudentProfileRepository;
import com.acadcore.backend.repository.SubjectRepository;
import com.acadcore.backend.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/faculty/dashboard")
@PreAuthorize("hasRole('FACULTY')")
public class FacultyDashboardController {

    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final StudentProfileRepository studentProfileRepository;

    public FacultyDashboardController(
            UserRepository userRepository,
            SubjectRepository subjectRepository,
            StudentProfileRepository studentProfileRepository) {

        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.studentProfileRepository = studentProfileRepository;
    }

    // ============================================================
    // GET FACULTY PROFILE
    // ============================================================

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(
            org.springframework.security.core.Authentication authentication) {

        User faculty = getFaculty(authentication);

        if (faculty == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("message", "Faculty not found"));
        }

        return ResponseEntity.ok(Map.of(
                "id", faculty.getId(),
                "fullName", faculty.getFullName(),
                "email", faculty.getEmail(),
                "role", faculty.getRole().name()
        ));
    }

    // ============================================================
    // GET ALL CLASSES ASSIGNED TO FACULTY
    // ============================================================

    @GetMapping("/classes")
    public ResponseEntity<?> getMyClasses(
            org.springframework.security.core.Authentication authentication) {

        User faculty = getFaculty(authentication);

        if (faculty == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("message", "Faculty not found"));
        }

        List<Subject> subjects =
                subjectRepository.findByFacultyId(faculty.getId());

        Map<Long, Map<String, Object>> classMap =
                new LinkedHashMap<>();

        for (Subject subject : subjects) {

            if (subject.getAcademicClass() == null) {
                continue;
            }

            Long classId = subject.getAcademicClass().getId();

            classMap.putIfAbsent(
                    classId,
                    new LinkedHashMap<>(
                            Map.of(
                                    "id", classId,
                                    "name", subject.getAcademicClass().getName(),
                                    "academicYear",
                                    subject.getAcademicClass().getAcademicYear()
                            )
                    )
            );
        }

        return ResponseEntity.ok(
                new ArrayList<>(classMap.values())
        );
    }

    // ============================================================
    // GET SUBJECTS FOR SELECTED CLASS
    // ============================================================

    @GetMapping("/subjects")
    public ResponseEntity<?> getMySubjects(
            @RequestParam Long classId,
            org.springframework.security.core.Authentication authentication) {

        User faculty = getFaculty(authentication);

        if (faculty == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("message", "Faculty not found"));
        }

        List<Subject> subjects =
                subjectRepository.findByFacultyId(faculty.getId());

        List<Map<String, Object>> response =
                new ArrayList<>();

        for (Subject subject : subjects) {

            if (subject.getAcademicClass() == null) {
                continue;
            }

            if (!subject.getAcademicClass()
                    .getId()
                    .equals(classId)) {
                continue;
            }

            Map<String, Object> data =
                    new LinkedHashMap<>();

            data.put("id", subject.getId());
            data.put("name", subject.getName());
            data.put("description", subject.getDescription());
            data.put("classId", classId);
            data.put(
                    "className",
                    subject.getAcademicClass().getName()
            );

            response.add(data);
        }

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // GET STUDENTS FOR SELECTED CLASS + SUBJECT
    // ============================================================

    @GetMapping("/students")
    public ResponseEntity<?> getStudents(
            @RequestParam Long classId,
            @RequestParam Long subjectId,
            org.springframework.security.core.Authentication authentication) {

        User faculty = getFaculty(authentication);

        if (faculty == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("message", "Faculty not found"));
        }

        // --------------------------------------------------------
        // Verify subject belongs to this faculty
        // --------------------------------------------------------

        Subject subject =
                subjectRepository.findById(subjectId)
                        .orElse(null);

        if (subject == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Subject not found"
                    ));
        }

        if (subject.getFaculty() == null ||
                !subject.getFaculty()
                        .getId()
                        .equals(faculty.getId())) {

            return ResponseEntity.status(403)
                    .body(Map.of(
                            "message",
                            "You are not assigned to this subject"
                    ));
        }

        // --------------------------------------------------------
        // Verify subject belongs to selected class
        // --------------------------------------------------------

        if (subject.getAcademicClass() == null ||
                !subject.getAcademicClass()
                        .getId()
                        .equals(classId)) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Subject does not belong to selected class"
                    ));
        }

        // --------------------------------------------------------
        // Get students
        // --------------------------------------------------------

        List<StudentProfile> students =
                studentProfileRepository
                        .findByAcademicClassId(classId);

        List<Map<String, Object>> response =
                new ArrayList<>();

        for (StudentProfile student : students) {

            Map<String, Object> data =
                    new LinkedHashMap<>();

            data.put("id", student.getId());
            data.put(
                    "fullName",
                    student.getUser() != null
                            ? student.getUser().getFullName()
                            : ""
            );
            data.put(
                    "email",
                    student.getUser() != null
                            ? student.getUser().getEmail()
                            : ""
            );
            data.put(
                    "enrollmentNumber",
                    student.getEnrollmentNumber()
            );
            data.put(
                    "phone",
                    student.getPhone()
            );
            data.put("classId", classId);
            data.put(
                    "className",
                    subject.getAcademicClass().getName()
            );
            data.put("subjectId", subject.getId());
            data.put("subjectName", subject.getName());

            response.add(data);
        }

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // HELPER
    // ============================================================

    private User getFaculty(
            org.springframework.security.core.Authentication authentication) {

        if (authentication == null ||
                authentication.getName() == null) {

            return null;
        }

        User user =
                userRepository
                        .findByEmail(authentication.getName())
                        .orElse(null);

        if (user == null ||
                user.getRole() != Role.FACULTY) {

            return null;
        }

        return user;
    }
}