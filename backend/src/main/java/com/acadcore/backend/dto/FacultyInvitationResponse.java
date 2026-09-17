package com.acadcore.backend.dto;

public class FacultyInvitationResponse {

    private Long id;
    private String fullName;
    private String email;

    private Long classId;
    private String className;

    private Long subjectId;
    private String subjectName;

    private String status;

    public FacultyInvitationResponse(
            Long id,
            String fullName,
            String email,
            Long classId,
            String className,
            Long subjectId,
            String subjectName,
            String status) {

        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.classId = classId;
        this.className = className;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public Long getClassId() {
        return classId;
    }

    public String getClassName() {
        return className;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public String getStatus() {
        return status;
    }
}