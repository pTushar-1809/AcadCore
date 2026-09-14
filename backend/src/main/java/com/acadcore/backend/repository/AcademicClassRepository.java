package com.acadcore.backend.repository;

import com.acadcore.backend.entity.AcademicClass;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademicClassRepository
        extends JpaRepository<AcademicClass, Long> {

    boolean existsByName(String name);
}