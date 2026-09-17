import React from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";

import { AuthProvider } from "./context/AuthContext";

// Auth
import Login from "./pages/Login";
import Register from "./pages/Register";
import AcceptInvitation from "./pages/AcceptInvitation";

// Admin
import AdminDashboard from "./pages/AdminDashboard";
import AdminLayout from "./layouts/AdminLayout";
import Classes from "./pages/Classes";
import Subjects from "./pages/Subjects";
import Faculty from "./pages/Faculty";
import Students from "./pages/Students";
import Assessments from "./pages/Assessments";

// Faculty
import FacultyLayout from "./layouts/FacultyLayout";
import Dashboard from "./pages/Dashboard";
import FacultySubjects from "./pages/FacultySubjects";
import FacultyAssessments from "./pages/FacultyAssessments";
import QuestionBuilder from "./pages/QuestionBuilder";

const ProtectedRoute = ({ children }) => {
  const token = localStorage.getItem("token");

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  return children;
};

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>

          {/* AUTH */}
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route
            path="/accept-invitation"
            element={<AcceptInvitation />}
          />

          {/* ADMIN DASHBOARD */}
          <Route
            path="/admin"
            element={
              <ProtectedRoute>
                <AdminDashboard />
              </ProtectedRoute>
            }
          />

          {/* ADMIN PAGES */}
          <Route
            path="/admin"
            element={
              <ProtectedRoute>
                <AdminLayout />
              </ProtectedRoute>
            }
          >
            <Route path="classes" element={<Classes />} />
            <Route path="subjects" element={<Subjects />} />
            <Route path="faculty" element={<Faculty />} />
            <Route path="students" element={<Students />} />
            <Route path="assessments" element={<Assessments />} />
          </Route>

          {/* FACULTY */}
          <Route
            path="/faculty"
            element={
              <ProtectedRoute>
                <FacultyLayout />
              </ProtectedRoute>
            }
          >
            <Route index element={<Dashboard />} />

            <Route
              path="subjects"
              element={<FacultySubjects />}
            />

            <Route
              path="assessments"
              element={<FacultyAssessments />}
            />

            <Route
              path="assessments/:assessmentId/questions"
              element={<QuestionBuilder />}
            />

            <Route
              path="students"
              element={
                <div>Faculty Students</div>
              }
            />

            <Route
              path="results"
              element={
                <div>Faculty Results</div>
              }
            />
          </Route>

          {/* DEFAULT */}
          <Route
            path="/"
            element={<Navigate to="/login" replace />}
          />

          <Route
            path="*"
            element={<Navigate to="/login" replace />}
          />

        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;