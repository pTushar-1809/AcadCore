package com.acadcore.backend.repository;

import com.acadcore.backend.entity.Role;
import com.acadcore.backend.entity.StudentProfile;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentProfileRepository
        extends JpaRepository<StudentProfile, Long> {

    Optional<StudentProfile> findByUserId(Long userId);

    List<StudentProfile> findByAcademicClassId(Long classId);

    boolean existsByEnrollmentNumber(String enrollmentNumber);

    boolean existsByEnrollmentNumberAndIdNot(
            String enrollmentNumber,
            Long id
    );

    List<StudentProfile> findByUserRole(Role role);
}