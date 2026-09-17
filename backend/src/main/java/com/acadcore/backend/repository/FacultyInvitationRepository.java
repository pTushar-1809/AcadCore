package com.acadcore.backend.repository;

import com.acadcore.backend.entity.FacultyInvitation;
import com.acadcore.backend.entity.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FacultyInvitationRepository extends JpaRepository<FacultyInvitation, Long> {

    Optional<FacultyInvitation> findByToken(String token);

    List<FacultyInvitation> findByStatus(InvitationStatus status);

    List<FacultyInvitation> findByEmailIgnoreCase(String email);

    Optional<FacultyInvitation> findByEmailIgnoreCaseAndSubjectIdAndStatus(
            String email,
            Long subjectId,
            InvitationStatus status
    );
}