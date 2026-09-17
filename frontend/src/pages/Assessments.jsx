import { useEffect, useState } from "react";
import {
  ClipboardList,
  Clock,
  BookOpen,
  UserRound,
  Trash2,
  Search,
} from "lucide-react";

import api from "../services/api";

export default function Assessments() {
  const [assessments, setAssessments] = useState([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [search, setSearch] = useState("");
  const [deletingId, setDeletingId] = useState(null);

  const loadAssessments = async () => {
    try {
      setLoading(true);
      setError("");

      const response =
        await api.get("/assessments");

      setAssessments(
        response.data || []
      );
    } catch (err) {
      console.error(
        "Load assessments error:",
        err
      );

      setError(
        err.response?.data?.message ||
          "Failed to load assessments"
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAssessments();
  }, []);

  const handleDelete = async (
    id,
    title
  ) => {
    const confirmed =
      window.confirm(
        `Are you sure you want to delete "${title}"?`
      );

    if (!confirmed) {
      return;
    }

    try {
      setDeletingId(id);
      setError("");

      await api.delete(
        `/assessments/${id}`
      );

      setAssessments(
        (current) =>
          current.filter(
            (assessment) =>
              assessment.id !== id
          )
      );
    } catch (err) {
      console.error(
        "Delete assessment error:",
        err
      );

      setError(
        err.response?.data?.message ||
          "Failed to delete assessment"
      );
    } finally {
      setDeletingId(null);
    }
  };

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
        assessment.facultyName
          ?.toLowerCase()
          .includes(query) ||
        assessment.type
          ?.toLowerCase()
          .includes(query)
      );
    });

  return (
    <section className="dashboard-content">

      {/* =====================================================
          PAGE HEADING
          ===================================================== */}

      <div className="page-heading">

        <div>
          <h1>
            Assessments
          </h1>

          <p>
            Monitor and manage academic
            assessments.
          </p>
        </div>

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
          SEARCH
          ===================================================== */}

      <div className="assessment-toolbar">

        <div className="assessment-search">

          <Search size={17} />

          <input
            type="text"
            placeholder="Search assessments..."
            value={search}
            onChange={(e) =>
              setSearch(e.target.value)
            }
          />

        </div>

      </div>

      {/* =====================================================
          LOADING
          ===================================================== */}

      {loading ? (

        <div className="loading">
          Loading assessments...
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
              ? "Assessments created by faculty will appear here."
              : "Try changing your search."}
          </p>

        </div>

      ) : (

        <div className="assessment-table-card">

          <div className="assessment-table-wrapper">

            <table className="assessment-table">

              <thead>
                <tr>
                  <th>
                    Assessment
                  </th>

                  <th>
                    Subject
                  </th>

                  <th>
                    Faculty
                  </th>

                  <th>
                    Type
                  </th>

                  <th>
                    Marks
                  </th>

                  <th>
                    Duration
                  </th>

                  <th>
                    Actions
                  </th>
                </tr>
              </thead>

              <tbody>

                {filteredAssessments.map(
                  (assessment) => (

                    <tr
                      key={assessment.id}
                    >

                      {/* Assessment */}
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
                                {
                                  assessment.description
                                }
                              </small>
                            )}
                          </div>

                        </div>

                      </td>

                      {/* Subject */}
                      <td>

                        <div className="assessment-info">

                          <BookOpen
                            size={16}
                          />

                          <span>
                            {
                              assessment.subjectName ||
                              "—"
                            }
                          </span>

                        </div>

                      </td>

                      {/* Faculty */}
                      <td>

                        <div className="assessment-info">

                          <UserRound
                            size={16}
                          />

                          <span>
                            {
                              assessment.facultyName ||
                              "—"
                            }
                          </span>

                        </div>

                      </td>

                      {/* Type */}
                      <td>

                        <span
                          className={`assessment-type ${assessment.type?.toLowerCase()}`}
                        >
                          {
                            assessment.type
                          }
                        </span>

                      </td>

                      {/* Marks */}
                      <td>

                        <strong>
                          {
                            assessment.totalMarks
                          }
                        </strong>

                        <span className="marks-label">
                          marks
                        </span>

                      </td>

                      {/* Duration */}
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

                      {/* Actions */}
                      <td>

                        <div className="assessment-actions">

                          <button
                            type="button"
                            className="assessment-delete-btn"
                            disabled={
                              deletingId ===
                              assessment.id
                            }
                            onClick={() =>
                              handleDelete(
                                assessment.id,
                                assessment.title
                              )
                            }
                          >

                            <Trash2
                              size={16}
                            />

                            {deletingId ===
                            assessment.id
                              ? "Deleting..."
                              : "Delete"}

                          </button>

                        </div>

                      </td>

                    </tr>

                  )
                )}

              </tbody>

            </table>

          </div>

        </div>

      )}

    </section>
  );
}