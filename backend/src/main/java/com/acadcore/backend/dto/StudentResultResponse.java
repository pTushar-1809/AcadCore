package com.acadcore.backend.dto;

public class StudentResultResponse {

    private String studentName;
    private String enrollmentNumber;
    private String className;

    private long totalAssessments;
    private long completedAssessments;

    private int totalMarks;
    private int obtainedMarks;

    private double averagePercentage;

    public StudentResultResponse() {
    }

    public StudentResultResponse(
            String studentName,
            String enrollmentNumber,
            String className,
            long totalAssessments,
            long completedAssessments,
            int totalMarks,
            int obtainedMarks,
            double averagePercentage) {

        this.studentName = studentName;
        this.enrollmentNumber = enrollmentNumber;
        this.className = className;
        this.totalAssessments = totalAssessments;
        this.completedAssessments = completedAssessments;
        this.totalMarks = totalMarks;
        this.obtainedMarks = obtainedMarks;
        this.averagePercentage = averagePercentage;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getEnrollmentNumber() {
        return enrollmentNumber;
    }

    public String getClassName() {
        return className;
    }

    public long getTotalAssessments() {
        return totalAssessments;
    }

    public long getCompletedAssessments() {
        return completedAssessments;
    }

    public int getTotalMarks() {
        return totalMarks;
    }

    public int getObtainedMarks() {
        return obtainedMarks;
    }

    public double getAveragePercentage() {
        return averagePercentage;
    }
}