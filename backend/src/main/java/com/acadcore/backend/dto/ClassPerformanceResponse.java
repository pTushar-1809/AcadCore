package com.acadcore.backend.dto;

public class ClassPerformanceResponse {

    private Long classId;
    private String className;
    private String academicYear;

    private long studentCount;
    private long subjectCount;
    private long assessmentCount;
    private long attemptCount;

    private double averagePercentage;

    public ClassPerformanceResponse() {
    }

    public ClassPerformanceResponse(
            Long classId,
            String className,
            String academicYear,
            long studentCount,
            long subjectCount,
            long assessmentCount,
            long attemptCount,
            double averagePercentage) {

        this.classId = classId;
        this.className = className;
        this.academicYear = academicYear;
        this.studentCount = studentCount;
        this.subjectCount = subjectCount;
        this.assessmentCount = assessmentCount;
        this.attemptCount = attemptCount;
        this.averagePercentage = averagePercentage;
    }

    public Long getClassId() {
        return classId;
    }

    public String getClassName() {
        return className;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public long getStudentCount() {
        return studentCount;
    }

    public long getSubjectCount() {
        return subjectCount;
    }

    public long getAssessmentCount() {
        return assessmentCount;
    }

    public long getAttemptCount() {
        return attemptCount;
    }

    public double getAveragePercentage() {
        return averagePercentage;
    }
}