package com.acadcore.backend.dto;

public class AssessmentResponse {

    private Long id;
    private String title;
    private String description;
    private Integer totalMarks;
    private Integer durationMinutes;
    private String type;
    private Long subjectId;
    private String subjectName;
    private Long facultyId;
    private String facultyName;

    public AssessmentResponse() {
    }

    public AssessmentResponse(
            Long id,
            String title,
            String description,
            Integer totalMarks,
            Integer durationMinutes,
            String type,
            Long subjectId,
            String subjectName,
            Long facultyId,
            String facultyName) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.totalMarks = totalMarks;
        this.durationMinutes = durationMinutes;
        this.type = type;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.facultyId = facultyId;
        this.facultyName = facultyName;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Integer getTotalMarks() {
        return totalMarks;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public String getType() {
        return type;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public Long getFacultyId() {
        return facultyId;
    }

    public String getFacultyName() {
        return facultyName;
    }
}