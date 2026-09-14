package com.acadcore.backend.dto;

public class DashboardStatsResponse {

    private long totalClasses;
    private long totalSubjects;
    private long totalFaculty;
    private long totalStudents;
    private long totalAssessments;
    private long totalAttempts;

    public DashboardStatsResponse() {
    }

    public DashboardStatsResponse(
            long totalClasses,
            long totalSubjects,
            long totalFaculty,
            long totalStudents,
            long totalAssessments,
            long totalAttempts) {

        this.totalClasses = totalClasses;
        this.totalSubjects = totalSubjects;
        this.totalFaculty = totalFaculty;
        this.totalStudents = totalStudents;
        this.totalAssessments = totalAssessments;
        this.totalAttempts = totalAttempts;
    }

    public long getTotalClasses() {
        return totalClasses;
    }

    public long getTotalSubjects() {
        return totalSubjects;
    }

    public long getTotalFaculty() {
        return totalFaculty;
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public long getTotalAssessments() {
        return totalAssessments;
    }

    public long getTotalAttempts() {
        return totalAttempts;
    }
}