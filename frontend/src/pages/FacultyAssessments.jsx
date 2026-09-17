import { useEffect, useState } from "react";
import {
  Plus,
  ClipboardList,
  Clock,
  BookOpen,
  Search,
  X,
  Save,
} from "lucide-react";

import api from "../services/api";

export default function FacultyAssessments() {
  const [assessments, setAssessments] = useState([]);

  const [classes, setClasses] = useState([]);
  const [subjects, setSubjects] = useState([]);

  const [selectedClassId, setSelectedClassId] =
    useState("");

  const [loading, setLoading] = useState(true);
  const [loadingSubjects, setLoadingSubjects] =
    useState(false);

  const [saving, setSaving] = useState(false);

  const [error, setError] = useState("");
  const [search, setSearch] = useState("");

  const [showModal, setShowModal] = useState(false);

  // ============================================================
  // FORM
  // ============================================================

  const [title, setTitle] = useState("");
  const [description, setDescription] =
    useState("");

  const [subjectId, setSubjectId] =
    useState("");

  const [totalMarks, setTotalMarks] =
    useState("");

  const [durationMinutes, setDurationMinutes] =
    useState("");

  const [type, setType] =
    useState("PRACTICE");

  // ============================================================
  // LOAD ASSESSMENTS
  // ============================================================

  const loadAssessments = async () => {
    try {
      setLoading(true);
      setError("");

      const response =
        await api.get("/assessments/my");

      setAssessments(
        Array.isArray(response.data)
          ? response.data
          : []
      );
    } catch (err) {
      console.error(
        "Load faculty assessments error:",
        err
      );

      setError(
        err.response?.data?.message ||
          "Failed to load assessments."
      );
    } finally {
      setLoading(false);
    }
  };

  // ============================================================
  // LOAD FACULTY CLASSES
  // ============================================================

  const loadClasses = async () => {
    try {
      const response = await api.get(
        "/faculty/dashboard/classes"
      );

      const data = Array.isArray(response.data)
        ? response.data
        : [];

      setClasses(data);

      if (data.length > 0) {
        setSelectedClassId(
          String(data[0].id)
        );
      }
    } catch (err) {
      console.error(
        "Load faculty classes error:",
        err
      );

      setError(
        err.response?.data?.message ||
          "Failed to load classes."
      );
    }
  };

  // ============================================================
  // INITIAL LOAD
  // ============================================================

  useEffect(() => {
    loadAssessments();
    loadClasses();
  }, []);

  // ============================================================
  // LOAD SUBJECTS FOR CLASS
  // ============================================================

  useEffect(() => {
    if (!selectedClassId) {
      setSubjects([]);
      setSubjectId("");
      return;
    }

    loadSubjects(selectedClassId);
  }, [selectedClassId]);

  const loadSubjects = async (classId) => {
    try {
      setLoadingSubjects(true);
      setError("");

      const response = await api.get(
        `/faculty/dashboard/subjects?classId=${classId}`
      );

      const data = Array.isArray(response.data)
        ? response.data
        : [];

      setSubjects(data);

      if (data.length > 0) {
        setSubjectId(
          String(data[0].id)
        );
      } else {
        setSubjectId("");
      }
    } catch (err) {
      console.error(
        "Load faculty subjects error:",
        err
      );

      setError(
        err.response?.data?.message ||
          "Failed to load subjects."
      );

      setSubjects([]);
      setSubjectId("");
    } finally {
      setLoadingSubjects(false);
    }
  };

  // ============================================================
  // OPEN CREATE MODAL
  // ============================================================

  const openCreateModal = () => {
    setTitle("");
    setDescription("");
    setTotalMarks("");
    setDurationMinutes("");
    setType("PRACTICE");

    if (subjects.length > 0) {
      setSubjectId(
        String(subjects[0].id)
      );
    } else {
      setSubjectId("");
    }

    setError("");
    setShowModal(true);
  };

  // ============================================================
  // CLOSE MODAL
  // ============================================================

  const closeModal = () => {
    if (saving) {
      return;
    }

    setShowModal(false);
  };

  // ============================================================
  // CREATE ASSESSMENT
  // ============================================================

  const handleCreateAssessment = async (e) => {
    e.preventDefault();

    if (!title.trim()) {
      setError("Assessment title is required.");
      return;
    }

    if (!subjectId) {
      setError("Please select a subject.");
      return;
    }

    if (
      !totalMarks ||
      Number(totalMarks) <= 0
    ) {
      setError(
        "Total marks must be greater than 0."
      );
      return;
    }

    if (
      !durationMinutes ||
      Number(durationMinutes) <= 0
    ) {
      setError(
        "Duration must be greater than 0."
      );
      return;
    }

    try {
      setSaving(true);
      setError("");

      const response =
        await api.post("/assessments", {
          title: title.trim(),
          description: description.trim(),
          totalMarks: Number(totalMarks),
          durationMinutes:
            Number(durationMinutes),
          type,
          subjectId: Number(subjectId),
        });

      const createdAssessment =
        response.data;

      setAssessments((current) => [
        createdAssessment,
        ...current,
      ]);

      setShowModal(false);

      setTitle("");
      setDescription("");
      setTotalMarks("");
      setDurationMinutes("");
      setType("PRACTICE");

    } catch (err) {
      console.error(
        "Create assessment error:",
        err
      );

      setError(
        err.response?.data?.message ||
          "Failed to create assessment."
      );
    } finally {
      setSaving(false);
    }
  };

  // ============================================================
  // SEARCH
  // ============================================================

  const filteredAssessments =
    assessments.filter((assessment) => {
      const query =
        search.toLowerCase().trim();

      if (!query) {
        return true;
      }

      return (
        assessment.title
          ?.toLowerCase()
          .includes(query) ||
        assessment.subjectName
          ?.toLowerCase()
          .includes(query) ||
        assessment.type
          ?.toLowerCase()
          .includes(query)
      );
    });

  // ============================================================
  // SELECTED SUBJECT
  // ============================================================

  const selectedSubject =
    subjects.find(
      (subject) =>
        String(subject.id) ===
        String(subjectId)
    );

  return (
    <section className="dashboard-content">

      {/* =====================================================
          HEADER
          ===================================================== */}

      <div className="page-heading">

        <div>
          <h1>Assessments</h1>

          <p>
            Create and manage assessments for your subjects.
          </p>
        </div>

        <button
          className="primary-btn"
          type="button"
          onClick={openCreateModal}
          disabled={
            classes.length === 0 ||
            subjects.length === 0
          }
        >
          <Plus size={18} />

          Create Assessment
        </button>

      </div>

      {/* =====================================================
          ERROR
          ===================================================== */}

      {error && !showModal && (
        <div className="error-banner">
          {error}
        </div>
      )}

      {/* =====================================================
          CLASS + SUBJECT SELECTION
          ===================================================== */}

      <div className="faculty-assessment-selection">

        <div className="faculty-assessment-field">

          <label>Class</label>

          <select
            value={selectedClassId}
            onChange={(e) =>
              setSelectedClassId(
                e.target.value
              )
            }
          >
            {classes.length === 0 ? (
              <option value="">
                No classes assigned
              </option>
            ) : (
              classes.map((academicClass) => (
                <option
                  key={academicClass.id}
                  value={academicClass.id}
                >
                  {academicClass.name}
                </option>
              ))
            )}
          </select>

        </div>

        <div className="faculty-assessment-field">

          <label>Subject</label>

          <select
            value={subjectId}
            onChange={(e) =>
              setSubjectId(e.target.value)
            }
            disabled={
              loadingSubjects ||
              subjects.length === 0
            }
          >
            {loadingSubjects ? (
              <option value="">
                Loading subjects...
              </option>
            ) : subjects.length === 0 ? (
              <option value="">
                No subjects assigned
              </option>
            ) : (
              subjects.map((subject) => (
                <option
                  key={subject.id}
                  value={subject.id}
                >
                  {subject.name}
                </option>
              ))
            )}
          </select>

        </div>

      </div>

      {/* =====================================================
          SEARCH
          ===================================================== */}

      <div className="assessment-toolbar">

        <div className="assessment-search">

          <Search size={17} />

          <input
            type="text"
            placeholder="Search your assessments..."
            value={search}
            onChange={(e) =>
              setSearch(e.target.value)
            }
          />

        </div>

      </div>

      {/* =====================================================
          ASSESSMENT LIST
          ===================================================== */}

      {loading ? (

        <div className="loading">
          Loading your assessments...
        </div>

      ) : filteredAssessments.length === 0 ? (

        <div className="panel empty-page">

          <ClipboardList size={50} />

          <h3>
            {assessments.length === 0
              ? "No assessments yet"
              : "No matching assessments"}
          </h3>

          <p>
            {assessments.length === 0
              ? "Create your first assessment to get started."
              : "Try changing your search."}
          </p>

          {assessments.length === 0 &&
            subjects.length > 0 && (
              <button
                className="primary-btn"
                type="button"
                onClick={openCreateModal}
              >
                <Plus size={18} />
                Create Assessment
              </button>
            )}

        </div>

      ) : (

        <div className="assessment-table-card">

          <div className="assessment-table-wrapper">

            <table className="assessment-table">

              <thead>
                <tr>
                  <th>Assessment</th>
                  <th>Subject</th>
                  <th>Type</th>
                  <th>Marks</th>
                  <th>Duration</th>
                  <th>Actions</th>
                </tr>
              </thead>

              <tbody>

                {filteredAssessments.map(
                  (assessment) => (

                    <tr
                      key={assessment.id}
                    >

                      <td>

                        <div className="assessment-name">

                          <div className="assessment-avatar">
                            <ClipboardList
                              size={19}
                            />
                          </div>

                          <div>
                            <strong>
                              {assessment.title}
                            </strong>

                            {assessment.description && (
                              <small>
                                {assessment.description}
                              </small>
                            )}
                          </div>

                        </div>

                      </td>

                      <td>

                        <div className="assessment-info">

                          <BookOpen
                            size={16}
                          />

                          <span>
                            {assessment.subjectName ||
                              "—"}
                          </span>

                        </div>

                      </td>

                      <td>

                        <span
                          className={`assessment-type ${assessment.type?.toLowerCase()}`}
                        >
                          {assessment.type}
                        </span>

                      </td>

                      <td>

                        <strong>
                          {assessment.totalMarks}
                        </strong>

                        <span className="marks-label">
                          marks
                        </span>

                      </td>

                      <td>

                        <div className="assessment-info">

                          <Clock
                            size={16}
                          />

                          <span>
                            {
                              assessment.durationMinutes
                            }{" "}
                            min
                          </span>

                        </div>

                      </td>

                      <td>

                        {assessment.status === "PUBLISHED" ? (
  <span className="faculty-draft-badge published">
    Published
  </span>
) : (
  <button
    type="button"
    className="faculty-draft-badge question-builder-btn"
    onClick={() =>
      window.location.href =
        `/faculty/assessments/${assessment.id}/questions`
    }
  >
    Add Questions
  </button>
)}
                      </td>

                    </tr>

                  )
                )}

              </tbody>

            </table>

          </div>

        </div>

      )}

      {/* =====================================================
          CREATE ASSESSMENT MODAL
          ===================================================== */}

      {showModal && (

        <div className="faculty-modal-overlay">

          <div className="faculty-assessment-modal">

            <div className="faculty-modal-header">

              <div>
                <h2>Create Assessment</h2>

                <p>
                  Create an assessment for your assigned subject.
                </p>
              </div>

              <button
                type="button"
                className="faculty-modal-close"
                onClick={closeModal}
                disabled={saving}
              >
                <X size={20} />
              </button>

            </div>

            {error && (
              <div className="error-banner">
                {error}
              </div>
            )}

            <form
              className="faculty-assessment-form"
              onSubmit={handleCreateAssessment}
            >

              {/* SUBJECT */}

              <div className="faculty-form-group">

                <label>Subject</label>

                <select
                  value={subjectId}
                  onChange={(e) =>
                    setSubjectId(
                      e.target.value
                    )
                  }
                  required
                >
                  {subjects.map(
                    (subject) => (
                      <option
                        key={subject.id}
                        value={subject.id}
                      >
                        {subject.name}
                      </option>
                    )
                  )}
                </select>

              </div>

              {/* TITLE */}

              <div className="faculty-form-group">

                <label>Assessment Title</label>

                <input
                  type="text"
                  placeholder="e.g. Java OOP Quiz"
                  value={title}
                  onChange={(e) =>
                    setTitle(e.target.value)
                  }
                  required
                />

              </div>

              {/* DESCRIPTION */}

              <div className="faculty-form-group">

                <label>Description</label>

                <textarea
                  rows="3"
                  placeholder="Describe this assessment..."
                  value={description}
                  onChange={(e) =>
                    setDescription(
                      e.target.value
                    )
                  }
                />

              </div>

              {/* MARKS + DURATION */}

              <div className="faculty-form-row">

                <div className="faculty-form-group">

                  <label>Total Marks</label>

                  <input
                    type="number"
                    min="1"
                    placeholder="50"
                    value={totalMarks}
                    onChange={(e) =>
                      setTotalMarks(
                        e.target.value
                      )
                    }
                    required
                  />

                </div>

                <div className="faculty-form-group">

                  <label>Duration (minutes)</label>

                  <input
                    type="number"
                    min="1"
                    placeholder="60"
                    value={durationMinutes}
                    onChange={(e) =>
                      setDurationMinutes(
                        e.target.value
                      )
                    }
                    required
                  />

                </div>

              </div>

              {/* TYPE */}

              <div className="faculty-form-group">

                <label>Assessment Type</label>

                <select
                  value={type}
                  onChange={(e) =>
                    setType(e.target.value)
                  }
                >
                  <option value="PRACTICE">
                    Practice
                  </option>

                  <option value="QUIZ">
                    Quiz
                  </option>

                  <option value="EXAM">
                    Exam
                  </option>
                </select>

              </div>

              {/* PREVIEW */}

              {selectedSubject && (
                <div className="assessment-create-preview">

                  <BookOpen size={18} />

                  <div>
                    <span>Creating for</span>

                    <strong>
                      {selectedSubject.name}
                    </strong>
                  </div>

                </div>
              )}

              {/* ACTIONS */}

              <div className="faculty-modal-actions">

                <button
                  type="button"
                  onClick={closeModal}
                  disabled={saving}
                >
                  Cancel
                </button>

                <button
                  type="submit"
                  className="primary-btn"
                  disabled={saving}
                >
                  <Save size={17} />

                  {saving
                    ? "Creating..."
                    : "Create Assessment"}
                </button>

              </div>

            </form>

          </div>

        </div>
      )}

    </section>
  );
}