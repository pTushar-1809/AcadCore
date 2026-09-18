import { useState } from "react";

import {
  GraduationCap,
  UsersRound,
  ArrowRight,
  CheckCircle,
} from "lucide-react";

import api from "../services/api";

export default function StudentJoinClass() {

  const [joinCode, setJoinCode] = useState("");

  const [loading, setLoading] = useState(false);

  const [error, setError] = useState("");

  const [success, setSuccess] = useState(null);

  const handleJoinClass = async (e) => {

    e.preventDefault();

    setError("");
    setSuccess(null);

    const code =
      joinCode
        .trim()
        .toUpperCase();

    if (!code) {

      setError(
        "Please enter the class join code."
      );

      return;
    }

    try {

      setLoading(true);

      const response =
        await api.post(
          "/students/join-class",
          {
            joinCode: code,
          }
        );

      setSuccess(
        response.data
      );

      setJoinCode("");

    } catch (err) {

      console.error(
        "Join class error:",
        err
      );

      setError(
        err.response?.data?.message ||
        "Failed to join class."
      );

    } finally {

      setLoading(false);
    }
  };

  return (
    <div className="join-class-page">

      <div className="join-class-card">

        <div className="join-class-icon">
          <GraduationCap size={32} />
        </div>

        <h1>
          Join Your Class
        </h1>

        <p className="join-class-subtitle">
          Enter the class join code provided
          by your Admin.
        </p>

        {error && (
          <div className="error-banner">
            {error}
          </div>
        )}

        {success && (
          <div className="join-success">

            <CheckCircle size={24} />

            <div>

              <strong>
                Class joined successfully!
              </strong>

              <p>
                {success.className}
                {" — "}
                {success.academicYear}
              </p>

            </div>

          </div>
        )}

        <form
          onSubmit={handleJoinClass}
          className="join-class-form"
        >

          <label>
            Class Join Code
          </label>

          <div className="join-code-input">

            <UsersRound size={19} />

            <input
              type="text"
              value={joinCode}
              onChange={(e) =>
                setJoinCode(
                  e.target.value
                    .toUpperCase()
                    .replace(/\s/g, "")
                )
              }
              placeholder="e.g. A7K29PQM"
              maxLength={8}
              autoComplete="off"
              required
            />

          </div>

          <button
            type="submit"
            className="primary-btn join-class-btn"
            disabled={loading}
          >

            {loading
              ? "Joining..."
              : "Join Class"}

            {!loading && (
              <ArrowRight size={18} />
            )}

          </button>

        </form>

        <div className="join-class-help">

          <strong>
            Where can I find the code?
          </strong>

          <p>
            Ask your Admin for the 8-character
            class join code.
          </p>

        </div>

      </div>

    </div>
  );
}