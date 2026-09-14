package com.acadcore.backend.dto;

import java.util.List;

public class AttemptRequest {

    private Long assessmentId;
    private List<AnswerRequest> answers;

    public AttemptRequest() {
    }

    public Long getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(Long assessmentId) {
        this.assessmentId = assessmentId;
    }

    public List<AnswerRequest> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerRequest> answers) {
        this.answers = answers;
    }
}