import React from "react";
import { NavLink, Outlet, useLocation, useNavigate } from "react-router-dom";
import {
  GraduationCap,
  LayoutDashboard,
  BookOpen,
  UsersRound,
  UserRound,
  ClipboardList,
  LogOut,
} from "lucide-react";

const AdminLayout = () => {
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
      path: "/admin",
      icon: LayoutDashboard,
    },
    {
      label: "Classes",
      path: "/admin/classes",
      icon: BookOpen,
    },
    {
      label: "Subjects",
      path: "/admin/subjects",
      icon: BookOpen,
    },
    {
      label: "Faculty",
      path: "/admin/faculty",
      icon: UsersRound,
    },
    {
      label: "Students",
      path: "/admin/students",
      icon: UserRound,
    },
    {
      label: "Assessments",
      path: "/admin/assessments",
      icon: ClipboardList,
    },
  ];

  const getPageTitle = () => {
    if (location.pathname.includes("/classes")) return "Classes";
    if (location.pathname.includes("/subjects")) return "Subjects";
    if (location.pathname.includes("/faculty")) return "Faculty";
    if (location.pathname.includes("/students")) return "Students";
    if (location.pathname.includes("/assessments")) return "Assessments";

    return "Dashboard";
  };

  return (
    <div className="dashboard admin-layout">

      {/* SIDEBAR */}
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
                end={item.path === "/admin"}
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

      {/* MAIN CONTENT */}
      <main className="main-content">

        {/* TOPBAR */}
        <header className="topbar">

          <div>
            <h2>{getPageTitle()}</h2>
            <p>Welcome back, AcadCore Admin</p>
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
                A
              </div>

              <div>
                <strong>AcadCore Admin</strong>
                <small>ADMIN</small>
              </div>

            </div>

          </div>

        </header>

        {/* PAGE CONTENT */}
        <Outlet />

      </main>

    </div>
  );
};

export default AdminLayout;