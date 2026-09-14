package com.acadcore.backend.repository;

import com.acadcore.backend.entity.Attempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttemptRepository
        extends JpaRepository<Attempt, Long> {

    List<Attempt> findByStudentId(Long studentId);

    List<Attempt> findByAssessmentId(Long assessmentId);

    boolean existsByStudentIdAndAssessmentId(
            Long studentId,
            Long assessmentId);
}