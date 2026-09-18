package com.acadcore.backend.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "academic_classes")
public class AcademicClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String academicYear;

    @Column(unique = true, length = 20)
    private String joinCode;

    public AcademicClass() {
    }

    public AcademicClass(
            String name,
            String academicYear) {

        this.name = name;
        this.academicYear = academicYear;
    }

    @PrePersist
    private void generateJoinCode() {

        if (joinCode == null || joinCode.isBlank()) {

            joinCode =
                    UUID.randomUUID()
                            .toString()
                            .replace("-", "")
                            .substring(0, 8)
                            .toUpperCase();
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public String getJoinCode() {
        return joinCode;
    }

    public void setJoinCode(String joinCode) {
        this.joinCode = joinCode;
    }
}