package com.acadcore.backend.controller;

import com.acadcore.backend.dto.QuestionRequest;
import com.acadcore.backend.dto.QuestionResponse;
import com.acadcore.backend.entity.Assessment;
import com.acadcore.backend.entity.Question;
import com.acadcore.backend.entity.QuestionType;
import com.acadcore.backend.entity.User;
import com.acadcore.backend.repository.AssessmentRepository;
import com.acadcore.backend.repository.QuestionRepository;
import com.acadcore.backend.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@CrossOrigin(origins = "http://localhost:5173")
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

    // =========================================================
    // GET QUESTIONS
    // =========================================================

    @GetMapping("/assessment/{assessmentId}")
    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<List<QuestionResponse>> getQuestions(
            @PathVariable Long assessmentId,
            Authentication authentication) {

        Assessment assessment =
                assessmentRepository.findById(assessmentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Assessment not found"));

        validateFacultyAccess(
                assessment,
                authentication);

        List<QuestionResponse> response =
                questionRepository
                        .findByAssessmentIdOrderByIdAsc(
                                assessmentId)
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // CREATE QUESTION
    // =========================================================

    @PostMapping("/assessment/{assessmentId}")
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<QuestionResponse> createQuestion(
            @PathVariable Long assessmentId,
            @RequestBody QuestionRequest request,
            Authentication authentication) {

        Assessment assessment =
                assessmentRepository.findById(assessmentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Assessment not found"));

        validateFacultyAccess(
                assessment,
                authentication);

        validateQuestion(request);

        Question question = new Question();

        question.setAssessment(assessment);
        question.setType(request.getType());
        question.setQuestionText(
                request.getQuestionText().trim());
        question.setMarks(request.getMarks());

        question.setOptionA(request.getOptionA());
        question.setOptionB(request.getOptionB());
        question.setOptionC(request.getOptionC());
        question.setOptionD(request.getOptionD());

        question.setCorrectOption(
                request.getCorrectOption());

        question.setProgrammingLanguage(
                request.getProgrammingLanguage());

        question.setStarterCode(
                request.getStarterCode());

        question.setExpectedAnswer(
                request.getExpectedAnswer());

        Question saved =
                questionRepository.save(question);

        return ResponseEntity.ok(
                toResponse(saved));
    }

    // =========================================================
// UPDATE QUESTION
// =========================================================

@PutMapping("/{questionId}")
@PreAuthorize("hasRole('FACULTY')")
public ResponseEntity<QuestionResponse> updateQuestion(
        @PathVariable Long questionId,
        @RequestBody QuestionRequest request,
        Authentication authentication) {

    Question question =
            questionRepository.findById(questionId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Question not found"));

    // Check faculty ownership
    validateFacultyAccess(
            question.getAssessment(),
            authentication);

    // Published assessment cannot be edited
    if (question.getAssessment().getStatus() != null &&
            question.getAssessment().getStatus().name()
                    .equals("PUBLISHED")) {

        throw new RuntimeException(
                "Published assessment questions cannot be edited");
    }

    // Validate updated question
    validateQuestion(request);

    // Update common fields
    question.setType(request.getType());

    question.setQuestionText(
            request.getQuestionText().trim());

    question.setMarks(
            request.getMarks());

    // Update MCQ fields
    question.setOptionA(
            request.getOptionA());

    question.setOptionB(
            request.getOptionB());

    question.setOptionC(
            request.getOptionC());

    question.setOptionD(
            request.getOptionD());

    question.setCorrectOption(
            request.getCorrectOption());

    // Update coding fields
    question.setProgrammingLanguage(
            request.getProgrammingLanguage());

    question.setStarterCode(
            request.getStarterCode());

    // Update brief answer
    question.setExpectedAnswer(
            request.getExpectedAnswer());

    Question updated =
            questionRepository.save(question);

    return ResponseEntity.ok(
            toResponse(updated));
}
    // =========================================================
    // DELETE QUESTION
    // =========================================================

    @DeleteMapping("/{questionId}")
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<?> deleteQuestion(
            @PathVariable Long questionId,
            Authentication authentication) {

        Question question =
                questionRepository.findById(questionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Question not found"));

        validateFacultyAccess(
                question.getAssessment(),
                authentication);

        questionRepository.delete(question);

        return ResponseEntity.ok(
                java.util.Map.of(
                        "message",
                        "Question deleted successfully"
                ));
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateQuestion(
            QuestionRequest request) {

        if (request.getType() == null) {
            throw new RuntimeException(
                    "Question type is required");
        }

        if (request.getQuestionText() == null ||
                request.getQuestionText().isBlank()) {

            throw new RuntimeException(
                    "Question text is required");
        }

        if (request.getMarks() == null ||
                request.getMarks() <= 0) {

            throw new RuntimeException(
                    "Question marks must be greater than 0");
        }

        if (request.getType() == QuestionType.MCQ) {

            if (isBlank(request.getOptionA()) ||
                    isBlank(request.getOptionB()) ||
                    isBlank(request.getOptionC()) ||
                    isBlank(request.getOptionD())) {

                throw new RuntimeException(
                        "All four MCQ options are required");
            }

            if (isBlank(request.getCorrectOption()) ||
                    !List.of("A", "B", "C", "D")
                            .contains(
                                    request.getCorrectOption())) {

                throw new RuntimeException(
                        "Correct MCQ option must be A, B, C or D");
            }
        }

        if (request.getType() == QuestionType.CODING) {

            if (isBlank(
                    request.getProgrammingLanguage())) {

                throw new RuntimeException(
                        "Programming language is required");
            }
        }

        if (request.getType() == QuestionType.BRIEF_ANSWER) {

            if (isBlank(
                    request.getExpectedAnswer())) {

                throw new RuntimeException(
                        "Expected answer is required");
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null ||
                value.isBlank();
    }

    // =========================================================
    // FACULTY ACCESS
    // =========================================================

    private void validateFacultyAccess(
            Assessment assessment,
            Authentication authentication) {

        if ("ADMIN".equals(
                authentication.getAuthorities()
                        .stream()
                        .findFirst()
                        .map(Object::toString)
                        .orElse(""))) {

            return;
        }

        User faculty =
                userRepository
                        .findByEmail(
                                authentication.getName())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Faculty not found"));

        if (assessment.getFaculty() == null ||
                !assessment.getFaculty()
                        .getId()
                        .equals(faculty.getId())) {

            throw new RuntimeException(
                    "You are not authorized to manage this assessment");
        }
    }

    // =========================================================
    // RESPONSE
    // =========================================================

    private QuestionResponse toResponse(
            Question question) {

        QuestionResponse response =
                new QuestionResponse();

        response.setId(question.getId());

        response.setAssessmentId(
                question.getAssessment()
                        .getId());

        response.setType(
                question.getType());

        response.setQuestionText(
                question.getQuestionText());

        response.setMarks(
                question.getMarks());

        response.setOptionA(
                question.getOptionA());

        response.setOptionB(
                question.getOptionB());

        response.setOptionC(
                question.getOptionC());

        response.setOptionD(
                question.getOptionD());

        response.setCorrectOption(
                question.getCorrectOption());

        response.setProgrammingLanguage(
                question.getProgrammingLanguage());

        response.setStarterCode(
                question.getStarterCode());

        response.setExpectedAnswer(
                question.getExpectedAnswer());

        return response;
    }
}