package com.acadcore.backend.dto;

public class ResultResponse {

    private Long attemptId;
    private Long assessmentId;
    private String assessmentTitle;
    private Long studentId;
    private String studentName;
    private String enrollmentNumber;
    private Integer obtainedMarks;
    private Integer totalMarks;
    private double percentage;
    private String submittedAt;

    public ResultResponse() {
    }

    public ResultResponse(
            Long attemptId,
            Long assessmentId,
            String assessmentTitle,
            Long studentId,
            String studentName,
            String enrollmentNumber,
            Integer obtainedMarks,
            Integer totalMarks,
            double percentage,
            String submittedAt) {

        this.attemptId = attemptId;
        this.assessmentId = assessmentId;
        this.assessmentTitle = assessmentTitle;
        this.studentId = studentId;
        this.studentName = studentName;
        this.enrollmentNumber = enrollmentNumber;
        this.obtainedMarks = obtainedMarks;
        this.totalMarks = totalMarks;
        this.percentage = percentage;
        this.submittedAt = submittedAt;
    }

    public Long getAttemptId() {
        return attemptId;
    }

    public Long getAssessmentId() {
        return assessmentId;
    }

    public String getAssessmentTitle() {
        return assessmentTitle;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getEnrollmentNumber() {
        return enrollmentNumber;
    }

    public Integer getObtainedMarks() {
        return obtainedMarks;
    }

    public Integer getTotalMarks() {
        return totalMarks;
    }

    public double getPercentage() {
        return percentage;
    }

    public String getSubmittedAt() {
        return submittedAt;
    }
}