package com.acadcore.backend.controller;

import com.acadcore.backend.dto.AnswerRequest;
import com.acadcore.backend.dto.AttemptRequest;
import com.acadcore.backend.entity.*;
import com.acadcore.backend.repository.*;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/attempts")
public class AttemptController {

    private final AttemptRepository attemptRepository;
    private final AttemptAnswerRepository attemptAnswerRepository;
    private final AssessmentRepository assessmentRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    public AttemptController(
            AttemptRepository attemptRepository,
            AttemptAnswerRepository attemptAnswerRepository,
            AssessmentRepository assessmentRepository,
            QuestionRepository questionRepository,
            UserRepository userRepository) {

        this.attemptRepository = attemptRepository;
        this.attemptAnswerRepository = attemptAnswerRepository;
        this.assessmentRepository = assessmentRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> submitAttempt(
            @RequestBody AttemptRequest request,
            Authentication authentication) {

        if (request.getAssessmentId() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Assessment ID is required"));
        }

        Assessment assessment =
                assessmentRepository.findById(request.getAssessmentId())
                        .orElse(null);

        if (assessment == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Assessment not found"));
        }

        User student =
                userRepository.findByEmail(authentication.getName())
                        .orElse(null);

        if (student == null || student.getRole() != Role.STUDENT) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Student not found"));
        }

        if (attemptRepository.existsByStudentIdAndAssessmentId(
                student.getId(),
                assessment.getId())) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "You have already attempted this assessment"
                    ));
        }

        List<Question> questions =
                questionRepository.findByAssessmentId(
                        assessment.getId());

        Map<Long, AnswerRequest> submittedAnswers =
                new HashMap<>();

        if (request.getAnswers() != null) {
            for (AnswerRequest answer : request.getAnswers()) {
                if (answer.getQuestionId() != null) {
                    submittedAnswers.put(
                            answer.getQuestionId(),
                            answer
                    );
                }
            }
        }

        LocalDateTime submittedAt = LocalDateTime.now();

        Attempt attempt =
                new Attempt(
                        student,
                        assessment,
                        0,
                        assessment.getTotalMarks(),
                        submittedAt,
                        submittedAt
                );

        Attempt savedAttempt =
                attemptRepository.save(attempt);

        int obtainedMarks = 0;

        for (Question question : questions) {

            AnswerRequest answerRequest =
                    submittedAnswers.get(question.getId());

            String answer = answerRequest == null
                    ? ""
                    : answerRequest.getAnswer();

            int marksObtained = 0;

            /*
             * MCQ questions are automatically evaluated.
             * Coding and Brief Q&A are stored with 0 marks
             * for future/manual evaluation.
             */
            if (question.getType() == QuestionType.MCQ
                    && question.getCorrectAnswer() != null
                    && answer != null
                    && answer.trim().equalsIgnoreCase(
                            question.getCorrectAnswer().trim())) {

                marksObtained = question.getMarks();
            }

            obtainedMarks += marksObtained;

            AttemptAnswer attemptAnswer =
                    new AttemptAnswer(
                            savedAttempt,
                            question,
                            answer,
                            marksObtained
                    );

            attemptAnswerRepository.save(attemptAnswer);
        }

        savedAttempt.setObtainedMarks(obtainedMarks);
        savedAttempt.setSubmittedAt(submittedAt);

        attemptRepository.save(savedAttempt);

        double percentage =
                assessment.getTotalMarks() == 0
                        ? 0
                        : (obtainedMarks * 100.0)
                        / assessment.getTotalMarks();

        return ResponseEntity.ok(
                Map.of(
                        "attemptId", savedAttempt.getId(),
                        "assessmentId", assessment.getId(),
                        "assessmentTitle", assessment.getTitle(),
                        "studentId", student.getId(),
                        "studentName", student.getFullName(),
                        "obtainedMarks", obtainedMarks,
                        "totalMarks", assessment.getTotalMarks(),
                        "percentage",
                        Math.round(percentage * 100.0) / 100.0,
                        "message",
                        "Assessment submitted successfully"
                )
        );
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getMyAttempts(
            Authentication authentication) {

        User student =
                userRepository.findByEmail(authentication.getName())
                        .orElse(null);

        if (student == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("message", "Student not found"));
        }

        List<Map<String, Object>> attempts =
                attemptRepository.findByStudentId(student.getId())
                        .stream()
                        .map(attempt -> {

                            double percentage =
                                    attempt.getTotalMarks() == 0
                                            ? 0
                                            : (attempt.getObtainedMarks()
                                            * 100.0)
                                            / attempt.getTotalMarks();

                            return Map.<String, Object>of(
                                    "attemptId",
                                    attempt.getId(),
                                    "assessmentId",
                                    attempt.getAssessment().getId(),
                                    "assessmentTitle",
                                    attempt.getAssessment().getTitle(),
                                    "subjectName",
                                    attempt.getAssessment()
                                            .getSubject()
                                            .getName(),
                                    "obtainedMarks",
                                    attempt.getObtainedMarks(),
                                    "totalMarks",
                                    attempt.getTotalMarks(),
                                    "percentage",
                                    Math.round(percentage * 100.0) / 100.0,
                                    "submittedAt",
                                    attempt.getSubmittedAt().toString()
                            );
                        })
                        .toList();

        return ResponseEntity.ok(attempts);
    }

    @GetMapping("/{attemptId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getAttempt(
            @PathVariable Long attemptId,
            Authentication authentication) {

        User student =
                userRepository.findByEmail(authentication.getName())
                        .orElse(null);

        if (student == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("message", "Student not found"));
        }

        Attempt attempt =
                attemptRepository.findById(attemptId)
                        .orElse(null);

        if (attempt == null) {
            return ResponseEntity.notFound().build();
        }

        if (!attempt.getStudent().getId().equals(student.getId())) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Access denied"));
        }

        List<AttemptAnswer> answers =
                attemptAnswerRepository.findByAttemptId(attemptId);

        List<Map<String, Object>> answerList =
                answers.stream()
                        .map(answer -> Map.<String, Object>of(
                                "questionId",
                                answer.getQuestion().getId(),
                                "questionText",
                                answer.getQuestion().getQuestionText(),
                                "type",
                                answer.getQuestion().getType(),
                                "answer",
                                answer.getAnswer() == null
                                        ? ""
                                        : answer.getAnswer(),
                                "marks",
                                answer.getQuestion().getMarks(),
                                "marksObtained",
                                answer.getMarksObtained()
                        ))
                        .toList();

        double percentage =
                attempt.getTotalMarks() == 0
                        ? 0
                        : (attempt.getObtainedMarks() * 100.0)
                        / attempt.getTotalMarks();

        return ResponseEntity.ok(
                Map.of(
                        "attemptId", attempt.getId(),
                        "assessmentTitle",
                        attempt.getAssessment().getTitle(),
                        "subjectName",
                        attempt.getAssessment()
                                .getSubject()
                                .getName(),
                        "obtainedMarks",
                        attempt.getObtainedMarks(),
                        "totalMarks",
                        attempt.getTotalMarks(),
                        "percentage",
                        Math.round(percentage * 100.0) / 100.0,
                        "submittedAt",
                        attempt.getSubmittedAt().toString(),
                        "answers",
                        answerList
                )
        );
    }
}