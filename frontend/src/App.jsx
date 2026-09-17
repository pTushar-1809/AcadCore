import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "./context/AuthContext";

import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import AdminDashboard from "./pages/AdminDashboard";
import Classes from "./pages/Classes";
import Subjects from "./pages/Subjects";
import Faculty from "./pages/Faculty";
import Students from "./pages/Students";
import AcceptInvitation from "./pages/AcceptInvitation";
import AdminLayout from "./components/AdminLayout";
import Assessments from "./pages/Assessments";

function ProtectedRoute({ children }) {
  const { user } = useAuth();

  return user ? children : <Navigate to="/login" />;
}

function AppRoutes() {
  return (
    <Routes>
      {/* Default */}
      <Route path="/" element={<Navigate to="/login" />} />

      {/* Authentication */}
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      {/* =====================================================
          ADMIN DASHBOARD
          AdminDashboard already has its own dashboard layout,
          so keep it separate.
          ===================================================== */}
      <Route
        path="/admin"
        element={
          <ProtectedRoute>
            <AdminDashboard />
          </ProtectedRoute>
        }
      />

      {/* =====================================================
          ADMIN INNER PAGES
          AdminLayout provides ONE sidebar + ONE topbar.
          ===================================================== */}
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

        <Route
          path="assessments"
          element={
            <div className="dashboard-content">
              <div className="page-heading">
                <div>
                  <h1>Assessments</h1>
                  <p>
                    Create and manage academic assessments.
                  </p>
                </div>
              </div>

              <div className="empty-page">
                <h3>Assessment Management</h3>
                <p>
                  Assessment management will be added next.
                </p>
              </div>
            </div>
          }
        />
      </Route>

      {/* =====================================================
          FACULTY
          ===================================================== */}
      <Route
        path="/faculty"
        element={
          <ProtectedRoute>
            <Dashboard />
          </ProtectedRoute>
        }
      />

      {/* =====================================================
          STUDENT
          ===================================================== */}
      <Route
        path="/student"
        element={
          <ProtectedRoute>
            <Dashboard />
          </ProtectedRoute>
        }
      />

      {/* =====================================================
          FACULTY INVITATION ACCEPTANCE
          Public route
          ===================================================== */}
      <Route
        path="/faculty/accept"
        element={<AcceptInvitation />}
      />

      {/* Unknown route */}
      <Route path="*" element={<Navigate to="/login" />} />
    </Routes>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </BrowserRouter>
  );
}