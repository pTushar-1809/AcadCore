package com.acadcore.backend.repository;

import com.acadcore.backend.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    List<Subject> findByAcademicClassId(Long classId);

    boolean existsByNameAndAcademicClassId(
            String name,
            Long classId
    );
}