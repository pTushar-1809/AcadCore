import React, { useEffect, useState } from "react";
import {
  UserRound,
  Mail,
  Pencil,
  Trash2,
  RotateCw,
  Plus,
  X,
  Send,
} from "lucide-react";

import api from "../services/api";

const Faculty = () => {

  // ============================================================
  // STATE
  // ============================================================

  const [facultyList, setFacultyList] = useState([]);

  const [classes, setClasses] = useState([]);

  const [subjects, setSubjects] = useState([]);

  const [loading, setLoading] = useState(true);

  const [error, setError] = useState("");

  const [success, setSuccess] = useState("");

  const [showModal, setShowModal] = useState(false);

  const [editingFaculty, setEditingFaculty] = useState(null);

  const [resendingId, setResendingId] = useState(null);


  const [form, setForm] = useState({
    fullName: "",
    email: "",
    classId: "",
    subjectId: "",
  });


  // ============================================================
  // LOAD FACULTY
  // ============================================================

  const loadFaculty = async () => {

    try {

      setLoading(true);

      const [activeResponse, pendingResponse] =
        await Promise.all([
          api.get("/faculty"),
          api.get("/faculty-invitations"),
        ]);

      const activeFaculty =
        activeResponse.data || [];

      const pendingFaculty =
        pendingResponse.data || [];

      setFacultyList([
        ...activeFaculty,
        ...pendingFaculty,
      ]);

    } catch (err) {

      console.error(err);

      setError(
        err.response?.data?.message ||
        "Failed to load faculty"
      );

    } finally {

      setLoading(false);
    }
  };


  // ============================================================
  // LOAD CLASSES
  // ============================================================

  const loadClasses = async () => {

    try {

      const response =
        await api.get("/classes");

      setClasses(response.data || []);

    } catch (err) {

      console.error(err);
    }
  };


  // ============================================================
  // LOAD SUBJECTS
  // ============================================================

  const loadSubjects = async (classId) => {

    if (!classId) {

      setSubjects([]);

      return;
    }

    try {

      const response =
        await api.get(
          `/classes/${classId}/subjects`
        );

      setSubjects(response.data || []);

    } catch (err) {

      console.error(err);

      setSubjects([]);
    }
  };


  // ============================================================
  // INITIAL LOAD
  // ============================================================

  useEffect(() => {

    loadFaculty();
    loadClasses();

  }, []);


  // ============================================================
  // CLASS CHANGE
  // ============================================================

  const handleClassChange = async (e) => {

    const classId = e.target.value;

    setForm((previous) => ({
      ...previous,
      classId,
      subjectId: "",
    }));

    await loadSubjects(classId);
  };


  // ============================================================
  // OPEN INVITE MODAL
  // ============================================================

  const openInviteModal = () => {

    setEditingFaculty(null);

    setForm({
      fullName: "",
      email: "",
      classId: "",
      subjectId: "",
    });

    setSubjects([]);

    setError("");

    setSuccess("");

    setShowModal(true);
  };


  // ============================================================
  // OPEN EDIT MODAL
  // ============================================================

  const openEditModal = async (faculty) => {

    setEditingFaculty(faculty);

    const classId =
      faculty.classId || "";

    const subjectId =
      faculty.subjectId || "";

    setForm({
      fullName: faculty.fullName || "",
      email: faculty.email || "",
      classId: classId.toString(),
      subjectId: subjectId.toString(),
    });

    setError("");

    setSuccess("");

    setShowModal(true);

    if (classId) {

      await loadSubjects(classId);
    }
  };


  // ============================================================
  // CLOSE MODAL
  // ============================================================

  const closeModal = () => {

    setShowModal(false);

    setEditingFaculty(null);

    setForm({
      fullName: "",
      email: "",
      classId: "",
      subjectId: "",
    });

    setSubjects([]);
  };


  // ============================================================
  // FORM CHANGE
  // ============================================================

  const handleChange = (e) => {

    const { name, value } = e.target;

    setForm((previous) => ({
      ...previous,
      [name]: value,
    }));
  };


  // ============================================================
  // INVITE FACULTY
  // ============================================================

  const sendInvitation = async (e) => {

    e.preventDefault();

    setError("");

    setSuccess("");

    try {

      await api.post(
        "/faculty-invitations",
        {
          fullName: form.fullName,
          email: form.email,
          classId: Number(form.classId),
          subjectId: Number(form.subjectId),
        }
      );

      setSuccess(
        "Faculty invitation sent successfully."
      );

      closeModal();

      await loadFaculty();

    } catch (err) {

      console.error(err);

      setError(
        err.response?.data?.message ||
        "Failed to send invitation"
      );
    }
  };


  // ============================================================
  // EDIT ACTIVE FACULTY
  // ============================================================

  const updateActiveFaculty = async (e) => {

    e.preventDefault();

    setError("");

    setSuccess("");

    try {

      await api.put(
        `/faculty/${editingFaculty.facultyId}`,
        {
          fullName: form.fullName,
          email: form.email,
          classId: form.classId
            ? Number(form.classId)
            : null,
          subjectId: form.subjectId
            ? Number(form.subjectId)
            : null,
        }
      );

      setSuccess(
        "Faculty updated successfully."
      );

      closeModal();

      await loadFaculty();

    } catch (err) {

      console.error(err);

      setError(
        err.response?.data?.message ||
        "Failed to update faculty"
      );
    }
  };


  // ============================================================
  // EDIT PENDING INVITATION
  // ============================================================

  const updatePendingInvitation = async (e) => {

    e.preventDefault();

    setError("");

    setSuccess("");

    try {

      await api.put(
        `/faculty-invitations/${editingFaculty.invitationId}`,
        {
          fullName: form.fullName,
          email: form.email,
          classId: Number(form.classId),
          subjectId: Number(form.subjectId),
        }
      );

      setSuccess(
        "Invitation updated successfully."
      );

      closeModal();

      await loadFaculty();

    } catch (err) {

      console.error(err);

      setError(
        err.response?.data?.message ||
        "Failed to update invitation"
      );
    }
  };


  // ============================================================
  // SAVE
  // ============================================================

  const handleSubmit = (e) => {

    if (!editingFaculty) {

      return sendInvitation(e);
    }

    if (editingFaculty.status === "PENDING") {

      return updatePendingInvitation(e);
    }

    return updateActiveFaculty(e);
  };


  // ============================================================
  // DELETE ACTIVE
  // ============================================================

  const deleteActiveFaculty = async (faculty) => {

    const confirmed =
      window.confirm(
        `Delete faculty "${faculty.fullName}"?`
      );

    if (!confirmed) return;

    try {

      await api.delete(
        `/faculty/${faculty.facultyId}`
      );

      setSuccess(
        "Faculty deleted successfully."
      );

      await loadFaculty();

    } catch (err) {

      console.error(err);

      setError(
        err.response?.data?.message ||
        "Failed to delete faculty"
      );
    }
  };


  // ============================================================
  // DELETE PENDING
  // ============================================================

  const deletePendingInvitation = async (faculty) => {

    const confirmed =
      window.confirm(
        `Delete invitation for "${faculty.fullName}"?`
      );

    if (!confirmed) return;

    try {

      await api.delete(
        `/faculty-invitations/${faculty.invitationId}`
      );

      setSuccess(
        "Invitation deleted successfully."
      );

      await loadFaculty();

    } catch (err) {

      console.error(err);

      setError(
        err.response?.data?.message ||
        "Failed to delete invitation"
      );
    }
  };


  // ============================================================
  // RESEND
  // ============================================================

  const resendInvitation = async (faculty) => {

    try {

      setResendingId(
        faculty.invitationId
      );

      setError("");

      setSuccess("");

      await api.post(
        "/faculty-invitations",
        {
          fullName: faculty.fullName,
          email: faculty.email,
          classId: Number(faculty.classId),
          subjectId: Number(faculty.subjectId),
        }
      );

      setSuccess(
        "Invitation resent successfully."
      );

      await loadFaculty();

    } catch (err) {

      console.error(err);

      setError(
        err.response?.data?.message ||
        "Failed to resend invitation"
      );

    } finally {

      setResendingId(null);
    }
  };


  // ============================================================
  // RENDER
  // ============================================================

  return (
    <div className="dashboard-content">

      {/* ======================================================
          HEADER
          ====================================================== */}

      <div className="page-heading">

        <div>
          <h1>Faculty</h1>

          <p>
            Manage faculty members,
            assignments and invitations.
          </p>
        </div>

        <button
          className="primary-btn"
          onClick={openInviteModal}
        >
          <Plus size={17} />
          Invite Faculty
        </button>

      </div>


      {/* ======================================================
          MESSAGES
          ====================================================== */}

      {error && (
        <div className="error-banner">
          {error}
        </div>
      )}

      {success && (
        <div className="success-banner">
          {success}
        </div>
      )}


      {/* ======================================================
          TABLE
          ====================================================== */}

      {loading ? (

        <div className="loading">
          Loading faculty...
        </div>

      ) : facultyList.length === 0 ? (

        <div className="empty-page">

          <UserRound size={48} />

          <h3>No faculty members yet</h3>

          <p>
            Invite your first faculty member
            to get started.
          </p>

          <button
            className="primary-btn"
            onClick={openInviteModal}
          >
            <Plus size={17} />
            Invite Faculty
          </button>

        </div>

      ) : (

        <div className="faculty-table-wrapper">

          <table className="faculty-table">

            <thead>

              <tr>

                <th>Name</th>

                <th>Email</th>

                <th>Class</th>

                <th>Subject</th>

                <th>Status</th>

                <th>Actions</th>

              </tr>

            </thead>

            <tbody>

              {facultyList.map((faculty) => (

                <tr key={faculty.id}>

                  {/* NAME */}

                  <td>

                    <div className="faculty-user">

                      <div className="faculty-avatar">
                        <UserRound size={19} />
                      </div>

                      <div>

                        <strong>
                          {faculty.fullName}
                        </strong>

                      </div>

                    </div>

                  </td>


                  {/* EMAIL */}

                  <td>

                    <div className="faculty-email-cell">

                      <Mail size={15} />

                      <span>
                        {faculty.email}
                      </span>

                    </div>

                  </td>


                  {/* CLASS */}

                  <td>

                    <span className="faculty-data">

                      {faculty.className || "—"}

                    </span>

                  </td>


                  {/* SUBJECT */}

                  <td>

                    <span className="faculty-data">

                      {faculty.subjectName || "—"}

                    </span>

                  </td>


                  {/* STATUS */}

                  <td>

                    {faculty.status === "ACTIVE" ? (

                      <span className="status-badge active">

                        <span className="status-dot" />

                        Active

                      </span>

                    ) : (

                      <span className="status-badge pending">

                        <span className="status-dot" />

                        Pending

                      </span>

                    )}

                  </td>


                  {/* ACTIONS */}

                  <td>

                    <div className="faculty-table-actions">

                      {faculty.status === "PENDING" && (

                        <button
                          className="table-action resend"
                          onClick={() =>
                            resendInvitation(faculty)
                          }
                          disabled={
                            resendingId ===
                            faculty.invitationId
                          }
                          title="Resend invitation"
                        >

                          <RotateCw
                            size={15}
                            className={
                              resendingId ===
                              faculty.invitationId
                                ? "spin"
                                : ""
                            }
                          />

                          Resend

                        </button>

                      )}


                      <button
                        className="table-action edit"
                        onClick={() =>
                          openEditModal(faculty)
                        }
                        title="Edit"
                      >

                        <Pencil size={15} />

                        Edit

                      </button>


                      <button
                        className="table-action delete"
                        onClick={() => {

                          if (
                            faculty.status ===
                            "PENDING"
                          ) {

                            deletePendingInvitation(
                              faculty
                            );

                          } else {

                            deleteActiveFaculty(
                              faculty
                            );
                          }

                        }}
                        title="Delete"
                      >

                        <Trash2 size={15} />

                        Delete

                      </button>

                    </div>

                  </td>

                </tr>

              ))}

            </tbody>

          </table>

        </div>

      )}


      {/* ======================================================
          INVITE / EDIT MODAL
          ====================================================== */}

      {showModal && (

        <div className="modal-overlay">

          <div className="modal">

            <div className="modal-header">

              <div>

                <h2>
                  {editingFaculty
                    ? "Edit Faculty"
                    : "Invite Faculty"}
                </h2>

                <p>
                  {editingFaculty
                    ? "Update faculty information."
                    : "Send a faculty invitation."}
                </p>

              </div>

              <button
                className="modal-close"
                onClick={closeModal}
              >
                <X size={19} />
              </button>

            </div>


            <form onSubmit={handleSubmit}>

              {/* FULL NAME */}

              <div className="form-group">

                <label>
                  Full Name
                </label>

                <input
                  type="text"
                  name="fullName"
                  value={form.fullName}
                  onChange={handleChange}
                  placeholder="Enter full name"
                  required
                />

              </div>


              {/* EMAIL */}

              <div className="form-group">

                <label>
                  Email
                </label>

                <input
                  type="email"
                  name="email"
                  value={form.email}
                  onChange={handleChange}
                  placeholder="faculty@gmail.com"
                  required
                />

              </div>


              {/* CLASS */}

              <div className="form-group">

                <label>
                  Class
                </label>

                <select
                  name="classId"
                  value={form.classId}
                  onChange={handleClassChange}
                  required
                >

                  <option value="">
                    Select Class
                  </option>

                  {classes.map((item) => (

                    <option
                      key={item.id}
                      value={item.id}
                    >
                      {item.name}
                      {item.academicYear
                        ? ` — ${item.academicYear}`
                        : ""}
                    </option>

                  ))}

                </select>

              </div>


              {/* SUBJECT */}

              <div className="form-group">

                <label>
                  Subject
                </label>

                <select
                  name="subjectId"
                  value={form.subjectId}
                  onChange={handleChange}
                  required
                  disabled={!form.classId}
                >

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

                </select>

              </div>


              {/* BUTTONS */}

              <div className="modal-actions">

                <button
                  type="button"
                  className="secondary-btn"
                  onClick={closeModal}
                >
                  Cancel
                </button>

                <button
                  type="submit"
                  className="primary-btn"
                >

                  {editingFaculty ? (
                    <>
                      <Pencil size={16} />
                      Update Faculty
                    </>
                  ) : (
                    <>
                      <Send size={16} />
                      Send Invitation
                    </>
                  )}

                </button>

              </div>

            </form>

          </div>

        </div>

      )}

    </div>
  );
};

export default Faculty;