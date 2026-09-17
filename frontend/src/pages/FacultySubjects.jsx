import { useEffect, useState } from "react";
import {
  BookOpen,
  Calendar,
  Users,
  RefreshCw,
} from "lucide-react";

import api from "../services/api";

export default function FacultySubjects() {
  const [classes, setClasses] = useState([]);
  const [subjectsByClass, setSubjectsByClass] = useState({});

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadSubjects = async () => {
    try {
      setLoading(true);
      setError("");

      const classResponse = await api.get(
        "/faculty/dashboard/classes"
      );

      const classList = Array.isArray(classResponse.data)
        ? classResponse.data
        : [];

      setClasses(classList);

      const subjectMap = {};

      for (const academicClass of classList) {
        try {
          const response = await api.get(
            `/faculty/dashboard/subjects?classId=${academicClass.id}`
          );

          subjectMap[academicClass.id] = Array.isArray(
            response.data
          )
            ? response.data
            : [];
        } catch (err) {
          console.error(
            `Failed to load subjects for class ${academicClass.id}`,
            err
          );

          subjectMap[academicClass.id] = [];
        }
      }

      setSubjectsByClass(subjectMap);
    } catch (err) {
      console.error("Load faculty subjects error:", err);

      setError(
        err.response?.data?.message ||
          "Failed to load your subjects."
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadSubjects();
  }, []);

  const totalSubjects = Object.values(
    subjectsByClass
  ).reduce(
    (total, subjects) => total + subjects.length,
    0
  );

  return (
    <section className="dashboard-content">

      {/* =====================================================
          HEADER
          ===================================================== */}

      <div className="page-heading">

        <div>
          <h1>My Subjects</h1>

          <p>
            View the classes and subjects assigned to you.
          </p>
        </div>

        <button
          type="button"
          className="primary-btn"
          onClick={loadSubjects}
          disabled={loading}
        >
          <RefreshCw size={17} />

          {loading ? "Refreshing..." : "Refresh"}
        </button>

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
          SUMMARY
          ===================================================== */}

      {!loading && (
        <div className="faculty-subject-summary">

          <div className="faculty-subject-summary-card">
            <div className="faculty-summary-icon">
              <Users size={21} />
            </div>

            <div>
              <span>Assigned Classes</span>
              <strong>{classes.length}</strong>
            </div>
          </div>

          <div className="faculty-subject-summary-card">
            <div className="faculty-summary-icon">
              <BookOpen size={21} />
            </div>

            <div>
              <span>Assigned Subjects</span>
              <strong>{totalSubjects}</strong>
            </div>
          </div>

        </div>
      )}

      {/* =====================================================
          LOADING
          ===================================================== */}

      {loading ? (
        <div className="loading">
          Loading your subjects...
        </div>

      ) : classes.length === 0 ? (

        <div className="panel empty-page">

          <Users size={50} />

          <h3>No Classes Assigned</h3>

          <p>
            You have not been assigned to any academic class yet.
          </p>

        </div>

      ) : (

        /* ===================================================
           CLASS SECTIONS
           =================================================== */

        <div className="faculty-subject-class-list">

          {classes.map((academicClass) => {

            const subjects =
              subjectsByClass[academicClass.id] || [];

            return (
              <div
                className="faculty-subject-class-card"
                key={academicClass.id}
              >

                {/* CLASS HEADER */}

                <div className="faculty-class-header">

                  <div className="faculty-class-icon">
                    <Users size={22} />
                  </div>

                  <div>
                    <h2>
                      {academicClass.name}
                    </h2>

                    {academicClass.academicYear && (
                      <p>
                        <Calendar size={14} />

                        Academic Year:{" "}
                        {academicClass.academicYear}
                      </p>
                    )}
                  </div>

                  <div className="faculty-class-count">
                    {subjects.length}{" "}
                    {subjects.length === 1
                      ? "Subject"
                      : "Subjects"}
                  </div>

                </div>

                {/* SUBJECTS */}

                {subjects.length === 0 ? (

                  <div className="faculty-no-subjects">
                    <BookOpen size={25} />

                    <span>
                      No subjects assigned in this class.
                    </span>
                  </div>

                ) : (

                  <div className="faculty-subject-grid">

                    {subjects.map((subject) => (

                      <div
                        className="faculty-subject-card"
                        key={subject.id}
                      >

                        <div className="faculty-subject-icon">
                          <BookOpen size={22} />
                        </div>

                        <div className="faculty-subject-content">

                          <h3>
                            {subject.name}
                          </h3>

                          {subject.description && (
                            <p>
                              {subject.description}
                            </p>
                          )}

                          <div className="faculty-subject-meta">

                            <span>
                              Subject ID: #{subject.id}
                            </span>

                            <span>
                              {academicClass.name}
                            </span>

                          </div>

                        </div>

                      </div>

                    ))}

                  </div>

                )}

              </div>
            );
          })}

        </div>
      )}

    </section>
  );
}