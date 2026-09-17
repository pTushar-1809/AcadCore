package com.acadcore.backend.dto;

public class SubjectResponse {

    private Long id;
    private String name;
    private String description;
    private Long classId;
    private String className;
    private Long facultyId;
    private String facultyName;

    public SubjectResponse() {
    }

    public SubjectResponse(
            Long id,
            String name,
            String description,
            Long classId,
            String className,
            Long facultyId,
            String facultyName) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.classId = classId;
        this.className = className;
        this.facultyId = facultyId;
        this.facultyName = facultyName;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Long getClassId() {
        return classId;
    }

    public String getClassName() {
        return className;
    }

    public Long getFacultyId() {
        return facultyId;
    }

    public String getFacultyName() {
        return facultyName;
    }
}