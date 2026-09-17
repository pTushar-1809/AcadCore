import { useEffect, useState } from "react";

import {
  Users,
  GraduationCap,
  BookOpen,
  ClipboardList,
} from "lucide-react";

import Sidebar from "../components/Sidebar";
import Topbar from "../components/Topbar";
import StatCard from "../components/StatCard";
import api from "../services/api";

export default function AdminDashboard() {
  const [stats, setStats] = useState({
    totalStudents: 0,
    totalFaculty: 0,
    totalClasses: 0,
    totalAssessments: 0,
  });

  const [classes, setClasses] = useState([]);

  const [loading, setLoading] = useState(true);
  const [classesLoading, setClassesLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    loadDashboardStats();
    loadRecentClasses();
  }, []);

  const loadDashboardStats = async () => {
    try {
      setLoading(true);
      setError("");

      const response = await api.get("/dashboard/admin");

      setStats({
        totalStudents: response.data.totalStudents ?? 0,
        totalFaculty: response.data.totalFaculty ?? 0,
        totalClasses: response.data.totalClasses ?? 0,
        totalAssessments: response.data.totalAssessments ?? 0,
      });
    } catch (err) {
      console.error("Dashboard API error:", err);

      setError(
        err.response?.data?.message ||
          "Unable to load dashboard statistics."
      );
    } finally {
      setLoading(false);
    }
  };

  const loadRecentClasses = async () => {
    try {
      setClassesLoading(true);

      const response = await api.get("/classes");

      setClasses(response.data || []);
    } catch (err) {
      console.error("Classes API error:", err);
    } finally {
      setClassesLoading(false);
    }
  };

  return (
    <div className="dashboard">
      <Sidebar />

      <main className="main-content">
        <Topbar />

        <section className="dashboard-content">
          <div className="page-heading">
            <div>
              <h1>Academic Overview</h1>
              <p>
                Monitor classes, faculty, students and assessments.
              </p>
            </div>

            <button className="primary-btn">
              + Create Class
            </button>
          </div>

          {error && (
            <div className="error-message">
              {error}
            </div>
          )}

          <div className="stats-grid">
            <StatCard
              title="Total Students"
              value={loading ? "..." : stats.totalStudents}
              description="Currently enrolled"
              icon={GraduationCap}
            />

            <StatCard
              title="Faculty"
              value={loading ? "..." : stats.totalFaculty}
              description="Active faculty members"
              icon={Users}
            />

            <StatCard
              title="Classes"
              value={loading ? "..." : stats.totalClasses}
              description="Academic classes"
              icon={BookOpen}
            />

            <StatCard
              title="Assessments"
              value={loading ? "..." : stats.totalAssessments}
              description="Created assessments"
              icon={ClipboardList}
            />
          </div>

          <div className="dashboard-grid">
            <div className="panel">
              <div className="panel-header">
                <div>
                  <h3>Recent Classes</h3>
                  <p>
                    Academic classes created in AcadCore
                  </p>
                </div>

                <button>View All</button>
              </div>

              {classesLoading ? (
                <div className="empty-state">
                  <BookOpen size={40} />

                  <h4>Loading classes...</h4>

                  <p>
                    Please wait while classes are loaded.
                  </p>
                </div>
              ) : classes.length === 0 ? (
                <div className="empty-state">
                  <BookOpen size={40} />

                  <h4>No recent classes</h4>

                  <p>
                    Create your first academic class to get started.
                  </p>
                </div>
              ) : (
                <div className="recent-classes">
                  {classes.slice(0, 5).map((academicClass) => (
                    <div
                      className="class-item"
                      key={academicClass.id}
                    >
                      <div className="class-icon">
                        <BookOpen size={22} />
                      </div>

                      <div className="class-info">
                        <h4>{academicClass.name}</h4>

                        <p>
                          Academic Year:{" "}
                          {academicClass.academicYear}
                        </p>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div className="panel">
              <div className="panel-header">
                <div>
                  <h3>Quick Actions</h3>

                  <p>
                    Frequently used administration tools
                  </p>
                </div>
              </div>

              <div className="quick-actions">
                <button>+ Create Class</button>
                <button>+ Add Faculty</button>
                <button>+ Add Subject</button>
                <button>View Students</button>
              </div>
            </div>
          </div>
        </section>
      </main>
    </div>
  );
}