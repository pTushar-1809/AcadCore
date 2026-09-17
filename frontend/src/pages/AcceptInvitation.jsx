import { useState } from "react";
import { useSearchParams, useNavigate } from "react-router-dom";
import { LockKeyhole, CheckCircle } from "lucide-react";

import api from "../services/api";

export default function AcceptInvitation() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const token = searchParams.get("token");

  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();

    setError("");
    setSuccess("");

    if (!token) {
      setError("Invalid invitation link.");
      return;
    }

    if (password.length < 8) {
      setError(
        "Password must contain at least 8 characters."
      );
      return;
    }

    if (password !== confirmPassword) {
      setError("Passwords do not match.");
      return;
    }

    try {
      setSaving(true);

      const response = await api.post(
        "/faculty-invitations/accept",
        {
          token,
          password,
          confirmPassword,
        }
      );

      setSuccess(
        `${response.data.message}. You can now login as faculty.`
      );

      setPassword("");
      setConfirmPassword("");

      setTimeout(() => {
        navigate("/login");
      }, 2000);

    } catch (err) {
      console.error(
        "Accept invitation error:",
        err
      );

      setError(
        err.response?.data?.message ||
          "Unable to accept invitation."
      );
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="auth-page">

      <div className="auth-card">

        <div className="auth-header">
          <div className="auth-icon">
            <LockKeyhole size={28} />
          </div>

          <h1>Accept Faculty Invitation</h1>

          <p>
            Create your password to activate your
            AcadCore faculty account.
          </p>
        </div>

        {error && (
          <div className="error-banner">
            {error}
          </div>
        )}

        {success && (
          <div className="success-banner">
            <CheckCircle size={18} />
            {success}
          </div>
        )}

        <form
          className="modal-form"
          onSubmit={handleSubmit}
        >

          <label>Create Password</label>

          <input
            type="password"
            placeholder="Minimum 8 characters"
            value={password}
            onChange={(e) =>
              setPassword(e.target.value)
            }
            required
          />

          <label>Confirm Password</label>

          <input
            type="password"
            placeholder="Enter password again"
            value={confirmPassword}
            onChange={(e) =>
              setConfirmPassword(e.target.value)
            }
            required
          />

          <button
            type="submit"
            className="primary-btn"
            disabled={saving || !token}
          >
            {saving
              ? "Activating Account..."
              : "Accept Invitation"}
          </button>

        </form>

      </div>

    </div>
  );
}