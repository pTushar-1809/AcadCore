package com.acadcore.backend.controller;

import com.acadcore.backend.entity.AcademicClass;
import com.acadcore.backend.repository.AcademicClassRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/classes")
public class AcademicClassController {

    private final AcademicClassRepository classRepository;

    public AcademicClassController(
            AcademicClassRepository classRepository) {

        this.classRepository = classRepository;
    }

    // ============================================================
    // CREATE CLASS
    // ============================================================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createClass(
            @RequestBody AcademicClass academicClass) {

        if (academicClass.getName() == null ||
                academicClass.getName().isBlank() ||
                academicClass.getAcademicYear() == null ||
                academicClass.getAcademicYear().isBlank()) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Class name and academic year are required"
                    ));
        }

        String className =
                academicClass.getName().trim();

        String academicYear =
                academicClass.getAcademicYear().trim();

        if (classRepository.existsByName(className)) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Class already exists"
                    ));
        }

        academicClass.setName(className);
        academicClass.setAcademicYear(academicYear);

        /*
         * New classes normally receive the code
         * through @PrePersist.
         *
         * This fallback also guarantees that the
         * class receives a code here.
         */
        if (academicClass.getJoinCode() == null ||
                academicClass.getJoinCode().isBlank()) {

            academicClass.setJoinCode(
                    generateUniqueJoinCode()
            );
        }

        AcademicClass savedClass =
                classRepository.save(academicClass);

        return ResponseEntity.ok(savedClass);
    }

    // ============================================================
    // GET ALL CLASSES
    // ============================================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AcademicClass>> getAllClasses() {

        List<AcademicClass> classes =
                classRepository.findAll();

        /*
         * Existing classes may have been created before
         * joinCode was added.
         *
         * Generate a code for those classes.
         */
        for (AcademicClass academicClass : classes) {

            if (academicClass.getJoinCode() == null ||
                    academicClass.getJoinCode().isBlank()) {

                academicClass.setJoinCode(
                        generateUniqueJoinCode()
                );

                classRepository.save(academicClass);
            }
        }

        return ResponseEntity.ok(
                classRepository.findAll()
        );
    }

    // ============================================================
    // DELETE CLASS
    // ============================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteClass(
            @PathVariable Long id) {

        if (!classRepository.existsById(id)) {

            return ResponseEntity.notFound().build();
        }

        classRepository.deleteById(id);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Class deleted successfully"
                )
        );
    }

    // ============================================================
    // GENERATE UNIQUE JOIN CODE
    // ============================================================

    private String generateUniqueJoinCode() {

        String code;

        do {

            code =
                    UUID.randomUUID()
                            .toString()
                            .replace("-", "")
                            .substring(0, 8)
                            .toUpperCase();

        } while (
                classRepository
                        .findByJoinCodeIgnoreCase(code)
                        .isPresent()
        );

        return code;
    }
}