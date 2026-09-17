import { useEffect, useState } from "react";
import {
  BookOpen,
  Users,
  ClipboardList,
  BarChart3,
  ChevronDown,
  RefreshCw,
} from "lucide-react";
import { useAuth } from "../context/AuthContext";
import api from "../services/api";

export default function Dashboard() {
  const { user } = useAuth();

  const [classes, setClasses] = useState([]);
  const [subjects, setSubjects] = useState([]);
  const [students, setStudents] = useState([]);

  const [selectedClass, setSelectedClass] = useState("");
  const [selectedSubject, setSelectedSubject] = useState("");

  const [loadingClasses, setLoadingClasses] = useState(true);
  const [loadingSubjects, setLoadingSubjects] = useState(false);
  const [loadingStudents, setLoadingStudents] = useState(false);

  const [error, setError] = useState("");

  // ============================================================
  // LOAD FACULTY CLASSES
  // ============================================================

  useEffect(() => {
    loadClasses();
  }, []);

  const loadClasses = async () => {
    try {
      setLoadingClasses(true);
      setError("");

      const response = await api.get("/faculty/dashboard/classes");

      const data = Array.isArray(response.data)
        ? response.data
        : [];

      setClasses(data);

      // Automatically select first class
      if (data.length > 0) {
        setSelectedClass(String(data[0].id));
      }
    } catch (err) {
      console.error("Failed to load classes:", err);

      setError(
        err.response?.data?.message ||
          "Unable to load your assigned classes."
      );
    } finally {
      setLoadingClasses(false);
    }
  };

  // ============================================================
  // LOAD SUBJECTS WHEN CLASS CHANGES
  // ============================================================

  useEffect(() => {
    if (!selectedClass) {
      setSubjects([]);
      setSelectedSubject("");
      setStudents([]);
      return;
    }

    loadSubjects(selectedClass);
  }, [selectedClass]);

  const loadSubjects = async (classId) => {
    try {
      setLoadingSubjects(true);
      setError("");

      setSelectedSubject("");
      setStudents([]);

      const response = await api.get(
        `/faculty/dashboard/subjects?classId=${classId}`
      );

      const data = Array.isArray(response.data)
        ? response.data
        : [];

      setSubjects(data);

      // Automatically select first subject
      if (data.length > 0) {
        setSelectedSubject(String(data[0].id));
      }
    } catch (err) {
      console.error("Failed to load subjects:", err);

      setError(
        err.response?.data?.message ||
          "Unable to load subjects for this class."
      );

      setSubjects([]);
    } finally {
      setLoadingSubjects(false);
    }
  };

  // ============================================================
  // LOAD STUDENTS WHEN CLASS + SUBJECT ARE SELECTED
  // ============================================================

  useEffect(() => {
    if (!selectedClass || !selectedSubject) {
      setStudents([]);
      return;
    }

    loadStudents(selectedClass, selectedSubject);
  }, [selectedClass, selectedSubject]);

  const loadStudents = async (classId, subjectId) => {
    try {
      setLoadingStudents(true);
      setError("");

      const response = await api.get(
        `/faculty/dashboard/students?classId=${classId}&subjectId=${subjectId}`
      );

      const data = Array.isArray(response.data)
        ? response.data
        : [];

      setStudents(data);
    } catch (err) {
      console.error("Failed to load students:", err);

      setError(
        err.response?.data?.message ||
          "Unable to load students."
      );

      setStudents([]);
    } finally {
      setLoadingStudents(false);
    }
  };

  // ============================================================
  // SELECTED CLASS / SUBJECT
  // ============================================================

  const currentClass = classes.find(
    (item) => String(item.id) === String(selectedClass)
  );

  const currentSubject = subjects.find(
    (item) => String(item.id) === String(selectedSubject)
  );

  // ============================================================
  // REFRESH
  // ============================================================

  const handleRefresh = () => {
    if (selectedClass && selectedSubject) {
      loadStudents(selectedClass, selectedSubject);
    } else {
      loadClasses();
    }
  };

  return (
    <div className="faculty-dashboard">
      {/* ======================================================
          HEADER
      ====================================================== */}

      <div className="faculty-dashboard-header">
        <div>
          <p className="dashboard-eyebrow">
            FACULTY DASHBOARD
          </p>

          <h1>
            Welcome back, {user?.fullName || "Faculty"} 👋
          </h1>

          <p className="dashboard-subtitle">
            Manage your classes, subjects and student performance
            from one place.
          </p>
        </div>

        <button
          className="refresh-dashboard-btn"
          onClick={handleRefresh}
          title="Refresh"
        >
          <RefreshCw size={18} />
          Refresh
        </button>
      </div>

      {/* ======================================================
          ERROR
      ====================================================== */}

      {error && (
        <div className="faculty-error">
          {error}
        </div>
      )}

      {/* ======================================================
          STAT CARDS
      ====================================================== */}

      <div className="faculty-stat-grid">

        <div className="faculty-stat-card">
          <div className="faculty-stat-icon">
            <BookOpen size={22} />
          </div>

          <div>
            <span>My Classes</span>
            <strong>{classes.length}</strong>
          </div>
        </div>

        <div className="faculty-stat-card">
          <div className="faculty-stat-icon">
            <BookOpen size={22} />
          </div>

          <div>
            <span>My Subjects</span>
            <strong>{subjects.length}</strong>
          </div>
        </div>

        <div className="faculty-stat-card">
          <div className="faculty-stat-icon">
            <Users size={22} />
          </div>

          <div>
            <span>Students</span>
            <strong>{students.length}</strong>
          </div>
        </div>

        <div className="faculty-stat-card">
          <div className="faculty-stat-icon">
            <ClipboardList size={22} />
          </div>

          <div>
            <span>Assessments</span>
            <strong>0</strong>
          </div>
        </div>

      </div>

      {/* ======================================================
          CLASS + SUBJECT FILTER
      ====================================================== */}

      <div className="faculty-selection-card">

        <div className="selection-card-header">
          <div>
            <h2>Student Overview</h2>
            <p>
              Select a class and subject to view your students.
            </p>
          </div>
        </div>

        <div className="faculty-select-grid">

          {/* CLASS */}

          <div className="faculty-field">
            <label>Class</label>

            <div className="select-wrapper">
              <select
                value={selectedClass}
                onChange={(e) =>
                  setSelectedClass(e.target.value)
                }
                disabled={
                  loadingClasses || classes.length === 0
                }
              >
                {loadingClasses ? (
                  <option value="">
                    Loading classes...
                  </option>
                ) : classes.length === 0 ? (
                  <option value="">
                    No classes assigned
                  </option>
                ) : (
                  <>
                    <option value="">
                      Select Class
                    </option>

                    {classes.map((item) => (
                      <option
                        key={item.id}
                        value={item.id}
                      >
                        {item.name}
                      </option>
                    ))}
                  </>
                )}
              </select>

              <ChevronDown size={18} />
            </div>
          </div>

          {/* SUBJECT */}

          <div className="faculty-field">
            <label>Subject</label>

            <div className="select-wrapper">
              <select
                value={selectedSubject}
                onChange={(e) =>
                  setSelectedSubject(e.target.value)
                }
                disabled={
                  !selectedClass ||
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
                  <>
                    <option value="">
                      Select Subject
                    </option>

                    {subjects.map((item) => (
                      <option
                        key={item.id}
                        value={item.id}
                      >
                        {item.name}
                      </option>
                    ))}
                  </>
                )}
              </select>

              <ChevronDown size={18} />
            </div>
          </div>

        </div>

        {/* CURRENT SELECTION */}

        {currentClass && currentSubject && (
          <div className="current-selection">

            <div>
              <span>Selected Class</span>
              <strong>{currentClass.name}</strong>
            </div>

            <div className="selection-divider" />

            <div>
              <span>Selected Subject</span>
              <strong>{currentSubject.name}</strong>
            </div>

            <div className="selection-student-count">
              <Users size={18} />
              <strong>{students.length}</strong>
              <span>Students</span>
            </div>

          </div>
        )}

      </div>

      {/* ======================================================
          STUDENT TABLE
      ====================================================== */}

      <div className="faculty-students-card">

        <div className="students-card-header">

          <div>
            <h2>Students</h2>

            <p>
              {currentClass && currentSubject
                ? `${currentClass.name} • ${currentSubject.name}`
                : "Select a class and subject"}
            </p>
          </div>

          {selectedClass && selectedSubject && (
            <div className="student-count-badge">
              {students.length} Students
            </div>
          )}

        </div>

        {loadingStudents ? (
          <div className="faculty-loading">
            Loading students...
          </div>
        ) : !selectedClass || !selectedSubject ? (
          <div className="faculty-empty">
            <Users size={40} />
            <h3>Select Class and Subject</h3>
            <p>
              Choose a class and subject above to view students.
            </p>
          </div>
        ) : students.length === 0 ? (
          <div className="faculty-empty">
            <Users size={40} />
            <h3>No Students Found</h3>
            <p>
              There are currently no students enrolled in this
              class.
            </p>
          </div>
        ) : (
          <div className="faculty-table-wrapper">

            <table className="faculty-students-table">

              <thead>
                <tr>
                  <th>#</th>
                  <th>Student</th>
                  <th>Email</th>
                  <th>Enrollment</th>
                  <th>Phone</th>
                  <th>Performance</th>
                </tr>
              </thead>

              <tbody>
                {students.map((student, index) => (
                  <tr key={student.id}>

                    <td className="student-number">
                      {index + 1}
                    </td>

                    <td>
                      <div className="student-name-cell">

                        <div className="student-avatar">
                          {student.fullName
                            ?.charAt(0)
                            ?.toUpperCase() || "S"}
                        </div>

                        <strong>
                          {student.fullName || "—"}
                        </strong>

                      </div>
                    </td>

                    <td>
                      {student.email || "—"}
                    </td>

                    <td>
                      {student.enrollmentNumber || "—"}
                    </td>

                    <td>
                      {student.phone || "—"}
                    </td>

                    <td>
                      <span className="performance-pending">
                        <BarChart3 size={15} />
                        No data
                      </span>
                    </td>

                  </tr>
                ))}
              </tbody>

            </table>

          </div>
        )}

      </div>
    </div>
  );
}