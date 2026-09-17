import { useEffect, useState } from "react";
import {
  Plus,
  BookOpen,
  Calendar,
  Pencil,
  Trash2,
} from "lucide-react";

import api from "../services/api";
import Modal from "../components/Modal";

export default function Subjects() {
  const [classes, setClasses] = useState([]);
  const [subjects, setSubjects] = useState([]);

  const [selectedClassId, setSelectedClassId] =
    useState("");

  const [showModal, setShowModal] = useState(false);
  const [editingSubject, setEditingSubject] =
    useState(null);

  const [name, setName] = useState("");
  const [description, setDescription] =
    useState("");

  const [loadingClasses, setLoadingClasses] =
    useState(true);

  const [loadingSubjects, setLoadingSubjects] =
    useState(false);

  const [saving, setSaving] = useState(false);
  const [deletingId, setDeletingId] =
    useState(null);

  const [error, setError] = useState("");

  /* =====================================================
     LOAD CLASSES
     ===================================================== */

  const loadClasses = async () => {
    try {
      setLoadingClasses(true);
      setError("");

      const response =
        await api.get("/classes");

      const classList =
        response.data || [];

      setClasses(classList);

      if (classList.length > 0) {
        setSelectedClassId(
          String(classList[0].id)
        );
      }
    } catch (err) {
      console.error(
        "Load classes error:",
        err
      );

      setError(
        err.response?.data?.message ||
          "Failed to load classes"
      );
    } finally {
      setLoadingClasses(false);
    }
  };

  /* =====================================================
     LOAD SUBJECTS
     ===================================================== */

  const loadSubjects = async (classId) => {
    if (!classId) {
      setSubjects([]);
      return;
    }

    try {
      setLoadingSubjects(true);
      setError("");

      const response =
        await api.get(
          `/classes/${classId}/subjects`
        );

      setSubjects(
        response.data || []
      );
    } catch (err) {
      console.error(
        "Load subjects error:",
        err
      );

      setError(
        err.response?.data?.message ||
          "Failed to load subjects"
      );

      setSubjects([]);
    } finally {
      setLoadingSubjects(false);
    }
  };

  useEffect(() => {
    loadClasses();
  }, []);

  useEffect(() => {
    if (selectedClassId) {
      loadSubjects(selectedClassId);
    }
  }, [selectedClassId]);

  /* =====================================================
     CREATE MODAL
     ===================================================== */

  const openCreateModal = () => {
    setEditingSubject(null);
    setName("");
    setDescription("");
    setError("");
    setShowModal(true);
  };

  /* =====================================================
     EDIT MODAL
     ===================================================== */

  const openEditModal = (subject) => {
    setEditingSubject(subject);

    setName(subject.name || "");
    setDescription(
      subject.description || ""
    );

    setError("");
    setShowModal(true);
  };

  /* =====================================================
     CREATE / UPDATE
     ===================================================== */

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!selectedClassId) {
      setError(
        "Please select a class first."
      );
      return;
    }

    if (!name.trim()) {
      setError(
        "Subject name is required."
      );
      return;
    }

    try {
      setSaving(true);
      setError("");

      if (editingSubject) {

        await api.put(
          `/classes/${selectedClassId}/subjects/${editingSubject.id}`,
          {
            name: name.trim(),
            description:
              description.trim(),
          }
        );

      } else {

        await api.post(
          `/classes/${selectedClassId}/subjects`,
          {
            name: name.trim(),
            description:
              description.trim(),
          }
        );

      }

      setName("");
      setDescription("");
      setEditingSubject(null);
      setShowModal(false);

      await loadSubjects(
        selectedClassId
      );

    } catch (err) {
      console.error(
        "Save subject error:",
        err
      );

      setError(
        err.response?.data?.message ||
          "Failed to save subject"
      );
    } finally {
      setSaving(false);
    }
  };

  /* =====================================================
     DELETE
     ===================================================== */

  const handleDelete = async (subject) => {
    const confirmed =
      window.confirm(
        `Are you sure you want to delete "${subject.name}"?`
      );

    if (!confirmed) {
      return;
    }

    try {
      setDeletingId(subject.id);
      setError("");

      await api.delete(
        `/classes/${selectedClassId}/subjects/${subject.id}`
      );

      setSubjects(
        (currentSubjects) =>
          currentSubjects.filter(
            (item) =>
              item.id !== subject.id
          )
      );

    } catch (err) {
      console.error(
        "Delete subject error:",
        err
      );

      setError(
        err.response?.data?.message ||
          "Failed to delete subject"
      );
    } finally {
      setDeletingId(null);
    }
  };

  const selectedClass =
    classes.find(
      (academicClass) =>
        String(academicClass.id) ===
        selectedClassId
    );

  return (
    <section className="dashboard-content">

      {/* =====================================================
          PAGE HEADING
          ===================================================== */}
      <div className="page-heading">

        <div>
          <h1>Subjects</h1>

          <p>
            Create and manage subjects
            for academic classes.
          </p>
        </div>

        <button
          className="primary-btn"
          disabled={!selectedClassId}
          onClick={openCreateModal}
        >
          <Plus size={18} />

          Add Subject
        </button>

      </div>

      {/* ERROR */}
      {error && (
        <div className="error-banner">
          {error}
        </div>
      )}

      {/* =====================================================
          CLASS SELECTOR
          ===================================================== */}
      <div className="panel">

        <div className="panel-header">

          <div>
            <h3>
              Select Class
            </h3>

            <p>
              Choose an academic class
              to manage its subjects.
            </p>
          </div>

        </div>

        {loadingClasses ? (

          <div className="loading">
            Loading classes...
          </div>

        ) : classes.length === 0 ? (

          <div className="empty-state">

            <BookOpen size={40} />

            <h4>
              No classes available
            </h4>

            <p>
              Create an academic class
              before adding subjects.
            </p>

          </div>

        ) : (

          <select
            className="class-selector"
            value={selectedClassId}
            onChange={(e) =>
              setSelectedClassId(
                e.target.value
              )
            }
          >

            {classes.map(
              (academicClass) => (

                <option
                  key={academicClass.id}
                  value={academicClass.id}
                >
                  {academicClass.name} —{" "}
                  {academicClass.academicYear}
                </option>

              )
            )}

          </select>

        )}

      </div>

      {/* =====================================================
          SELECTED CLASS
          ===================================================== */}
      {selectedClass && (

        <div className="selected-class">

          <div className="selected-class-icon">
            <BookOpen size={24} />
          </div>

          <div>

            <h2>
              {selectedClass.name}
            </h2>

            <p>
              <Calendar size={15} />

              Academic Year:{" "}
              {selectedClass.academicYear}
            </p>

          </div>

        </div>

      )}

      {/* =====================================================
          SUBJECT LIST
          ===================================================== */}
      {loadingSubjects ? (

        <div className="loading">
          Loading subjects...
        </div>

      ) : subjects.length === 0 ? (

        <div className="panel empty-page">

          <BookOpen size={50} />

          <h3>
            No subjects yet
          </h3>

          <p>
            Add the first subject
            for this class.
          </p>

          <button
            className="primary-btn"
            disabled={!selectedClassId}
            onClick={openCreateModal}
          >
            <Plus size={18} />

            Add Subject
          </button>

        </div>

      ) : (

        <div className="class-grid">

          {subjects.map(
            (subject) => (

              <div
                className="class-card"
                key={subject.id}
              >

                <div className="class-icon">
                  <BookOpen size={26} />
                </div>

                <h3>
                  {subject.name}
                </h3>

                {subject.description && (
                  <p className="subject-description">
                    {subject.description}
                  </p>
                )}

                <div className="class-info">

                  <Calendar size={16} />

                  <span>
                    {
                      selectedClass?.academicYear
                    }
                  </span>

                </div>

                <div className="class-footer">

                  <span>
                    Subject ID: #{subject.id}
                  </span>

                  <span>
                    {subject.facultyName
                      ? `Faculty: ${subject.facultyName}`
                      : "Faculty not assigned"}
                  </span>

                </div>

                {/* ACTIONS */}
                <div className="subject-actions">

                  <button
                    type="button"
                    onClick={() =>
                      openEditModal(subject)
                    }
                  >
                    <Pencil size={16} />

                    Edit
                  </button>

                  <button
                    type="button"
                    className="delete-btn"
                    disabled={
                      deletingId ===
                      subject.id
                    }
                    onClick={() =>
                      handleDelete(subject)
                    }
                  >
                    <Trash2 size={16} />

                    {deletingId ===
                    subject.id
                      ? "Deleting..."
                      : "Delete"}
                  </button>

                </div>

              </div>

            )
          )}

        </div>

      )}

      {/* =====================================================
          CREATE / EDIT MODAL
          ===================================================== */}
      {showModal && (

        <Modal
          title={
            editingSubject
              ? "Edit Subject"
              : "Add Subject"
          }
          onClose={() => {
            if (!saving) {
              setShowModal(false);
            }
          }}
        >

          <form
            className="modal-form"
            onSubmit={handleSubmit}
          >

            <label>
              Subject Name
            </label>

            <input
              type="text"
              placeholder="e.g. Java"
              value={name}
              onChange={(e) =>
                setName(e.target.value)
              }
              required
              autoFocus
            />

            <label>
              Subject Details
            </label>

            <textarea
              placeholder="e.g. Core Java, OOP and programming fundamentals"
              value={description}
              onChange={(e) =>
                setDescription(
                  e.target.value
                )
              }
              rows={4}
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
                  ? "Saving..."
                  : editingSubject
                  ? "Save Changes"
                  : "Create Subject"}
              </button>

            </div>

          </form>

        </Modal>

      )}

    </section>
  );
}