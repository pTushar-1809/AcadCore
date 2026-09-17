package com.acadcore.backend.dto;

import com.acadcore.backend.entity.AssessmentStatus;

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

    private AssessmentStatus status;


    // =========================================================
    // DEFAULT CONSTRUCTOR
    // =========================================================

    public AssessmentResponse() {
    }


    // =========================================================
    // OLD CONSTRUCTOR
    // =========================================================
    // Keeps existing controllers working

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

        this(
                id,
                title,
                description,
                totalMarks,
                durationMinutes,
                type,
                subjectId,
                subjectName,
                facultyId,
                facultyName,
                null
        );
    }


    // =========================================================
    // NEW CONSTRUCTOR WITH STATUS
    // =========================================================

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
            String facultyName,
            AssessmentStatus status) {

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
        this.status = status;
    }


    // =========================================================
    // GETTERS / SETTERS
    // =========================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public Integer getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(Integer totalMarks) {
        this.totalMarks = totalMarks;
    }


    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }


    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }


    public Long getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(Long facultyId) {
        this.facultyId = facultyId;
    }


    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }


    public AssessmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssessmentStatus status) {
        this.status = status;
    }
}