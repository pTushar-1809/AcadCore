package com.acadcore.backend.repository;

import com.acadcore.backend.entity.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssessmentRepository
        extends JpaRepository<Assessment, Long> {

    List<Assessment> findBySubjectId(Long subjectId);

    List<Assessment> findByFacultyId(Long facultyId);
}