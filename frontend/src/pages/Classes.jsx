import { useEffect, useState } from "react";
import {
  Plus,
  BookOpen,
  Calendar,
  Trash2,
  Copy,
} from "lucide-react";

import api from "../services/api";
import Modal from "../components/Modal";

export default function Classes() {
  const [classes, setClasses] = useState([]);
  const [showModal, setShowModal] = useState(false);

  const [name, setName] = useState("");
  const [academicYear, setAcademicYear] = useState("");

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [deletingId, setDeletingId] = useState(null);

  const [error, setError] = useState("");

  const loadClasses = async () => {
    try {
      setLoading(true);
      setError("");

      const response = await api.get("/classes");

      setClasses(response.data || []);
    } catch (err) {
      console.error("Load classes error:", err);

      setError(
        err.response?.data?.message ||
          "Failed to load classes"
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadClasses();
  }, []);

  const handleCreate = async (e) => {
    e.preventDefault();

    try {
      setSaving(true);
      setError("");

      await api.post("/classes", {
        name: name.trim(),
        academicYear: academicYear.trim(),
      });

      setName("");
      setAcademicYear("");
      setShowModal(false);

      await loadClasses();
    } catch (err) {
      console.error("Create class error:", err);

      setError(
        err.response?.data?.message ||
          "Failed to create class"
      );
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id, className) => {
    const confirmed = window.confirm(
      `Are you sure you want to delete "${className}"?`
    );

    if (!confirmed) {
      return;
    }

    try {
      setDeletingId(id);
      setError("");

      await api.delete(`/classes/${id}`);

      setClasses((currentClasses) =>
        currentClasses.filter(
          (academicClass) =>
            academicClass.id !== id
        )
      );
    } catch (err) {
      console.error("Delete class error:", err);

      setError(
        err.response?.data?.message ||
          "Failed to delete class"
      );
    } finally {
      setDeletingId(null);
    }
  };

  return (
    <section className="dashboard-content">

      {/* =====================================================
          PAGE HEADING
          ===================================================== */}
      <div className="page-heading">

        <div>
          <h1>Academic Classes</h1>

          <p>
            Create and manage academic classes.
          </p>
        </div>

        <button
          className="primary-btn"
          onClick={() => {
            setError("");
            setShowModal(true);
          }}
        >
          <Plus size={18} />

          Create Class
        </button>

      </div>

      {/* ERROR */}
      {error && (
        <div className="error-banner">
          {error}
        </div>
      )}

      {/* =====================================================
          CONTENT
          ===================================================== */}
      {loading ? (
        <div className="loading">
          Loading classes...
        </div>
      ) : classes.length === 0 ? (

        <div className="panel empty-page">

          <BookOpen size={50} />

          <h3>
            No classes yet
          </h3>

          <p>
            Create your first academic class
            to get started.
          </p>

          <button
            className="primary-btn"
            onClick={() => {
              setError("");
              setShowModal(true);
            }}
          >
            <Plus size={18} />

            Create Class
          </button>

        </div>

      ) : (

        <div className="class-grid">

          {classes.map((academicClass) => (

            <div
              className="class-card"
              key={academicClass.id}
            >

              <div className="class-icon">
                <BookOpen size={26} />
              </div>

              <h3>
                {academicClass.name}
              </h3>

              <div className="class-info">

                <Calendar size={16} />

                <span>
                  {academicClass.academicYear}
                </span>

              </div>

<div className="join-code-box">

  <div>
    <small>
      Student Join Code
    </small>

    <strong>
      {academicClass.joinCode || "Generating..."}
    </strong>
  </div>

  {academicClass.joinCode && (
    <button
      type="button"
      title="Copy join code"
      onClick={async () => {
        try {
          await navigator.clipboard.writeText(
            academicClass.joinCode
          );
        } catch (err) {
          console.error(
            "Copy join code error:",
            err
          );
        }
      }}
    >
      <Copy size={16} />
    </button>
  )}

</div>
              <div className="class-footer">

                <span>
                  Class ID: #{academicClass.id}
                </span>

                <button
                  type="button"
                  title="Delete"
                  disabled={
                    deletingId === academicClass.id
                  }
                  onClick={() =>
                    handleDelete(
                      academicClass.id,
                      academicClass.name
                    )
                  }
                >
                  <Trash2 size={17} />
                </button>

              </div>

            </div>

          ))}

        </div>

      )}

      {/* =====================================================
          CREATE CLASS MODAL
          ===================================================== */}
      {showModal && (

        <Modal
          title="Create Academic Class"
          onClose={() => {
            if (!saving) {
              setShowModal(false);
            }
          }}
        >

          <form
            className="modal-form"
            onSubmit={handleCreate}
          >

            <label>
              Class Name
            </label>

            <input
              type="text"
              placeholder="e.g. MSc FY CS"
              value={name}
              onChange={(e) =>
                setName(e.target.value)
              }
              required
            />

            <label>
              Academic Year
            </label>

            <input
              type="text"
              placeholder="e.g. 2026-27"
              value={academicYear}
              onChange={(e) =>
                setAcademicYear(
                  e.target.value
                )
              }
              required
            />

            <div className="modal-actions">

              <button
                type="button"
                onClick={() =>
                  setShowModal(false)
                }
                disabled={saving}
              >
                Cancel
              </button>

              <button
                type="submit"
                className="primary-btn"
                disabled={saving}
              >
                {saving
                  ? "Creating..."
                  : "Create Class"}
              </button>

            </div>

          </form>

        </Modal>

      )}

    </section>
  );
}