package com.acadcore.backend.repository;

import com.acadcore.backend.entity.AcademicClass;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AcademicClassRepository
        extends JpaRepository<AcademicClass, Long> {

    boolean existsByName(String name);

    Optional<AcademicClass> findByJoinCodeIgnoreCase(
            String joinCode
    );
}