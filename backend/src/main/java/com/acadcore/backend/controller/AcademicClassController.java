package com.acadcore.backend.controller;

import com.acadcore.backend.entity.AcademicClass;
import com.acadcore.backend.repository.AcademicClassRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/classes")
public class AcademicClassController {

    private final AcademicClassRepository classRepository;

    public AcademicClassController(
            AcademicClassRepository classRepository) {
        this.classRepository = classRepository;
    }

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

        if (classRepository.existsByName(academicClass.getName())) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Class already exists"
                    ));
        }

        AcademicClass savedClass =
                classRepository.save(academicClass);

        return ResponseEntity.ok(savedClass);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AcademicClass>> getAllClasses() {

        return ResponseEntity.ok(
                classRepository.findAll()
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteClass(
            @PathVariable Long id) {

        if (!classRepository.existsById(id)) {

            return ResponseEntity.notFound().build();
        }

        classRepository.deleteById(id);

        return ResponseEntity.ok(
                Map.of("message", "Class deleted successfully")
        );
    }
}