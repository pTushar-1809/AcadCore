package com.acadcore.backend.controller;

import com.acadcore.backend.dto.FacultyResponse;
import com.acadcore.backend.entity.Role;
import com.acadcore.backend.entity.User;
import com.acadcore.backend.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/faculty")
public class FacultyController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public FacultyController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createFaculty(
            @RequestBody Map<String, String> request) {

        String email = request.get("email");
        String password = request.get("password");
        String fullName = request.get("fullName");

        if (email == null || email.isBlank()
                || password == null || password.isBlank()
                || fullName == null || fullName.isBlank()) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Full name, email and password are required"
                    ));
        }

        if (userRepository.existsByEmail(email)) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Email already registered"
                    ));
        }

        User faculty = new User(
                email,
                passwordEncoder.encode(password),
                fullName,
                Role.FACULTY
        );

        User savedFaculty =
                userRepository.save(faculty);

        return ResponseEntity.ok(
                new FacultyResponse(
                        savedFaculty.getId(),
                        savedFaculty.getEmail(),
                        savedFaculty.getFullName(),
                        savedFaculty.getRole().name()
                )
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FacultyResponse>> getAllFaculty() {

        List<FacultyResponse> facultyList =
                userRepository.findByRole(Role.FACULTY)
                        .stream()
                        .map(faculty ->
                                new FacultyResponse(
                                        faculty.getId(),
                                        faculty.getEmail(),
                                        faculty.getFullName(),
                                        faculty.getRole().name()
                                )
                        )
                        .toList();

        return ResponseEntity.ok(facultyList);
    }
}