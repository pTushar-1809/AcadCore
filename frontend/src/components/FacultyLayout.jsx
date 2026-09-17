import React from "react";
import { NavLink, Outlet, useLocation, useNavigate } from "react-router-dom";
import {
  GraduationCap,
  LayoutDashboard,
  BookOpen,
  ClipboardList,
  UsersRound,
  BarChart3,
  LogOut,
} from "lucide-react";
import { useAuth } from "../context/AuthContext";

const FacultyLayout = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    navigate("/login");
    window.location.reload();
  };

  const navItems = [
    {
      label: "Dashboard",
      path: "/faculty",
      icon: LayoutDashboard,
    },
    {
      label: "My Subjects",
      path: "/faculty/subjects",
      icon: BookOpen,
    },
    {
      label: "Assessments",
      path: "/faculty/assessments",
      icon: ClipboardList,
    },
    {
      label: "Students",
      path: "/faculty/students",
      icon: UsersRound,
    },
    {
      label: "Results",
      path: "/faculty/results",
      icon: BarChart3,
    },
  ];

  const getPageTitle = () => {
    if (location.pathname.includes("/subjects")) {
      return "My Subjects";
    }

    if (location.pathname.includes("/assessments")) {
      return "Assessments";
    }

    if (location.pathname.includes("/students")) {
      return "Students";
    }

    if (location.pathname.includes("/results")) {
      return "Results";
    }

    return "Dashboard";
  };

  return (
    <div className="dashboard admin-layout">
      {/* =====================================================
          SIDEBAR
          ===================================================== */}

      <aside className="sidebar">

        <div className="logo">
          <GraduationCap size={23} />
          <span>AcadCore</span>
        </div>

        <nav>
          {navItems.map((item) => {
            const Icon = item.icon;

            return (
              <NavLink
                key={item.path}
                to={item.path}
                end={item.path === "/faculty"}
                className={({ isActive }) =>
                  isActive ? "active" : undefined
                }
              >
                <Icon size={18} />
                <span>{item.label}</span>
              </NavLink>
            );
          })}
        </nav>

        <button
          className="logout-btn"
          onClick={handleLogout}
        >
          <LogOut size={18} />
          <span>Logout</span>
        </button>

      </aside>

      {/* =====================================================
          MAIN CONTENT
          ===================================================== */}

      <main className="main-content">

        <header className="topbar">

          <div>
            <h2>{getPageTitle()}</h2>

            <p>
              Welcome back,{" "}
              {user?.fullName || "Faculty"}
            </p>
          </div>

          <div className="topbar-right">

            <div className="search-box">
              <span>⌕</span>
              <input
                type="text"
                placeholder="Search..."
              />
            </div>

            <span className="notification-icon">
              ♧
            </span>

            <div className="profile">

              <div className="avatar">
                {user?.fullName
                  ?.charAt(0)
                  ?.toUpperCase() || "F"}
              </div>

              <div>
                <strong>
                  {user?.fullName || "Faculty"}
                </strong>

                <small>
                  FACULTY
                </small>
              </div>

            </div>

          </div>

        </header>

        <Outlet />

      </main>
    </div>
  );
};

export default FacultyLayout;