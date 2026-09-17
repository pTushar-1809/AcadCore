package com.acadcore.backend.dto;

public class FacultyManagementResponse {

    private Long id;
    private Long invitationId;

    private String email;
    private String fullName;
    private String role;

    private String status;

    private String className;
    private String subjectName;

    public FacultyManagementResponse() {
    }

    public FacultyManagementResponse(
            Long id,
            Long invitationId,
            String email,
            String fullName,
            String role,
            String status,
            String className,
            String subjectName
    ) {
        this.id = id;
        this.invitationId = invitationId;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.status = status;
        this.className = className;
        this.subjectName = subjectName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInvitationId() {
        return invitationId;
    }

    public void setInvitationId(Long invitationId) {
        this.invitationId = invitationId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }
}