package com.acadcore.backend.controller;

import com.acadcore.backend.dto.QuestionRequest;
import com.acadcore.backend.entity.Assessment;
import com.acadcore.backend.entity.Question;
import com.acadcore.backend.entity.Role;
import com.acadcore.backend.entity.User;
import com.acadcore.backend.repository.AssessmentRepository;
import com.acadcore.backend.repository.QuestionRepository;
import com.acadcore.backend.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionRepository questionRepository;
    private final AssessmentRepository assessmentRepository;
    private final UserRepository userRepository;

    public QuestionController(
            QuestionRepository questionRepository,
            AssessmentRepository assessmentRepository,
            UserRepository userRepository) {

        this.questionRepository = questionRepository;
        this.assessmentRepository = assessmentRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/assessment/{assessmentId}")
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<?> createQuestion(
            @PathVariable Long assessmentId,
            @RequestBody QuestionRequest request,
            Authentication authentication) {

        if (request.getQuestionText() == null ||
                request.getQuestionText().isBlank()) {

            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Question text is required"));
        }

        if (request.getType() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Question type is required"));
        }

        if (request.getMarks() == null ||
                request.getMarks() <= 0) {

            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Marks must be greater than 0"));
        }

        Assessment assessment =
                assessmentRepository.findById(assessmentId).orElse(null);

        if (assessment == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Assessment not found"));
        }

        User faculty =
                userRepository.findByEmail(authentication.getName())
                        .orElse(null);

        if (faculty == null || faculty.getRole() != Role.FACULTY) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Faculty user not found"));
        }

        if (!assessment.getFaculty().getId().equals(faculty.getId())) {
            return ResponseEntity.status(403)
                    .body(Map.of(
                            "message",
                            "You are not the faculty assigned to this assessment"
                    ));
        }

        Question question =
                new Question(
                        request.getQuestionText(),
                        request.getType(),
                        request.getMarks(),
                        request.getOptionA(),
                        request.getOptionB(),
                        request.getOptionC(),
                        request.getOptionD(),
                        request.getCorrectAnswer(),
                        assessment
                );

        Question savedQuestion = questionRepository.save(question);

        return ResponseEntity.ok(
                Map.of(
                        "id", savedQuestion.getId(),
                        "questionText", savedQuestion.getQuestionText(),
                        "type", savedQuestion.getType(),
                        "marks", savedQuestion.getMarks(),
                        "assessmentId", assessment.getId(),
                        "assessmentTitle", assessment.getTitle()
                )
        );
    }

    @GetMapping("/assessment/{assessmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
    public ResponseEntity<List<Question>> getQuestions(
            @PathVariable Long assessmentId) {

        return ResponseEntity.ok(
                questionRepository.findByAssessmentId(assessmentId)
        );
    }
}