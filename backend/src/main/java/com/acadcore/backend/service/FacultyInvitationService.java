package com.acadcore.backend.service;

import com.acadcore.backend.entity.AcademicClass;
import com.acadcore.backend.entity.FacultyInvitation;
import com.acadcore.backend.entity.InvitationStatus;
import com.acadcore.backend.entity.Subject;
import com.acadcore.backend.repository.AcademicClassRepository;
import com.acadcore.backend.repository.FacultyInvitationRepository;
import com.acadcore.backend.repository.SubjectRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class FacultyInvitationService {

    private final FacultyInvitationRepository invitationRepository;
    private final AcademicClassRepository classRepository;
    private final SubjectRepository subjectRepository;
    private final JavaMailSender mailSender;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public FacultyInvitationService(
            FacultyInvitationRepository invitationRepository,
            AcademicClassRepository classRepository,
            SubjectRepository subjectRepository,
            JavaMailSender mailSender
    ) {
        this.invitationRepository = invitationRepository;
        this.classRepository = classRepository;
        this.subjectRepository = subjectRepository;
        this.mailSender = mailSender;
    }

    public FacultyInvitation createInvitation(
            String fullName,
            String email,
            Long classId,
            Long subjectId
    ) {

        AcademicClass academicClass = classRepository.findById(classId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Class not found"));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Subject not found"));

        if (!subject.getAcademicClass().getId().equals(classId)) {
            throw new IllegalArgumentException(
                    "Subject does not belong to selected class"
            );
        }

        String cleanEmail = email.trim();
        String cleanFullName = fullName.trim();

        /*
         * Check whether a pending invitation already exists
         * for the same email and subject.
         */
        Optional<FacultyInvitation> existingInvitation =
                invitationRepository
                        .findByEmailIgnoreCaseAndSubjectIdAndStatus(
                                cleanEmail,
                                subjectId,
                                InvitationStatus.PENDING
                        );

        /*
         * If a pending invitation already exists,
         * resend the same invitation.
         */
        if (existingInvitation.isPresent()) {

            FacultyInvitation invitation = existingInvitation.get();

            invitation.setFullName(cleanFullName);
            invitation.setEmail(cleanEmail);

            // Extend invitation validity by 3 days
            invitation.setExpiresAt(
                    LocalDateTime.now().plusDays(3)
            );

            FacultyInvitation updatedInvitation =
                    invitationRepository.save(invitation);

            sendInvitationEmail(updatedInvitation);

            return updatedInvitation;
        }

        /*
         * No pending invitation exists.
         * Create a new invitation.
         */
        String token = UUID.randomUUID().toString();

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime expiresAt = now.plusDays(3);

        FacultyInvitation invitation = new FacultyInvitation(
                cleanFullName,
                cleanEmail,
                academicClass,
                subject,
                token,
                InvitationStatus.PENDING,
                expiresAt,
                now
        );

        FacultyInvitation savedInvitation =
                invitationRepository.save(invitation);

        sendInvitationEmail(savedInvitation);

        return savedInvitation;
    }

    private void sendInvitationEmail(
            FacultyInvitation invitation
    ) {

        String acceptLink =
                frontendUrl
                        + "/faculty/accept?token="
                        + invitation.getToken();

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(invitation.getEmail());

        message.setSubject(
                "AcadCore Faculty Invitation"
        );

        message.setText(
                "Hello "
                        + invitation.getFullName()
                        + ",\n\n"

                        + "You have been invited to join AcadCore "
                        + "as a faculty member.\n\n"

                        + "Class: "
                        + invitation.getAcademicClass().getName()
                        + "\n"

                        + "Academic Year: "
                        + invitation.getAcademicClass().getAcademicYear()
                        + "\n"

                        + "Subject: "
                        + invitation.getSubject().getName()
                        + "\n\n"

                        + "Please accept the invitation using "
                        + "the link below:\n\n"

                        + acceptLink
                        + "\n\n"

                        + "This invitation is valid for 3 days.\n\n"

                        + "You will create your own password "
                        + "when accepting the invitation.\n\n"

                        + "Regards,\n"
                        + "AcadCore"
        );

        mailSender.send(message);
    }
}