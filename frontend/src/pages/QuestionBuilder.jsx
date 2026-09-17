import { useEffect, useState } from "react";
import {
  ArrowLeft,
  Plus,
  Trash2,
  Pencil,
  Eye,
  CheckCircle,
  Code2,
  MessageSquare,
  ListChecks,
  Lock,
  Send,
} from "lucide-react";

import { useNavigate, useParams } from "react-router-dom";
import api from "../services/api";

export default function QuestionBuilder() {
  const { assessmentId } = useParams();
  const navigate = useNavigate();

  const [assessment, setAssessment] = useState(null);
  const [questions, setQuestions] = useState([]);

  const [loading, setLoading] = useState(true);
  const [publishing, setPublishing] = useState(false);
  const [saving, setSaving] = useState(false);

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const [showForm, setShowForm] = useState(false);
  const [editingQuestion, setEditingQuestion] = useState(null);
  const [showReview, setShowReview] = useState(false);

  const [type, setType] = useState("MCQ");
  const [questionText, setQuestionText] = useState("");
  const [marks, setMarks] = useState("");

  const [optionA, setOptionA] = useState("");
  const [optionB, setOptionB] = useState("");
  const [optionC, setOptionC] = useState("");
  const [optionD, setOptionD] = useState("");
  const [correctOption, setCorrectOption] = useState("A");

  const [programmingLanguage, setProgrammingLanguage] =
    useState("Java");

  const [starterCode, setStarterCode] = useState("");
  const [expectedAnswer, setExpectedAnswer] = useState("");

  // =========================================================
  // LOAD DATA
  // =========================================================

  const loadData = async () => {
    try {
      setLoading(true);
      setError("");

      const [assessmentResponse, questionsResponse] =
        await Promise.all([
          api.get("/assessments/my"),
          api.get(
            `/questions/assessment/${assessmentId}`
          ),
        ]);

      const assessmentList =
        Array.isArray(assessmentResponse.data)
          ? assessmentResponse.data
          : [];

      const currentAssessment =
        assessmentList.find(
          (item) =>
            String(item.id) ===
            String(assessmentId)
        );

      setAssessment(currentAssessment || null);

      setQuestions(
        Array.isArray(questionsResponse.data)
          ? questionsResponse.data
          : []
      );
    } catch (err) {
      console.error(
        "Load question builder error:",
        err
      );

      setError(
        err.response?.data?.message ||
          "Failed to load assessment."
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [assessmentId]);

  // =========================================================
  // RESET FORM
  // =========================================================

  const resetForm = () => {
    setQuestionText("");
    setMarks("");

    setOptionA("");
    setOptionB("");
    setOptionC("");
    setOptionD("");

    setCorrectOption("A");

    setProgrammingLanguage("Java");
    setStarterCode("");

    setExpectedAnswer("");

    setError("");
  };

  // =========================================================
// OPEN EDIT
// =========================================================

const openEditQuestion = (question) => {
  setEditingQuestion(question);

  setType(question.type || "MCQ");

  setQuestionText(
    question.questionText || ""
  );

  setMarks(
    question.marks ?? ""
  );

  setOptionA(
    question.optionA || ""
  );

  setOptionB(
    question.optionB || ""
  );

  setOptionC(
    question.optionC || ""
  );

  setOptionD(
    question.optionD || ""
  );

  setCorrectOption(
    question.correctOption || "A"
  );

  setProgrammingLanguage(
    question.programmingLanguage || "Java"
  );

  setStarterCode(
    question.starterCode || ""
  );

  setExpectedAnswer(
    question.expectedAnswer || ""
  );

  setError("");
  setShowForm(true);
};
  // =========================================================
  // CREATE QUESTION
  // =========================================================

  // =========================================================
// CREATE / UPDATE QUESTION
// =========================================================

const handleSave = async (e) => {
  e.preventDefault();

  try {
    setSaving(true);
    setError("");

    const payload = {
      type,
      questionText: questionText.trim(),
      marks: Number(marks),

      optionA: type === "MCQ" ? optionA : null,
      optionB: type === "MCQ" ? optionB : null,
      optionC: type === "MCQ" ? optionC : null,
      optionD: type === "MCQ" ? optionD : null,

      correctOption:
        type === "MCQ"
          ? correctOption
          : null,

      programmingLanguage:
        type === "CODING"
          ? programmingLanguage
          : null,

      starterCode:
        type === "CODING"
          ? starterCode
          : null,

      expectedAnswer:
        type === "BRIEF_ANSWER"
          ? expectedAnswer
          : null,
    };


    // =====================================================
    // UPDATE EXISTING QUESTION
    // =====================================================

    if (editingQuestion) {

      const response = await api.put(
        `/questions/${editingQuestion.id}`,
        payload
      );

      setQuestions((current) =>
        current.map((question) =>
          question.id === editingQuestion.id
            ? response.data
            : question
        )
      );

    }


    // =====================================================
    // CREATE NEW QUESTION
    // =====================================================

    else {

      const response = await api.post(
        `/questions/assessment/${assessmentId}`,
        payload
      );

      setQuestions((current) => [
        ...current,
        response.data,
      ]);
    }


    // =====================================================
    // RESET
    // =====================================================

    resetForm();
    setEditingQuestion(null);
    setShowForm(false);

  } catch (err) {

    console.error(err);

    setError(
      err.response?.data?.message ||
      "Failed to save question."
    );

  } finally {

    setSaving(false);
  }
};
  // =========================================================
  // DELETE QUESTION
  // =========================================================

  const handleDelete = async (id) => {
    if (assessment?.status === "PUBLISHED") {
      setError(
        "Published assessments cannot be modified."
      );
      return;
    }

    if (!window.confirm(
      "Delete this question?"
    )) {
      return;
    }

    try {
      setError("");
      setSuccess("");

      await api.delete(
        `/questions/${id}`
      );

      setQuestions((current) =>
        current.filter(
          (question) =>
            question.id !== id
        )
      );

      setSuccess(
        "Question deleted successfully."
      );
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Failed to delete question."
      );
    }
  };

  // =========================================================
  // MARK CALCULATION
  // =========================================================

  const questionMarks =
    questions.reduce(
      (sum, question) =>
        sum + Number(question.marks || 0),
      0
    );

  const assessmentMarks =
    Number(assessment?.totalMarks || 0);

  const marksMatch =
    questionMarks === assessmentMarks;

  const hasQuestions =
    questions.length > 0;

  const canPublish =
    hasQuestions &&
    marksMatch &&
    assessment?.status !== "PUBLISHED" &&
    assessment?.status !== "CLOSED";

  // =========================================================
  // REVIEW
  // =========================================================

  const handleReview = () => {
    setError("");
    setSuccess("");

    if (!hasQuestions) {
      setError(
        "Add at least one question before reviewing."
      );
      return;
    }

    setShowReview(true);
  };

  // =========================================================
  // PUBLISH
  // =========================================================

  const handlePublish = async () => {
    if (!hasQuestions) {
      setError(
        "Add at least one question before publishing."
      );
      return;
    }

    if (!marksMatch) {
      setError(
        `Question marks (${questionMarks}) must equal assessment total marks (${assessmentMarks}).`
      );
      return;
    }

    if (
      assessment?.status === "PUBLISHED"
    ) {
      setError(
        "Assessment is already published."
      );
      return;
    }

    if (
      assessment?.status === "CLOSED"
    ) {
      setError(
        "Closed assessment cannot be published."
      );
      return;
    }

    const confirmed =
      window.confirm(
        "Are you sure you want to publish this assessment? Students will be able to attempt it."
      );

    if (!confirmed) {
      return;
    }

    try {
      setPublishing(true);
      setError("");
      setSuccess("");

      const response =
        await api.post(
          `/assessments/${assessmentId}/publish`
        );

      setAssessment((current) => ({
        ...current,
        status:
          response.data.status ||
          "PUBLISHED",
      }));

      setShowReview(false);

      setSuccess(
        "Assessment published successfully."
      );
    } catch (err) {
      console.error(
        "Publish assessment error:",
        err
      );

      setError(
        err.response?.data?.message ||
          "Failed to publish assessment."
      );
    } finally {
      setPublishing(false);
    }
  };

  // =========================================================
  // LOADING
  // =========================================================

  if (loading) {
    return (
      <section className="dashboard-content">
        <div className="loading">
          Loading assessment...
        </div>
      </section>
    );
  }

  // =========================================================
  // RETURN
  // =========================================================

  return (
    <section className="dashboard-content">

      {/* =====================================================
          HEADER
          ===================================================== */}

      <div className="page-heading">

        <div>

          <button
            className="back-btn"
            onClick={() =>
              navigate(
                "/faculty/assessments"
              )
            }
          >
            <ArrowLeft size={16} />
            Back to Assessments
          </button>

          <div
            style={{
              display: "flex",
              alignItems: "center",
              gap: "12px",
              marginTop: "10px",
            }}
          >
            <h1>
              {assessment?.title ||
                "Question Builder"}
            </h1>

            {assessment?.status && (
              <span
                className={`faculty-draft-badge ${
                  assessment.status ===
                  "PUBLISHED"
                    ? "published"
                    : ""
                }`}
              >
                {assessment.status}
              </span>
            )}
          </div>

          <p>
            Create, review and publish your
            assessment.
          </p>

        </div>

        {assessment?.status !==
          "PUBLISHED" && (
          <div
            style={{
              display: "flex",
              gap: "10px",
            }}
          >

            <button
              className="secondary-btn"
              type="button"
              onClick={handleReview}
              disabled={
                questions.length === 0
              }
            >
              <Eye size={17} />
              Review Assessment
            </button>

            <button
              className="primary-btn"
              type="button"
              onClick={() => {
                resetForm();
                setEditingQuestion(null);
                setShowForm(true);
              }}
            >
              <Plus size={18} />
                Add First Question
            </button>

          </div>
        )}

        {assessment?.status ===
          "PUBLISHED" && (
          <div
            style={{
              display: "flex",
              alignItems: "center",
              gap: "8px",
            }}
          >
            <Lock size={18} />
            <strong>
              Published
            </strong>
          </div>
        )}

      </div>

      {/* =====================================================
          ERROR
          ===================================================== */}

      {error && (
        <div className="error-banner">
          {error}
        </div>
      )}

      {/* =====================================================
          SUCCESS
          ===================================================== */}

      {success && (
        <div className="success-banner">
          {success}
        </div>
      )}

      {/* =====================================================
          ASSESSMENT SUMMARY
          ===================================================== */}

      {assessment && (
        <div className="question-summary">

          <div>
            <span>Assessment</span>
            <strong>
              {assessment.title}
            </strong>
          </div>

          <div>
            <span>Type</span>
            <strong>
              {assessment.type}
            </strong>
          </div>

          <div>
            <span>Questions</span>
            <strong>
              {questions.length}
            </strong>
          </div>

          <div>
            <span>Question Marks</span>
            <strong>
              {questionMarks}
            </strong>
          </div>

          <div>
            <span>Total Marks</span>
            <strong>
              {assessmentMarks}
            </strong>
          </div>

          <div>
            <span>Duration</span>
            <strong>
              {assessment.durationMinutes} min
            </strong>
          </div>

        </div>
      )}

      {/* =====================================================
          MARK VALIDATION
          ===================================================== */}

      {questions.length > 0 &&
        assessment && (
          <div
            style={{
              marginTop: "16px",
              padding: "14px 18px",
              borderRadius: "10px",
              background:
                marksMatch
                  ? "rgba(34, 197, 94, 0.10)"
                  : "rgba(239, 68, 68, 0.10)",
              border:
                marksMatch
                  ? "1px solid rgba(34, 197, 94, 0.25)"
                  : "1px solid rgba(239, 68, 68, 0.25)",
            }}
          >
            {marksMatch ? (
              <strong>
                ✓ Question marks match
                assessment total.
              </strong>
            ) : (
              <strong>
                Question marks: {questionMarks} /
                Required: {assessmentMarks}
              </strong>
            )}
          </div>
        )}

      {/* =====================================================
          QUESTIONS
          ===================================================== */}

      {questions.length === 0 ? (

        <div className="panel empty-page">

          <ListChecks size={48} />

          <h3>
            No questions yet
          </h3>

          <p>
            Add MCQ, coding, or brief-answer
            questions.
          </p>

          {assessment?.status !==
            "PUBLISHED" && (
            <button
              className="primary-btn"
              onClick={() => {
                resetForm();
                setShowForm(true);
              }}
            >
              <Plus size={18} />
              Add First Question
            </button>
          )}

        </div>

      ) : (

        <div className="question-list">

          {questions.map(
            (question, index) => (

              <div
                className="question-card"
                key={question.id}
              >

                <div
                  className="question-card-header"
                >

                  <div className="question-number">
                    Q{index + 1}
                  </div>

                  <div className="question-type">

                    {question.type ===
                      "MCQ" && (
                      <ListChecks size={15} />
                    )}

                    {question.type ===
                      "CODING" && (
                      <Code2 size={15} />
                    )}

                    {question.type ===
                      "BRIEF_ANSWER" && (
                      <MessageSquare size={15} />
                    )}

                    {question.type}

                  </div>

                  <span className="question-marks">
                    {question.marks} marks
                  </span>

                  {assessment?.status !==
                    "PUBLISHED" && (
                    <div className="question-actions">

  <button
    type="button"
    className="question-edit"
    title="Edit question"
    onClick={() =>
      openEditQuestion(question)
    }
  >
    <Pencil size={17} />
  </button>

  <button
    type="button"
    className="question-delete"
    title="Delete question"
    onClick={() =>
      handleDelete(question.id)
    }
  >
    <Trash2 size={17} />
  </button>

</div>
)}

                </div>

                <div className="question-text">
                  {question.questionText}
                </div>

                {/* MCQ */}

                {question.type ===
                  "MCQ" && (

                  <div className="question-options">

                    {["A", "B", "C", "D"].map(
                      (letter) => {

                        const option =
                          question[
                            `option${letter}`
                          ];

                        return (
                          <div
                            className={
                              `question-option ${
                                question.correctOption ===
                                letter
                                  ? "correct"
                                  : ""
                              }`
                            }
                            key={letter}
                          >

                            <span>
                              {letter}
                            </span>

                            <p>
                              {option}
                            </p>

                            {question.correctOption ===
                              letter && (
                              <CheckCircle
                                size={16}
                              />
                            )}

                          </div>
                        );
                      }
                    )}

                  </div>
                )}

                {/* CODING */}

                {question.type ===
                  "CODING" && (

                  <div className="coding-question-info">

                    <strong>
                      Language:
                    </strong>

                    <span>
                      {question.programmingLanguage}
                    </span>

                  </div>
                )}

                {/* BRIEF ANSWER */}

                {question.type ===
                  "BRIEF_ANSWER" && (

                  <div className="brief-answer-info">

                    <strong>
                      Expected Answer:
                    </strong>

                    <p>
                      {question.expectedAnswer}
                    </p>

                  </div>
                )}

              </div>
            )
          )}

        </div>
      )}

      {/* =====================================================
          REVIEW FOOTER
          ===================================================== */}

      {questions.length > 0 &&
        assessment?.status !==
          "PUBLISHED" && (

        <div
          style={{
            display: "flex",
            justifyContent: "flex-end",
            marginTop: "24px",
          }}
        >

          <button
            className="primary-btn"
            onClick={handleReview}
          >
            <Eye size={17} />
            Review & Publish
          </button>

        </div>
      )}

      {/* =====================================================
          ADD QUESTION MODAL
          ===================================================== */}

      {showForm && (

        <div className="question-modal-overlay">

          <div className="question-modal">

            <div className="question-modal-header">

              <div>

                <h2>
  {editingQuestion
    ? "Edit Question"
    : "Add Question"}
</h2>
                <p>
  {editingQuestion
    ? "Update the question details."
    : "Choose the question type and enter its details."}
</p>
              </div>

              <button
                className="modal-close"
                onClick={() =>
                  setShowForm(false)
                }
              >
                ×
              </button>

            </div>

            <form
              className="question-form"
              onSubmit={handleSave}
            >

              <div className="form-group">

                <label>
                  Question Type
                </label>

                <select
                  value={type}
                  onChange={(e) =>
                    setType(
                      e.target.value
                    )
                  }
                >

                  <option value="MCQ">
                    MCQ
                  </option>

                  <option value="CODING">
                    Coding
                  </option>

                  <option value="BRIEF_ANSWER">
                    Brief Answer
                  </option>

                </select>

              </div>

              <div className="form-group">

                <label>
                  Question
                </label>

                <textarea
                  rows="4"
                  placeholder="Enter your question..."
                  value={questionText}
                  onChange={(e) =>
                    setQuestionText(
                      e.target.value
                    )
                  }
                  required
                />

              </div>

              <div className="form-group">

                <label>
                  Marks
                </label>

                <input
                  type="number"
                  min="1"
                  value={marks}
                  onChange={(e) =>
                    setMarks(
                      e.target.value
                    )
                  }
                  required
                />

              </div>

              {/* MCQ */}

              {type === "MCQ" && (
                <>

                  <div className="form-group">
                    <label>Option A</label>
                    <input
                      value={optionA}
                      onChange={(e) =>
                        setOptionA(
                          e.target.value
                        )
                      }
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label>Option B</label>
                    <input
                      value={optionB}
                      onChange={(e) =>
                        setOptionB(
                          e.target.value
                        )
                      }
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label>Option C</label>
                    <input
                      value={optionC}
                      onChange={(e) =>
                        setOptionC(
                          e.target.value
                        )
                      }
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label>Option D</label>
                    <input
                      value={optionD}
                      onChange={(e) =>
                        setOptionD(
                          e.target.value
                        )
                      }
                      required
                    />
                  </div>

                  <div className="form-group">

                    <label>
                      Correct Option
                    </label>

                    <select
                      value={correctOption}
                      onChange={(e) =>
                        setCorrectOption(
                          e.target.value
                        )
                      }
                    >

                      <option value="A">
                        Option A
                      </option>

                      <option value="B">
                        Option B
                      </option>

                      <option value="C">
                        Option C
                      </option>

                      <option value="D">
                        Option D
                      </option>

                    </select>

                  </div>

                </>
              )}

              {/* CODING */}

              {type === "CODING" && (
                <>

                  <div className="form-group">

                    <label>
                      Programming Language
                    </label>

                    <select
                      value={programmingLanguage}
                      onChange={(e) =>
                        setProgrammingLanguage(
                          e.target.value
                        )
                      }
                    >

                      <option value="Java">
                        Java
                      </option>

                      <option value="Python">
                        Python
                      </option>

                      <option value="C++">
                        C++
                      </option>

                      <option value="JavaScript">
                        JavaScript
                      </option>

                    </select>

                  </div>

                  <div className="form-group">

                    <label>
                      Starter Code
                    </label>

                    <textarea
                      rows="8"
                      placeholder="Optional starter code..."
                      value={starterCode}
                      onChange={(e) =>
                        setStarterCode(
                          e.target.value
                        )
                      }
                    />

                  </div>

                </>
              )}

              {/* BRIEF ANSWER */}

              {type ===
                "BRIEF_ANSWER" && (

                <div className="form-group">

                  <label>
                    Expected Answer
                  </label>

                  <textarea
                    rows="5"
                    placeholder="Enter expected answer..."
                    value={expectedAnswer}
                    onChange={(e) =>
                      setExpectedAnswer(
                        e.target.value
                      )
                    }
                    required
                  />

                </div>
              )}

              <div className="question-form-actions">

                <button
                  type="button"
                  onClick={() =>
                    setShowForm(false)
                  }
                >
                  Cancel
                </button>

                <button
  type="submit"
  className="primary-btn"
  disabled={saving}
>
  {saving
    ? "Saving..."
    : editingQuestion
      ? "Update Question"
      : "Save Question"}
</button>

              </div>

            </form>

          </div>

        </div>
      )}

      {/* =====================================================
          REVIEW MODAL
          ===================================================== */}

      {showReview && (

        <div className="question-modal-overlay">

          <div className="question-modal">

            <div className="question-modal-header">

              <div>

                <h2>
                  Review Assessment
                </h2>

                <p>
                  Check everything before
                  publishing.
                </p>

              </div>

              <button
                className="modal-close"
                onClick={() =>
                  setShowReview(false)
                }
                disabled={publishing}
              >
                ×
              </button>

            </div>

            <div className="question-form">

              <div className="question-summary">

                <div>
                  <span>Assessment</span>
                  <strong>
                    {assessment?.title}
                  </strong>
                </div>

                <div>
                  <span>Questions</span>
                  <strong>
                    {questions.length}
                  </strong>
                </div>

                <div>
                  <span>Marks</span>
                  <strong>
                    {questionMarks} /{" "}
                    {assessmentMarks}
                  </strong>
                </div>

              </div>

              <div
                style={{
                  marginTop: "18px",
                  padding: "16px",
                  borderRadius: "10px",
                  background:
                    marksMatch
                      ? "rgba(34, 197, 94, 0.10)"
                      : "rgba(239, 68, 68, 0.10)",
                }}
              >

                {marksMatch ? (
                  <strong>
                    ✓ Assessment is ready
                    to publish.
                  </strong>
                ) : (
                  <strong>
                    ✕ Total question marks
                    must equal{" "}
                    {assessmentMarks}.
                  </strong>
                )}

              </div>

              <div
                style={{
                  marginTop: "18px",
                }}
              >

                <h3>
                  Question Summary
                </h3>

                {questions.map(
                  (question, index) => (

                    <div
                      key={question.id}
                      style={{
                        display: "flex",
                        justifyContent:
                          "space-between",
                        padding: "10px 0",
                        borderBottom:
                          "1px solid #eee",
                      }}
                    >

                      <span>
                        Q{index + 1}.{" "}
                        {question.questionText}
                      </span>

                      <strong>
                        {question.marks} marks
                      </strong>

                    </div>
                  )
                )}

              </div>

              <div
                className="question-form-actions"
                style={{
                  marginTop: "24px",
                }}
              >

                <button
                  type="button"
                  onClick={() =>
                    setShowReview(false)
                  }
                  disabled={publishing}
                >
                  Back
                </button>

                <button
                  type="button"
                  className="primary-btn"
                  onClick={handlePublish}
                  disabled={
                    !canPublish ||
                    publishing
                  }
                >
                  <Send size={17} />

                  {publishing
                    ? "Publishing..."
                    : "Publish Assessment"}
                </button>

              </div>

            </div>

          </div>

        </div>
      )}

    </section>
  );
}