package com.acadcore.backend.entity;

import jakarta.persistence.*;

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

    public AcademicClass() {
    }

    public AcademicClass(String name, String academicYear) {
        this.name = name;
        this.academicYear = academicYear;
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
}