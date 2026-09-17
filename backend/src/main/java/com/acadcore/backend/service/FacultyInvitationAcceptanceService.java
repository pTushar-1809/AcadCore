package com.acadcore.backend.service;

import com.acadcore.backend.dto.AcceptFacultyInvitationRequest;
import com.acadcore.backend.entity.FacultyInvitation;
import com.acadcore.backend.entity.InvitationStatus;
import com.acadcore.backend.entity.Role;
import com.acadcore.backend.entity.Subject;
import com.acadcore.backend.entity.User;
import com.acadcore.backend.repository.FacultyInvitationRepository;
import com.acadcore.backend.repository.SubjectRepository;
import com.acadcore.backend.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class FacultyInvitationAcceptanceService {

    private final FacultyInvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final PasswordEncoder passwordEncoder;

    public FacultyInvitationAcceptanceService(
            FacultyInvitationRepository invitationRepository,
            UserRepository userRepository,
            SubjectRepository subjectRepository,
            PasswordEncoder passwordEncoder) {

        this.invitationRepository = invitationRepository;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Map<String, Object> acceptInvitation(
            AcceptFacultyInvitationRequest request) {

        // ----------------------------------------------------
        // 1. Find invitation
        // ----------------------------------------------------

        FacultyInvitation invitation =
                invitationRepository
                        .findByToken(request.getToken())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid invitation"
                                )
                        );

        // ----------------------------------------------------
        // 2. Check invitation status
        // ----------------------------------------------------

        if (invitation.getStatus()
                != InvitationStatus.PENDING) {

            throw new IllegalArgumentException(
                    "This invitation is no longer active"
            );
        }

        // ----------------------------------------------------
        // 3. Check invitation expiry
        // ----------------------------------------------------

        if (invitation.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            invitation.setStatus(
                    InvitationStatus.EXPIRED
            );

            invitationRepository.save(invitation);

            throw new IllegalArgumentException(
                    "This invitation has expired"
            );
        }

        // ----------------------------------------------------
        // 4. Validate password
        // ----------------------------------------------------

        if (request.getPassword() == null ||
                request.getPassword().length() < 8) {

            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters"
            );
        }

        if (!request.getPassword()
                .equals(request.getConfirmPassword())) {

            throw new IllegalArgumentException(
                    "Passwords do not match"
            );
        }

        // ----------------------------------------------------
        // 5. Check whether faculty account already exists
        // ----------------------------------------------------

        if (userRepository.existsByEmail(
                invitation.getEmail())) {

            throw new IllegalArgumentException(
                    "An account already exists with this email"
            );
        }

        // ----------------------------------------------------
        // 6. Create faculty account
        // ----------------------------------------------------

        User faculty = new User();

        faculty.setFullName(
                invitation.getFullName()
        );

        faculty.setEmail(
                invitation.getEmail()
        );

        faculty.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        faculty.setRole(Role.FACULTY);

        User savedFaculty =
                userRepository.save(faculty);

        // ----------------------------------------------------
        // 7. Assign faculty to the subject
        // ----------------------------------------------------

        Subject subject =
                invitation.getSubject();

        subject.setFaculty(savedFaculty);

        subjectRepository.save(subject);

        // ----------------------------------------------------
        // 8. Mark invitation as accepted
        // ----------------------------------------------------

        invitation.setStatus(
                InvitationStatus.ACCEPTED
        );

        invitation.setAcceptedAt(
                LocalDateTime.now()
        );

        invitationRepository.save(invitation);

        // ----------------------------------------------------
        // 9. Return success response
        // ----------------------------------------------------

        return Map.of(
                "message",
                "Faculty account created successfully",

                "facultyId",
                savedFaculty.getId(),

                "className",
                invitation.getAcademicClass().getName(),

                "subjectName",
                subject.getName(),

                "status",
                "ACTIVE"
        );
    }
}